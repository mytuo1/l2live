package l2f.gameserver.model.entity.CCPHelpers;

import l2f.gameserver.Config;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.Zone;
import l2f.gameserver.model.Zone.ZoneType;
import l2f.gameserver.model.entity.olympiad.Olympiad;
import l2f.gameserver.network.serverpackets.components.CustomMessage;
import l2f.gameserver.scripts.Functions;

public class CCPOffline
{
	public static boolean setOfflineStore(Player activeChar)
	{
		if (!Config.SERVICES_OFFLINE_TRADE_ALLOW)
		{
			activeChar.sendMessage("This option is currently disabled!");
			return false;
		}

		if (activeChar.getOlympiadObserveGame() != null || activeChar.getOlympiadGame() != null  || Olympiad.isRegisteredInComp(activeChar) || activeChar.getKarma() > 0)
		{
			activeChar.sendMessage("You cannot do it right now!");
			return false;
		}

		if (activeChar.getLevel() < Config.SERVICES_OFFLINE_TRADE_MIN_LEVEL)
		{
			activeChar.sendMessage("Your level is too low!");
			return false;
		}

		if (!activeChar.isInZone(Zone.ZoneType.offshore) && !activeChar.isInZone(ZoneType.buff_store_only) && !activeChar.isInZone(ZoneType.RESIDENCE) && Config.SERVICES_OFFLINE_TRADE_ALLOW_OFFSHORE)
		{
			activeChar.sendMessage("You cannot set offline store in this area!");
			return false;
		}

		if (!activeChar.isInStoreMode())
		{
			activeChar.sendMessage("You need to place Private Store first!");
			return false;
		}
		
		if (activeChar.isInBuffStore())
		{
			activeChar.sendMessage("Logging out your buffer securely.");
			activeChar.offlineBuffStore();
			return true;
		}

		if (activeChar.getNoChannelRemained() > 0)
		{
			activeChar.sendMessage("You cannot set offline store while having Chat Ban!");
			return false;
		}

		/*if (activeChar.isActionBlocked(Zone.BLOCKED_ACTION_PRIVATE_STORE))
		{
			activeChar.sendMessage("You cannot set offline store in this area!!");
			return false;
		}*/

		if (Config.SERVICES_OFFLINE_TRADE_PRICE > 0 && Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM > 0)
		{
			if (Functions.getItemCount(activeChar, Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM) < Config.SERVICES_OFFLINE_TRADE_PRICE)
			{
				Functions.show(new CustomMessage("voicedcommandhandlers.Offline.NotEnough", activeChar).addItemName(Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM).addNumber(Config.SERVICES_OFFLINE_TRADE_PRICE), activeChar);
				return false;
			}
			Functions.removeItem(activeChar, Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM, Config.SERVICES_OFFLINE_TRADE_PRICE, "Offline Store");
		}

		activeChar.offline();
		return true;
	}
}
