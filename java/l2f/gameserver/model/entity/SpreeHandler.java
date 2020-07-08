package l2f.gameserver.model.entity;

import l2f.gameserver.Announcements;
import l2f.gameserver.Config;
import l2f.gameserver.model.Creature;
import l2f.gameserver.model.Player;
import l2f.gameserver.network.serverpackets.ExShowScreenMessage;
import l2f.gameserver.network.serverpackets.L2GameServerPacket;
import l2f.gameserver.network.serverpackets.MagicSkillUse;
import l2f.gameserver.network.serverpackets.components.ChatType;
import l2f.gameserver.utils.ItemFunctions;

public class SpreeHandler {

    public void spreeSystem(Creature killer1, int spreeKills , Player target) {
    	Player killer = (Player) killer1;
        ExShowScreenMessage msgCase = null;
        String announceMessage = null;
        switch (spreeKills) {
            case 1: {
            	if (killer.isInZonePvP())
            	{
				ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");
				killer.broadcastPacket(new MagicSkillUse(killer, killer, 23097, 1, 1000, 0));
            	}
            	else if (!killer.isInZonePvP())
            	{
       			 killer.broadcastPacket(new MagicSkillUse(killer, killer, 23019, 1, 1000, 0));
            	}
//            	AntiFeedManager.getInstance().setLastDeathTime(target.getPlayer().getObjectId());
                break;
            }
            case 2: {
                	if (killer.isInZonePvP())
                	{
                		if (!killer.isInSameParty(target.getPlayer()) || !killer.isInSameClan(target.getPlayer()) || !killer.isInSameChannel(target.getPlayer()) || !killer.isInSameAlly(target.getPlayer())) // only if killer.is in both epic and battle zone
                		{
                			ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");
            				killer.broadcastPacket(new MagicSkillUse(killer, killer, 23098, 1, 1000, 0));
                		}
                	}
                	else if (!killer.isInZonePvP())
                	{
                		if (killer.getKarma() <= 0 && (!killer.isInSameParty(target.getPlayer()) || !killer.isInSameClan(target.getPlayer()) || !killer.isInSameChannel(target.getPlayer()) || !killer.isInSameAlly(target.getPlayer()))) // only if killer.is in both epic and battle zone
                		{
	            			killer.broadcastPacket(new MagicSkillUse(killer, killer, 23019, 1, 1000, 0));
//                			ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");	
                		}
                	}
                break;
            }
            case 3: {
                	if (killer.isInZonePvP())
                	{
                		if (!killer.isInSameParty(target.getPlayer()) || !killer.isInSameClan(target.getPlayer()) || !killer.isInSameChannel(target.getPlayer()) || !killer.isInSameAlly(target.getPlayer())) // only if killer.is in both epic and battle zone
                		{
                            msgCase = new ExShowScreenMessage("Killing Spree!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (killer.getNameColor() == Config.NORMAL_NAME_COLOUR)
            				{
            				killer.setNameColor(Config.NAME_COLOR_1);
            				killer.broadcastUserInfo(true);
            				}
            				killer.broadcastPacket(new MagicSkillUse(killer, killer, 23099, 1, 1000, 0));
                			ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");
                		}
                	}
                	else if (!killer.isInZonePvP())
                	{
                		if (killer.getKarma() <= 0 && (!killer.isInSameParty(target.getPlayer()) || !killer.isInSameClan(target.getPlayer()) || !killer.isInSameChannel(target.getPlayer()) || !killer.isInSameAlly(target.getPlayer()))) // only if killer.is in both epic and battle zone
                		{
                            msgCase = new ExShowScreenMessage("Killing Spree!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (killer.getNameColor() == Config.NORMAL_NAME_COLOUR)
            				{
            				killer.setNameColor(Config.NAME_COLOR_1);
            				killer.broadcastUserInfo(true);
            				}
	            			killer.broadcastPacket(new MagicSkillUse(killer, killer, 23019, 1, 1000, 0));
                			ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");	
                		}
                	}
                break;
              }
            case 4: {
                	if (killer.isInZonePvP())
                	{
                		if (!killer.isInSameParty(target.getPlayer()) || !killer.isInSameClan(target.getPlayer()) || !killer.isInSameChannel(target.getPlayer()) || !killer.isInSameAlly(target.getPlayer())) // only if killer.is in both epic and battle zone
                		{
                            msgCase = new ExShowScreenMessage("Dominating!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (killer.getNameColor() == Config.NAME_COLOR_1)
            				{
            				killer.setNameColor(Config.NAME_COLOR_2);
            				killer.broadcastUserInfo(true);
            				}
//            				if (killer.getTitleColor() == Config.NORMAL_TITLE_COLOUR)
//            				{
//            				killer.setNameColor(Config.TITLE_COLOR_1);
//            				killer.broadcastUserInfo(true);
//            				}
            				killer.broadcastPacket(new MagicSkillUse(killer, killer, 23100, 1, 1000, 0));
                			ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");
                		}
                	}
                	else if (!killer.isInZonePvP())
                	{
                		if (killer.getKarma() <= 0 && (!killer.isInSameParty(target.getPlayer()) || !killer.isInSameClan(target.getPlayer()) || !killer.isInSameChannel(target.getPlayer()) || !killer.isInSameAlly(target.getPlayer()))) // only if killer.is in both epic and battle zone
                		{
                            msgCase = new ExShowScreenMessage("Dominating!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (killer.getNameColor() == Config.NAME_COLOR_1)
            				{
            				killer.setNameColor(Config.NAME_COLOR_2);
            				killer.broadcastUserInfo(true);
            				}
//            				if (killer.getTitleColor() == Config.NORMAL_TITLE_COLOUR)
//            				{
//            				killer.setNameColor(Config.TITLE_COLOR_1);
//            				killer.broadcastUserInfo(true);
//            				}
	            			killer.broadcastPacket(new MagicSkillUse(killer, killer, 23019, 1, 1000, 0));
                			ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");	
                		}
                	}
                break;
              }
            case 5: {
                	if (killer.isInZonePvP())
                	{
                		if (!killer.isInSameParty(target.getPlayer()) || !killer.isInSameClan(target.getPlayer()) || !killer.isInSameChannel(target.getPlayer()) || !killer.isInSameAlly(target.getPlayer())) // only if killer.is in both epic and battle zone
                		{
                            msgCase = new ExShowScreenMessage("MEGA KILL!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (killer.getNameColor() == Config.NAME_COLOR_2)
            				{
            				killer.setNameColor(Config.NAME_COLOR_3);
            				killer.broadcastUserInfo(true);
            				}
//            				if (killer.getTitleColor() == Config.TITLE_COLOR_1)
//            				{
//            				killer.setNameColor(Config.TITLE_COLOR_2);
//            				killer.broadcastUserInfo(true);
//            				}
            				killer.broadcastPacket(new MagicSkillUse(killer, killer, 23101, 1, 1000, 0));
                			ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");
                		}
                	}
                	else if (!killer.isInZonePvP())
                	{
                		if (killer.getKarma() <= 0 && (!killer.isInSameParty(target.getPlayer()) || !killer.isInSameClan(target.getPlayer()) || !killer.isInSameChannel(target.getPlayer()) || !killer.isInSameAlly(target.getPlayer()))) // only if killer.is in both epic and battle zone
                		{
                            msgCase = new ExShowScreenMessage("MEGA KILL!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (killer.getNameColor() == Config.NAME_COLOR_2)
            				{
            				killer.setNameColor(Config.NAME_COLOR_3);
            				killer.broadcastUserInfo(true);
            				}
//            				if (killer.getTitleColor() == Config.TITLE_COLOR_1)
//            				{
//            				killer.setNameColor(Config.TITLE_COLOR_2);
//            				killer.broadcastUserInfo(true);
//            				}
	            			killer.broadcastPacket(new MagicSkillUse(killer, killer, 23019, 1, 1000, 0));
                			ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");	
                		}
                	}
                break;
              }
            case 8: {
                	if (killer.isInZonePvP())
                	{
                		if (!killer.isInSameParty(target.getPlayer()) || !killer.isInSameClan(target.getPlayer()) || !killer.isInSameChannel(target.getPlayer()) || !killer.isInSameAlly(target.getPlayer())) // only if killer.is in both epic and battle zone
                		{
                            announceMessage = " is on an ULTRAKILL!";
                            msgCase = new ExShowScreenMessage("ULTRAKILL!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (killer.getNameColor() == Config.NAME_COLOR_3)
            				{
            				killer.setNameColor(Config.NAME_COLOR_4);
            				killer.broadcastUserInfo(true);
            				}
//            				if (killer.getTitleColor() == Config.TITLE_COLOR_2)
//            				{
//            				killer.setNameColor(Config.TITLE_COLOR_3);
//            				killer.broadcastUserInfo(true);
//            				}
            				killer.broadcastPacket(new MagicSkillUse(killer, killer, 23104, 1, 1000, 0));
                			ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");
                		}
                	}
                	else if (!killer.isInZonePvP())
                	{
                		if (killer.getKarma() <= 0 && (!killer.isInSameParty(target.getPlayer()) || !killer.isInSameClan(target.getPlayer()) || !killer.isInSameChannel(target.getPlayer()) || !killer.isInSameAlly(target.getPlayer()))) // only if killer.is in both epic and battle zone
                		{
                            announceMessage = " is on an ULTRAKILL!";
                            msgCase = new ExShowScreenMessage("ULTRAKILL!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (killer.getNameColor() == Config.NAME_COLOR_3)
            				{
            				killer.setNameColor(Config.NAME_COLOR_4);
            				killer.broadcastUserInfo(true);
            				}
//            				if (killer.getTitleColor() == Config.TITLE_COLOR_2)
//            				{
//            				killer.setNameColor(Config.TITLE_COLOR_3);
//            				killer.broadcastUserInfo(true);
//            				}
	            			killer.broadcastPacket(new MagicSkillUse(killer, killer, 23019, 1, 1000, 0));
                			ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");	
                		}
                	}
                break;
              }
            case 10: {
                	if (killer.isInZonePvP())
                	{
                		if (!killer.isInSameParty(target.getPlayer()) || !killer.isInSameClan(target.getPlayer()) || !killer.isInSameChannel(target.getPlayer()) || !killer.isInSameAlly(target.getPlayer())) // only if killer.is in both epic and battle zone
                		{
                            announceMessage = " is Unstoppable!";
                            msgCase = new ExShowScreenMessage("UNSTOPPABLE!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (killer.getNameColor() == Config.NAME_COLOR_4)
            				{
            				killer.setNameColor(Config.NAME_COLOR_5);
            				killer.broadcastUserInfo(true);
            				}
//            				if (killer.getTitleColor() == Config.TITLE_COLOR_3)
//            				{
//            				killer.setNameColor(Config.TITLE_COLOR_4);
//            				killer.broadcastUserInfo(true);
//            				}
            				killer.broadcastPacket(new MagicSkillUse(killer, killer, 23106, 1, 1000, 0));
                			ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");
                		}
                	}
                	else if (!killer.isInZonePvP())
                	{
                		if (killer.getKarma() <= 0 && (!killer.isInSameParty(target.getPlayer()) || !killer.isInSameClan(target.getPlayer()) || !killer.isInSameChannel(target.getPlayer()) || !killer.isInSameAlly(target.getPlayer()))) // only if killer.is in both epic and battle zone
                		{
                            announceMessage = " is Unstoppable!";
                            msgCase = new ExShowScreenMessage("UNSTOPPABLE!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (killer.getNameColor() == Config.NAME_COLOR_4)
            				{
            				killer.setNameColor(Config.NAME_COLOR_5);
            				killer.broadcastUserInfo(true);
            				}
//            				if (killer.getTitleColor() == Config.TITLE_COLOR_3)
//            				{
//            				killer.setNameColor(Config.TITLE_COLOR_4);
//            				killer.broadcastUserInfo(true);
//            				}
	            			killer.broadcastPacket(new MagicSkillUse(killer, killer, 23019, 1, 1000, 0));
                			ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");	
                		}
                	}
                break;
              }
            case 13: {
                	if (killer.isInZonePvP())
                	{
                		if (!killer.isInSameParty(target.getPlayer()) || !killer.isInSameClan(target.getPlayer()) || !killer.isInSameChannel(target.getPlayer()) || !killer.isInSameAlly(target.getPlayer())) // only if killer.is in both epic and battle zone
                		{
                            announceMessage = " is Wicked Sick!";
                            msgCase = new ExShowScreenMessage("Wicked Sick!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (killer.getNameColor() == Config.NAME_COLOR_5)
            				{
            				killer.setNameColor(Config.NAME_COLOR_6);
            				killer.broadcastUserInfo(true);
            				}
//            				if (killer.getTitleColor() == Config.TITLE_COLOR_4)
//            				{
//            				killer.setNameColor(Config.TITLE_COLOR_5);
//            				killer.broadcastUserInfo(true);
//            				}
            				killer.broadcastPacket(new MagicSkillUse(killer, killer, 23109, 1, 1000, 0));
                			ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");
                		}
                	}
                	else if (!killer.isInZonePvP())
                	{
                		if (killer.getKarma() <= 0 && (!killer.isInSameParty(target.getPlayer()) || !killer.isInSameClan(target.getPlayer()) || !killer.isInSameChannel(target.getPlayer()) || !killer.isInSameAlly(target.getPlayer()))) // only if killer.is in both epic and battle zone
                		{
                            announceMessage = " is Wicked Sick!";
                            msgCase = new ExShowScreenMessage("Wicked Sick!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (killer.getNameColor() == Config.NAME_COLOR_5)
            				{
            				killer.setNameColor(Config.NAME_COLOR_6);
            				killer.broadcastUserInfo(true);
            				}
//            				if (killer.getTitleColor() == Config.TITLE_COLOR_4)
//            				{
//            				killer.setNameColor(Config.TITLE_COLOR_5);
//            				killer.broadcastUserInfo(true);
//            				}
	            			killer.broadcastPacket(new MagicSkillUse(killer, killer, 23019, 1, 1000, 0));
                			ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");	
                		}
                	}
                break;
              }
            case 15: {
                	if (killer.isInZonePvP())
                	{
                		if (!killer.isInSameParty(target.getPlayer()) || !killer.isInSameClan(target.getPlayer()) || !killer.isInSameChannel(target.getPlayer()) || !killer.isInSameAlly(target.getPlayer())) // only if killer.is in both epic and battle zone
                		{
                            announceMessage = " is on a MONSTER KILL!";
                            msgCase = new ExShowScreenMessage("MONSTER KILL!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (killer.getNameColor() == Config.NAME_COLOR_6)
            				{
            				killer.setNameColor(Config.NAME_COLOR_7);
            				killer.broadcastUserInfo(true);
            				}
//            				if (killer.getTitleColor() == Config.TITLE_COLOR_5)
//            				{
//            				killer.setNameColor(Config.TITLE_COLOR_6);
//            				killer.broadcastUserInfo(true);
//            				}
            				killer.broadcastPacket(new MagicSkillUse(killer, killer, 23111, 1, 1000, 0));
                			ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");
                		}
                	}
                	else if (!killer.isInZonePvP())
                	{
                		if (killer.getKarma() <= 0 && (!killer.isInSameParty(target.getPlayer()) || !killer.isInSameClan(target.getPlayer()) || !killer.isInSameChannel(target.getPlayer()) || !killer.isInSameAlly(target.getPlayer()))) // only if killer.is in both epic and battle zone
                		{
                            announceMessage = " is on a MONSTER KILL!";
                            msgCase = new ExShowScreenMessage("MONSTER KILL!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (killer.getNameColor() == Config.NAME_COLOR_6)
            				{
            				killer.setNameColor(Config.NAME_COLOR_7);
            				killer.broadcastUserInfo(true);
            				}
//            				if (killer.getTitleColor() == Config.TITLE_COLOR_5)
//            				{
//            				killer.setNameColor(Config.TITLE_COLOR_6);
//            				killer.broadcastUserInfo(true);
//            				}
	            			killer.broadcastPacket(new MagicSkillUse(killer, killer, 23019, 1, 1000, 0));
                			ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");	
                		}
                	}
                break;
              }
            case 20: {
                	if (killer.isInZonePvP())
                	{
                		if (!killer.isInSameParty(target.getPlayer()) || !killer.isInSameClan(target.getPlayer()) || !killer.isInSameChannel(target.getPlayer()) || !killer.isInSameAlly(target.getPlayer())) // only if killer.is in both epic and battle zone
                		{
                            announceMessage = " is GODLIKE!";
                            msgCase = new ExShowScreenMessage("GODLIKE!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (killer.getNameColor() == Config.NAME_COLOR_7)
            				{
            				killer.setNameColor(Config.NAME_COLOR_8);
            				killer.broadcastUserInfo(true);
            				}
//            				if (killer.getTitleColor() == Config.TITLE_COLOR_6)
//            				{
//            				killer.setNameColor(Config.TITLE_COLOR_7);
//            				killer.broadcastUserInfo(true);
//            				}
            				killer.broadcastPacket(new MagicSkillUse(killer, killer, 23116, 1, 1000, 0));
                			ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");
                		}
                	}
                	else if (!killer.isInZonePvP())
                	{
                		if (killer.getKarma() <= 0 && (!killer.isInSameParty(target.getPlayer()) || !killer.isInSameClan(target.getPlayer()) || !killer.isInSameChannel(target.getPlayer()) || !killer.isInSameAlly(target.getPlayer()))) // only if killer.is in both epic and battle zone
                		{
                            announceMessage = " is GODLIKE!";
                            msgCase = new ExShowScreenMessage("GODLIKE!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (killer.getNameColor() == Config.NAME_COLOR_7)
            				{
            				killer.setNameColor(Config.NAME_COLOR_8);
            				killer.broadcastUserInfo(true);
            				}
//            				if (killer.getTitleColor() == Config.TITLE_COLOR_6)
//            				{
//            				killer.setNameColor(Config.TITLE_COLOR_7);
//            				killer.broadcastUserInfo(true);
//            				}
	            			killer.broadcastPacket(new MagicSkillUse(killer, killer, 23019, 1, 1000, 0));
                			ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");	
                		}
                	}
                break;
              }
            case 25: {
                	if (killer.isInZonePvP())
                	{
                		if (!killer.isInSameParty(target.getPlayer()) || !killer.isInSameClan(target.getPlayer()) || !killer.isInSameChannel(target.getPlayer()) || !killer.isInSameAlly(target.getPlayer())) // only if killer.is in both epic and battle zone
                		{
                            announceMessage = " is Beyond GODLIKE!";
                            msgCase = new ExShowScreenMessage("BEYOND GODLIKE!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (killer.getNameColor() == Config.NAME_COLOR_8)
            				{
            				killer.setNameColor(Config.NAME_COLOR_9);
            				killer.broadcastUserInfo(true);
            				}
//            				if (killer.getTitleColor() == Config.TITLE_COLOR_7)
//            				{
//            				killer.setNameColor(Config.TITLE_COLOR_8);
//            				killer.broadcastUserInfo(true);
//            				}
	            			killer.broadcastPacket(new MagicSkillUse(killer, killer, 23019, 1, 1000, 0));
                			ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");
                		}
                	}
                	else if (!killer.isInZonePvP())
                	{
                		if (killer.getKarma() <= 0 && (!killer.isInSameParty(target.getPlayer()) || !killer.isInSameClan(target.getPlayer()) || !killer.isInSameChannel(target.getPlayer()) || !killer.isInSameAlly(target.getPlayer()))) // only if killer.is in both epic and battle zone
                		{
                            announceMessage = " is Beyond GODLIKE!";
                            msgCase = new ExShowScreenMessage("BEYOND GODLIKE!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
            				if (killer.getNameColor() == Config.NAME_COLOR_8)
            				{
            				killer.setNameColor(Config.NAME_COLOR_9);
            				killer.broadcastUserInfo(true);
            				}
//            				if (killer.getTitleColor() == Config.TITLE_COLOR_7)
//            				{
//            				killer.setNameColor(Config.TITLE_COLOR_8);
//            				killer.broadcastUserInfo(true);
//            				}
	            			killer.broadcastPacket(new MagicSkillUse(killer, killer, 23019, 1, 1000, 0));
                			ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");	
                		}
                	}
                break;
              }
            case 30: {
                	if (killer.isInZonePvP())
                	{
                		if (!killer.isInSameParty(target.getPlayer()) || !killer.isInSameClan(target.getPlayer()) || !killer.isInSameChannel(target.getPlayer()) || !killer.isInSameAlly(target.getPlayer())) // only if killer.is in both epic and battle zone
                		{
                            msgCase = new ExShowScreenMessage("BEYOND GODLIKE! EVERYONE HAS BEEN WARNED!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
                            announceMessage = "is Beyond GODLIKE!!! Somebody stop him now!!!";
            				if (killer.getNameColor() == Config.NAME_COLOR_9)
            				{
            				killer.setNameColor(Config.NAME_COLOR_10);
            				killer.broadcastUserInfo(true);
            				}
//            				if (killer.getTitleColor() == Config.TITLE_COLOR_8)
//            				{
//            				killer.setNameColor(Config.TITLE_COLOR_9);
//            				killer.broadcastUserInfo(true);
//            				}
	            			killer.broadcastPacket(new MagicSkillUse(killer, killer, 23019, 1, 1000, 0));
                			ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");
                			
                		}
                	}
                	else if (!killer.isInZonePvP())
                	{
                		if (killer.getKarma() <= 0 && (!killer.isInSameParty(target.getPlayer()) || !killer.isInSameClan(target.getPlayer()) || !killer.isInSameChannel(target.getPlayer()) || !killer.isInSameAlly(target.getPlayer()))) // only if killer.is in both epic and battle zone
                		{
                            msgCase = new ExShowScreenMessage("BEYOND GODLIKE! EVERYONE HAS BEEN WARNED!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
                            announceMessage = "is Beyond GODLIKE!!! Somebody stop him now!!!";
            				if (killer.getNameColor() == Config.NAME_COLOR_9)
            				{
            				killer.setNameColor(Config.NAME_COLOR_10);
            				killer.broadcastUserInfo(true);
            				}
//            				if (killer.getTitleColor() == Config.TITLE_COLOR_8)
//            				{
//            				killer.setNameColor(Config.TITLE_COLOR_9);
//            				killer.broadcastUserInfo(true);
//            				}
	            			killer.broadcastPacket(new MagicSkillUse(killer, killer, 23019, 1, 1000, 0));
                			ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_REWARD_ITEM, 1, true, "PvP");	
                		}
                	}
                break;
              }
        }
        if (msgCase != null && announceMessage != null) {
            killer.sendPacket((L2GameServerPacket)msgCase);
            Announcements.getInstance().announceToAll("Battle Zone: " + killer.getName() + " " + announceMessage, ChatType.BATTLEFIELD);
//            Announcements.getInstance().announceToAll("PvP Manager: " + killer.getName() + " " + announceMessage);
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
