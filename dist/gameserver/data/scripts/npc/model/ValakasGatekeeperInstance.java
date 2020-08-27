package npc.model;

import l2f.gameserver.Announcements;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.instances.NpcInstance;
import l2f.gameserver.network.serverpackets.components.ChatType;
import l2f.gameserver.templates.npc.NpcTemplate;
import l2f.gameserver.utils.ItemFunctions;
import l2f.gameserver.utils.Location;
import bosses.ValakasManager;

/**
 * @author pchayka
 */

public final class ValakasGatekeeperInstance extends NpcInstance
{
	private static final int FLOATING_STONE = 7267;
	private static final Location TELEPORT_POSITION1 = new Location(183831, -115457, -3296);

	public ValakasGatekeeperInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (!canBypassCheck(player, this))
			return;

		if (command.equalsIgnoreCase("request_passage"))
		{
			if (!ValakasManager.isEnableEnterToLair())
			{
				player.sendMessage("Valakas is now reborning and there's no way to enter the hall now.");
				return;
			}
			if (player.getInventory().getCountOf(FLOATING_STONE) < 1)
			{
				player.sendMessage("In order to enter the Hall of Flames you should carry at least one Flotaing Stone");
				return;
			}
			ItemFunctions.removeItem(player, FLOATING_STONE, 1, true, "ValakasGateKeeperInstance");
			player.teleToLocation(TELEPORT_POSITION1);
			if (player.isInParty() && player.isLeader(player))
			{
				if (player.getClan() !=null)
				{
				Announcements.getInstance().announceToAll("A party of Clan " + player.getClan().getName().toString() + " " + "entered the Valakas Nest!" , ChatType.BATTLEFIELD);
				}
				else
				Announcements.getInstance().announceToAll("A party led by " + player.getName().toString() + " " + "entered the Valakas Nest!" , ChatType.BATTLEFIELD);
			}
			return;
		}
		else if (command.equalsIgnoreCase("request_valakas"))
		{
			ValakasManager.enterTheLair(player);
			return;
		}
		else
			super.onBypassFeedback(player, command);
	}
}