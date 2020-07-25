package l2f.gameserver.model.actor.listener;

import l2f.commons.listener.Listener;
import l2f.gameserver.listener.actor.player.OnFCEventStopListener;
import l2f.gameserver.listener.actor.player.OnFishDieListener;
import l2f.gameserver.listener.actor.player.OnGeneralDQCompletedListener;
import l2f.gameserver.listener.actor.player.OnHuntDQCompletedListener;
import l2f.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2f.gameserver.listener.actor.player.OnPlayerExitListener;
import l2f.gameserver.listener.actor.player.OnPlayerPartyInviteListener;
import l2f.gameserver.listener.actor.player.OnPlayerPartyLeaveListener;
import l2f.gameserver.listener.actor.player.OnQuestionMarkListener;
import l2f.gameserver.listener.actor.player.OnTeleportListener;
import l2f.gameserver.listener.actor.player.OnTimeDQCompletedListener;
import l2f.gameserver.listener.actor.player.OnWeeklyDQCompletedListener;
import l2f.gameserver.listener.actor.player.OnWeeklyHuntDQCompletedListener;
import l2f.gameserver.listener.item.OnItemEnchantListener;
import l2f.gameserver.model.Creature;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.entity.Reflection;
import l2f.gameserver.model.items.ItemInstance;
import l2f.gameserver.model.quest.QuestState;

public class PlayerListenerList extends CharListenerList
{
	public PlayerListenerList(Player actor)
	{
		super(actor);
	}

	@Override
	public Player getActor()
	{
		return (Player) actor;
	}

	public void onEnter()
	{
		if (!global.getListeners().isEmpty())
			for (Listener<Creature> listener : global.getListeners())
				if (OnPlayerEnterListener.class.isInstance(listener))
					((OnPlayerEnterListener) listener).onPlayerEnter(getActor());

		if (!getListeners().isEmpty())
			for (Listener<Creature> listener : getListeners())
				if (OnPlayerEnterListener.class.isInstance(listener))
					((OnPlayerEnterListener) listener).onPlayerEnter(getActor());
	}
	
	public void onExit()
	{
		if (!global.getListeners().isEmpty())
			for (Listener<Creature> listener : global.getListeners())
				if (OnPlayerExitListener.class.isInstance(listener))
					((OnPlayerExitListener) listener).onPlayerExit(getActor());

		if (!getListeners().isEmpty())
			for (Listener<Creature> listener : getListeners())
				if (OnPlayerExitListener.class.isInstance(listener))
					((OnPlayerExitListener) listener).onPlayerExit(getActor());
	}

	public void onTeleport(int x, int y, int z, Reflection reflection)
	{
		if (!global.getListeners().isEmpty())
			for (Listener<Creature> listener : global.getListeners())
				if (OnTeleportListener.class.isInstance(listener))
					((OnTeleportListener) listener).onTeleport(getActor(), x, y, z, reflection);

		if (!getListeners().isEmpty())
			for (Listener<Creature> listener : getListeners())
				if (OnTeleportListener.class.isInstance(listener))
					((OnTeleportListener) listener).onTeleport(getActor(), x, y, z, reflection);
	}

	public void onPartyInvite()
	{
		if (!global.getListeners().isEmpty())
			for (Listener<Creature> listener : global.getListeners())
				if (OnPlayerPartyInviteListener.class.isInstance(listener))
					((OnPlayerPartyInviteListener) listener).onPartyInvite(getActor());

		if (!getListeners().isEmpty())
			for (Listener<Creature> listener : getListeners())
				if (OnPlayerPartyInviteListener.class.isInstance(listener))
					((OnPlayerPartyInviteListener) listener).onPartyInvite(getActor());
	}

	public void onPartyLeave()
	{
		if (!global.getListeners().isEmpty())
			for (Listener<Creature> listener : global.getListeners())
				if (OnPlayerPartyLeaveListener.class.isInstance(listener))
					((OnPlayerPartyLeaveListener) listener).onPartyLeave(getActor());

		if (!getListeners().isEmpty())
			for (Listener<Creature> listener : getListeners())
				if (OnPlayerPartyLeaveListener.class.isInstance(listener))
					((OnPlayerPartyLeaveListener) listener).onPartyLeave(getActor());
	}
	
