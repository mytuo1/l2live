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

import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import fandc.dailyquests.drops.DroplistGroup;
import fandc.dailyquests.drops.DroplistItem;
import fandc.dailyquests.quests.ClassSpecificPvPDailyQuest;
import l2f.gameserver.handler.bbs.ICommunityBoardHandler;
import l2f.gameserver.listener.actor.player.OnAnswerListener;
import l2f.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.actor.listener.CharListenerList;
import l2f.gameserver.model.items.ItemHold;
import l2f.gameserver.model.quest.QuestState;
import l2f.gameserver.network.serverpackets.ConfirmDlg;
import l2f.gameserver.network.serverpackets.Say2;
import l2f.gameserver.network.serverpackets.ShowBoard;
import l2f.gameserver.network.serverpackets.components.ChatType;
import l2f.gameserver.network.serverpackets.components.SystemMsg;
import l2f.gameserver.skills.SkillHold;
import l2f.gameserver.templates.StatsSet;

/**
 * @author UnAfraid
 */
public class WeeklyQuestHandler extends AbstractDPScript implements ICommunityBoardHandler, OnPlayerEnterListener {
	private static final AbstractDailyQuest[] QUESTS = new AbstractDailyQuest[] 
		{ 
		  new ClassSpecificPvPDailyQuest(), 
		};

	@Override
	public String[] getBypassCommands() {
		return new String[] {
//          "_friendlist_",
				"_bbs_weekly_quests" };
	}

	public WeeklyQuestHandler() {
//      if (!Config.ENABLE_DAILY_QUESTS)
//          return;

		load();

		CharListenerList.addGlobal(this);
	}

	@Override
	protected void load() {
		parseDatapackFile("config/mod/WeeklyQuests.xml");
		log("Loaded WeeklyQuests data");
	}

	/********************************************/
	// Parser
	/********************************************/

