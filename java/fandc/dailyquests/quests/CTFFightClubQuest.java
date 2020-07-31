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
import l2f.gameserver.model.entity.events.impl.AbstractFightClub;
import l2f.gameserver.model.quest.QuestState;
import l2f.gameserver.utils.HtmlUtils;

import org.strixplatform.logging.Log;

import fandc.dailyquests.AbstractDailyQuest;

/**
 * @author Gnacik
 */
public class CTFFightClubQuest extends AbstractDailyQuest
{
	public CTFFightClubQuest()
	{
		CharListenerList.addGlobal(new OnCTFEventExit());
	}

	@Override
    public int getQuestIntId()
	{
		// Random quest id
		return 35008;
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
		sb.append("You must participate in 15 CTF events in order to complete the quest.<br1>");
		sb.append("<br1>");
		return sb.toString();
	}

	@Override
	protected String writeQuestProgress(Player player)
	{
		AbstractDailyQuest dq = CTFFightClubQuest.this;
		final QuestState st = player.getQuestState(dq.getName());
		if (st == null)
		{
			return "You must take the quest to check your progress!";
		}

		final StringBuilder sb = new StringBuilder();
		sb.append("Progress:<br>");
		sb.append(HtmlUtils.getWeightGauge(450, st.getInt("CTF_PARTS"), 10, false));
		sb.append("<br>");

		sb.append("You must participate in 10 CTF events in order to complete the quest.<br1>");
		sb.append("<br1>");
		return sb.toString();
	}

	@Override
	public void onQuestStart(QuestState st)
	{
		st.set("CTF_PARTS", "0");
		st.set("CTF_PARTS_NEEDED", "9");
		st.set("rewardClaimed", "no");
		st.setRestartTimeWeekly();
	}
	
	public void onQuestUpdate(QuestState st)
	{
		st.set("CTF_PARTS", st.getInt("CTF_PARTS") + 1);
	}
	
	@Override
	public void onQuestFinish(QuestState st)
	{
		final Player player = st.getPlayer();
		showScreenMessage(player, "completed and rewards can be claimed!", 5000);
		player.getListeners().onWeeklyDQCompleted(player);
	}

	private class OnCTFEventExit implements OnFCEventStopListener
	{
		@Override
		public void onEventStop(Player player)
		{
			AbstractDailyQuest dq = CTFFightClubQuest.this;
			AbstractFightClub event = player.getFightClubEvent();
			if (event.getType() == null)
			{
				Log.warn("ctf type null or event not NOT_ACTIVE");
			}
			if (event.getType().toString().contentEquals("ctf"))
			{
				Log.warn("Found the Event, trying to execute CTF DB tasks for " + player.getName() + " .");
				final QuestState st = player.getQuestState(dq.getName());
				if ((st == null) || st.isCompleted())
				{
					return;
				}
				onQuestUpdate(st);
				if (st.getInt("CTF_PARTS") >= st.getInt("CTF_PARTS_NEEDED"))
				{
					st.setState(COMPLETED);
					onQuestFinish(st);
				}
				else
				{
					showScreenMessage(player, "progress " + st.get("CTF_PARTS") + "/" + "10" + " CTF Events completed!", 5000);
				}
			}
		}

	}
}
