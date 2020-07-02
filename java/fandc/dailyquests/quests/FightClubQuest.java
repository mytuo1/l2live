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

import l2f.gameserver.listener.actor.player.OnPlayerExitListener;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.actor.listener.CharListenerList;
import l2f.gameserver.model.quest.QuestState;
import l2f.gameserver.utils.HtmlUtils;
import events.FightClub.FightClubManager;
import fandc.dailyquests.AbstractDailyQuest;

/**
 * @author Gnacik
 */
public class FightClubQuest extends AbstractDailyQuest
{
	public FightClubQuest()
	{
		CharListenerList.addGlobal(new OnEventExit());
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
		sb.append("You must catch randomly between " + getMinKillsRequired() + " and " + getMaxKillsRequired() + " fishes.<br1>");
		sb.append("Grade and type is not important.<br1>");
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
		sb.append(HtmlUtils.getWeightGauge(450, st.getInt("KILLS"), st.getInt("KILLS_NEEDED"), false));
		sb.append("<br>");

		sb.append("You must catch " + st.getInt("KILLS_NEEDED") + " fishes in order to complete the quest.<br1>");
		sb.append("Type and grade are not important.<br1>");
		return sb.toString();
	}

	@Override
	public void onQuestStart(QuestState st)
	{
		st.set("KILLS", "0");
		st.set("KILLS_NEEDED", getRandomKillsRequired());
		st.set("rewardClaimed", "no");
	}

	private class OnEventExit extends FightClubManager implements OnPlayerExitListener
	{
		@Override
		public void onPlayerExit(Player player)
		{
//			if (!isMonster && (fishId > 0))
//			{
				final QuestState st = player.getQuestState(getName());
				if ((st == null) || st.isCompleted())
				{
					return;
				}
				st.set("KILLS", st.getInt("KILLS") + 1);
				if (st.getInt("KILLS") >= st.getInt("KILLS_NEEDED"))
				{
					st.setState(COMPLETED);
					st.setRestartTime();
					onQuestFinish(st);
				}
				else
				{
					showScreenMessage(player, "progress " + st.get("KILLS") + "/" + st.get("KILLS_NEEDED") + " completed!", 5000);
				}
//			}
		}
	}
}
