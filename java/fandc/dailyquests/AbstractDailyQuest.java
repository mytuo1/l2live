/*
 * Copyright (C) 2004-2013 L2J Server
 *
 * This file is part of L2J Server.
 *
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package fandc.dailyquests;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import fandc.dailyquests.drops.Droplist;
import fandc.dailyquests.drops.DroplistGroup;
import fandc.dailyquests.drops.DroplistItem;
import fandc.dailyquests.quests.EnchantingDailyQuest;
import fandc.dailyquests.quests.FishingDailyQuest;
import fandc.dailyquests.quests.GeneralPvPDailyQuest;
import fandc.dailyquests.quests.PKHunterDailyQuest;
import l2f.gameserver.data.htm.HtmCache;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.Zone.ZoneType;
import l2f.gameserver.model.quest.QuestState;
import l2f.gameserver.network.serverpackets.ExShowScreenMessage;
import l2f.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import l2f.gameserver.network.serverpackets.ShowBoard;
import l2f.gameserver.templates.StatsSet;
import l2f.gameserver.utils.Util;

/**
 * @author UnAfraid
 */
public abstract class AbstractDailyQuest extends AbstractDPScript
{
	private final Map<String, Long> _questReuse = new ConcurrentHashMap<>();
	private DailyQuestSettings _settings = new DailyQuestSettings();

	protected static final String KILLS_STATIC_COUNT = "KillStaticCount";
	protected static final String KILLS_ADDON_COUNT = "KillAddonCount";
	protected static final String KILLS_ADDON_MIN_COUNT = "KillAddonMinCount";
	protected static final String KILLS_ADDON_MAX_COUNT = "KillAddonMaxCount";

	@Override
	protected void load()
	{

	}

	public boolean isDailyQuest()
	{
		return true;
	}

	protected int getRandomKillsRequired()
	{
		final StatsSet set = getSettings().getParameters();
		return set.getInteger(KILLS_STATIC_COUNT, 20) + (set.getInteger(KILLS_ADDON_COUNT, 10) * getRandom(set.getInteger(KILLS_ADDON_MIN_COUNT, 1), set.getInteger(KILLS_ADDON_MAX_COUNT, 5)));
	}

	protected int getMinKillsRequired()
	{
		final StatsSet set = getSettings().getParameters();
		return set.getInteger(KILLS_STATIC_COUNT, 20) + (set.getInteger(KILLS_ADDON_COUNT, 10) * set.getInteger(KILLS_ADDON_MIN_COUNT, 1));
	}

	protected int getMaxKillsRequired()
	{
		final StatsSet set = getSettings().getParameters();
		return set.getInteger(KILLS_STATIC_COUNT, 40) + (set.getInteger(KILLS_ADDON_COUNT, 10) * set.getInteger(KILLS_ADDON_MAX_COUNT, 5));
	}
	
	// TODO : Make specific protected like above for Online time, Events etc. - Mutiny



	public long getReuseTime(Player player)
	{
		final QuestState st = player.getQuestState(getName());
		if ((st != null) && (st.getState() == COMPLETED) && !st.isNowAvailable())
		{
			return st.getRestartTime();
		}
		else if (isInReuse(player.getNetConnection().getStrixClientData().getClientHWID().toString()))
		{
			return getReuse(player.getNetConnection().getStrixClientData().getClientHWID().toString());
		}
		return 0;
	}

	public String getReuseTimePattern(Player player)
	{
		final long reuse = getReuseTime(player);
		if (getQuestStatus(player).contains("Reusing"))
		{
			if (reuse > System.currentTimeMillis())
			{
				final int uptime = (int) (reuse - System.currentTimeMillis()) / 1000;
				int h = uptime / 3600;
				int m = (uptime - (h * 3600)) / 60;
				int s = ((uptime - (h * 3600)) - (m * 60));
				return h + "h " + m + "m " + s + "s";
			}
		}	
		return "N/A";
	}

