package l2f.gameserver.model.entity;

import l2f.gameserver.Announcements;
import l2f.gameserver.model.Player;
import l2f.gameserver.network.serverpackets.ExShowScreenMessage;
import l2f.gameserver.network.serverpackets.L2GameServerPacket;

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
                break;
            }
            case 4: {
                msgCase = new ExShowScreenMessage("You've reached 4 killing spree!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
                announceMessage = "is Dominating!";
                break;
            }
            case 5: {
                msgCase = new ExShowScreenMessage("You've reached 5 killing spree!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
                announceMessage = "just got a Mega Kill!";
                break;
            }
            case 8: {
                msgCase = new ExShowScreenMessage("You've reached 8 killing spree!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
                announceMessage = "is on an ULTRAKILL!";
                break;
            }
            case 10: {
                msgCase = new ExShowScreenMessage("You've reached 10 killing spree!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
                announceMessage = "is Unstoppable!";
                break;
            }
            case 13: {
                msgCase = new ExShowScreenMessage("You've reached 13 killing spree!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
                announceMessage = "is Wicked Sick!";
                break;
            }
            case 15: {
                msgCase = new ExShowScreenMessage("You've reached 15 killing spree!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
                announceMessage = "is on a MONSTER KILL!";
                break;
            }
            case 20: {
                msgCase = new ExShowScreenMessage("You've reached 20 killing spree!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
                announceMessage = "is Godlike!";
                break;
            }
            case 25: {
                msgCase = new ExShowScreenMessage("You've reached 25 killing spree!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
                announceMessage = "is is Beyond GODLIKE!";
                break;
            }
            case 30: {
                msgCase = new ExShowScreenMessage("You've reached MAX killing spree!", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
                announceMessage = "is Beyond GODLIKE!!! Somebody stop him now!!!";
                break;
            }
        }
        if (msgCase != null && announceMessage != null) {
            player.sendPacket((L2GameServerPacket)msgCase);
            Announcements.getInstance().announceToAll("PvP Manager: " + player.getName() + " " + announceMessage);
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
