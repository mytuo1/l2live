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
package fandc.dailyquests.quests;

import l2f.gameserver.listener.actor.player.OnFCEventStopListener;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.actor.listener.CharListenerList;
import l2f.gameserver.model.quest.QuestState;
import l2f.gameserver.utils.HtmlUtils;

import org.strixplatform.logging.Log;

import events.FightClub.FightClubManager;
import fandc.dailyquests.AbstractDailyQuest;

/**
 * @author Gnacik
 */
public class KoreanFightClubQuest extends AbstractDailyQuest
{
	public KoreanFightClubQuest()
	{
		CharListenerList.addGlobal(new OnKoreanEventExit());
	}

	@Override
    public int getQuestIntId()
	{
		// Random quest id
		return 35010;
	}

	@Override
	protected int writeHeight(Player player, int index)
	{
		switch (index)
		{
			case 1:
			{
				return 620;
			}
		}
		return 480;
	}

	@Override
	protected String writeQuestInfo(Player player)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("You must participate in 15 Korean Style events in order to complete the quest.<br1>");
		sb.append("<br1>");
		return sb.toString();
	}

	@Override
	protected String writeQuestProgress(Player player)
	{
		AbstractDailyQuest dq = KoreanFightClubQuest.this;
		final QuestState st = player.getQuestState(dq.getName());
		if (st == null)
		{
			return "You must take the quest to check your progress!";
		}

		final StringBuilder sb = new StringBuilder();
		sb.append("Progress:<br>");
		sb.append(HtmlUtils.getWeightGauge(450, st.getInt("KOREAN_PARTS"), 15, false));
		sb.append("<br>");

		sb.append("You must participate in 15 Korean Style events in order to complete the quest.<br1>");
		sb.append("<br1>");
		return sb.toString();
	}

	@Override
	public void onQuestStart(QuestState st)
	{
		st.set("KOREAN_PARTS", "0");
		st.set("KOREAN_PARTS_NEEDED", "14");
		st.set("rewardClaimed", "no");
	}
	
	public void onQuestUpdate(QuestState st)
	{
		st.set("KOREAN_PARTS", st.getInt("KOREAN_PARTS") + 1);
	}


	private class OnKoreanEventExit extends FightClubManager implements OnFCEventStopListener
	{
		@Override
		public void onEventStop(Player player)
		{
			AbstractDailyQuest dq = KoreanFightClubQuest.this;
			if (player.getFightClubEvent() == null)
			{
				Log.warn("No FC Event found, Korean");
				return;
			}
			else if (player.getFightClubEvent().getEventId() == 6)
			{
				Log.warn("Found the Event, trying to execute Korean DB tasks");
				final QuestState st = player.getQuestState(dq.getName());
				if ((st == null) || st.isCompleted())
				{
					return;
				}
				onQuestUpdate(st);
				if (st.getInt("KOREAN_PARTS") >= st.getInt("KOREAN_PARTS_NEEDED"))
				{
					st.setState(COMPLETED);
					st.setRestartTimeWeekly();
					onQuestFinish(st);
				}
				else
				{
					showScreenMessage(player, "progress " + st.get("KOREAN_PARTS") + "/" + st.get("KOREAN_PARTS_NEEDED") + " TVT Events completed!", 5000);
				}
			}
		}
	}
}
