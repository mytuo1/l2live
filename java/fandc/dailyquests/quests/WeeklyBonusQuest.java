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

import l2f.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2f.gameserver.listener.actor.player.OnWeeklyDQCompletedListener;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.actor.listener.CharListenerList;
import l2f.gameserver.model.quest.QuestState;
import l2f.gameserver.utils.HtmlUtils;

import fandc.dailyquests.AbstractDailyQuest;

/**
 * @author Mutiny
 */
public class WeeklyBonusQuest extends AbstractDailyQuest
{
	public WeeklyBonusQuest()
	{
		CharListenerList.addGlobal(new EnterWorldList());
		CharListenerList.addGlobal(new WeeklyComplete());
	}

	@Override
    public int getQuestIntId()
	{
		// Random quest id
		return 35025;
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
		sb.append("You must complete all 4 tasks in this section to get a bonus reward.<br1>");
		return sb.toString();
	}

	@Override
	protected String writeQuestProgress(Player player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return "You must take the quest to check your progress!";
		}

		final StringBuilder sb = new StringBuilder();
		sb.append("Progress:<br>");
		sb.append(HtmlUtils.getWeightGauge(450, st.getInt("QUESTS"), 4, false));
		sb.append("<br>");
		sb.append("You must complete all 4 tasks in this section to get a bonus reward.<br1>");
		return sb.toString();
	}

	@Override
	public void onQuestStart(QuestState st)
	{
		st.set("QUESTS", "0");
		st.set("QUESTS_NEEDED", "3");
		st.set("rewardClaimed", "no");
		st.setRestartTimeWeekly();
	}
	public void onQuestUpdate(QuestState st)
	{
		st.set("QUESTS", st.getInt("QUESTS") + 1);
	}
	
	private class EnterWorldList implements OnPlayerEnterListener
	{
		@Override
		public void onPlayerEnter(Player player)
		{
				
				final QuestState st = player.getQuestState(getName());
				if ((st == null) || (st.isCompleted() && (st.getRestartTime() <= System.currentTimeMillis())) || (st.isStarted() && (st.getRestartTime() <= System.currentTimeMillis())))
				{
					AbstractDailyQuest quest = WeeklyBonusQuest.this;
					final QuestState qs = quest.newQuestState(player, STARTED);
					quest.onQuestStart(qs);
				}
				
		}
	}

	private class WeeklyComplete implements OnWeeklyDQCompletedListener
	{
		@Override
		public void onWeeklyDQCompleted(Player player)
		{
			AbstractDailyQuest bonusdq = WeeklyBonusQuest.this;
			QuestState bonusst = player.getQuestState(bonusdq.getName());

			onQuestUpdate(bonusst);
			if (bonusst.getInt("QUESTS") > bonusst.getInt("QUESTS_NEEDED"))
			{
				bonusst.setState(COMPLETED);
				onQuestFinish(bonusst);
				return;
			}

		}
	}

}