	public void onQuestionMarkClicked(int questionMarkId)
	{
		if (!global.getListeners().isEmpty())
			for (Listener<Creature> listener : global.getListeners())
				if (OnQuestionMarkListener.class.isInstance(listener))
					((OnQuestionMarkListener) listener).onQuestionMarkClicked(getActor(), questionMarkId);
		
		if (!getListeners().isEmpty())
			for (Listener<Creature> listener : getListeners())
				if (OnQuestionMarkListener.class.isInstance(listener))
					((OnQuestionMarkListener) listener).onQuestionMarkClicked(getActor(), questionMarkId);
	}
	public void onEnchantFinish(ItemInstance item, boolean succeed)
	{
		if (!global.getListeners().isEmpty())
			for (Listener<Creature> listener : global.getListeners())
				if (OnItemEnchantListener.class.isInstance(listener))
					((OnItemEnchantListener) listener).onEnchantFinish(getActor(), item, succeed);
		if (!getListeners().isEmpty())
			for (Listener<Creature> listener : getListeners())
				if (OnItemEnchantListener.class.isInstance(listener))
					((OnItemEnchantListener) listener).onEnchantFinish(getActor(), item, succeed);
	}
	public void onFishDied(int fishId, boolean isMonster)
	{
		if (!global.getListeners().isEmpty())
			for (Listener<Creature> listener : global.getListeners())
				if (OnFishDieListener.class.isInstance(listener))
					((OnFishDieListener) listener).onFishDied(getActor(), fishId, isMonster);
		if (!getListeners().isEmpty())
			for (Listener<Creature> listener : getListeners())
				if (OnFishDieListener.class.isInstance(listener))
					((OnFishDieListener) listener).onFishDied(getActor(), fishId, isMonster);
	}
	public void onEventStop()
	{
		if (!global.getListeners().isEmpty())
			for (Listener<Creature> listener : global.getListeners())
				if (OnFCEventStopListener.class.isInstance(listener))
					((OnFCEventStopListener) listener).onEventStop(getActor());
		if (!getListeners().isEmpty())
			for (Listener<Creature> listener : getListeners())
				if (OnFCEventStopListener.class.isInstance(listener))
					((OnFCEventStopListener) listener).onEventStop(getActor());
	}
	public void onHuntDQCompleted(Player player)
	{
		if (!global.getListeners().isEmpty())
			for (Listener<Creature> listener : global.getListeners())
				if (OnHuntDQCompletedListener.class.isInstance(listener))
					((OnHuntDQCompletedListener) listener).onHuntDQCompleted(getActor());
		if (!getListeners().isEmpty())
			for (Listener<Creature> listener : getListeners())
				if (OnHuntDQCompletedListener.class.isInstance(listener))
					((OnHuntDQCompletedListener) listener).onHuntDQCompleted(getActor());
	}
	public void onWeeklyDQCompleted(Player player)
	{
		if (!global.getListeners().isEmpty())
			for (Listener<Creature> listener : global.getListeners())
				if (OnWeeklyDQCompletedListener.class.isInstance(listener))
					((OnWeeklyDQCompletedListener) listener).onWeeklyDQCompleted(getActor());
		if (!getListeners().isEmpty())
			for (Listener<Creature> listener : getListeners())
				if (OnWeeklyDQCompletedListener.class.isInstance(listener))
					((OnWeeklyDQCompletedListener) listener).onWeeklyDQCompleted(getActor());
	}
	public void onTimeDQCompleted(Player player)
	{
		if (!global.getListeners().isEmpty())
			for (Listener<Creature> listener : global.getListeners())
				if (OnTimeDQCompletedListener.class.isInstance(listener))
					((OnTimeDQCompletedListener) listener).onTimeDQCompleted(getActor());
		if (!getListeners().isEmpty())
			for (Listener<Creature> listener : getListeners())
				if (OnTimeDQCompletedListener.class.isInstance(listener))
					((OnTimeDQCompletedListener) listener).onTimeDQCompleted(getActor());
	}
	public void onGeneralDQCompleted(Player player)
	{
		if (!global.getListeners().isEmpty())
			for (Listener<Creature> listener : global.getListeners())
				if (OnGeneralDQCompletedListener.class.isInstance(listener))
					((OnGeneralDQCompletedListener) listener).onGeneralDQCompleted(getActor());
		if (!getListeners().isEmpty())
			for (Listener<Creature> listener : getListeners())
				if (OnGeneralDQCompletedListener.class.isInstance(listener))
					((OnGeneralDQCompletedListener) listener).onGeneralDQCompleted(getActor());
	}
	public void onWeeklyHuntDQCompleted(Player player)
	{
		if (!global.getListeners().isEmpty())
			for (Listener<Creature> listener : global.getListeners())
				if (OnWeeklyHuntDQCompletedListener.class.isInstance(listener))
					((OnWeeklyHuntDQCompletedListener) listener).onWeeklyHuntDQCompleted(getActor());
		if (!getListeners().isEmpty())
			for (Listener<Creature> listener : getListeners())
				if (OnWeeklyHuntDQCompletedListener.class.isInstance(listener))
					((OnWeeklyHuntDQCompletedListener) listener).onWeeklyHuntDQCompleted(getActor());
	}
}
