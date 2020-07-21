package services;

import l2f.gameserver.Config;
import l2f.gameserver.instancemanager.QuestManager;
import l2f.gameserver.listener.actor.player.OnAnswerListener;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.base.Race;
import l2f.gameserver.model.entity.olympiad.Olympiad;
import l2f.gameserver.model.quest.Quest;
import l2f.gameserver.model.quest.QuestState;
import l2f.gameserver.network.serverpackets.ConfirmDlg;
import l2f.gameserver.network.serverpackets.L2GameServerPacket;
import l2f.gameserver.network.serverpackets.MagicSkillUse;
import l2f.gameserver.network.serverpackets.SkillList;
import l2f.gameserver.network.serverpackets.components.SystemMsg;
import l2f.gameserver.scripts.Functions;
import quests._234_FatesWhisper;

public class NoblessSell extends Functions
{
	public void get()
	{
		Player player = getSelf();

		if (player.isNoble())
		{
			player.sendMessage("You are already Nobless!");
			return;
		}
		if (!Config.SERVICES_NOBLESS_SELL_ENABLED)
		{
			show("Service is disabled.", player);
			return;
		}//setleve
		if ((player.getLevel() < 75) && (player.getActiveClass().isBase()))
		{
			player.sendMessage("You need to be over 75 level to purchase noblesse!");
			return;
		}
		if (player.getInventory().getCountOf(Config.SERVICES_NOBLESS_SELL_ITEM) < Config.SERVICES_NOBLESS_SELL_PRICE)
		{
			player.sendMessage("You do not have enough Donator Coins!");
			return;
		}
		if (player.getInventory().getCountOf(Config.SERVICES_NOBLESS_SELL_ITEM) >= Config.SERVICES_NOBLESS_SELL_PRICE)
		{
//			makeSubQuests();
//			becomeNoble();
			askQuestion(player);
		}

		
//		else if (Config.SERVICES_NOBLESS_SELL_ITEM == 37000)
//			player.sendMessage("You don't have 10 Donator Coins!");
//		else
//			player.sendMessage("You don't have 10 Donator Coins!");
	}

	public void makeSubQuests()
	{
		Player player = getSelf();
		if (player == null)
			return;
		Quest q = QuestManager.getQuest(_234_FatesWhisper.class);
		QuestState qs = player.getQuestState(q.getClass());
		if (qs != null)
			qs.exitCurrentQuest(true);
		q.newQuestState(player, Quest.COMPLETED);

		if (player.getRace() == Race.kamael)
		{
			q = QuestManager.getQuest("_236_SeedsOfChaos");
			qs = player.getQuestState(q.getClass());
			if (qs != null)
				qs.exitCurrentQuest(true);
			q.newQuestState(player, Quest.COMPLETED);
		}
		else
		{
			q = QuestManager.getQuest("_235_MimirsElixir");
			qs = player.getQuestState(q.getClass());
			if (qs != null)
				qs.exitCurrentQuest(true);
			q.newQuestState(player, Quest.COMPLETED);
		}
	}

	public void becomeNoble()
	{
		Player player = getSelf();
		if (player == null || player.isNoble())
			return;

		Olympiad.addNoble(player);
		player.setNoble(true);
		player.updatePledgeClass();
		player.updateNobleSkills();
		player.sendPacket(new SkillList(player));
		player.getInventory().addItem(7694, 1L, "nobleTiara");
		player.sendMessage("Congratulations! You gained noblesse rank.");
		player.broadcastUserInfo(true);
		player.broadcastPacket(new L2GameServerPacket[]{ new MagicSkillUse(player, player, 6696, 1, 1000, 0)});
	}
	
	private static void askQuestion(Player player) {
		ConfirmDlg packet = new ConfirmDlg(SystemMsg.S1, 60000).addString("Do you really want to buy Noblesse Status for " + Config.SERVICES_NOBLESS_SELL_PRICE + " Donation Coins?");
		player.ask(packet, new AskQuestionAnswerListener(player));
	}

	private static class AskQuestionAnswerListener implements OnAnswerListener {
		private final Player _player;

		private AskQuestionAnswerListener(Player player) {
			_player = player;
		}

		@Override
		public void sayYes() {
			if (_player == null || _player.isNoble())
				return;

			_player.getInventory().destroyItemByItemId(Config.SERVICES_NOBLESS_SELL_ITEM, Config.SERVICES_NOBLESS_SELL_PRICE, "NoblessSell");
			Quest q = QuestManager.getQuest(_234_FatesWhisper.class);
			QuestState qs = _player.getQuestState(q.getClass());
			if (qs != null)
				qs.exitCurrentQuest(true);
			q.newQuestState(_player, Quest.COMPLETED);

			if (_player.getRace() == Race.kamael)
			{
				q = QuestManager.getQuest("_236_SeedsOfChaos");
				qs = _player.getQuestState(q.getClass());
				if (qs != null)
					qs.exitCurrentQuest(true);
				q.newQuestState(_player, Quest.COMPLETED);
			}
			else
			{
				q = QuestManager.getQuest("_235_MimirsElixir");
				qs = _player.getQuestState(q.getClass());
				if (qs != null)
					qs.exitCurrentQuest(true);
				q.newQuestState(_player, Quest.COMPLETED);
			}
			
			Olympiad.addNoble(_player);
			_player.setNoble(true);
			_player.updatePledgeClass();
			_player.updateNobleSkills();
			_player.sendPacket(new SkillList(_player));
			_player.getInventory().addItem(7694, 1L, "nobleTiara");
			_player.sendMessage("Congratulations! You gained noblesse rank.");
			_player.broadcastUserInfo(true);
			_player.broadcastPacket(new L2GameServerPacket[]{ new MagicSkillUse(_player, _player, 6696, 1, 1000, 0)});	
		}

		@Override
		public void sayNo() {
			_player.sendMessage("Action cancelled.");
		}

	}
}