package l2f.gameserver.network.clientpackets;

import l2f.gameserver.dao.CharacterPostFriendDAO;
import l2f.gameserver.model.Player;
import l2f.gameserver.network.serverpackets.SystemMessage2;
import l2f.gameserver.network.serverpackets.components.SystemMsg;
import org.apache.commons.lang3.StringUtils;
import org.napile.primitive.maps.IntObjectMap;

public class RequestExDeletePostFriendForPostBox extends L2GameClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
   		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if (player == null)
			return;

		if (StringUtils.isEmpty(_name))
			return;

		int key = 0;
		IntObjectMap<String> postFriends = player.getPostFriends();
		for (IntObjectMap.Entry<String> entry : postFriends.entrySet())
		{
			if (entry.getValue().equalsIgnoreCase(_name))
				key = entry.getKey();
		}

		if (key == 0)
		{
			player.sendPacket(SystemMsg.THE_NAME_IS_NOT_CURRENTLY_REGISTERED);
			return;
		}

		player.getPostFriends().remove(key);

		CharacterPostFriendDAO.delete(player, key);
		player.sendPacket(new SystemMessage2(SystemMsg.S1_WAS_SUCCESSFULLY_DELETED_FROM_YOUR_CONTACT_LIST).addString(_name));
	}
}
