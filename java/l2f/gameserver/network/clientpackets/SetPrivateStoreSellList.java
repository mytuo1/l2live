package l2f.gameserver.network.clientpackets;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.ArrayUtils;

import l2f.gameserver.Config;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.entity.auction.Auction;
import l2f.gameserver.model.entity.auction.AuctionManager;
import l2f.gameserver.model.items.ItemInstance;
import l2f.gameserver.model.items.TradeItem;
import l2f.gameserver.network.serverpackets.PrivateStoreManageListSell;
import l2f.gameserver.network.serverpackets.PrivateStoreMsgSell;
import l2f.gameserver.network.serverpackets.components.SystemMsg;
import l2f.gameserver.templates.item.ItemTemplate;
import l2f.gameserver.utils.TradeHelper;


public class SetPrivateStoreSellList extends L2GameClientPacket
{
	private int _count;
	private boolean _package;
	private int[] _items; // objectId
	private long[] _itemQ; // count
	private long[] _itemP; // price

	@Override
	protected void readImpl()
	{
		_package = readD() == 1;
		_count = readD();
		if (_count * 20 > _buf.remaining() || _count > Short.MAX_VALUE || _count < 1)
		{
			_count = 0;
			return;
		}

		_items = new int[_count];
		_itemQ = new long[_count];
		_itemP = new long[_count];

		for (int i = 0; i < _count; i++)
		{
			_items[i] = readD();
			_itemQ[i] = readQ();
			_itemP[i] = readQ();
			if (_itemQ[i] < 1 || _itemP[i] < 0 || ArrayUtils.indexOf(_items, _items[i]) < i)
			{
				_count = 0;
				break;
			}
		}
	}

	@Override
	protected void runImpl()
	{
		Player seller = getClient().getActiveChar();
		if (seller == null || _count == 0)
			return;

		if (!TradeHelper.checksIfCanOpenStore(seller, _package ? Player.STORE_PRIVATE_SELL_PACKAGE : Player.STORE_PRIVATE_SELL) || !Config.ALLOW_PRIVATE_STORES)
		{
			seller.sendActionFailed();
			return;
		}

		TradeItem temp;
		List<TradeItem> sellList = new CopyOnWriteArrayList<TradeItem>();

		seller.getInventory().writeLock();
		try
		{
			for (int i = 0; i < _count; i++)
			{
				int objectId = _items[i];
				long count = _itemQ[i];
				long price = _itemP[i];

				ItemInstance item = seller.getInventory().getItemByObjectId(objectId);

				if (item == null || item.getCount() < count || !item.canBeTraded(seller) || item.getItemId() == ItemTemplate.ITEM_ID_ADENA)
					continue;

				temp = new TradeItem(item);
				temp.setCount(count);
				temp.setOwnersPrice(price);
				sellList.add(temp);
			}
		}
		finally
		{
			seller.getInventory().writeUnlock();
		}

		if (sellList.size() > seller.getTradeLimit())
		{
			seller.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			seller.sendPacket(new PrivateStoreManageListSell(seller, _package));
			return;
		}

		if (!sellList.isEmpty())
		{
			seller.setSellList(_package, sellList);
			seller.saveTradeList();
			seller.setPrivateStoreType(_package ? Player.STORE_PRIVATE_SELL_PACKAGE : Player.STORE_PRIVATE_SELL);
			seller.broadcastPacket(new PrivateStoreMsgSell(seller));
			seller.sitDown(null);
			seller.broadcastCharInfo();
		
			if (Config.AUCTION_PRIVATE_STORE_AUTO_ADDED && !_package)
			{
				for (TradeItem ti : sellList)
				{
					ItemInstance item = seller.getInventory().getItemByObjectId(ti.getObjectId());
					if (item == null)
						continue;
					Auction auc = AuctionManager.getInstance().addNewStore(seller, item, ti.getOwnersPrice(), ti.getCount());
					ti.setAuctionId(auc.getAuctionId());
				}
			}
		}

		seller.sendActionFailed();
	}
}