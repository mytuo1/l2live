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

import fandc.dailyquests.quests.CalendarQuest;
import l2f.gameserver.handler.bbs.ICommunityBoardHandler;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.quest.QuestState;
import l2f.gameserver.network.serverpackets.ShowBoard;
import l2f.gameserver.scripts.Functions;


/**
 * @author UnAfraid
 */
public class CalendarHandler extends AbstractDPScript implements ICommunityBoardHandler {
	private static final AbstractDailyQuest[] QUESTS = new AbstractDailyQuest[] 
		{ 
		  new CalendarQuest(), 
		};

	@Override
	public String[] getBypassCommands() {
		return new String[] {
				"_bbs_calendar" };
	}

	public CalendarHandler() {

		load();
	}

	@Override
	public void onBypassCommand(Player player, String bypass) {

		final StringTokenizer st = new StringTokenizer(bypass, ";");
		final String cmd = st.nextToken();
		switch (cmd) {
		case "_bbs_calendar": {
			if (!st.hasMoreTokens()) {
				sendMainHtml(player);
				break;
			}

			final String subCmd = st.nextToken();
			switch (subCmd) {
			case "reward1": {
				final String questName = st.nextToken();
				for (AbstractDailyQuest quest : QUESTS) {
					if (quest.getName().equals(questName)) {
						final QuestState qs = player.getQuestState(quest.getName());
							qs.set("REWARDS_CLAIMED", qs.getInt("REWARDS_CLAIMED") + 1);
							Functions.addItem(player, 37000, 1, "");
							onBypassCommand(player, "_bbshome");
							onBypassCommand(player, "_bbs_calendar");
							break;
					}
				}
				break;
			}
			case "reward2": {
				final String questName = st.nextToken();
				for (AbstractDailyQuest quest : QUESTS) {
					if (quest.getName().equals(questName)) {
						final QuestState qs = player.getQuestState(quest.getName());
							qs.set("REWARDS_CLAIMED", qs.getInt("REWARDS_CLAIMED") + 1);
							Functions.addItem(player, 37000, 1, "");
							onBypassCommand(player, "_bbshome");
							onBypassCommand(player, "_bbs_calendar");
							break;
					}
				}
				break;
			}
			case "reward3": {
				final String questName = st.nextToken();
				for (AbstractDailyQuest quest : QUESTS) {
					if (quest.getName().equals(questName)) {
						final QuestState qs = player.getQuestState(quest.getName());
							qs.set("REWARDS_CLAIMED", qs.getInt("REWARDS_CLAIMED") + 1);
							Functions.addItem(player, 37000, 1, "");
							onBypassCommand(player, "_bbshome");
							onBypassCommand(player, "_bbs_calendar");
							break;
					}
				}
				break;
			}
			case "reward4": {
				final String questName = st.nextToken();
				for (AbstractDailyQuest quest : QUESTS) {
					if (quest.getName().equals(questName)) {
						final QuestState qs = player.getQuestState(quest.getName());
							qs.set("REWARDS_CLAIMED", qs.getInt("REWARDS_CLAIMED") + 1);
							Functions.addItem(player, 37000, 1, "");
							onBypassCommand(player, "_bbshome");
							onBypassCommand(player, "_bbs_calendar");
							break;
					}
				}
				break;
			}
			case "reward5": {
				final String questName = st.nextToken();
				for (AbstractDailyQuest quest : QUESTS) {
					if (quest.getName().equals(questName)) {
						final QuestState qs = player.getQuestState(quest.getName());
							qs.set("REWARDS_CLAIMED", qs.getInt("REWARDS_CLAIMED") + 1);
							player.getInventory().addItem(37000, 1, "");
							onBypassCommand(player, "_bbshome");
							onBypassCommand(player, "_bbs_calendar");
							break;
					}
				}
				break;
			}
			}
			}
		}
	}

