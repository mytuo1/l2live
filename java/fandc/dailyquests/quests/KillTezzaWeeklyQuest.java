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
import l2f.gameserver.model.actor.listener.CharListenerList;
import l2f.gameserver.model.quest.QuestState;
import l2f.gameserver.utils.HtmlUtils;

import fandc.dailyquests.AbstractDailyQuest;

/**
 * @author UnAfraid
 */
public class KillTezzaWeeklyQuest extends AbstractDailyQuest
{
	public KillTezzaWeeklyQuest()
	{
		CharListenerList.addGlobal(new OnDeathList());
	}

	@Override
    public int getQuestIntId()
	{
		// Random quest id
		return 35030;
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
		sb.append("You seem so powerful, perfect specimen!<br1>");
		sb.append("Those bosses... especially Frintezza are setting up an attack on our peaceful world.<br1>");
		sb.append("In order for balance to be restored, we will need you to kill his Monster, Scarlet Van Halisha.<br1>");
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
		sb.append("<table width=725 height=20 background=\"L2UI_CT1.Windows.Windows_DF_TooltipBG\">");
		sb.append("<tr>");
		sb.append("<td fixwidth=5></td>");
		sb.append("<td fixwidth=60> Frintezza: </td>");
		sb.append("<td fixwidth=120>" + HtmlUtils.getWeightGauge(450, st.getInt("TEZZA_KILLS"), 1, false) + "</td>");
		sb.append("<td fixwidth=5></td>");
		sb.append("</tr>");
		sb.append("</table>");
		return sb.toString();
	}

	@Override
	public void onQuestStart(QuestState st)
	{
		st.set("TEZZA_KILLS", "0");
		st.set("TEZZAS_NEEDED", "0");
		st.set("rewardClaimed", "no");
		st.setRestartTimeWeekly();
	}
	
	@Override
	public void onQuestFinish(QuestState st)
	{
		final Player player = st.getPlayer();
		showScreenMessage(player, "completed and rewards can be claimed!", 5000);
		player.getListeners().onWeeklyHuntDQCompleted(player);
	}

	private class OnDeathList implements OnDeathListener
	{
		private final AbstractDailyQuest _dq = KillTezzaWeeklyQuest.this;
		@Override
		public void onDeath(Creature actor, Creature killer)
		{
			if (!actor.isNpc())
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
//			final Player attackerMember = getRandomPartyMember(attacker, attacker.getQuestState(_dq.getName()));
			final QuestState st = attacker != null ? attacker.getQuestState(_dq.getName()) : null;
			if ((attacker == null) || (st == null) || st.isCompleted())
			{
				return;
			}
			if ((actor.getNpcId() == 29047) && st.getInt("TEZZA_KILLS") >= st.getInt("TEZZAS_NEEDED"))
			{
				st.setState(COMPLETED);
				onQuestFinish(st);
			}
			if ((actor.getNpcId() == 29047) && st.getInt("TEZZA_KILLS") <= st.getInt("TEZZAS_NEEDED"))
			{
				st.set("TEZZA_KILLS", st.getInt("TEZZA_KILLS") + 1);
				showScreenMessage(attacker, "progress " + st.get("TEZZA_KILLS") + "/" + " 1"  + " Frintezza defeated!", 5000);
			}
			else
				return;

		}
	}
}