	protected void showInfo(Player player, StringTokenizer st)
	{
		int index = -1;
		if (st.hasMoreTokens())
		{
			String token = st.nextToken();
			if (Util.isDigit(token))
			{
				index = Integer.parseInt(token);
			}
		}
		showInfoTemplate(player, index);
	}

	protected void showInfoTemplate(Player player, int index)
	{
		String html = HtmCache.getInstance().getNotNull("DailyQuests/info.htm", player);
		if (html == null)
		{
			player.sendMessage("Couldn't find DailyQuests/info.htm");
			return;
		}
		html = html.replace("%name%", getName());
		html = html.replace("%questName%", getQuestName());
		html = html.replace("%questDescr%", getQuestDescr());

		if (index == 1)
		{
			html = html.replace("%questInfo%", writeQuestInfo(player));
		}
		else if (index == 2)
		{
			html = html.replace("%questRewards%", writeQuestRewards(player));
		}
		else if (index == 3)
		{
			html = html.replace("%questProgress%", writeQuestProgress(player));
		}

		html = html.replace("%questInfo%", ""); // empty place holder
		html = html.replace("%questRewards%", ""); // empty place holder
		html = html.replace("%questProgress%", ""); // empty place holder
		html = html.replace("%startAbort%", ""); // empty place holder
		html = html.replace("%height%", Integer.toString(writeHeight(player, index)));
		ShowBoard.separateAndSend(html, player);
	}
	
	protected void showInfoWeekly(Player player, StringTokenizer st)
	{
		int index = -1;
		if (st.hasMoreTokens())
		{
			String token = st.nextToken();
			if (Util.isDigit(token))
			{
				index = Integer.parseInt(token);
			}
		}
		showInfoWeeklyTemplate(player, index);
	}

	protected void showInfoWeeklyTemplate(Player player, int index)
	{
		String html = HtmCache.getInstance().getNotNull("DailyQuests/infoweekly.htm", player);
		if (html == null)
		{
			player.sendMessage("Couldn't find DailyQuests/infoweekly.htm");
			return;
		}
		html = html.replace("%name%", getName());
		html = html.replace("%questName%", getQuestName());
		html = html.replace("%questDescr%", getQuestDescr());

		if (index == 1)
		{
			html = html.replace("%questInfo%", writeQuestInfo(player));
		}
		else if (index == 2)
		{
			html = html.replace("%questRewards%", writeQuestRewards(player));
		}
		else if (index == 3)
		{
			html = html.replace("%questProgress%", writeQuestProgress(player));
		}

		html = html.replace("%questInfo%", ""); // empty place holder
		html = html.replace("%questRewards%", ""); // empty place holder
		html = html.replace("%questProgress%", ""); // empty place holder
		html = html.replace("%startAbort%", ""); // empty place holder
		html = html.replace("%height%", Integer.toString(writeHeight(player, index)));
		ShowBoard.separateAndSend(html, player);
	}
	
	protected void showInfoTime(Player player, StringTokenizer st)
	{
		int index = -1;
		if (st.hasMoreTokens())
		{
			String token = st.nextToken();
			if (Util.isDigit(token))
			{
				index = Integer.parseInt(token);
			}
		}
		showInfoTimeTemplate(player, index);
	}

	protected void showInfoTimeTemplate(Player player, int index)
	{
		String html = HtmCache.getInstance().getNotNull("DailyQuests/infotime.htm", player);
		if (html == null)
		{
			player.sendMessage("Couldn't find DailyQuests/infotime.htm");
			return;
		}
		html = html.replace("%name%", getName());
		html = html.replace("%questName%", getQuestName());
		html = html.replace("%questDescr%", getQuestDescr());

		if (index == 1)
		{
			html = html.replace("%questInfo%", writeQuestInfo(player));
		}
		else if (index == 2)
		{
			html = html.replace("%questRewards%", writeQuestRewards(player));
		}
		else if (index == 3)
		{
			html = html.replace("%questProgress%", writeQuestProgress(player));
		}

		html = html.replace("%questInfo%", ""); // empty place holder
		html = html.replace("%questRewards%", ""); // empty place holder
		html = html.replace("%questProgress%", ""); // empty place holder
		html = html.replace("%startAbort%", ""); // empty place holder
		html = html.replace("%height%", Integer.toString(writeHeight(player, index)));
		ShowBoard.separateAndSend(html, player);
	}
	protected void showInfoHunting(Player player, StringTokenizer st)
	{
		int index = -1;
		if (st.hasMoreTokens())
		{
			String token = st.nextToken();
			if (Util.isDigit(token))
			{
				index = Integer.parseInt(token);
			}
		}
		showInfoHuntingTemplate(player, index);
	}

