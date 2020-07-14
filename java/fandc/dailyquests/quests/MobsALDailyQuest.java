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
public class MobsALDailyQuest extends AbstractDailyQuest
{
	public MobsALDailyQuest()
	{
		CharListenerList.addGlobal(new OnDeathList());
	}

	@Override
    public int getQuestIntId()
	{
		// Random quest id
		return 35018;
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
		sb.append("So our consortium has been trying to get to Antharas' Heart, but the way there is filled with monsters. Bad ones. Big ones.<br1>");
		sb.append("In order for us to conduct our survey, we must have the path from the Worst of the Worst in Antharas Lair to be cleared. So you are a cruicial part of our bigger plans.<br1>");
		sb.append("You will have to hunt down 20 Bloody Kariks, 30 Bloody Karinness and 50 Bloody Berserkers in Antharas Lair to achieve success.<br1>");
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
		sb.append("Bloody Karik " + HtmlUtils.getWeightGauge(450, st.getInt("KARIKS"), st.getInt("KARIKS_NEEDED"), false));
		sb.append("Bloody Karinness " + HtmlUtils.getWeightGauge(450, st.getInt("KARINNESS"), st.getInt("KARINNESS_NEEDED"), false));
		sb.append("Bloody Berserker " +  HtmlUtils.getWeightGauge(450, st.getInt("BERSERKER"), st.getInt("BERSERKER_NEEDED"), false));
		sb.append("<br>");

		sb.append("You must hunt down " + st.getInt("ALMOBS_NEEDED") + " mobs in Dragon Valley in order to complete the quest.<br1>");
		return sb.toString();
	}

	@Override
	public void onQuestStart(QuestState st)
	{
		st.set("KARIKS", "0");
		st.set("KARIKS_NEEDED", "20");
		st.set("KARINNESS", "0");
		st.set("KARINNESS_NEEDED", "30");
		st.set("BERSERKER", "0");
		st.set("BERSERKER_NEEDED", "50");
		st.set("rewardClaimed", "no");
	}

	private class OnDeathList implements OnDeathListener
	{
		private final AbstractDailyQuest _dq = MobsALDailyQuest.this;
		Zone _zone = ReflectionUtils.getZone("[antharas_bridge]");
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
			if (actor.getName().equalsIgnoreCase("Bloody Karik"))
			{
				st.set("KARIKS", st.getInt("KARIKS") + 1);
			}
			else if (actor.getName().equalsIgnoreCase("Bloody Karinness"))
			{
				st.set("KARINNESS", st.getInt("KARINNESS") + 1);

			}
			else if (actor.getName().equalsIgnoreCase("Bloody Berserker"))
			{
				st.set("BERSERKER", st.getInt("BERSERKER") + 1);

			}
			else
				return;
//			if  (actor.isInZone(_zone))
//			{
//				Log.warn("Validated GOE mob kill for player " + attacker.getName() + ".");
//				st.set("ALMOBS", st.getInt("ALMOBS") + 1);
//			}
//			else 
//				return;
			
			if (st.getInt("KARIKS") >= st.getInt("KARIKS_NEEDED") && st.getInt("KARINNESS") >= st.getInt("KARINNESS_NEEDED") && st.getInt("BERSERKER") >= st.getInt("BERSERKER_NEEDED") )
			{
				st.setState(COMPLETED);
				st.setRestartTime();
				onQuestFinish(st);
			}
			else
			{
				if (actor.getName().equalsIgnoreCase("Bloody Karik"))
				{
					showScreenMessage(attackerMember, "progress " + st.get("KARIKS") + "/" + st.get("KARIKS_NEEDED") + " Kariks defeated!", 5000);

				}
				else if (actor.getName().equalsIgnoreCase("Bloody Karinness"))
				{
					showScreenMessage(attackerMember, "progress " + st.get("KARINNESS") + "/" + st.get("KARINNESS_NEEDED") + " Karinness defeated!", 5000);

				}
				else if (actor.getName().equalsIgnoreCase("Bloody Berserker"))
				{
					showScreenMessage(attackerMember, "progress " + st.get("BERSERKER") + "/" + st.get("BERSERKER_NEEDED") + " Berserkers defeated!", 5000);

				}
			}
		}
	}
}
