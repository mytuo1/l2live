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

import l2f.gameserver.listener.actor.OnDeathListener;
import l2f.gameserver.model.Creature;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.Zone;
import l2f.gameserver.model.actor.listener.CharListenerList;
import l2f.gameserver.model.quest.QuestState;
import l2f.gameserver.utils.HtmlUtils;
import l2f.gameserver.utils.ReflectionUtils;

import org.strixplatform.logging.Log;

import fandc.dailyquests.AbstractDailyQuest;

/**
 * @author UnAfraid
 */
public class MobsDVDailyQuest extends AbstractDailyQuest
{
	public MobsDVDailyQuest()
	{
		CharListenerList.addGlobal(new OnDeathList());
	}

	@Override
    public int getQuestIntId()
	{
		// Random quest id
		return 35016;
	}

	/**
	 * @param player
	 * @param index
	 * @return
	 */
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
		sb.append("Greetings Champion!<br1>");
		sb.append("You seem very powerful, a perfect candidate!<br1>");
		sb.append("It's been bad these days... The monsters in Dragon Valley are getting up to no good. So.. Listen, we need someone to do us a favor.<br1>");
		sb.append("We need Dragon Valley cleansed. Take the fear back, you know? So you are a cruicial part of our bigger plans.<br1>");
		sb.append("You will have to hunt down  " + getMinKillsRequired() + " - " + getMaxKillsRequired() + " mobs in Dragon Valley to achieve success.<br1>");
		sb.append("Should you succeed we will reward you handsomely.<br1>");
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
		sb.append(HtmlUtils.getWeightGauge(450, st.getInt("DVMOBS"), st.getInt("DVMOBS_NEEDED"), false));
		sb.append("<br>");

		sb.append("You must hunt down " + st.getInt("DVMOBS_NEEDED") + " mobs in Dragon Valley in order to complete the quest.<br1>");
		return sb.toString();
	}

	@Override
	public void onQuestStart(QuestState st)
	{
		st.set("DVMOBS", "0");
		st.set("DVMOBS_NEEDED", getRandomKillsRequired());
		st.set("rewardClaimed", "no");
		st.setRestartTime();
	}
	
	@Override
	public void onQuestFinish(QuestState st)
	{
		final Player player = st.getPlayer();
		showScreenMessage(player, "completed and rewards can be claimed!", 5000);
		player.getListeners().onHuntDQCompleted(player);
	}

	private class OnDeathList implements OnDeathListener
	{
		private final AbstractDailyQuest _dq = MobsDVDailyQuest.this;
		Zone _zone = ReflectionUtils.getZone("[dragon_valley]");
		@Override
		public void onDeath(Creature actor, Creature killer)
		{
			if (!actor.isMonster())
			{
				return;
			}
			if (actor == null || killer == null)
			{
				return;
			}
			if (!killer.isPlayer() || (killer.isPlayer() && killer.getPlayer().getQuestState(_dq.getName()) == null))
			{
				return;
			}
			final Player attacker = killer != null ? killer.getPlayer() : null;
			final Player attackerMember = getRandomPartyMember(attacker, attacker.getQuestState(_dq.getName()));
			final QuestState st = attackerMember != null ? attackerMember.getQuestState(getName()) : null;
			if ((attackerMember == null) || (st == null) || st.isCompleted())
			{
				return;
			}
			if  (actor.isInZone(_zone))
			{
				Log.warn("Validated DV mob kill for player " + attacker.getName() + ".");
				st.set("DVMOBS", st.getInt("DVMOBS") + 1);
			}
			else 
				return;
			
			if (st.getInt("DVMOBS") >= st.getInt("DVMOBS_NEEDED"))
			{
				st.setState(COMPLETED);
				onQuestFinish(st);
			}
		}
	}
}
