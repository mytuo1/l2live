package l2f.gameserver.network.clientpackets;

import l2f.gameserver.Shutdown;
import l2f.gameserver.network.GameClient;
import l2f.gameserver.network.loginservercon.AuthServerCommunication;
import l2f.gameserver.network.loginservercon.SessionKey;
import l2f.gameserver.network.loginservercon.gspackets.PlayerAuthRequest;
import l2f.gameserver.network.serverpackets.LoginFail;
import l2f.gameserver.network.serverpackets.ServerClose;

import org.strixplatform.StrixPlatform;
import org.strixplatform.logging.Log;

/**
 * cSddddd
 * cSdddddQ
 * loginName + keys must match what the loginserver used.
 */
public class AuthLogin extends L2GameClientPacket
{
	private String _loginName;
	private int _playKey1;
	private int _playKey2;
	private int _loginKey1;
	private int _loginKey2;
	private byte[] _data = new byte[48];

	@Override
	protected void readImpl()
	{
		_loginName = readS(32).toLowerCase();
		_playKey2 = readD();
		_playKey1 = readD();
		_loginKey1 = readD();
		_loginKey2 = readD();
	}

	@Override
	protected void runImpl()
	{
		GameClient client = getClient();
		
		SessionKey key = new SessionKey(_loginKey1, _loginKey2, _playKey1, _playKey2);
		client.setSessionId(key);
		client.setLoginName(_loginName);
		
		if (Shutdown.getInstance().getMode() != Shutdown.NONE && Shutdown.getInstance().getSeconds() <= 15)
			client.closeNow(false);
		else
		{			
			if (AuthServerCommunication.getInstance().isShutdown())
			{
				client.close(new LoginFail(LoginFail.SYSTEM_ERROR_LOGIN_LATER));
				return;
			}
			
			GameClient oldClient = AuthServerCommunication.getInstance().addWaitingClient(client);
			if (oldClient != null)
				oldClient.close(ServerClose.STATIC);
			
			AuthServerCommunication.getInstance().sendPacket(new PlayerAuthRequest(client));

			// TODO[K] - Strix section start
			if(StrixPlatform.getInstance().isPlatformEnabled()) {
				if(client.getStrixClientData() != null) {
					client.getStrixClientData().setClientAccount(_loginName);
					if(StrixPlatform.getInstance().isAuthLogEnabled()) {
						Log.auth("Account: [" + _loginName + "] HWID: [" + client.getStrixClientData().getClientHWID() + "] SessionID: [" + client.getStrixClientData().getSessionId() + "] entered to Game Server");
					}
				} else {
					client.close(ServerClose.STATIC);
					return;
				}
			}
			// TODO[K] - Strix section end
		}
	}
}