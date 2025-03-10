package l2f.gameserver.network;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import l2f.commons.net.nio.impl.MMOClient;
import l2f.commons.net.nio.impl.MMOConnection;
import l2f.gameserver.Config;
import l2f.gameserver.SecondaryPasswordAuth;
import l2f.gameserver.dao.CharacterDAO;
import l2f.gameserver.database.DatabaseFactory;
//import l2f.gameserver.hwid.SmartGuard;
import l2f.gameserver.model.CharSelectInfoPackage;
import l2f.gameserver.model.GameObjectsStorage;
import l2f.gameserver.model.Player;
import l2f.gameserver.network.loginservercon.AuthServerCommunication;
import l2f.gameserver.network.loginservercon.SessionKey;
import l2f.gameserver.network.loginservercon.gspackets.PlayerLogout;
import l2f.gameserver.network.serverpackets.L2GameServerPacket;
import l2f.gameserver.network.serverpackets.components.SystemMsg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.lameguard.session.LameClientV195;
import org.strixplatform.StrixPlatform;
import org.strixplatform.network.IStrixClientData;
import org.strixplatform.network.cipher.StrixGameCrypt;
import org.strixplatform.utils.StrixClientData;


public class GameClient extends MMOClient<MMOConnection<GameClient>> implements IStrixClientData
{
	private static final Logger _log = LoggerFactory.getLogger(GameClient.class);
	public static final String NO_IP = "?.?.?.?";
	private SecondaryPasswordAuth _secondaryAuth;
	public static boolean SESSION_OK = MMOClient.SESSION_OK;

	public StrixGameCrypt _crypt = null;
	private StrixClientData clientData;

	public GameClientState _state;

	private String _HWID = "";
	//private String _fileId = "";
	//private int _systemVer = -1;
	private int _serverId;

	public static enum GameClientState
	{
		CONNECTED,
		AUTHED,
		IN_GAME,
		DISCONNECTED
	}

	/** Данные аккаунта */
	private String _login;
	private int _bonus = 0;
	private int _bonusExpire;

	private Player _activeChar;
	private SessionKey _sessionKey;
	private String _ip = NO_IP;
	private int revision = 0;
	private boolean _gameGuardOk = false;
	// private SecondaryPasswordAuth _secondaryAuth;

	private final List<Integer> _charSlotMapping = new ArrayList<Integer>();

	public GameClient(MMOConnection<GameClient> con)
	{
		super(con);

		_state = GameClientState.CONNECTED;
		//TODO[K] - Guard section start
		_crypt = new StrixGameCrypt();
		//TODO[K] - Guard section end
		if (con != null)
			_ip = con.getSocket().getInetAddress().getHostAddress();
	}

	@Override
	protected void onDisconnection()
	{
		final Player player;

		setState(GameClientState.DISCONNECTED);
		player = getActiveChar();
		setActiveChar(null);

		if (player != null && player.getNetConnection() != null)
		{
			player.setNetConnection(null);
			player.scheduleDelete();
		}

		if (getSessionKey() != null)
		{
			if (isAuthed())
			{
				AuthServerCommunication.getInstance().removeAuthedClient(getLogin());
				AuthServerCommunication.getInstance().sendPacket(new PlayerLogout(getLogin()));
			}
			else
			{
				AuthServerCommunication.getInstance().removeWaitingClient(getLogin());
			}
		}
	}

	@Override
	protected void onForcedDisconnection()
	{
		// TODO Auto-generated method stub

	}