	private void sendMainHtml(Player player) {
		String html = getHtm(player, "main.htm");
		final StringBuilder sb = new StringBuilder();

		for (AbstractDailyQuest quest : QUESTS) 
		{
			QuestState st = player.getQuestState(quest.getName());

//			sb.append("<table width=\"720\" height=\"70\" bgcolor=\"1F1818\">");
//			sb.append("<tr><td><center><font name=\"hs10\" color=\"LEVEL\">" + quest.getQuestName()
//					+ "</font></center></td></tr>");
//			sb.append("<tr><td><center>" + quest.getQuestDescr() + "</center></td>");
//
//			if ((st == null) || ((st.getState() == COMPLETED) 
//					&& (quest.isRewardClaimed(player.getQuestState(quest.getName()))) 
//					&& (st.getRestartTime() <= System.currentTimeMillis())))
//			{
//				sb.append(
//						"<td><center><button value=\"Info\" action=\"bypass _bbs_time_quests;info;"
//						+ quest.getName()
//								+ "\" width=\"120\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>");
//				sb.append("<td><center><button value=\"Start\" action=\"bypass _bbs_time_quests;start;"
//						+ quest.getName()
//						+ "\" width=\"120\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>");
//			}
			// Day 1
			if (st.getInt("REWARDS_AVAILABLE") == 1 && st.getInt("REWARDS_CLAIMED") == 0 )
			{
				sb.append("<table width=\"100\" height=\"100\" bgcolor=\"1F1818\">");
				sb.append(
						"<tr><td><center><button value=\"Claim Reward\" action=\"bypass _bbs_calendar;reward1;"
								+ quest.getName()
								+ "\" width=\"80\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td></tr>");
				sb.append("</table>");
				sb.append("<br>");
				//TODO:	green box can take reward / reward 1 / put claim button
			}
			else if (st.getInt("REWARDS_CLAIMED") == 1)
			{
				sb.append("<table width=\"100\" height=\"100\" bgcolor=\"1F1818\">");
				sb.append(
						"<tr><td><center><button value=\"Info\" action=\"bypass _bbs_time_quests;info;"
						+ quest.getName()
								+ "\" width=\"80\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td></tr>");
				sb.append("</table>");
				sb.append("<br>");
				//TODO: //grey box / reward already taken / day 1
			}
			else if (st.getInt("REWARDS_CLAIMED") == 0)
			{
				sb.append("<table width=\"100\" height=\"100\" bgcolor=\"1F1818\">");
				sb.append(
						"<tr><td><center><button value=\"Info\" action=\"bypass _bbs_time_quests;info;"
						+ quest.getName()
								+ "\" width=\"120\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td></tr>");
				sb.append("</table>");
				sb.append("<br>");
				//TODO: red box not available
			}
			// Day 2
			if (st.getInt("REWARDS_AVAILABLE") >= 2 && st.getInt("REWARDS_CLAIMED") < 2)
			{
				sb.append("<table width=\"100\" height=\"100\" bgcolor=\"1F1818\">");
				sb.append(
						"<tr><td><center><button value=\"Claim Reward\" action=\"bypass _bbs_calendar;reward2;"
								+ quest.getName()
								+ "\" width=\"80\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td></tr>");
				sb.append("</table>");
				sb.append("<br>");
				//TODO:	green box can take reward / reward 2 / put claim button
			}
			else if (st.getInt("REWARDS_CLAIMED") == 2)
			{
				sb.append("<table width=\"100\" height=\"100\" bgcolor=\"1F1818\">");
				sb.append(
						"<tr><td><center><button value=\"Info\" action=\"bypass _bbs_time_quests;info;"
						+ quest.getName()
								+ "\" width=\"80\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td></tr>");
				sb.append("</table>");
				sb.append("<br>");
				//TODO: //grey box / reward already taken / day 2
			}
			else if (st.getInt("REWARDS_AVAILABLE") < 2 && st.getInt("REWARDS_CLAIMED") <= 1)
			{
				sb.append("<table width=\"100\" height=\"100\" bgcolor=\"1F1818\">");
				sb.append(
						"<tr><td><center><button value=\"Info\" action=\"bypass _bbs_time_quests;info;"
						+ quest.getName()
								+ "\" width=\"120\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td></tr>");
				sb.append("</table>");
				sb.append("<br>");
				//TODO: //red box / reward not available
			}
			// Day 3
			if (st.getInt("REWARDS_AVAILABLE") >= 3 && st.getInt("REWARDS_CLAIMED") < 3)
			{
				sb.append("<table width=\"100\" height=\"100\" bgcolor=\"1F1818\">");
				sb.append(
						"<tr><td><center><button value=\"Claim Reward\" action=\"bypass _bbs_calendar;reward3;"
								+ quest.getName()
								+ "\" width=\"80\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td></tr>");
				sb.append("</table>");
				sb.append("<br>");
				//TODO:	green box can take reward / reward 3 / put claim button
			}
			else if (st.getInt("REWARDS_CLAIMED") == 3)
			{
				sb.append("<table width=\"100\" height=\"100\" bgcolor=\"1F1818\">");
				sb.append(
						"<tr><td><center><button value=\"Info\" action=\"bypass _bbs_time_quests;info;"
						+ quest.getName()
								+ "\" width=\"80\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td></tr>");
				sb.append("</table>");
				sb.append("<br>");
				//TODO: //grey box / reward already taken / day 3
			}
			else if (st.getInt("REWARDS_AVAILABLE") < 3 && st.getInt("REWARDS_CLAIMED") <= 2)
			{
				sb.append("<table width=\"100\" height=\"100\" bgcolor=\"1F1818\">");
				sb.append(
						"<tr><td><center><button value=\"Info\" action=\"bypass _bbs_time_quests;info;"
						+ quest.getName()
								+ "\" width=\"120\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td></tr>");
				sb.append("</table>");
				sb.append("<br>");
				//TODO: //red box / 
			}
			// Day 4
			if (st.getInt("REWARDS_AVAILABLE") >= 4 && st.getInt("REWARDS_CLAIMED") < 4)
			{
				sb.append("<table width=\"100\" height=\"100\" bgcolor=\"1F1818\">");
				sb.append(
						"<tr><td><center><button value=\"Claim Reward\" action=\"bypass _bbs_calendar;reward4;"
								+ quest.getName()
								+ "\" width=\"80\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td></tr>");
				sb.append("</table>");
				sb.append("<br>");
				//TODO:	green box can take reward / reward 2 / put claim button
			}
			else if (st.getInt("REWARDS_CLAIMED") == 4)
			{
				sb.append("<table width=\"100\" height=\"100\" bgcolor=\"1F1818\">");
				sb.append(
						"<tr><td><center><button value=\"Info\" action=\"bypass _bbs_time_quests;info;"
						+ quest.getName()
								+ "\" width=\"80\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td></tr>");
				sb.append("</table>");
				sb.append("<br>");
				//TODO: //grey box / reward already taken / day 2
			}
			else if (st.getInt("REWARDS_AVAILABLE") < 4 && st.getInt("REWARDS_CLAIMED") <= 3)
			{
				sb.append("<table width=\"100\" height=\"100\" bgcolor=\"1F1818\">");
				sb.append(
						"<tr><td><center><button value=\"Info\" action=\"bypass _bbs_time_quests;info;"
						+ quest.getName()
								+ "\" width=\"120\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td></tr>");
				sb.append("</table>");
				sb.append("<br>");
				//TODO: //red box / reward not available
			}
			// Day 5
			if (st.getInt("REWARDS_AVAILABLE") >= 5 && st.getInt("REWARDS_CLAIMED") < 5)
			{
				sb.append("<table width=\"100\" height=\"100\" bgcolor=\"1F1818\">");
				sb.append(
						"<tr><td><center><button value=\"Claim Reward\" action=\"bypass _bbs_calendar;reward5;"
								+ quest.getName()
								+ "\" width=\"80\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td></tr>");
				sb.append("</table>");
				sb.append("<br>");
				//TODO:	green box can take reward / reward 3 / put claim button
			}
			else if (st.getInt("REWARDS_CLAIMED") == 5)
			{
				sb.append("<table width=\"100\" height=\"100\" bgcolor=\"1F1818\">");
				sb.append(
						"<tr><td><center><button value=\"Info\" action=\"bypass _bbs_time_quests;info;"
						+ quest.getName()
								+ "\" width=\"80\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td></tr>");
				sb.append("</table>");
				sb.append("<br>");
				//TODO: //grey box / reward already taken / day 3
			}
			else if (st.getInt("REWARDS_AVAILABLE") < 5 && st.getInt("REWARDS_CLAIMED") <= 4)
			{
				sb.append("<table width=\"100\" height=\"100\" bgcolor=\"1F1818\">");
				sb.append(
						"<tr><td><center><button value=\"Info\" action=\"bypass _bbs_time_quests;info;"
						+ quest.getName()
								+ "\" width=\"120\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td></tr>");
				sb.append("</table>");
				sb.append("<br>");
				//TODO: //red box / 
			}
//	
//			else if (!st.isCompleted() 
//					&& st.isStarted() 
//					&& quest.getQuestName() != "Online Time Challenge")
//			{
//				sb.append(
//						"<td><center><button value=\"Info\" action=\"bypass _bbs_time_quests;info;"
//						+ quest.getName()
//								+ "\" width=\"120\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>");
//				sb.append("<td><center><button value=\"Abort\" action=\"bypass _bbs_time_quests;abort;"
//						+ quest.getName()
//						+ "\" width=\"120\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>");
//			}
//			else if (st.getState() == COMPLETED 
//					&& !quest.isRewardClaimed(player.getQuestState(quest.getName()))
//					&& st.getRestartTime() > System.currentTimeMillis()) 
//			{
//				sb.append(
//						"<td><center><button value=\"Info\" action=\"bypass _bbs_time_quests;info;"
//						+ quest.getName()
//								+ "\" width=\"120\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>");
//				sb.append(
//						"<td><center><button value=\"Claim Reward\" action=\"bypass _bbs_calendar;reward1;"
//								+ quest.getName()
//								+ "\" width=\"120\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>");
//			} 
//			else 
//			{
//				sb.append(
//						"<td><center><button value=\"Info\" action=\"bypass _bbs_time_quests;info;"
//						+ quest.getName()
//								+ "\" width=\"120\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>");
//				sb.append("<td><center><font name=\"hs10\" color=\"F4FA58\">"
//						+ "Reusing in: " + quest.getReuseTimePattern(player) + "</font></center></td>");
//
//			}
//			sb.append("</tr>");
//			if (st == null
//					|| (st.getState() == COMPLETED 
//					&& quest.isRewardClaimed(player.getQuestState(quest.getName()))
//					&& st.getRestartTime() <= System.currentTimeMillis())) {
//				sb.append("<tr><td><center><font name=\"hs10\" color=\"3ADF00\">" + " Available "
//						+ "</font></center></td></tr>");
//
//
//			} else if (st.isStarted()) {
//				sb.append("<tr><td><center><font name=\"hs10\" color=\"FF6633\">" + " In Progress "
//						+ "</font></center></td></tr>");
//			} else {
//				sb.append("<tr><td><center><font name=\"hs10\" color=\"F4FA58\">"
//						+ " Not Available " + "</font></center></td></tr>");
//
//			}
//			sb.append("</table>");
//			sb.append("<br>");
//
		}
		html = html.replace("%data%", sb.toString());
		ShowBoard.separateAndSend(html, player);
	}
	
	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4,
			String arg5) {
	}
}