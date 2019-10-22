//package events.GvG;
//
//import instances.GvGInstance;
//import l2f.commons.dbutils.DbUtils;
//import l2f.commons.lang.reference.HardReference;
//import l2f.commons.lang.reference.HardReferences;
//import l2f.commons.threading.RunnableImpl;
//import l2f.commons.util.Rnd;
//import l2f.gameserver.Announcements;
//import l2f.gameserver.ThreadPoolManager;
//import l2f.gameserver.data.xml.holder.InstantZoneHolder;
//import l2f.gameserver.data.xml.holder.ResidenceHolder;
//import l2f.gameserver.database.DatabaseFactory;
//import l2f.gameserver.instancemanager.ServerVariables;
//import l2f.gameserver.model.Player;
//import l2f.gameserver.model.base.TeamType;
//import l2f.gameserver.model.entity.olympiad.Olympiad;
//import l2f.gameserver.model.entity.residence.Castle;
//import l2f.gameserver.scripts.Functions;
//import l2f.gameserver.scripts.ScriptFile;
//import l2f.gameserver.templates.InstantZone;
//import l2f.gameserver.utils.Location;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.util.Calendar;
//import java.util.List;
//import java.util.concurrent.CopyOnWriteArrayList;
//import java.util.concurrent.ScheduledFuture;
//
///**
// * Global class for GvG tournament
// *
// * @author pchayka
// */
//public class GvG extends Functions implements ScriptFile {
//    private static final Logger _log = LoggerFactory.getLogger(GvG.class);
//
//    public static final Location TEAM1_LOC = new Location(139736, 145832, -15264); // Team location after teleportation
//    public static final Location TEAM2_LOC = new Location(139736, 139832, -15264);
//    public static final Location RETURN_LOC = new Location(43816, -48232, -822);
//    public static final int[] everydayStartTime = {21, 30, 00}; // hh mm ss
//
//    private static boolean _active = false;
//    private static boolean _isRegistrationActive = false;
//
//    private static int _minLevel = 85;
//    private static int _maxLevel = 99;
//    private static int _groupsLimit = 100; // Limit of groups can register
//    private static int _minPartyMembers = 6; // self-explanatory
//    private static long regActiveTime = 10 * 60 * 1000L; // Timelimit for registration
//
//    private static ScheduledFuture<?> _globalTask;
//    private static ScheduledFuture<?> _regTask;
//    private static ScheduledFuture<?> _countdownTask1;
//    private static ScheduledFuture<?> _countdownTask2;
//    private static ScheduledFuture<?> _countdownTask3;
//
//    private static List<HardReference<Player>> leaderList = new CopyOnWriteArrayList<HardReference<Player>>();
//
//    public static class RegTask extends RunnableImpl {
//        @Override
//        public void runImpl() throws Exception {
//            prepare();
//        }
//    }
//
//    public static class Countdown extends RunnableImpl {
//        int _timer;
//
//        public Countdown(int timer) {
//            _timer = timer;
//        }
//
//        @Override
//        public void runImpl() throws Exception {
//            Announcements.getInstance().announceToAll("GvG: Until the end of application timer there's  " + Integer.toString(_timer) + " min.");
//        }
//    }
//
//    @Override
//    public void onLoad() {
//        _log.info("Loaded Event: GvG");
//        initTimer();
//    }
//
//    @Override
//    public void onReload() {
//    }
//
//    @Override
//    public void onShutdown() {
//    }
//
//    private static void initTimer() {
//        long day = 24 * 60 * 60 * 1000L;
//        Calendar ci = Calendar.getInstance();
//        ci.set(Calendar.HOUR_OF_DAY, everydayStartTime[0]);
//        ci.set(Calendar.MINUTE, everydayStartTime[1]);
//        ci.set(Calendar.SECOND, everydayStartTime[2]);
//
//        long delay = ci.getTimeInMillis() - System.currentTimeMillis();
//        if (delay < 0)
//            delay = delay + day;
//
//        if (_globalTask != null)
//            _globalTask.cancel(true);
//        _globalTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Launch(), delay, day);
//    }
//
//    public static class Launch extends RunnableImpl {
//        @Override
//        public void runImpl() {
//            activateEvent();
//        }
//    }
//
//    private static boolean canBeStarted() {
//        for (Castle c : ResidenceHolder.getInstance().getResidenceList(Castle.class))
//            if (c.getSiegeEvent() != null && c.getSiegeEvent().isInProgress())
//                return false;
//        return true;
//    }
//
//    private static boolean isActive() {
//        return _active;
//    }
//
//    public static void activateEvent() {
//        if (!isActive() && canBeStarted()) {
//            _regTask = ThreadPoolManager.getInstance().schedule(new RegTask(), regActiveTime);
//            if (regActiveTime > 2 * 60 * 1000L) //display countdown announcements only when timelimit for registration is more than 3 mins
//            {
//                if (regActiveTime > 5 * 60 * 1000L)
//                    _countdownTask3 = ThreadPoolManager.getInstance().schedule(new Countdown(5), regActiveTime - 300 * 1000);
//
//                _countdownTask1 = ThreadPoolManager.getInstance().schedule(new Countdown(2), regActiveTime - 120 * 1000);
//                _countdownTask2 = ThreadPoolManager.getInstance().schedule(new Countdown(1), regActiveTime - 60 * 1000);
//            }
//            ServerVariables.set("GvG", "on");
//            _log.info("Event 'GvG' activated.");
//            Announcements.getInstance().announceToAll("Registration for GvG has started" );
//            Announcements.getInstance().announceToAll("Applications will carry on for" + regActiveTime / 60000 + " minutes");
//            _active = true;
//            _isRegistrationActive = true;
//        }
//    }
//
//    /**
//     * Cancels the event during registration time
//     */
//    public static void deactivateEvent() {
//        if (isActive()) {
//            stopTimers();
//            ServerVariables.unset("GvG");
//            _log.info("Event 'GvG' canceled.");
//            Announcements.getInstance().announceToAll("GvG: Tournament Cancelled");
//            _active = false;
//            _isRegistrationActive = false;
//            leaderList.clear();
//        }
//    }
//
//    /**
//     * Shows groups and their leaders who's currently in registration list
//     */
//    public void showStats() {
//        Player player = getSelf();
//        if (!player.getPlayerAccess().IsEventGm)
//            return;
//
//        if (!isActive()) {
//            player.sendMessage("GvG event is not launched");
//            return;
//        }
//
//        StringBuilder string = new StringBuilder();
//        String refresh = "<button value=\"Refresh\" action=\"bypass -h scripts_events.GvG.GvG:showStats\" width=60 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
//        String start = "<button value=\"Start Now\" action=\"bypass -h scripts_events.GvG.GvG:startNow\" width=60 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
//        int i = 0;
//
//        if (!leaderList.isEmpty()) {
//            for (Player leader : HardReferences.unwrap(leaderList)) {
//                if (!leader.isInParty())
//                    continue;
//                string.append("*").append(leader.getName()).append("*").append(" | group members: ").append(leader.getParty().getMemberCount()).append("\n\n");
//                i++;
//            }
//            show("There are " + i + " group leaders who registered for the event:\n\n" + string + "\n\n" + refresh + "\n\n" + start, player, null);
//        } else
//            show("There are no participants at the time\n\n" + refresh, player, null);
//    }
//
//    public void startNow() {
//        Player player = getSelf();
//        if (!player.getPlayerAccess().IsEventGm)
//            return;
//
//        if (!isActive() || !canBeStarted()) {
//            player.sendMessage("GvG event is not launched");
//            return;
//        }
//
//        prepare();
//    }
//
//    /**
//     * Handles the group applications and apply restrictions
//     */
//    public void addGroup() {
//        Player player = getSelf();
//        if (player == null)
//            return;
//
//        if (!_isRegistrationActive) {
//            player.sendMessage("GvG Tournament de-activated.");
//            return;
//        }
//
//        if (leaderList.contains(player.getRef())) {
//            player.sendMessage("You are already registered for the GvG Tournament.");
//            return;
//        }
//
//        if (!player.isInParty()) {
//            player.sendMessage("You are not in a party, so you can not apply.");
//            return;
//        }
//
//        if (!player.getParty().isLeader(player)) {
//            player.sendMessage("Only the Party Leader can issue applications.");
//            return;
//        }
//        if (player.getParty().isInCommandChannel()) {
//            player.sendMessage("If you want to participate in the Tournament you must have a Command Channel.");
//            return;
//        }
//
//        if (leaderList.size() >= _groupsLimit) {
//            player.sendMessage("Group limit count for the event has been reached.");
//            return;
//        }
//
//        List<Player> party = player.getParty().getPartyMembers();
//
//        String[] abuseReason = {
//                "is not in the game",
//                "is not in the group",
//                "is a not full group. Minimum members - 6 .",
//                "Group leader is not here.",
//                "does not meet the required level",
//                "Uses a ride-able pet, which is against the Tournament's regulations.",
//                "is in a Duel, which is against Tournament regulations.",
//                "is participating in another event, which is against Tournament regulations.",
//                "is in a list for Olympiad participation or is in an Olympiad Match.",
//                "is in teleportation state, which is against Tournament regulations.",
//                "is in the Dimensional Rift, which is against Tournament regulations.",
//                "has a Cursed Weapon, which is against Tournament Regulations.",
//                "is not in Peace Zone",
//                "is in Observer mode",};
//
//        for (Player eachmember : party) {
//            int abuseId = checkPlayer(eachmember, false);
//            if (abuseId != 0) {
//                player.sendMessage("Player " + eachmember.getName() + " " + abuseReason[abuseId - 1]);
//                return;
//            }
//        }
//
//        leaderList.add(player.getRef());
//        player.getParty().broadcastMessageToPartyMembers("Your group has applied to the event. Please do not register in other events and do not do Duels until the start of the Tournament.");
//    }
//
//    private static void stopTimers() {
//        if (_regTask != null) {
//            _regTask.cancel(false);
//            _regTask = null;
//        }
//        if (_countdownTask1 != null) {
//            _countdownTask1.cancel(false);
//            _countdownTask1 = null;
//        }
//        if (_countdownTask2 != null) {
//            _countdownTask2.cancel(false);
//            _countdownTask2 = null;
//        }
//        if (_countdownTask3 != null) {
//            _countdownTask3.cancel(false);
//            _countdownTask3 = null;
//        }
//    }
//
//    private static void prepare() {
//        checkPlayers();
//        shuffleGroups();
//
//        if (isActive()) {
//            stopTimers();
//            ServerVariables.unset("GvG");
//            _active = false;
//            _isRegistrationActive = false;
//        }
//
//        if (leaderList.size() < 2) {
//            leaderList.clear();
//            Announcements.getInstance().announceToAll("GvG: Tournament Cancelled due to insufficient players.");
//            return;
//        }
//
//        Announcements.getInstance().announceToAll("GvG: Player applications finished. Starting tournament.");
//        start();
//    }
//
//    /**
//     * @param player
//     * @param doCheckLeadership
//     * @return Handles all limits for every group member. Called 2 times: when registering group and before sending it to the instance
//     */
//    private static int checkPlayer(Player player, boolean doCheckLeadership) {
//        if (!player.isOnline())
//            return 1;
//
//        if (!player.isInParty())
//            return 2;
//
//        if (doCheckLeadership && (player.getParty() == null || !player.getParty().isLeader(player)))
//            return 4;
//
//        if (player.getParty() == null || player.getParty().getMemberCount() < _minPartyMembers)
//            return 3;
//
//        if (player.getLevel() < _minLevel || player.getLevel() > _maxLevel)
//            return 5;
//
//        if (player.isMounted())
//            return 6;
//
//        if (player.isInDuel())
//            return 7;
//
//        if (player.getTeam() != TeamType.NONE)
//            return 8;
//
//        if (player.getOlympiadGame() != null || Olympiad.isRegistered(player))
//            return 9;
//
//        if (player.isTeleporting())
//            return 10;
//
//        if (player.getParty().isInDimensionalRift())
//            return 11;
//
//        if (player.isCursedWeaponEquipped())
//            return 12;
//
//        if (!player.isInPeaceZone())
//            return 13;
//
//        if (player.isInObserverMode())
//            return 14;
//
//        return 0;
//    }
//
//    /**
//     * @return Shuffles groups to separate them in two lists of equals size
//     */
//    private static void shuffleGroups() {
//        if (leaderList.size() % 2 != 0) // If there are odd quantity of groups in the list we should remove one of them to make it even
//        {
//            int rndindex = Rnd.get(leaderList.size());
//            Player expelled = leaderList.remove(rndindex).get();
//            if (expelled != null)
//                expelled.sendMessage("In the formation of players list, your group has been removed. Please excuse us and try next time.");
//        }
//
//        //what hte fuck
//        for (int i = 0; i < leaderList.size(); i++) {
//            int rndindex = Rnd.get(leaderList.size());
//            leaderList.set(i, leaderList.set(rndindex, leaderList.get(i)));
//        }
//    }
//
//    private static void checkPlayers() {
//        for (Player player : HardReferences.unwrap(leaderList)) {
//            if (checkPlayer(player, true) != 0) {
//                leaderList.remove(player.getRef());
//                continue;
//            }
//
//            for (Player partymember : player.getParty().getPartyMembers())
//                if (checkPlayer(partymember, false) != 0) {
//                    player.sendMessage("Your group has been disqualified and removed from Tournament participation because one or more members do not meet the requirements.");
//                    leaderList.remove(player.getRef());
//                    break;
//                }
//        }
//    }
//
//    public static void updateWinner(Player winner) {
//        Connection con = null;
//        PreparedStatement statement = null;
//        try {
//            con = DatabaseFactory.getInstance().getConnection();
//            statement = con.prepareStatement("INSERT INTO event_data(charId, score) VALUES (?,1) ON DUPLICATE KEY UPDATE score=score+1");
//            statement.setInt(1, winner.getObjectId());
//            statement.execute();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            DbUtils.closeQuietly(con, statement);
//        }
//    }
//
//    private static void start() {
//        int instancedZoneId = 504;
//        InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);
//        if (iz == null) {
//            _log.warn("GvG: InstanceZone : " + instancedZoneId + " not found!");
//            return;
//        }
//
//        for (int i = 0; i < leaderList.size(); i += 2) {
//            Player team1Leader = leaderList.get(i).get();
//            Player team2Leader = leaderList.get(i + 1).get();
//
//            GvGInstance r = new GvGInstance();
//            r.setTeam1(team1Leader.getParty());
//            r.setTeam2(team2Leader.getParty());
//            r.init(iz);
//            r.setReturnLoc(GvG.RETURN_LOC);
//
//            for (Player member : team1Leader.getParty().getPartyMembers()) {
//                Functions.unRide(member);
//                Functions.unSummonPet(member, true);
//                member.setTransformation(0);
//                member.setInstanceReuse(instancedZoneId, System.currentTimeMillis());
//                member.dispelBuffs();
//
//                member.teleToLocation(Location.findPointToStay(GvG.TEAM1_LOC, 0, 150, r.getGeoIndex()), r);
//            }
//
//            for (Player member : team2Leader.getParty().getPartyMembers()) {
//                Functions.unRide(member);
//                Functions.unSummonPet(member, true);
//                member.setTransformation(0);
//                member.setInstanceReuse(instancedZoneId, System.currentTimeMillis());
//                member.dispelBuffs();
//
//                member.teleToLocation(Location.findPointToStay(GvG.TEAM2_LOC, 0, 150, r.getGeoIndex()), r);
//            }
//
//            r.start();
//        }
//
//        leaderList.clear();
//        _log.info("GvG: Event started successfuly.");
//    }
//}
//