	public void markRestoredChar(int charSlot)
	{
		int objId = getObjectIdForSlot(charSlot);
		if (objId < 0)
		{
			return;
		}

		if ((_activeChar != null) && (_activeChar.getObjectId() == objId))
		{
			_activeChar.setDeleteTimer(0);
		}

		try (Connection con = DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE obj_id=?"))
		{
			statement.setInt(1, objId);
			statement.execute();
		}
		catch (SQLException e)
		{
			_log.error("Error on markRestoredChar ", e);
		}
	}

	public void markToDeleteChar(int charSlot)
	{
		int objId = getObjectIdForSlot(charSlot);
		if (objId < 0)
		{
			return;
		}

		if ((_activeChar != null) && (_activeChar.getObjectId() == objId))
		{
			_activeChar.setDeleteTimer((int) (System.currentTimeMillis() / 1000));
		}

		try (Connection con = DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("UPDATE characters SET deletetime=? WHERE obj_id=?"))
		{
			statement.setLong(1, (int) (System.currentTimeMillis() / 1000L));
			statement.setInt(2, objId);
			statement.execute();
		}
		catch (SQLException e)
		{
			_log.error("error on markToDeleteChar:", e);
		}
	}

	public void deleteChar(int charslot) throws Exception
	{
		// have to make sure active character must be nulled
		if (_activeChar != null)
		{
			return;
		}

		int objid = getObjectIdForSlot(charslot);
		if (objid == -1)
		{
			return;
		}

		CharacterDAO.getInstance().deleteCharByObjId(objid);
	}

	public Player loadCharFromDisk(int charSlot, int objectId)
	{
		if (objectId == -1)
		{
			return null;
		}

		Player character = null;
		Player oldPlayer = GameObjectsStorage.getPlayer(objectId);

		if (oldPlayer != null)
		{
			if (oldPlayer.isInOfflineMode() || oldPlayer.isLogoutStarted())
			{
				oldPlayer.kick();//Kicking Offline Shop Player
				return null;
			}
			else
			{
				oldPlayer.sendPacket(SystemMsg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT);
				//Kicking real player that was on the char
				GameClient oldClient = oldPlayer.getNetConnection();
				if (oldClient != null)
				{
					oldClient.setActiveChar(null);
					oldClient.closeNow(false);
				}
				oldPlayer.setNetConnection(this);
				character = oldPlayer;
			}
		}

		if (character == null)
		{
			character = Player.restore(objectId);
		}

		if (character != null)
		{
			setActiveChar(character);
		}
		else
		{
			_log.warn("could not restore obj_id: " + objectId + " in slot:" + charSlot);
		}

		return character;
	}

	public int getObjectIdForSlot(int charslot)
	{
		if ((charslot < 0) || (charslot >= _charSlotMapping.size()))
		{
			_log.warn(getLogin() + " tried to modify Character in slot " + charslot + " but no characters exits at that slot.");
			return -1;
		}
		return _charSlotMapping.get(charslot);
	}

	public int getSlotForObjectId(int objectId)
	{
		return _charSlotMapping.indexOf(objectId);
	}

	public Player getActiveChar()
	{
		return _activeChar;
	}

	/**
	 * @return Returns the sessionId.
	 */
	public SessionKey getSessionKey()
	{
		return _sessionKey;
	}

	public String getLogin()
	{
		return _login;
	}

	public void setLoginName(String loginName)
	{
		_login = loginName;
		if (Config.SECOND_AUTH_ENABLED)
		{
			_secondaryAuth = new SecondaryPasswordAuth(this);
		}
	}

	public void setActiveChar(Player player)
	{
		_activeChar = player;
		if (player != null)
		{
			player.setNetConnection(this);
		}
	}

	public void setSessionId(SessionKey sessionKey)
	{
		_sessionKey = sessionKey;
	}

	public void setCharSelection(CharSelectInfoPackage[] chars)
	{
		_charSlotMapping.clear();

		for (CharSelectInfoPackage element : chars)
		{
			int objectId = element.getObjectId();
			_charSlotMapping.add(objectId);
		}
	}

	public int getRevision()
	{
		return revision;
	}

	public void setRevision(int revision)
	{
		this.revision = revision;
	}

	@Override
	public boolean encrypt(final ByteBuffer buf, final int size)
	{
		_crypt.encrypt(buf.array(), buf.position(), size);
		buf.position(buf.position() + size);
		return true;
	}

	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
        boolean ret = true;
        this._crypt.decrypt(buf.array(), buf.position(), size);
		return ret;
	}

	public void sendPacket(L2GameServerPacket gsp)
	{
		if (isConnected())
		{
			getConnection().sendPacket(gsp);
		}
	}