	protected void showInfoHuntingTemplate(Player player, int index)
	{
		String html = HtmCache.getInstance().getNotNull("DailyQuests/infohunting.htm", player);
		if (html == null)
		{
			player.sendMessage("Couldn't find DailyQuests/infohunting.htm");
			return;
		}
		html = html.replace("%name%", getName());
		html = html.replace("%questName%", getQuestName());
		html = html.replace("%questDescr%", getQuestDescr());

		if (index == 1)
		{
			html = html.replace("%questInfo%", writeQuestInfo(player));
		}
		else if (index == 2)
		{
			html = html.replace("%questRewards%", writeQuestRewards(player));
		}
		else if (index == 3)
		{
			html = html.replace("%questProgress%", writeQuestProgress(player));
		}

		html = html.replace("%questInfo%", ""); // empty place holder
		html = html.replace("%questRewards%", ""); // empty place holder
		html = html.replace("%questProgress%", ""); // empty place holder
		html = html.replace("%startAbort%", ""); // empty place holder
		html = html.replace("%height%", Integer.toString(writeHeight(player, index)));
		ShowBoard.separateAndSend(html, player);
	}

	/**
	 * @param player
	 * @param index
	 * @return
	 */
	protected int writeHeight(Player player, int index)
	{
		return 480;
	}

	/**
	 * @param player
	 * @return
	 */
	protected String writeQuestProgress(Player player)
	{
		return "";
	}

	/**
	 * @param player
	 * @return
	 */
	protected String writeQuestRewards(Player player)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("Reward items:<br>");
		sb.append("<table width=725 height=20 background=\"L2UI_CT1.Button_DF_Calculator\">");
		sb.append("<tr>");
		sb.append("<td fixwidth=\"5\"></td>");
		sb.append("<td fixwidth=\"32\">Icon</td>");
		sb.append("<td fixwidth=\"220\">Name</td>");
		sb.append("<td fixwidth=\"60\">Count</td>");
		sb.append("<td fixwidth=\"20\">Cat</td>");
		sb.append("<td fixwidth=\"50\">Cat chance</td>");
		sb.append("<td fixwidth=\"50\">Item chance</td>");
		sb.append("<td fixwidth=\"5\"></td>");
		sb.append("</tr>");
		sb.append("</table>");

		final List<DroplistGroup> drops = getRewardList().getGroups();
		if (drops.size() > 2)
		{
			sb.append("<table width=725 height=20 background=\"L2UI_CT1.Windows.Windows_DF_TooltipBG\">");
			sb.append("<tr><td><br></td></tr>");
		}
		else
		{
			sb.append("<table width=725 height=20 background=\"L2UI_CT1.Windows.Windows_DF_TooltipBG\">");
		}
		int i = 1;
		for (DroplistGroup group : drops)
		{
			for (DroplistItem itemDrop : group.getItems())
			{
				sb.append("<tr>");
				sb.append("<td fixwidth=5></td>");
				sb.append("<td fixwidth=32><img src=\"" + getItemIcon(itemDrop.getId()) + "\" width=\"32\" height=\"32\"></td>");
				sb.append("<td fixwidth=220>" + getItemName(itemDrop.getId()) + "</td>");
				sb.append("<td fixwidth=60>" + (itemDrop.getMin() == itemDrop.getMax() ? itemDrop.getMax() : itemDrop.getMin() + " - " + itemDrop.getMax()) + "</td>");
				sb.append("<td fixwidth=20>" + i + "</td>");
				sb.append("<td fixwidth=50>" + Util.formatDouble(group.getChance(), "#.##", false) + "%</td>");
				sb.append("<td fixwidth=50>" + Util.formatDouble(itemDrop.getChance(), "#.##", false) + "%</td>");
				sb.append("<td fixwidth=5></td>");
				sb.append("</tr>");
			}
			i++;
		}

