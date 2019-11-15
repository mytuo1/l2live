package l2f.gameserver.model.entity;

import l2f.gameserver.Announcements;
import l2f.gameserver.Config;
import l2f.gameserver.model.Player;
import l2f.gameserver.network.serverpackets.ExShowScreenMessage;
import l2f.gameserver.network.serverpackets.L2GameServerPacket;
import l2f.gameserver.network.serverpackets.components.ChatType;
import l2f.gameserver.utils.ItemFunctions;

public class SpreeHandler {

    public void spreeSystem(Player player, int spreeKills) {
        ExShowScreenMessage msgCase = null;
        String announceMessage = null;
        switch (spreeKills) {
            case 1: {
                break;
            }
            case 2: {
                break;
            }
            case 3: {
                msgCase = new ExShowScreenMessage("You have reached a 3 kill spree!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
                announceMessage = "just got a Triple Kill!";
				ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");
				if (player.getNameColor() == Config.NORMAL_NAME_COLOUR)
				{
				player.setNameColor(Config.NAME_COLOR_1);
				player.broadcastUserInfo(true);
				}
                break;
            }
            case 4: {
                msgCase = new ExShowScreenMessage("You've reached 4 killing spree!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
                announceMessage = "is Dominating!";
				ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");
				if (player.getNameColor() == Config.NAME_COLOR_1)
				{
				player.setNameColor(Config.NAME_COLOR_2);
				player.broadcastUserInfo(true);
				}
//				if (player.getTitleColor() == Config.NORMAL_TITLE_COLOUR)
//				{
//				player.setNameColor(Config.TITLE_COLOR_1);
//				player.broadcastUserInfo(true);
//				}
				break;
            }
            case 5: {
                msgCase = new ExShowScreenMessage("You've reached 5 killing spree!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
                announceMessage = "just got a Mega Kill!";
				ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 2, true, "PvP");
				if (player.getNameColor() == Config.NAME_COLOR_2)
				{
				player.setNameColor(Config.NAME_COLOR_3);
				player.broadcastUserInfo(true);
				}
//				if (player.getTitleColor() == Config.TITLE_COLOR_1)
//				{
//				player.setNameColor(Config.TITLE_COLOR_2);
//				player.broadcastUserInfo(true);
//				}
                break;
            }
            case 8: {
                msgCase = new ExShowScreenMessage("You've reached 8 killing spree!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
                announceMessage = "is on an ULTRAKILL!";
				ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 2, true, "PvP");
				if (player.getNameColor() == Config.NAME_COLOR_3)
				{
				player.setNameColor(Config.NAME_COLOR_4);
				player.broadcastUserInfo(true);
				}
//				if (player.getTitleColor() == Config.TITLE_COLOR_2)
//				{
//				player.setNameColor(Config.TITLE_COLOR_3);
//				player.broadcastUserInfo(true);
//				}
                break;
            }
            case 10: {
                msgCase = new ExShowScreenMessage("You've reached 10 killing spree!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
                announceMessage = "is Unstoppable!";
				ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 3, true, "PvP");
				if (player.getNameColor() == Config.NAME_COLOR_4)
				{
				player.setNameColor(Config.NAME_COLOR_5);
				player.broadcastUserInfo(true);
				}
//				if (player.getTitleColor() == Config.TITLE_COLOR_3)
//				{
//				player.setNameColor(Config.TITLE_COLOR_4);
//				player.broadcastUserInfo(true);
//				}
                break;
            }
            case 13: {
                msgCase = new ExShowScreenMessage("You've reached 13 killing spree!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
                announceMessage = "is Wicked Sick!";
				ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 3, true, "PvP");
				if (player.getNameColor() == Config.NAME_COLOR_5)
				{
				player.setNameColor(Config.NAME_COLOR_6);
				player.broadcastUserInfo(true);
				}
//				if (player.getTitleColor() == Config.TITLE_COLOR_4)
//				{
//				player.setNameColor(Config.TITLE_COLOR_5);
//				player.broadcastUserInfo(true);
//				}
                break;
            }
            case 15: {
                msgCase = new ExShowScreenMessage("You've reached 15 killing spree!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
                announceMessage = "is on a MONSTER KILL!";
				ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 3, true, "PvP");
				if (player.getNameColor() == Config.NAME_COLOR_6)
				{
				player.setNameColor(Config.NAME_COLOR_7);
				player.broadcastUserInfo(true);
				}
//				if (player.getTitleColor() == Config.TITLE_COLOR_5)
//				{
//				player.setNameColor(Config.TITLE_COLOR_6);
//				player.broadcastUserInfo(true);
//				}
                break;
            }
            case 20: {
                msgCase = new ExShowScreenMessage("You've reached 20 killing spree!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
                announceMessage = "is Godlike!";
				ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 4, true, "PvP");
				if (player.getNameColor() == Config.NAME_COLOR_7)
				{
				player.setNameColor(Config.NAME_COLOR_8);
				player.broadcastUserInfo(true);
				}
//				if (player.getTitleColor() == Config.TITLE_COLOR_6)
//				{
//				player.setNameColor(Config.TITLE_COLOR_7);
//				player.broadcastUserInfo(true);
//				}
                break;
            }
            case 25: {
                msgCase = new ExShowScreenMessage("You've reached 25 killing spree!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
                announceMessage = "is is Beyond GODLIKE!";
				ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 5, true, "PvP");
				if (player.getNameColor() == Config.NAME_COLOR_8)
				{
				player.setNameColor(Config.NAME_COLOR_9);
				player.broadcastUserInfo(true);
				}
//				if (player.getTitleColor() == Config.TITLE_COLOR_7)
//				{
//				player.setNameColor(Config.TITLE_COLOR_8);
//				player.broadcastUserInfo(true);
//				}
                break;
            }
            case 30: {
                msgCase = new ExShowScreenMessage("You've reached MAX killing spree!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
                announceMessage = "is Beyond GODLIKE!!! Somebody stop him now!!!";
				ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 6, true, "PvP");
				if (player.getNameColor() == Config.NAME_COLOR_9)
				{
				player.setNameColor(Config.NAME_COLOR_10);
				player.broadcastUserInfo(true);
				}
//				if (player.getTitleColor() == Config.TITLE_COLOR_8)
//				{
//				player.setNameColor(Config.TITLE_COLOR_9);
//				player.broadcastUserInfo(true);
//				}
                break;
            }
        }
        if (msgCase != null && announceMessage != null) {
            player.sendPacket((L2GameServerPacket)msgCase);
            Announcements.getInstance().announceToAll(player.getName() + " " + announceMessage, ChatType.BATTLEFIELD);
//            Announcements.getInstance().announceToAll("PvP Manager: " + player.getName() + " " + announceMessage);
        }
    }

    public static SpreeHandler getInstance() {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder {
        protected static final SpreeHandler _instance = new SpreeHandler();

        private SingletonHolder() {
        }
    }

}
