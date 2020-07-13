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
import l2f.gameserver.model.Player;
import l2f.gameserver.model.actor.listener.CharListenerList;
import l2f.gameserver.model.quest.QuestState;
import l2f.gameserver.utils.HtmlUtils;

import l2f.gameserver.ThreadPoolManager;

import org.strixplatform.logging.Log;

import fandc.dailyquests.AbstractDailyQuest;

/**
 * @author Mutiny
 */
public class CalendarQuest extends AbstractDailyQuest
{
	public CalendarQuest()
	{
		CharListenerList.addGlobal(new EnterWorldList());
	}

	@Override
    public int getQuestIntId()
	{
		// Random quest id
		return 35012;
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
		sb.append("You must stay online in L2MUtiny for 1 hour.<br1>");
		sb.append("When relogging quest progress is stopped.<br1>");
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
		sb.append(HtmlUtils.getWeightGauge(450, st.getInt("MINUTES"), 60, false));
		sb.append("<br>");
		sb.append("You must stay online for 1 hour to finish this quest.<br1>");
		sb.append("When relogging the progress is stopped..<br1>");
		return sb.toString();
	}

	@Override
	public void onQuestStart(QuestState st)
	{
		st.set("REWARDS_CLAIMED", "0");
		st.set("REWARDS_AVAILABLE", "0");
		st.set("lastCheck" , "0");
	}
	
	public void onQuestUpdate(QuestState st)
	{
		st.set("REWARDS_AVAILABLE", st.getInt("REWARDS_AVAILABLE") + 1);
	}
	public void addTime(QuestState st)
	{
		long checkTime = (System.currentTimeMillis() + (24*60*60*1000));		
		st.set("lastCheck", String.valueOf(checkTime));
	}

	private class EnterWorldList implements OnPlayerEnterListener
	{
		@Override
		public void onPlayerEnter(Player player)
		{
				final QuestState st = player.getQuestState(getName());
				if ((st == null) || (st.isCompleted() && (st.getRestartTime() <= System.currentTimeMillis())))
				{
					AbstractDailyQuest quest = CalendarQuest.this;
					final QuestState qs = quest.newQuestState(player, STARTED);
					quest.onQuestStart(qs);
					ThreadPoolManager.getInstance().schedule(new CheckTime(player, st), 600000);
				}
				else if (st.getState() == STARTED)
				{
					ThreadPoolManager.getInstance().schedule(new CheckTime(player, st), 600000);
				}
		}
	}
	private class CheckTime implements Runnable
	{
		private final Player _activeChar;
		private final QuestState _qs;
		private final AbstractDailyQuest _dq;
		public CheckTime(Player activeChar, QuestState st)
		{
			this._dq = CalendarQuest.this;
			this._qs = activeChar.getQuestState(_dq.getName());
			this._activeChar = activeChar;
		}		
		@Override
		public void run()
		{

			if (_activeChar.isOnline() && _qs.getInt("REWARDS_CLAIMED") == 0)
			{
				onQuestUpdate(_qs);
				addTime(_qs);
				Log.warn("Adding 1 reward to claim for player " + _activeChar.getName() + " - Calendar.");
				ThreadPoolManager.getInstance().schedule(new CheckTime(_activeChar, _qs), 3600000); // check on every hour if online to not miss rewards
				// always give +1 rewards_available, and add check timer with current time + 24 hours if 0
			}
			else if (_activeChar.isOnline() && _qs.getInt("REWARDS_AVAILABLE") > 0)
			{
				if ( Integer.valueOf(_qs.get("lastCheck")) <= Integer.valueOf(String.valueOf(System.currentTimeMillis())))
				{
					onQuestUpdate(_qs);
					addTime(_qs);
					Log.warn("Adding 1 reward to claim for player " + _activeChar.getName() + " - Calendar as the previous reward is more then 24h ago.");
					// if last check (previous reward +24 hours) has passed, add 1 more reward available
				}
				ThreadPoolManager.getInstance().schedule(new CheckTime(_activeChar, _qs), 3600000);

			}

		}
	}
}