		sb.append("<tr><td><br></td></tr>");
		sb.append("</table>");
		sb.append("<font color=\"LEVEL\">The maximum reward per category is 1</font>");
		return sb.toString();
	}

	/**
	 * @param player
	 * @return
	 */
	protected String writeQuestInfo(Player player)
	{
		return "";
	}

	public Droplist getRewardList()
	{
		return getSettings().getRewards();
	}

	public String getQuestName()
	{
		return getSettings().getName();
	}

	public String getQuestDescr()
	{
		return getSettings().getDescription();
	}

	public boolean canStartQuest(Player player)
	{
		QuestState st = player.getQuestState(getName());
		if (player.getLevel() < getMinLevel())
		{
			return false;
		}
		else if (player.getLevel() > getMaxLevel())
		{
			return false;
		}
		else if ((st != null) && (st.getState() == COMPLETED) && (player.getQuestState(getName()).getRestartTime() <= System.currentTimeMillis()))
		{
			st.abortQuest();
			return true;
		}
		else if (isInReuse(player.getNetConnection().getStrixClientData().getClientHWID().toString()))
		{
			return false;
		}
		return player.getQuestState(getName()) == null;
	}

	public String getQuestStatus(Player player)
	{
		final boolean canStart = canStartQuest(player);
		final boolean isStarted = (player.getQuestState(getName()) != null) && (player.getQuestState(getName()).getState() == STARTED);
		final boolean isReusing = getReuseTime(player) > System.currentTimeMillis();
		// TODO: Use different colors
		if (isStarted)
		{
			return "<font color=\"LEVEL\">Started</font>";
		}
		else if (isReusing)
		{
			return "<font color=\"LEVEL\">Reusing</font>";
		}
		else if (canStart)
		{
			return "<font color=\"LEVEL\">Can be started</font>";
		}
		return "N/A";
	}

	public int getMinLevel()
	{
		return getSettings().getMinLevel();
	}

	public int getMaxLevel()
	{
		return getSettings().getMaxLevel();
	}

	public void onQuestStart(QuestState st)
	{
	}

	protected void onQuestFinish(QuestState st)
	{
		final Player player = st.getPlayer();
		showScreenMessage(player, "completed and rewards can be claimed!", 5000);
//		rewardPlayers(player, getRewardList(), getSettings().isProtectingReward());
	}
	protected void getQuestReward(QuestState st)
	{
		final Player player = st.getPlayer();
		rewardPlayers(player, getRewardList(), getSettings().isProtectingReward());
	}
	protected boolean isRewardClaimed(QuestState st)
	{
		if ((st == null) || (!st.isCompleted() && st.get("rewardClaimed").contentEquals("no")))
		{
			return false;
		}
		if ((st != null) && st.getState() == COMPLETED && st.get("rewardClaimed").contentEquals("yes")) 
		{
			return true;
		}
		return false;
	}

	protected void onQuestAbort(QuestState st)
	{

	}

	public Player getRandomPartyMember(Player player, QuestState st)
	{
		if (player.isInParty())
		{
			final List<Player> players = new ArrayList<>();
			for (Player member : player.getParty().getMembers())
			{
				if (validateRandomPartyMember(st, player, member))
				{
					players.add(member);
				}
			}
			if (!players.isEmpty())
			{
				return players.get(getRandom(players.size()));
			}
		}
		if ((st != null) && (st.getState() == STARTED))
		{			
			return player;
		}
		return null;
	}

	public void showScreenMessage(Player player, String msg, int time)
	{
		player.sendPacket(new ExShowScreenMessage(getQuestName() + " " + msg, time, ScreenMessageAlign.BOTTOM_RIGHT, false));
	}

	/**
	 * @param st
	 * @param killer
	 * @param member
	 * @return
	 */
	protected boolean validateRandomPartyMember(QuestState st, Player killer, Player member)
	{
		if (st != null) //&& (!member.getNetConnection().getStrixClientData().getClientHWID().toString().equals(killer.getNetConnection().getStrixClientData().getClientHWID().toString())))
		{
			return (st.getState() == STARTED) && Util.checkIfInRange(1000, killer, member, true);
		}
		return false;
	}

	/**
	 * @param target
	 * @param killer
	 * @return
	 */
	protected boolean validateKill(Player target, Player killer)
	{
		if ((!killer.isPlayer()) || !killer.isPlayable() || (target == null) || (killer == null) || killer.getNetConnection().getStrixClientData().getClientHWID() == null || target.getNetConnection().getStrixClientData().getClientHWID() == null || (killer.getLevel() < getMinLevel()) || (target.getLevel() < getMinLevel()))
		{
			return false;
		}

		final Player attacker = killer.getPlayer();
		if ((target.isInZone(ZoneType.SIEGE) || attacker.isInZone(ZoneType.SIEGE)) || (target.isInZone(ZoneType.battle_zone) || attacker.isInZone(ZoneType.battle_zone)) || attacker.isInOlympiadMode() || (attacker.getKarma() > 0))
		{
			return false;
		}
		if (target.isInSameClan(attacker) || target.isInSameAlly(attacker) || target.isInSameParty(attacker) || target.isInSameChannel(attacker) || (killer.getNetConnection().getStrixClientData().getClientHWID().toString().equals(target.getNetConnection().getStrixClientData().getClientHWID().toString())))
		{
			return false;
		}
		
		return !killer.getNetConnection().getStrixClientData().getClientHWID().toString().equals(target.getNetConnection().getStrixClientData().getClientHWID().toString());
	}

	public boolean isInReuse(String hwid)
	{
		return !"N/A".equals(hwid) && (_questReuse.containsKey(hwid) && (_questReuse.get(hwid) > System.currentTimeMillis()));
	}

	public void registerReuse(String hwid)
	{
		if (!"N/A".equals(hwid))
		{
			final Calendar reuse = Calendar.getInstance();
			if (reuse.get(Calendar.HOUR_OF_DAY) >= QuestState.RESTART_HOUR)
			{
				reuse.add(Calendar.DATE, 1);
			}
			reuse.set(Calendar.HOUR_OF_DAY, QuestState.RESTART_HOUR);
			reuse.set(Calendar.MINUTE, QuestState.RESTART_MINUTES);
			_questReuse.put(hwid, reuse.getTimeInMillis());
		}
	}
	public void registerWeeklyReuse(String hwid)
	{
		if (!"N/A".equals(hwid))
		{
			final Calendar reuse = Calendar.getInstance();
			if (reuse.get(Calendar.HOUR_OF_DAY) >= QuestState.RESTART_HOUR)
			{
				reuse.add(Calendar.DATE, 1);
			}
			reuse.add(Calendar.DATE, 6);
			reuse.set(Calendar.HOUR_OF_DAY, QuestState.RESTART_HOUR);
			reuse.set(Calendar.MINUTE, QuestState.RESTART_MINUTES);
			_questReuse.put(hwid, reuse.getTimeInMillis());
		}
	}

	public long getReuse(String hwid)
	{
		return !"N/A".equals(hwid) && _questReuse.containsKey(hwid) ? _questReuse.get(hwid) : 0;
	}

	public void resetReuse(String hwid)
	{
		_questReuse.remove(hwid);
	}

	public void setSettings(DailyQuestSettings settings)
	{
		_settings = settings;
	}

	public DailyQuestSettings getSettings()
	{
		return _settings;
	}

	@Override
	public void parseDocument(Document doc)
	{

	}

	public void parseNode(Node n)
	{

	}
}
