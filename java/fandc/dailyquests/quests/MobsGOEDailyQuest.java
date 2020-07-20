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
public class MobsGOEDailyQuest extends AbstractDailyQuest
{
	public MobsGOEDailyQuest()
	{
		CharListenerList.addGlobal(new OnDeathList());
	}

	@Override
    public int getQuestIntId()
	{
		// Random quest id
		return 35015;
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
		sb.append("You seem like a perfect candidate for a job we have!<br1>");
		sb.append("We will need you to hunt down " + getMinKillsRequired() + " - " + getMaxKillsRequired() + " mobs in the Garden of Eva.<br1>");
		sb.append("This is by far one of the toughest threats for us. Should you succeed with this one, we will reward you very handsomely!<br1>");
		

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
		sb.append(HtmlUtils.getWeightGauge(450, st.getInt("GOEMOBS"), st.getInt("GOEMOBS_NEEDED"), false));
		sb.append("<br>");

		sb.append("You must hunt down " + st.getInt("GOEMOBS_NEEDED") + " mobs in Garden of Eva in order to complete the quest.<br1>");
		sb.append("Each time you accept the quest the amount of mobs will be randomly selected.<br1>");
		return sb.toString();
	}

	@Override
	public void onQuestStart(QuestState st)
	{
		st.set("GOEMOBS", "0");
		st.set("GOEMOBS_NEEDED", getRandomKillsRequired());
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
		private final AbstractDailyQuest _dq = MobsGOEDailyQuest.this;
		Zone _zone = ReflectionUtils.getZone("[goe_pvp_epic]");
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
				Log.warn("Validated GOE mob kill for player " + attacker.getName() + ".");
				st.set("GOEMOBS", st.getInt("GOEMOBS") + 1);
			}
			else 
				return;
			
			if (st.getInt("GOEMOBS") >= st.getInt("GOEMOBS_NEEDED"))
			{
				st.setState(COMPLETED);
				onQuestFinish(st);
			}
		}
	}
}
