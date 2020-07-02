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
public class OnlineTimeQuest3 extends AbstractDailyQuest
{
	public OnlineTimeQuest3()
	{
		CharListenerList.addGlobal(new EnterWorldList());
	}

	@Override
    public int getQuestIntId()
	{
		// Random quest id
		return 35007;
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
		sb.append("You must stay online in L2MUtiny for 6 hours.<br1>");
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
		sb.append(HtmlUtils.getWeightGauge(450, st.getInt("MINUTES"), 360, false));
		sb.append("<br>");
		sb.append("You must stay online for 6 hours to finish this quest.<br1>");
		sb.append("When relogging the progress is stopped..<br1>");
		return sb.toString();
	}

	@Override
	public void onQuestStart(QuestState st)
	{
		st.set("MINUTES", "0");
		st.set("MINUTES_NEEDED", "360");
		st.set("rewardClaimed", "no");
	}
	
	public void onQuestUpdate(QuestState st)
	{
		st.set("MINUTES", st.getInt("MINUTES") + 10);
	}

	private class EnterWorldList implements OnPlayerEnterListener
	{
		@Override
		public void onPlayerEnter(Player player)
		{
				final QuestState st = player.getQuestState(getName());
				if ((st == null) || (st.isCompleted() && (st.getRestartTime() <= System.currentTimeMillis())))
				{
					AbstractDailyQuest quest = OnlineTimeQuest3.this;
					final QuestState qs = quest.newQuestState(player, STARTED);
					quest.onQuestStart(qs);
					quest.showScreenMessage(player, "Have been successfuly started!", 10000);
					quest.registerReuse(player.getNetConnection().getStrixClientData().getClientHWID().toString());
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
			this._dq = OnlineTimeQuest3.this;
			this._qs = activeChar.getQuestState(_dq.getName());
			this._activeChar = activeChar;
		}		
		@Override
		public void run()
		{
			if (_qs.getState() != STARTED)
			{
				Log.warn("Quest is not started : OTQ3 for " + _activeChar.getName() + " .");
				return;
			}
			if (_activeChar.isOnline() && _qs.getState() == STARTED && (_qs.getInt("MINUTES") >= _qs.getInt("MINUTES_NEEDED")))
			{
					_qs.setState(COMPLETED);
					_qs.setRestartTime();
					onQuestFinish(_qs);
					Log.warn("Quest finished : OTQ3 Daily for " + _activeChar.getName() + " ." );
					return;
			}
			if (_activeChar.isOnline() && _qs.getState() == STARTED && (_qs.getInt("MINUTES") < _qs.getInt("MINUTES_NEEDED")))
			{
				Log.warn("Trying to update DB : OTQ3 Daily for " + _activeChar.getName() + " -> adding 10 minutes to DB." );
				showScreenMessage(_activeChar, "progress " + _qs.get("MINUTES") + "/" + _qs.get("MINUTES_NEEDED") + " minutes have passed!", 5000);
				onQuestUpdate(_qs);
				ThreadPoolManager.getInstance().schedule(new CheckTime(_activeChar, _qs), 600000);
				return;
			}
		}
	}
}