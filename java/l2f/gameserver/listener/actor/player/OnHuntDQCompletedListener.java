package l2f.gameserver.listener.actor.player;

import l2f.gameserver.listener.PlayerListener;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.quest.QuestState;

public interface OnHuntDQCompletedListener extends PlayerListener
{
	void onHuntDQCompleted(Player player);
}

