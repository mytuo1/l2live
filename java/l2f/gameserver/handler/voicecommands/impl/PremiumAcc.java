package l2f.gameserver.handler.voicecommands.impl;

import l2f.gameserver.data.htm.HtmCache;
import l2f.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2f.gameserver.instancemanager.ReflectionManager;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.Zone.ZoneType;
import l2f.gameserver.model.entity.olympiad.Olympiad;
import l2f.gameserver.network.serverpackets.MagicSkillUse;
import l2f.gameserver.network.serverpackets.Say2;
import l2f.gameserver.network.serverpackets.components.ChatType;
import l2f.gameserver.scripts.Functions;
import l2f.gameserver.tables.SkillTable;

public class PremiumAcc extends Functions implements IVoicedCommandHandler
{
	private static String[] COMMANDS =
	{
		"premium","premiuminfo", "premiumbuffs", "premiumbuffscov15", "premiumbuffspow15", 
		"premiumbuffspow215", "premiumbuffspof15", "premiumbuffsblazing", "premiumbuffsfreezing",
		"premiumbuffsresistshock", "premiumbuffseye", "premiumbuffsfist"
	};

 //here is to check if the player is premium and then open the html
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		
		if (command.equals("premiuminfo") && (activeChar.getNetConnection().getBonus() > 0)
				&&  !activeChar.isInOfflineMode() &&  !activeChar.isInBuffStore()
			    &&  !activeChar.isInStoreMode()
			    &&  activeChar.isOnline()
			    &&  !activeChar.isInOlympiadMode()
			    &&  !Olympiad.isRegistered(activeChar)
			    &&  !activeChar.isJailed()
			    &&  !activeChar.isInFightClub()
			    &&  !activeChar.isInZone(ZoneType.SIEGE)
			    &&  activeChar.getReflection() == ReflectionManager.DEFAULT
			    &&  activeChar.getPvpFlag() == 0
			    &&  activeChar.getKarma() == 0
				&&  !activeChar.isInCombat()
				&&  !activeChar.isAttackingNow())
		{
			String dialog = HtmCache.getInstance().getNotNull("command/premiumacc1.htm", activeChar);
			show(dialog, activeChar);
			return true;
		}
		if (command.equals("premiumbuffs") && (activeChar.getNetConnection().getBonus() > 0)
				&&  !activeChar.isInOfflineMode() &&  !activeChar.isInBuffStore()
			    &&  !activeChar.isInStoreMode()
			    &&  activeChar.isOnline()
			    &&  !activeChar.isInOlympiadMode()
			    &&  !Olympiad.isRegistered(activeChar)
			    &&  !activeChar.isJailed()
			    &&  !activeChar.isInFightClub()
			    &&  !activeChar.isInZone(ZoneType.SIEGE)
			    &&  activeChar.getReflection() == ReflectionManager.DEFAULT
			    &&  activeChar.getPvpFlag() == 0
			    &&  activeChar.getKarma() == 0
				&&  !activeChar.isInCombat()
				&&  !activeChar.isAttackingNow())
		{
				String dialog = HtmCache.getInstance().getNotNull("command/premiumacc2.htm", activeChar);
				show(dialog, activeChar);
				return true;
			}
		if (command.equals("premiumbuffscov15") && (activeChar.getNetConnection().getBonus() > 0)
				&&  !activeChar.isInOfflineMode() &&  !activeChar.isInBuffStore()
			    &&  !activeChar.isInStoreMode()
			    &&  activeChar.isOnline()
			    &&  !activeChar.isInOlympiadMode()
			    &&  !Olympiad.isRegistered(activeChar)
			    &&  !activeChar.isJailed()
			    &&  !activeChar.isInFightClub()
			    &&  !activeChar.isInZone(ZoneType.SIEGE)
			    &&  activeChar.getReflection() == ReflectionManager.DEFAULT
			    &&  activeChar.getPvpFlag() == 0
			    &&  activeChar.getKarma() == 0
				&&  !activeChar.isInCombat()
				&&  !activeChar.isAttackingNow())
		{
			SkillTable.getInstance().getInfo(1363, 46).getEffects(activeChar, activeChar, false, false);
			activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, 1363, 46, 2, 0));
			String dialog = HtmCache.getInstance().getNotNull("command/premiumacc2.htm", activeChar);
			show(dialog, activeChar);
			return true;
			}
		if (command.equals("premiumbuffspow15") && (activeChar.getNetConnection().getBonus() > 0)
				&&  !activeChar.isInOfflineMode() &&  !activeChar.isInBuffStore()
			    &&  !activeChar.isInStoreMode()
			    &&  activeChar.isOnline()
			    &&  !activeChar.isInOlympiadMode()
			    &&  !Olympiad.isRegistered(activeChar)
			    &&  !activeChar.isJailed()
			    &&  !activeChar.isInFightClub()
			    &&  !activeChar.isInZone(ZoneType.SIEGE)
			    &&  activeChar.getReflection() == ReflectionManager.DEFAULT
			    &&  activeChar.getPvpFlag() == 0
			    &&  activeChar.getKarma() == 0
				&&  !activeChar.isInCombat()
				&&  !activeChar.isAttackingNow())
		{
			SkillTable.getInstance().getInfo(1355, 46).getEffects(activeChar, activeChar, false, false);
			activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, 1355, 46, 2, 0));
			String dialog = HtmCache.getInstance().getNotNull("command/premiumacc2.htm", activeChar);
			show(dialog, activeChar);
			return true;
			}
		if (command.equals("premiumbuffspow215") && (activeChar.getNetConnection().getBonus() > 0)
				&&  !activeChar.isInOfflineMode() &&  !activeChar.isInBuffStore()
			    &&  !activeChar.isInStoreMode()
			    &&  activeChar.isOnline()
			    &&  !activeChar.isInOlympiadMode()
			    &&  !Olympiad.isRegistered(activeChar)
			    &&  !activeChar.isJailed()
			    &&  !activeChar.isInFightClub()
			    &&  !activeChar.isInZone(ZoneType.SIEGE)
			    &&  activeChar.getReflection() == ReflectionManager.DEFAULT
			    &&  activeChar.getPvpFlag() == 0
			    &&  activeChar.getKarma() == 0
				&&  !activeChar.isInCombat()
				&&  !activeChar.isAttackingNow())
		{
			SkillTable.getInstance().getInfo(1356, 46).getEffects(activeChar, activeChar, false, false);
			activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, 1356, 46, 2, 0));
			String dialog = HtmCache.getInstance().getNotNull("command/premiumacc2.htm", activeChar);
			show(dialog, activeChar);
			return true;
			}
		if (command.equals("premiumbuffspof15") && (activeChar.getNetConnection().getBonus() > 0)
				&&  !activeChar.isInOfflineMode() &&  !activeChar.isInBuffStore()
			    &&  !activeChar.isInStoreMode()
			    &&  activeChar.isOnline()
			    &&  !activeChar.isInOlympiadMode()
			    &&  !Olympiad.isRegistered(activeChar)
			    &&  !activeChar.isJailed()
			    &&  !activeChar.isInFightClub()
			    &&  !activeChar.isInZone(ZoneType.SIEGE)
			    &&  activeChar.getReflection() == ReflectionManager.DEFAULT
			    &&  activeChar.getPvpFlag() == 0
			    &&  activeChar.getKarma() == 0
				&&  !activeChar.isInCombat()
				&&  !activeChar.isAttackingNow())
		{
			SkillTable.getInstance().getInfo(1357, 46).getEffects(activeChar, activeChar, false, false);
			activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, 1357, 46, 2, 0));
			String dialog = HtmCache.getInstance().getNotNull("command/premiumacc2.htm", activeChar);
			show(dialog, activeChar);
			return true;
			}
		if (command.equals("premiumbuffsblazing") && (activeChar.getNetConnection().getBonus() > 0)
				&&  !activeChar.isInOfflineMode() &&  !activeChar.isInBuffStore()
			    &&  !activeChar.isInStoreMode()
			    &&  activeChar.isOnline()
			    &&  !activeChar.isInOlympiadMode()
			    &&  !Olympiad.isRegistered(activeChar)
			    &&  !activeChar.isJailed()
			    &&  !activeChar.isInFightClub()
			    &&  !activeChar.isInZone(ZoneType.SIEGE)
			    &&  activeChar.getReflection() == ReflectionManager.DEFAULT
			    &&  activeChar.getPvpFlag() == 0
			    &&  activeChar.getKarma() == 0
				&&  !activeChar.isInCombat()
				&&  !activeChar.isAttackingNow())
		{
			SkillTable.getInstance().getInfo(1232, 93).getEffects(activeChar, activeChar, false, false);
			activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, 1232, 93, 2, 0));
			String dialog = HtmCache.getInstance().getNotNull("command/premiumacc2.htm", activeChar);
			show(dialog, activeChar);
			return true;
			}
		if (command.equals("premiumbuffsfreezing") && (activeChar.getNetConnection().getBonus() > 0)
				&&  !activeChar.isInOfflineMode() &&  !activeChar.isInBuffStore()
			    &&  !activeChar.isInStoreMode()
			    &&  activeChar.isOnline()
			    &&  !activeChar.isInOlympiadMode()
			    &&  !Olympiad.isRegistered(activeChar)
			    &&  !activeChar.isJailed()
			    &&  !activeChar.isInFightClub()
			    &&  !activeChar.isInZone(ZoneType.SIEGE)
			    &&  activeChar.getReflection() == ReflectionManager.DEFAULT
			    &&  activeChar.getPvpFlag() == 0
			    &&  activeChar.getKarma() == 0
				&&  !activeChar.isInCombat()
				&&  !activeChar.isAttackingNow())
		{
			SkillTable.getInstance().getInfo(1238, 93).getEffects(activeChar, activeChar, false, false);
			activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, 1238, 93, 2, 0));
			String dialog = HtmCache.getInstance().getNotNull("command/premiumacc2.htm", activeChar);
			show(dialog, activeChar);
			return true;
			}
		if (command.equals("premiumbuffsresistshock") && (activeChar.getNetConnection().getBonus() > 0)
				&&  !activeChar.isInOfflineMode() &&  !activeChar.isInBuffStore()
			    &&  !activeChar.isInStoreMode()
			    &&  activeChar.isOnline()
			    &&  !activeChar.isInOlympiadMode()
			    &&  !Olympiad.isRegistered(activeChar)
			    &&  !activeChar.isJailed()
			    &&  !activeChar.isInFightClub()
			    &&  !activeChar.isInZone(ZoneType.SIEGE)
			    &&  activeChar.getReflection() == ReflectionManager.DEFAULT
			    &&  activeChar.getPvpFlag() == 0
			    &&  activeChar.getKarma() == 0
				&&  !activeChar.isInCombat()
				&&  !activeChar.isAttackingNow())
		{
			SkillTable.getInstance().getInfo(1259, 94).getEffects(activeChar, activeChar, false, false);
			activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, 1259, 94, 2, 0));
			String dialog = HtmCache.getInstance().getNotNull("command/premiumacc2.htm", activeChar);
			show(dialog, activeChar);
			return true;

			}
		if (command.equals("premiumbuffseye") && (activeChar.getNetConnection().getBonus() > 0)
				&&  !activeChar.isInOfflineMode() &&  !activeChar.isInBuffStore()
			    &&  !activeChar.isInStoreMode()
			    &&  activeChar.isOnline()
			    &&  !activeChar.isInOlympiadMode()
			    &&  !Olympiad.isRegistered(activeChar)
			    &&  !activeChar.isJailed()
			    &&  !activeChar.isInFightClub()
			    &&  !activeChar.isInZone(ZoneType.SIEGE)
			    &&  activeChar.getReflection() == ReflectionManager.DEFAULT
			    &&  activeChar.getPvpFlag() == 0
			    &&  activeChar.getKarma() == 0
				&&  !activeChar.isInCombat()
				&&  !activeChar.isAttackingNow())
		{
			SkillTable.getInstance().getInfo(1364, 1).getEffects(activeChar, activeChar, false, false);
			activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, 1364, 1, 2, 0));
			String dialog = HtmCache.getInstance().getNotNull("command/premiumacc2.htm", activeChar);
			show(dialog, activeChar);
			return true;
			}
		if (command.equals("premiumbuffsfist") && (activeChar.getNetConnection().getBonus() > 0)
				&&  !activeChar.isInOfflineMode() &&  !activeChar.isInBuffStore()
			    &&  !activeChar.isInStoreMode()
			    &&  activeChar.isOnline()
			    &&  !activeChar.isInOlympiadMode()
			    &&  !Olympiad.isRegistered(activeChar)
			    &&  !activeChar.isJailed()
			    &&  !activeChar.isInFightClub()
			    &&  !activeChar.isInZone(ZoneType.SIEGE)
			    &&  activeChar.getReflection() == ReflectionManager.DEFAULT
			    &&  activeChar.getPvpFlag() == 0
			    &&  activeChar.getKarma() == 0
				&&  !activeChar.isInCombat()
				&&  !activeChar.isAttackingNow())
		{
			SkillTable.getInstance().getInfo(1416, 16).getEffects(activeChar, activeChar, false, false);
			activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, 1416, 16, 2, 0));
			String dialog = HtmCache.getInstance().getNotNull("command/premiumacc2.htm", activeChar);
			show(dialog, activeChar);
			return true;
			}
		if(command.equals("premium") && (activeChar.getNetConnection().getBonus() > 0)
				&&  !activeChar.isInOfflineMode() &&  !activeChar.isInBuffStore()
			    &&  !activeChar.isInStoreMode()
			    &&  activeChar.isOnline()
			    &&  !activeChar.isInOlympiadMode()
			    &&  !Olympiad.isRegistered(activeChar)
			    &&  !activeChar.isJailed()
			    &&  !activeChar.isInFightClub()
			    &&  !activeChar.isInZone(ZoneType.SIEGE)
			    &&  activeChar.getReflection() == ReflectionManager.DEFAULT
			    &&  activeChar.getPvpFlag() == 0
			    &&  activeChar.getKarma() == 0
				&&  !activeChar.isInCombat()
				&&  !activeChar.isAttackingNow() )
		{
			String dialog = HtmCache.getInstance().getNotNull("command/premiumacc.htm", activeChar);
			show(dialog, activeChar);
			return true;
		}
	// Only for premiums
