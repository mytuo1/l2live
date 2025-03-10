package l2f.gameserver.model.entity.events.impl.fightclub;

import l2f.commons.collections.MultiValueSet;
import l2f.gameserver.model.Creature;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.entity.events.fightclubmanager.FightClubPlayer;
import l2f.gameserver.model.entity.events.impl.AbstractFightClub;

public class TeamVSTeamEvent extends AbstractFightClub
{
	public TeamVSTeamEvent(MultiValueSet<String> set)
	{
		super(set);
	}
	
	@Override
	public void onKilled(Creature actor, Creature victim)
	{
		if(actor.getPlayer() != null && actor.getPlayer().getFightClubEvent().isFriend(actor, victim)) //shouldn't give points for killing a friend on the event.
		{
			super.onKilled(actor, victim);
			return;
		}
		if (actor != null && actor.isPlayable())
		{
			FightClubPlayer realActor = getFightClubPlayer(actor.getPlayer());
			if (victim.isPlayer() && realActor != null)
			{
				realActor.increaseKills(true);
				realActor.getTeam().incScore(1);
				updatePlayerScore(realActor);
				updateScreenScores();
				sendMessageToPlayer(realActor, MESSAGE_TYPES.GM, "You have killed "+victim.getName());
			}
			else if (victim.isPet())
			{
				
			}
			actor.getPlayer().sendUserInfo();
		}
		
		if (victim.isPlayer())
		{
			FightClubPlayer realVictim = getFightClubPlayer(victim);
			if (realVictim != null)
			{
				realVictim.increaseDeaths();
				if (actor != null)
					sendMessageToPlayer(realVictim, MESSAGE_TYPES.GM, "You have been killed by "+actor.getName());
			}
			victim.broadcastCharInfo();
		}
		
		super.onKilled(actor, victim);
	}
	
	@Override
	public String getVisibleTitle(Player player, String currentTitle, boolean toMe)
	{
		FightClubPlayer fPlayer = getFightClubPlayer(player);
		
		if (fPlayer == null)
			return currentTitle;
		
		return "Kills: " + fPlayer.getKills(true) + " Deaths: " + fPlayer.getDeaths();
	}
}