	public void sendPacket(L2GameServerPacket... gsp)
	{
		if (isConnected())
		{
			getConnection().sendPacket(gsp);
		}
	}

	public void sendPackets(List<L2GameServerPacket> gsp)
	{
		if (isConnected())
		{
			getConnection().sendPackets(gsp);
		}
	}

	public void close(L2GameServerPacket gsp)
	{
		if (isConnected())
		{
			getConnection().close(gsp);
		}
	}

	public String getIpAddr()
	{
		return _ip;
	}

	public byte[] enableCrypt()
	{
		byte[] key = BlowFishKeygen.getRandomKey();
		_crypt.setKey(key);

		/*if (SmartGuard.isSmartGuardEnabled())
			key = SmartGuard.getKey(key);*/
		return key;
	}

	/*public byte[] getDecryptedProtocol(byte[] key)
	{
		_crypt.setKey(key);
		key = SmartGuard.getHwidKey(key);
		return key;
	}*/

	public int getBonus()
	{
		return _bonus;
	}

	public int getBonusExpire()
	{
		return _bonusExpire;
	}

	public void setBonus(int bonus)
	{
		_bonus = bonus;
	}

	public void setBonusExpire(int bonusExpire)
	{
		_bonusExpire = bonusExpire;
	}

	public GameClientState getState()
	{
		return _state;
	}

	public void setState(GameClientState state)
	{
		_state = state;
	}

	private int _failedPackets = 0;
	private int _unknownPackets = 0;

	public void onPacketReadFail()
	{
		if (_failedPackets++ >= 10)
		{
			_log.warn("Too many client packet fails, connection closed : " + this);
			closeNow(true);
		}
	}

	public void onUnknownPacket()
	{
		if (_unknownPackets++ >= 10)
		{
			_log.warn("Too many client unknown packets, connection closed : " + this);
			closeNow(true);
		}
	}

	@Override
	public String toString()
	{
		return _state + " IP: " + getIpAddr() + (_login == null ? "" : " Account: " + _login) + (_activeChar == null ? "" : " Player : " + _activeChar);
	}

	public SecondaryPasswordAuth getSecondaryAuth()
	{
		return _secondaryAuth;
	}

	public void setGameGuardOk(boolean gameGuardOk)
	{
		_gameGuardOk = gameGuardOk;
	}

	public boolean isGameGuardOk()
	{
		return _gameGuardOk;
	}

	private static byte[] _keyClientEn = new byte[8];

	public static void setKeyClientEn(byte[] key)
	{
		_keyClientEn = key;
	}

	public static byte[] getKeyClientEn()
	{
		return _keyClientEn;
	}

	private int _instanceCount;

	public void setInstanceCount(int i) {
		_instanceCount = i;
	}

	public int getInstanceCount() {
		return _instanceCount;
	}

	/*public void setPatchVersion(int i) {
		_systemVer = i;
	}*/

	/*public int getPatchVersion() {
		return _systemVer;
	}*/

	/*public String getFileId()
	{
		return _fileId;
	}*/

	/*public int getSystemVer()
	{
		return _systemVer;
	}*/

	private boolean _isProtected;

	public void setProtected(boolean isProtected) {
		_isProtected = isProtected;
	}

	public boolean isProtected() {
		return _isProtected;
	}

	public void setHWID(String hwid) {
		_HWID = hwid;
	}

	public String getHWID() {
		return _HWID;
	}

	/*public void setFileId(String fileId)
	{
		this._fileId = fileId;
	}*/

	/*public void setSystemVersion(int ver)
	{
		_systemVer = ver;
	}*/

	public int getServerId()
	{
		return _serverId;
	}

	public void setServerId(int serverId)
	{
		_serverId = serverId;
	}

	//TODO[K] - Guard section start	
	@Override
	public void setStrixClientData(final StrixClientData clientData) {
		this.clientData = clientData;
	}

	@Override
	public StrixClientData getStrixClientData() {
		return clientData;
	}
	//TODO[K] - Guard section end
}