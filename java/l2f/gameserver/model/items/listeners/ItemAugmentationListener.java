package l2f.gameserver.model.items.listeners;

import java.util.List;

import l2f.gameserver.data.xml.holder.OptionDataHolder;
import l2f.gameserver.listener.inventory.OnEquipListener;
import l2f.gameserver.model.Playable;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.Skill;
import l2f.gameserver.model.Skill.SkillOpType;
import l2f.gameserver.model.items.ItemInstance;
import l2f.gameserver.network.serverpackets.SkillCoolTime;
import l2f.gameserver.network.serverpackets.SkillList;
import l2f.gameserver.templates.OptionDataTemplate;

public final class ItemAugmentationListener implements OnEquipListener
{
	private static final ItemAugmentationListener _instance = new ItemAugmentationListener();

	public static ItemAugmentationListener getInstance()
	{
		return _instance;
	}

	@Override
	public void onUnequip(int slot, ItemInstance item, Playable actor)
	{
		if (!item.isEquipable())
			return;
		if (!item.isAugmented())
			return;

		Player player = actor.getPlayer();

		int stats[] = new int[2];
		stats[0] = 0x0000FFFF & item.getAugmentationId();
		stats[1] = item.getAugmentationId() >> 16;

		boolean sendList = false;
		for (int i : stats)
		{
			OptionDataTemplate template = OptionDataHolder.getInstance().getTemplate(i);
			if (template == null)
				continue;

			player.removeStatsOwner(template);

			for (Skill skill : template.getSkills())
			{
				sendList = true;
				player.removeSkill(skill);
			}
			if (!template.getTriggerList().isEmpty())
			{
			player.removeTriggers(template);
			player.removeSkill(template.getTriggerList().get(0).getSkill());
			player.removeUnActiveVisualSkill(template.getTriggerList().get(0).getSkill());
			player.sendPacket(new SkillList(player));
			}
		}

		if (sendList)
			player.sendPacket(new SkillList(player));

		player.updateStats();
	}

	@Override
	public void onEquip(int slot, ItemInstance item, Playable actor)
	{
		if (!item.isEquipable())
			return;
		if (!item.isAugmented())
			return;

		Player player = actor.getPlayer();

		// При несоотвествии грейда аугмент не применяется
		if (player.getExpertisePenalty(item) > 0)
			return;

		int stats[] = new int[2];
		stats[0] = 0x0000FFFF & item.getAugmentationId();
		stats[1] = item.getAugmentationId() >> 16;

		boolean sendList = false;
		boolean sendReuseList = false;
		for (int i : stats)
		{
			OptionDataTemplate template = OptionDataHolder.getInstance().getTemplate(i);
			if (template == null)
				continue;

			player.addStatFuncs(template.getStatFuncs(template));

			for (Skill skill : template.getSkills())
			{
				sendList = true;
				player.addSkill(skill);
				if (player.isSkillDisabled(skill))
					sendReuseList = true;
			}

			if (!template.getTriggerList().isEmpty())
			{
			player.addTriggers(template);
			player.addSkill(template.getTriggerList().get(0).getSkill());
			player.addUnactiveVisualSkill(template.getTriggerList().get(0).getSkill());
			player.sendPacket(new SkillList(player));
			}
		}

		if (sendList)
			player.sendPacket(new SkillList(player));

		if (sendReuseList)
			player.sendPacket(new SkillCoolTime(player));

		player.updateStats();
	}
}