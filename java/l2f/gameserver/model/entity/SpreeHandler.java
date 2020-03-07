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
            	if (player.isInZonePvP())
            	{
				ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");
            	}
            	else
            	{
//  
            	}
//            	AntiFeedManager.getInstance().setLastDeathTime(player.getTarget().getPlayer().getObjectId());
                break;
            }
            case 2: {
                	if (player.isInZonePvP())
                	{
                		if (!player.isInSameParty(player.getTarget().getPlayer()) || !player.isInSameClan(player.getTarget().getPlayer()) || !player.isInSameChannel(player.getTarget().getPlayer()) || !player.isInSameAlly(player.getTarget().getPlayer())) // only if player.is in both epic and battle zone
                		{
                			ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");
                		}
                	}
                	else
                	{
                		if (player.getKarma() <= 0 && (!player.isInSameParty(player.getTarget().getPlayer()) || !player.isInSameClan(player.getTarget().getPlayer()) || !player.isInSameChannel(player.getTarget().getPlayer()) || !player.isInSameAlly(player.getTarget().getPlayer()))) // only if player.is in both epic and battle zone
                		{
//                			ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");	
                		}
                	}
                break;
            }
            case 3: {
                	if (player.isInZonePvP())
                	{
                		if (!player.isInSameParty(player.getTarget().getPlayer()) || !player.isInSameClan(player.getTarget().getPlayer()) || !player.isInSameChannel(player.getTarget().getPlayer()) || !player.isInSameAlly(player.getTarget().getPlayer())) // only if player.is in both epic and battle zone
                		{
                            msgCase = new ExShowScreenMessage("Killing Spree!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (player.getNameColor() == Config.NORMAL_NAME_COLOUR)
            				{
            				player.setNameColor(Config.NAME_COLOR_1);
            				player.broadcastUserInfo(true);
            				}
                			ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");
                		}
                	}
                	else
                	{
                		if (player.getKarma() <= 0 && (!player.isInSameParty(player.getTarget().getPlayer()) || !player.isInSameClan(player.getTarget().getPlayer()) || !player.isInSameChannel(player.getTarget().getPlayer()) || !player.isInSameAlly(player.getTarget().getPlayer()))) // only if player.is in both epic and battle zone
                		{
                            msgCase = new ExShowScreenMessage("Killing Spree!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (player.getNameColor() == Config.NORMAL_NAME_COLOUR)
            				{
            				player.setNameColor(Config.NAME_COLOR_1);
            				player.broadcastUserInfo(true);
            				}
                			ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");	
                		}
                	}
                break;
              }
            case 4: {
                	if (player.isInZonePvP())
                	{
                		if (!player.isInSameParty(player.getTarget().getPlayer()) || !player.isInSameClan(player.getTarget().getPlayer()) || !player.isInSameChannel(player.getTarget().getPlayer()) || !player.isInSameAlly(player.getTarget().getPlayer())) // only if player.is in both epic and battle zone
                		{
                            msgCase = new ExShowScreenMessage("Dominating!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (player.getNameColor() == Config.NAME_COLOR_1)
            				{
            				player.setNameColor(Config.NAME_COLOR_2);
            				player.broadcastUserInfo(true);
            				}
//            				if (player.getTitleColor() == Config.NORMAL_TITLE_COLOUR)
//            				{
//            				player.setNameColor(Config.TITLE_COLOR_1);
//            				player.broadcastUserInfo(true);
//            				}
                			ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");
                		}
                	}
                	else
                	{
                		if (player.getKarma() <= 0 && (!player.isInSameParty(player.getTarget().getPlayer()) || !player.isInSameClan(player.getTarget().getPlayer()) || !player.isInSameChannel(player.getTarget().getPlayer()) || !player.isInSameAlly(player.getTarget().getPlayer()))) // only if player.is in both epic and battle zone
                		{
                            msgCase = new ExShowScreenMessage("Dominating!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (player.getNameColor() == Config.NAME_COLOR_1)
            				{
            				player.setNameColor(Config.NAME_COLOR_2);
            				player.broadcastUserInfo(true);
            				}
//            				if (player.getTitleColor() == Config.NORMAL_TITLE_COLOUR)
//            				{
//            				player.setNameColor(Config.TITLE_COLOR_1);
//            				player.broadcastUserInfo(true);
//            				}
                			ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");	
                		}
                	}
                break;
              }
            case 5: {
                	if (player.isInZonePvP())
                	{
                		if (!player.isInSameParty(player.getTarget().getPlayer()) || !player.isInSameClan(player.getTarget().getPlayer()) || !player.isInSameChannel(player.getTarget().getPlayer()) || !player.isInSameAlly(player.getTarget().getPlayer())) // only if player.is in both epic and battle zone
                		{
                            msgCase = new ExShowScreenMessage("MEGA KILL!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (player.getNameColor() == Config.NAME_COLOR_2)
            				{
            				player.setNameColor(Config.NAME_COLOR_3);
            				player.broadcastUserInfo(true);
            				}
//            				if (player.getTitleColor() == Config.TITLE_COLOR_1)
//            				{
//            				player.setNameColor(Config.TITLE_COLOR_2);
//            				player.broadcastUserInfo(true);
//            				}
                			ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");
                		}
                	}
                	else
                	{
                		if (player.getKarma() <= 0 && (!player.isInSameParty(player.getTarget().getPlayer()) || !player.isInSameClan(player.getTarget().getPlayer()) || !player.isInSameChannel(player.getTarget().getPlayer()) || !player.isInSameAlly(player.getTarget().getPlayer()))) // only if player.is in both epic and battle zone
                		{
                            msgCase = new ExShowScreenMessage("MEGA KILL!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (player.getNameColor() == Config.NAME_COLOR_2)
            				{
            				player.setNameColor(Config.NAME_COLOR_3);
            				player.broadcastUserInfo(true);
            				}
//            				if (player.getTitleColor() == Config.TITLE_COLOR_1)
//            				{
//            				player.setNameColor(Config.TITLE_COLOR_2);
//            				player.broadcastUserInfo(true);
//            				}
                			ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");	
                		}
                	}
                break;
              }
            case 8: {
                	if (player.isInZonePvP())
                	{
                		if (!player.isInSameParty(player.getTarget().getPlayer()) || !player.isInSameClan(player.getTarget().getPlayer()) || !player.isInSameChannel(player.getTarget().getPlayer()) || !player.isInSameAlly(player.getTarget().getPlayer())) // only if player.is in both epic and battle zone
                		{
                            announceMessage = " is on an ULTRAKILL!";
                            msgCase = new ExShowScreenMessage("ULTRAKILL!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (player.getNameColor() == Config.NAME_COLOR_3)
            				{
            				player.setNameColor(Config.NAME_COLOR_4);
            				player.broadcastUserInfo(true);
            				}
//            				if (player.getTitleColor() == Config.TITLE_COLOR_2)
//            				{
//            				player.setNameColor(Config.TITLE_COLOR_3);
//            				player.broadcastUserInfo(true);
//            				}
                			ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");
                		}
                	}
                	else
                	{
                		if (player.getKarma() <= 0 && (!player.isInSameParty(player.getTarget().getPlayer()) || !player.isInSameClan(player.getTarget().getPlayer()) || !player.isInSameChannel(player.getTarget().getPlayer()) || !player.isInSameAlly(player.getTarget().getPlayer()))) // only if player.is in both epic and battle zone
                		{
                            announceMessage = " is on an ULTRAKILL!";
                            msgCase = new ExShowScreenMessage("ULTRAKILL!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (player.getNameColor() == Config.NAME_COLOR_3)
            				{
            				player.setNameColor(Config.NAME_COLOR_4);
            				player.broadcastUserInfo(true);
            				}
//            				if (player.getTitleColor() == Config.TITLE_COLOR_2)
//            				{
//            				player.setNameColor(Config.TITLE_COLOR_3);
//            				player.broadcastUserInfo(true);
//            				}
                			ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");	
                		}
                	}
                break;
              }
            case 10: {
                	if (player.isInZonePvP())
                	{
                		if (!player.isInSameParty(player.getTarget().getPlayer()) || !player.isInSameClan(player.getTarget().getPlayer()) || !player.isInSameChannel(player.getTarget().getPlayer()) || !player.isInSameAlly(player.getTarget().getPlayer())) // only if player.is in both epic and battle zone
                		{
                            announceMessage = " is Unstoppable!";
                            msgCase = new ExShowScreenMessage("UNSTOPPABLE!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (player.getNameColor() == Config.NAME_COLOR_4)
            				{
            				player.setNameColor(Config.NAME_COLOR_5);
            				player.broadcastUserInfo(true);
            				}
//            				if (player.getTitleColor() == Config.TITLE_COLOR_3)
//            				{
//            				player.setNameColor(Config.TITLE_COLOR_4);
//            				player.broadcastUserInfo(true);
//            				}
                			ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");
                		}
                	}
                	else
                	{
                		if (player.getKarma() <= 0 && (!player.isInSameParty(player.getTarget().getPlayer()) || !player.isInSameClan(player.getTarget().getPlayer()) || !player.isInSameChannel(player.getTarget().getPlayer()) || !player.isInSameAlly(player.getTarget().getPlayer()))) // only if player.is in both epic and battle zone
                		{
                            announceMessage = " is Unstoppable!";
                            msgCase = new ExShowScreenMessage("UNSTOPPABLE!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (player.getNameColor() == Config.NAME_COLOR_4)
            				{
            				player.setNameColor(Config.NAME_COLOR_5);
            				player.broadcastUserInfo(true);
            				}
//            				if (player.getTitleColor() == Config.TITLE_COLOR_3)
//            				{
//            				player.setNameColor(Config.TITLE_COLOR_4);
//            				player.broadcastUserInfo(true);
//            				}
                			ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");	
                		}
                	}
                break;
              }
            case 13: {
                	if (player.isInZonePvP())
                	{
                		if (!player.isInSameParty(player.getTarget().getPlayer()) || !player.isInSameClan(player.getTarget().getPlayer()) || !player.isInSameChannel(player.getTarget().getPlayer()) || !player.isInSameAlly(player.getTarget().getPlayer())) // only if player.is in both epic and battle zone
                		{
                            announceMessage = " is Wicked Sick!";
                            msgCase = new ExShowScreenMessage("Wicked Sick!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (player.getNameColor() == Config.NAME_COLOR_5)
            				{
            				player.setNameColor(Config.NAME_COLOR_6);
            				player.broadcastUserInfo(true);
            				}
//            				if (player.getTitleColor() == Config.TITLE_COLOR_4)
//            				{
//            				player.setNameColor(Config.TITLE_COLOR_5);
//            				player.broadcastUserInfo(true);
//            				}
                			ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");
                		}
                	}
                	else
                	{
                		if (player.getKarma() <= 0 && (!player.isInSameParty(player.getTarget().getPlayer()) || !player.isInSameClan(player.getTarget().getPlayer()) || !player.isInSameChannel(player.getTarget().getPlayer()) || !player.isInSameAlly(player.getTarget().getPlayer()))) // only if player.is in both epic and battle zone
                		{
                            announceMessage = " is Wicked Sick!";
                            msgCase = new ExShowScreenMessage("Wicked Sick!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (player.getNameColor() == Config.NAME_COLOR_5)
            				{
            				player.setNameColor(Config.NAME_COLOR_6);
            				player.broadcastUserInfo(true);
            				}
//            				if (player.getTitleColor() == Config.TITLE_COLOR_4)
//            				{
//            				player.setNameColor(Config.TITLE_COLOR_5);
//            				player.broadcastUserInfo(true);
//            				}
                			ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");	
                		}
                	}
                break;
              }
            case 15: {
                	if (player.isInZonePvP())
                	{
                		if (!player.isInSameParty(player.getTarget().getPlayer()) || !player.isInSameClan(player.getTarget().getPlayer()) || !player.isInSameChannel(player.getTarget().getPlayer()) || !player.isInSameAlly(player.getTarget().getPlayer())) // only if player.is in both epic and battle zone
                		{
                            announceMessage = " is on a MONSTER KILL!";
                            msgCase = new ExShowScreenMessage("MONSTER KILL!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (player.getNameColor() == Config.NAME_COLOR_6)
            				{
            				player.setNameColor(Config.NAME_COLOR_7);
            				player.broadcastUserInfo(true);
            				}
//            				if (player.getTitleColor() == Config.TITLE_COLOR_5)
//            				{
//            				player.setNameColor(Config.TITLE_COLOR_6);
//            				player.broadcastUserInfo(true);
//            				}
                			ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");
                		}
                	}
                	else
                	{
                		if (player.getKarma() <= 0 && (!player.isInSameParty(player.getTarget().getPlayer()) || !player.isInSameClan(player.getTarget().getPlayer()) || !player.isInSameChannel(player.getTarget().getPlayer()) || !player.isInSameAlly(player.getTarget().getPlayer()))) // only if player.is in both epic and battle zone
                		{
                            announceMessage = " is on a MONSTER KILL!";
                            msgCase = new ExShowScreenMessage("MONSTER KILL!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (player.getNameColor() == Config.NAME_COLOR_6)
            				{
            				player.setNameColor(Config.NAME_COLOR_7);
            				player.broadcastUserInfo(true);
            				}
//            				if (player.getTitleColor() == Config.TITLE_COLOR_5)
//            				{
//            				player.setNameColor(Config.TITLE_COLOR_6);
//            				player.broadcastUserInfo(true);
//            				}
                			ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");	
                		}
                	}
                break;
              }
            case 20: {
                	if (player.isInZonePvP())
                	{
                		if (!player.isInSameParty(player.getTarget().getPlayer()) || !player.isInSameClan(player.getTarget().getPlayer()) || !player.isInSameChannel(player.getTarget().getPlayer()) || !player.isInSameAlly(player.getTarget().getPlayer())) // only if player.is in both epic and battle zone
                		{
                            announceMessage = " is GODLIKE!";
                            msgCase = new ExShowScreenMessage("GODLIKE!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (player.getNameColor() == Config.NAME_COLOR_7)
            				{
            				player.setNameColor(Config.NAME_COLOR_8);
            				player.broadcastUserInfo(true);
            				}
//            				if (player.getTitleColor() == Config.TITLE_COLOR_6)
//            				{
//            				player.setNameColor(Config.TITLE_COLOR_7);
//            				player.broadcastUserInfo(true);
//            				}
                			ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");
                		}
                	}
                	else
                	{
                		if (player.getKarma() <= 0 && (!player.isInSameParty(player.getTarget().getPlayer()) || !player.isInSameClan(player.getTarget().getPlayer()) || !player.isInSameChannel(player.getTarget().getPlayer()) || !player.isInSameAlly(player.getTarget().getPlayer()))) // only if player.is in both epic and battle zone
                		{
                            announceMessage = " is GODLIKE!";
                            msgCase = new ExShowScreenMessage("GODLIKE!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (player.getNameColor() == Config.NAME_COLOR_7)
            				{
            				player.setNameColor(Config.NAME_COLOR_8);
            				player.broadcastUserInfo(true);
            				}
//            				if (player.getTitleColor() == Config.TITLE_COLOR_6)
//            				{
//            				player.setNameColor(Config.TITLE_COLOR_7);
//            				player.broadcastUserInfo(true);
//            				}
                			ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");	
                		}
                	}
                break;
              }
            case 25: {
                	if (player.isInZonePvP())
                	{
                		if (!player.isInSameParty(player.getTarget().getPlayer()) || !player.isInSameClan(player.getTarget().getPlayer()) || !player.isInSameChannel(player.getTarget().getPlayer()) || !player.isInSameAlly(player.getTarget().getPlayer())) // only if player.is in both epic and battle zone
                		{
                            announceMessage = " is Beyond GODLIKE!";
                            msgCase = new ExShowScreenMessage("BEYOND GODLIKE!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (player.getNameColor() == Config.NAME_COLOR_8)
            				{
            				player.setNameColor(Config.NAME_COLOR_9);
            				player.broadcastUserInfo(true);
            				}
//            				if (player.getTitleColor() == Config.TITLE_COLOR_7)
//            				{
//            				player.setNameColor(Config.TITLE_COLOR_8);
//            				player.broadcastUserInfo(true);
//            				}
                			ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");
                		}
                	}
                	else
                	{
                		if (player.getKarma() <= 0 && (!player.isInSameParty(player.getTarget().getPlayer()) || !player.isInSameClan(player.getTarget().getPlayer()) || !player.isInSameChannel(player.getTarget().getPlayer()) || !player.isInSameAlly(player.getTarget().getPlayer()))) // only if player.is in both epic and battle zone
                		{
                            announceMessage = " is Beyond GODLIKE!";
                            msgCase = new ExShowScreenMessage("BEYOND GODLIKE!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (player.getNameColor() == Config.NAME_COLOR_8)
            				{
            				player.setNameColor(Config.NAME_COLOR_9);
            				player.broadcastUserInfo(true);
            				}
//            				if (player.getTitleColor() == Config.TITLE_COLOR_7)
//            				{
//            				player.setNameColor(Config.TITLE_COLOR_8);
//            				player.broadcastUserInfo(true);
//            				}
                			ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");	
                		}
                	}
                break;
              }
            case 30: {
                	if (player.isInZonePvP())
                	{
                		if (!player.isInSameParty(player.getTarget().getPlayer()) || !player.isInSameClan(player.getTarget().getPlayer()) || !player.isInSameChannel(player.getTarget().getPlayer()) || !player.isInSameAlly(player.getTarget().getPlayer())) // only if player.is in both epic and battle zone
                		{
                            msgCase = new ExShowScreenMessage("BEYOND GODLIKE! EVERYONE HAS BEEN WARNED!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
                            announceMessage = "is Beyond GODLIKE!!! Somebody stop him now!!!";
            				if (player.getNameColor() == Config.NAME_COLOR_9)
            				{
            				player.setNameColor(Config.NAME_COLOR_10);
            				player.broadcastUserInfo(true);
            				}
//            				if (player.getTitleColor() == Config.TITLE_COLOR_8)
//            				{
//            				player.setNameColor(Config.TITLE_COLOR_9);
//            				player.broadcastUserInfo(true);
//            				}
                			ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");
                		}
                	}
                	else
                	{
                		if (player.getKarma() <= 0 && (!player.isInSameParty(player.getTarget().getPlayer()) || !player.isInSameClan(player.getTarget().getPlayer()) || !player.isInSameChannel(player.getTarget().getPlayer()) || !player.isInSameAlly(player.getTarget().getPlayer()))) // only if player.is in both epic and battle zone
                		{
                            msgCase = new ExShowScreenMessage("BEYOND GODLIKE! EVERYONE HAS BEEN WARNED!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
                            announceMessage = "is Beyond GODLIKE!!! Somebody stop him now!!!";
            				if (player.getNameColor() == Config.NAME_COLOR_9)
            				{
            				player.setNameColor(Config.NAME_COLOR_10);
            				player.broadcastUserInfo(true);
            				}
//            				if (player.getTitleColor() == Config.TITLE_COLOR_8)
//            				{
//            				player.setNameColor(Config.TITLE_COLOR_9);
//            				player.broadcastUserInfo(true);
//            				}
                			ItemFunctions.addItem(player, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");	
                		}
                	}
                break;
              }
        }
        if (msgCase != null && announceMessage != null) {
            player.sendPacket((L2GameServerPacket)msgCase);
            Announcements.getInstance().announceToAll("Battle Zone: " + player.getName() + " " + announceMessage, ChatType.BATTLEFIELD);
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