//		else if ((activeChar.getNetConnection().getBonus() > 0)
//	&&  !activeChar.isInOfflineMode() &&  !activeChar.isInBuffStore()
//    &&  !activeChar.isInStoreMode()
//    &&  activeChar.isOnline()
//    &&  !activeChar.isInOlympiadMode()
//    &&  !Olympiad.isRegistered(activeChar)
//    &&  !activeChar.isJailed()
//    &&  !activeChar.isInFightClub()
//    &&  !activeChar.isInZone(ZoneType.SIEGE)
//    &&  activeChar.getReflection() == ReflectionManager.DEFAULT
//    &&  activeChar.getPvpFlag() == 0
//    &&  activeChar.getKarma() == 0
//	&&  !activeChar.isInCombat()
//	&&  !activeChar.isAttackingNow())
//	
//	{
//		activeChar.sendPacket(new Say2(activeChar.getObjectId(), ChatType.BATTLEFIELD, "Premium Manager", "You do not meet the requirements to use this command."));
//		activeChar.sendMessage("Players which are under these modes: store/offline/buffstore mode, olympiad mode, jail, registered to an event, during siege, in combat/pvp or karma, are not eligible to use this command.");
//		return false;
//	}
	else
		{
		activeChar.sendPacket(new Say2(activeChar.getObjectId(), ChatType.BATTLEFIELD, "Premium Manager", "You do not meet the requirements to use this command."));
		activeChar.sendMessage("Players which are under these modes: store/offline/buffstore mode, olympiad mode, jail, registered to an event, during siege, in combat/pvp or karma, are not eligible to use this command.");
		return false;
		}
	}
	
// here to return back the commands
	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}
}