	@Override
	protected final void parseDocument(Document doc) {
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
			if ("list".equalsIgnoreCase(n.getNodeName())) {
				for (Node b = n.getFirstChild(); b != null; b = b.getNextSibling()) {
					if ("quest".equals(b.getNodeName())) {
						final String questName = parseString(b.getAttributes(), "name");
						AbstractDailyQuest quest = null;
						for (AbstractDailyQuest q : QUESTS) {
							if (q.getName().equalsIgnoreCase(questName)) {
								quest = q;
								break;
							}
						}
						if (quest == null) {
							_log.warn("Couldn't find quest: " + questName);
							continue;
						}

						// Set new settings to allow reloading.
						quest.setSettings(new DailyQuestSettings());

						// Notify quest to parse custom content
						quest.parseDocument(doc);
						quest.parseNode(b);

						for (Node d = b.getFirstChild(); d != null; d = d.getNextSibling()) {
							switch (d.getNodeName()) {
							case "display": {
								final NamedNodeMap attrs = d.getAttributes();
								quest.getSettings().setName(parseString(attrs, "name"));
								quest.getSettings().setDescription(parseString(attrs, "description"));
								quest.getSettings().setEnabled(parseBoolean(attrs, "enabled", true));
								break;
							}
							case "levels": {
								final NamedNodeMap attrs = d.getAttributes();
								quest.getSettings().setMinLevel(parseInt(attrs, "min", 1));
								quest.getSettings().setMaxLevel(parseInt(attrs, "max", 86));
								break;
							}
							case "params": {
								final StatsSet set = new StatsSet();
								for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
									final NamedNodeMap attrs = cd.getAttributes();
									switch (cd.getNodeName()) {
									case "param": {
										set.set(parseString(attrs, "name"), parseString(attrs, "val"));
										break;
									}
									case "skill_param": {
										set.set(parseString(attrs, "name"),
												new SkillHold(parseInt(attrs, "id"), parseInt(attrs, "level")));
										break;
									}
									case "item_param": {
										set.set(parseString(attrs, "name"),
												new ItemHold(parseInt(attrs, "id"), parseInt(attrs, "count")));
										break;
									}
									}
								}
								quest.getSettings().setParameters(set);
								break;
							}
							case "rewards": {
								// Set protection of rewards
								quest.getSettings()
										.setProtectedReward(parseBoolean(d.getAttributes(), "protectReward", true));

								// Parse categories and items
								for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
									if ("category".equalsIgnoreCase(cd.getNodeName())) {
										DroplistGroup group = new DroplistGroup(
												parseFloat(cd.getAttributes(), "chance"));
										for (Node z = cd.getFirstChild(); z != null; z = z.getNextSibling()) {
											if ("item".equalsIgnoreCase(z.getNodeName())) {
												int itemId = parseInt(z.getAttributes(), "id");
												int min = parseInt(z.getAttributes(), "min");
												int max = parseInt(z.getAttributes(), "max");
												float chance = parseFloat(z.getAttributes(), "chance");
												group.addItem(new DroplistItem(itemId, min, max, chance));
											}
										}
										quest.getSettings().addRewardGroup(group);
									}
								}
								break;
							}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void onPlayerEnter(Player player) {
		boolean hasQuestTaken = false;
		for (AbstractDailyQuest quest : QUESTS) {
			if (player.getQuestState(quest.getName()) != null) {
				hasQuestTaken = true;
				break;
			}
		}
		if (!hasQuestTaken) {
			player.sendPacket(new Say2(0, ChatType.TELL, "Mutiny Quest Engine",
					"There are weekly quests available for you.\nTry them from ALt + B -> Daily Quests"));
		}
	}

	@Override
	public void onBypassCommand(Player player, String bypass) {
		// Friendlist tab shows the main window
//      if (bypass.startsWith("_friendlist_"))
//      {
//          onBypassCommand(player, "_bbs_daily_quests");
//          return;
//      }

		final StringTokenizer st = new StringTokenizer(bypass, ";");
		final String cmd = st.nextToken();
		switch (cmd) {
		case "_bbs_weekly_quests": {
			if (!st.hasMoreTokens()) {
				sendMainHtml(player);
				break;
			}

			final String subCmd = st.nextToken();
			switch (subCmd) {
			case "info": {
				if (st.hasMoreTokens()) {
					final String questName = st.nextToken();
					for (AbstractDailyQuest quest : QUESTS) {
						if (quest.getName().equals(questName)) {
							quest.showInfoWeekly(player, st);
							break;
						}
					}
				}
				break;
			}
			case "start": {
				if (st.hasMoreTokens()) {
					final String questName = st.nextToken();
					for (AbstractDailyQuest quest : QUESTS) {
						if (quest.getName().equals(questName)) {
							if (quest.canStartQuest(player)) {
								final QuestState qs = quest.newQuestState(player, STARTED);
								quest.onQuestStart(qs);
								quest.showScreenMessage(player, "Have been successfuly started!", 10000);
								quest.registerWeeklyReuse(
										player.getNetConnection().getStrixClientData().getClientHWID().toString());
							} else {
								quest.showScreenMessage(player, "Can't be started because you don't meet conditions!",
										10000);
							}
							onBypassCommand(player, "_bbs_weekly_quests;info;" + quest.getName() + ";3");
						}
					}
				}
				break;
			}
			case "abort": {
				if (st.hasMoreTokens()) {
					final String questName = st.nextToken();
					for (AbstractDailyQuest quest : QUESTS) {
						if (quest.getName().equals(questName)) {
							final QuestState qs = player.getQuestState(quest.getName());
							if ((qs != null) && (qs.getState() == STARTED)) {
								final ConfirmDlg dlg = new ConfirmDlg(SystemMsg.S1, 15 * 1000).addString(
										"You are about to abort " + quest.getQuestName() + " are you sure about this?");
								player.ask(dlg, new AbortQuestDlg(player, quest));
							}
							break;
						}
					}
				}
				break;
			}
			case "reward": {
				final String questName = st.nextToken();
				for (AbstractDailyQuest quest : QUESTS) {
					if (quest.getName().equals(questName)) {
						final QuestState qs = player.getQuestState(quest.getName());
						if ((qs != null) && (qs.getState() == COMPLETED)
								&& (qs.getRestartTime() > System.currentTimeMillis())) {
							qs.set("rewardClaimed", "yes");
							onBypassCommand(player, "_bbshome");
							onBypassCommand(player, "_bbs_weekly_quests");
							rewardPlayers(player, quest.getRewardList(), quest.getSettings().isProtectingReward());
						}
						break;
					}
				}
				break;
			}
			}
		}
		}
	}

//	private void sendMainHtml(Player player) {
//		String html = getHtm(player, "main.htm");
//		final StringBuilder sb = new StringBuilder();
//
//		for (AbstractDailyQuest quest : QUESTS) 
//		{
//			QuestState st = player.getQuestState(quest.getName());
//
//			if (!quest.getSettings().isEnabled()) {
//				continue;
//			}
//			sb.append("<tr><td width=\"600\"><center><font name=\"hs12\" color=\"LEVEL\">" + quest.getQuestName()
//					+ "</font></td></tr>");
//			sb.append("<tr><td width=\"600\"><center>" + quest.getQuestDescr() + "</td>");
////			if (st == null
////					|| (st.getState() == COMPLETED 
////					&& quest.isRewardClaimed(player.getQuestState(quest.getName()))
////					&& st.getRestartTime() <= System.currentTimeMillis())) {
////				sb.append("<tr><td width=\"600\"><center><font name=\"hs10\" color=\"3ADF00\">" + " Available "
////						+ "</font></td>");
////
////			} else if (st.isStarted()) {
////				sb.append("<tr><td width=\"600\"><center><font name=\"hs10\" color=\"FF6633\">" + " In Progress "
////						+ "</font></td>");
////			} else {
//////				sb.append("<tr><td width=\"600\"><center><font name=\"hs10\" color=\"F4FA58\">"
//////						+ quest.getReuseTimePattern(player) + "</font></td>");
////				sb.append("<tr><td width=\"600\"><center><font name=\"hs10\" color=\"F4FA58\">"
////						+ " Not Available " + "</font></td>");
////			}
//
//			if ((st == null) || ((st.getState() == COMPLETED) 
//					&& (quest.isRewardClaimed(player.getQuestState(quest.getName()))) 
//					&& (st.getRestartTime() <= System.currentTimeMillis())))
//			{
//				sb.append(
//						"<td width=\"600\" valign=\"top\"><center><button value=\"Info\" action=\"bypass _bbs_daily_quests;info;"
//						+ quest.getName()
//						+ "\" width=\"120\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
//				sb.append("<td width=\"600\"><center><button value=\"Start\" action=\"bypass _bbs_daily_quests;start;"
//						+ quest.getName()
//						+ "\" width=\"120\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
//			}
//			else if (!st.isCompleted() 
//					&& st.isStarted() 
//					&& quest.getQuestName() != "Online Time Challenge")
//			{
//				sb.append(
//						"<td width=\"600\" valign=\"top\"><center><button value=\"Info\" action=\"bypass _bbs_daily_quests;info;"
//						+ quest.getName()
//						+ "\" width=\"120\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
//				sb.append("<td width=\"600\"><center><button value=\"Abort\" action=\"bypass _bbs_daily_quests;abort;"
//						+ quest.getName()
//						+ "\" width=\"120\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
//			}
//			else if (st.getState() == COMPLETED 
//					&& !quest.isRewardClaimed(player.getQuestState(quest.getName()))
//					&& st.getRestartTime() > System.currentTimeMillis()) 
//			{
//				sb.append(
//						"<td width=\"600\" valign=\"top\"><center><button value=\"Info\" action=\"bypass _bbs_daily_quests;info;"
//						+ quest.getName()
//						+ "\" width=\"120\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
//				sb.append(
//						"<td width=\"600\" valign=\"top\"><center><button value=\"Claim Reward\" action=\"bypass _bbs_daily_quests;reward;"
//								+ quest.getName()
//								+ "\" width=\"120\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
//			} 
//			else 
//			{
//				sb.append(
//						"<td width=\"600\" valign=\"top\"><center><button value=\"Info\" action=\"bypass _bbs_daily_quests;info;"
//						+ quest.getName()
//						+ "\" width=\"120\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
//				sb.append("<td width=\"600\"><center><font name=\"hs10\" color=\"F4FA58\">"
//						+ "Reusing in: " +  quest.getReuseTimePattern(player) + "</font></td>");
//
//			}
//			sb.append("</tr>");
//			if (st == null
//					|| (st.getState() == COMPLETED 
//					&& quest.isRewardClaimed(player.getQuestState(quest.getName()))
//					&& st.getRestartTime() <= System.currentTimeMillis())) {
//				sb.append("<tr><td width=\"600\"><center><font name=\"hs10\" color=\"3ADF00\">" + " Available "
//						+ "</font></td></tr>");
//				sb.append("<br>");
//
//
//			} else if (st.isStarted()) {
//				sb.append("<tr><td width=\"600\"><center><font name=\"hs10\" color=\"FF6633\">" + " In Progress "
//						+ "</font></td></tr>");
//				sb.append("<br>");
//			} else {
////				sb.append("<tr><td width=\"600\"><center><font name=\"hs10\" color=\"F4FA58\">"
////						+ quest.getReuseTimePattern(player) + "</font></td>");
//				sb.append("<tr><td width=\"600\"><center><font name=\"hs10\" color=\"F4FA58\">"
//						+ " Not Available " + "</font></td></tr>");
//				sb.append("<br>");
//
//			}
////			sb.append("<center><img src=\"L2UI.SquareGray\" width=\"280\" height=\"2\"></center>");
//
//		}
//		html = html.replace("%data%", sb.toString());
//		ShowBoard.separateAndSend(html, player);
//	}

	private void sendMainHtml(Player player) {
		String html = getHtm(player, "main.htm");
		final StringBuilder sb = new StringBuilder();

		for (AbstractDailyQuest quest : QUESTS) 
		{
			QuestState st = player.getQuestState(quest.getName());

			if (!quest.getSettings().isEnabled()) {
				continue;
			}
			sb.append("<table width=\"720\" height=\"70\" bgcolor=\"1F1818\">");
			sb.append("<tr><td><center><font name=\"hs10\" color=\"LEVEL\">" + quest.getQuestName()
					+ "</font></center></td></tr>");
			sb.append("<tr><td><center>" + quest.getQuestDescr() + "</center></td>");
//			if (st == null
//					|| (st.getState() == COMPLETED 
//					&& quest.isRewardClaimed(player.getQuestState(quest.getName()))
//					&& st.getRestartTime() <= System.currentTimeMillis())) {
//				sb.append("<tr><td width=\"600\"><center><font name=\"hs10\" color=\"3ADF00\">" + " Available "
//						+ "</font></td>");
//
//			} else if (st.isStarted()) {
//				sb.append("<tr><td width=\"600\"><center><font name=\"hs10\" color=\"FF6633\">" + " In Progress "
//						+ "</font></td>");
//			} else {
////				sb.append("<tr><td width=\"600\"><center><font name=\"hs10\" color=\"F4FA58\">"
////						+ quest.getReuseTimePattern(player) + "</font></td>");
//				sb.append("<tr><td width=\"600\"><center><font name=\"hs10\" color=\"F4FA58\">"
//						+ " Not Available " + "</font></td>");
//			}

			if ((st == null) || ((st.getState() == COMPLETED) 
					&& (quest.isRewardClaimed(player.getQuestState(quest.getName()))) 
					&& (st.getRestartTime() <= System.currentTimeMillis())))
			{
				sb.append(
						"<td><center><button value=\"Info\" action=\"bypass _bbs_weekly_quests;info;"
						+ quest.getName()
								+ "\" width=\"120\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>");
				sb.append("<td><center><button value=\"Start\" action=\"bypass _bbs_weekly_quests;start;"
						+ quest.getName()
						+ "\" width=\"120\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>");
			}
			else if (!st.isCompleted() 
					&& st.isStarted() 
					&& quest.getQuestName() != "Online Time Challenge")
			{
				sb.append(
						"<td><center><button value=\"Info\" action=\"bypass _bbs_weekly_quests;info;"
						+ quest.getName()
								+ "\" width=\"120\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>");
				sb.append("<td><center><button value=\"Abort\" action=\"bypass _bbs_weekly_quests;abort;"
						+ quest.getName()
						+ "\" width=\"120\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>");
			}
			else if (st.getState() == COMPLETED 
					&& !quest.isRewardClaimed(player.getQuestState(quest.getName()))
					&& st.getRestartTime() > System.currentTimeMillis()) 
			{
				sb.append(
						"<td><center><button value=\"Info\" action=\"bypass _bbs_weekly_quests;info;"
						+ quest.getName()
								+ "\" width=\"120\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>");
				sb.append(
						"<td><center><button value=\"Claim Reward\" action=\"bypass _bbs_weekly_quests;reward;"
								+ quest.getName()
								+ "\" width=\"120\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>");
			} 
			else 
			{
				sb.append(
						"<td><center><button value=\"Info\" action=\"bypass _bbs_weekly_quests;info;"
						+ quest.getName()
								+ "\" width=\"120\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>");
				sb.append("<td><center><font name=\"hs10\" color=\"F4FA58\">"
						+ "Reusing in: " + quest.getReuseTimePattern(player) + "</font></center></td>");

			}
			sb.append("</tr>");
			if (st == null
					|| (st.getState() == COMPLETED 
					&& quest.isRewardClaimed(player.getQuestState(quest.getName()))
					&& st.getRestartTime() <= System.currentTimeMillis())) {
				sb.append("<tr><td><center><font name=\"hs10\" color=\"3ADF00\">" + " Available "
						+ "</font></center></td></tr>");


			} else if (st.isStarted()) {
				sb.append("<tr><td><center><font name=\"hs10\" color=\"FF6633\">" + " In Progress "
						+ "</font></center></td></tr>");
			} else {
//				sb.append("<tr><td width=\"600\"><center><font name=\"hs10\" color=\"F4FA58\">"
//						+ quest.getReuseTimePattern(player) + "</font></td>");
				sb.append("<tr><td><center><font name=\"hs10\" color=\"F4FA58\">"
						+ " Not Available " + "</font></center></td></tr>");

			}
			sb.append("</table>");
			sb.append("<br>");

		}
		html = html.replace("%data%", sb.toString());
		ShowBoard.separateAndSend(html, player);
	}
	
	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4,
			String arg5) {
	}

	private class AbortQuestDlg implements OnAnswerListener {
		private final Player _player;
		private final AbstractDailyQuest _quest;

		private AbortQuestDlg(Player player, AbstractDailyQuest quest) {
			_player = player;
			_quest = quest;
		}

		@Override
		public void sayYes() {
			if (_quest != null) {
				final QuestState qs = _player.getQuestState(_quest.getName());
				if ((qs != null) && (qs.getState() == STARTED)) {
					qs.setState(COMPLETED);
					qs.setRestartTimeWeekly();

					_quest.onQuestAbort(qs);
					_quest.showScreenMessage(_player, "Have been aborted!", 10000);
					_quest.resetReuse(_player.getNetConnection().getStrixClientData().getClientHWID().toString());
					qs.set("rewardClaimed", "yes");
					onBypassCommand(_player, "_bbshome");
					onBypassCommand(_player, "_bbs_weekly_quests");
				}
			}
		}

		@Override
		public void sayNo() {
		}
	}
}