package l2f.gameserver.model.items;

import java.io.Serializable;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Elemental.templates.Ranking;
import l2f.commons.dao.JdbcEntityState;
import l2f.commons.listener.Listener;
import l2f.commons.listener.ListenerList;
import l2f.gameserver.data.xml.holder.ItemHolder;
import l2f.gameserver.listener.inventory.OnEquipListener;
import l2f.gameserver.model.Playable;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.items.ItemInstance.ItemLocation;
import l2f.gameserver.model.items.listeners.StatsListener;
import l2f.gameserver.templates.item.EtcItemTemplate.EtcItemType;
import l2f.gameserver.templates.item.ItemTemplate;
import l2f.gameserver.templates.item.WeaponTemplate.WeaponType;

public abstract class Inventory extends ItemContainer
{
	private static final Logger _log = LoggerFactory.getLogger(Inventory.class);

	public static final int PAPERDOLL_UNDER = 0;
	public static final int PAPERDOLL_REAR = 1;
	public static final int PAPERDOLL_LEAR = 2;
	public static final int PAPERDOLL_NECK = 3;
	public static final int PAPERDOLL_RFINGER = 4;
	public static final int PAPERDOLL_LFINGER = 5;
	public static final int PAPERDOLL_HEAD = 6;
	public static final int PAPERDOLL_RHAND = 7;
	public static final int PAPERDOLL_LHAND = 8;
	public static final int PAPERDOLL_GLOVES = 9;
	public static final int PAPERDOLL_CHEST = 10;
	public static final int PAPERDOLL_LEGS = 11;
	public static final int PAPERDOLL_FEET = 12;
	public static final int PAPERDOLL_BACK = 13;
	public static final int PAPERDOLL_LRHAND = 14;
	public static final int PAPERDOLL_HAIR = 15;
	public static final int PAPERDOLL_DHAIR = 16;
	public static final int PAPERDOLL_RBRACELET = 17;
	public static final int PAPERDOLL_LBRACELET = 18;
	public static final int PAPERDOLL_DECO1 = 19;
	public static final int PAPERDOLL_DECO2 = 20;
	public static final int PAPERDOLL_DECO3 = 21;
	public static final int PAPERDOLL_DECO4 = 22;
	public static final int PAPERDOLL_DECO5 = 23;
	public static final int PAPERDOLL_DECO6 = 24;
	public static final int PAPERDOLL_BELT = 25;

	public static final int PAPERDOLL_MAX = 26;

	public static final int[] PAPERDOLL_ORDER =
	{
			Inventory.PAPERDOLL_UNDER,
			Inventory.PAPERDOLL_REAR,
			Inventory.PAPERDOLL_LEAR,
			Inventory.PAPERDOLL_NECK,
			Inventory.PAPERDOLL_RFINGER,
			Inventory.PAPERDOLL_LFINGER,
			Inventory.PAPERDOLL_HEAD,
			Inventory.PAPERDOLL_RHAND,
			Inventory.PAPERDOLL_LHAND,
			Inventory.PAPERDOLL_GLOVES,
			Inventory.PAPERDOLL_CHEST,
			Inventory.PAPERDOLL_LEGS,
			Inventory.PAPERDOLL_FEET,
			Inventory.PAPERDOLL_BACK,
			Inventory.PAPERDOLL_LRHAND,
			Inventory.PAPERDOLL_HAIR,
			Inventory.PAPERDOLL_DHAIR,
			Inventory.PAPERDOLL_RBRACELET,
			Inventory.PAPERDOLL_LBRACELET,
			Inventory.PAPERDOLL_DECO1,
			Inventory.PAPERDOLL_DECO2,
			Inventory.PAPERDOLL_DECO3,
			Inventory.PAPERDOLL_DECO4,
			Inventory.PAPERDOLL_DECO5,
			Inventory.PAPERDOLL_DECO6,
			Inventory.PAPERDOLL_BELT // Пояс
	};

	public class InventoryListenerList extends ListenerList<Playable>
	{
		public void onEquip(int slot, ItemInstance item)
		{
			for (Listener<Playable> listener : getListeners())
				((OnEquipListener)listener).onEquip(slot, item, getActor());
		}

		public void onUnequip(int slot, ItemInstance item)
		{
			for (Listener<Playable> listener : getListeners())
				((OnEquipListener)listener).onUnequip(slot, item, getActor());
		}
	}

	public static class ItemOrderComparator implements Comparator<ItemInstance>, Serializable
	{
		private static final Comparator<ItemInstance> instance = new ItemOrderComparator();
		private static final long serialVersionUID = 306800337898292778L;

		public static Comparator<ItemInstance> getInstance()
		{
			return instance;
		}

		@Override
		public int compare(ItemInstance o1, ItemInstance o2)
		{
			if (o1 == null || o2 == null)
				return 0;
			return Integer.compare(o1.getLocData(), o2.getLocData());
		}
	}

	protected final int _ownerId;

	protected final ItemInstance[] _paperdoll = new ItemInstance[PAPERDOLL_MAX];
	protected final InventoryListenerList _listeners = new InventoryListenerList();

	protected int _totalWeight;
	// used to quickly check for using of items of special type
	protected long _wearedMask;

	protected Inventory(int ownerId)
	{
		_ownerId = ownerId;

		addListener(StatsListener.getInstance());
	}

	public abstract Playable getActor();

	protected abstract ItemLocation getBaseLocation();

	protected abstract ItemLocation getEquipLocation();

	public int getOwnerId()
	{
		return _ownerId;
	}

	protected void onRestoreItem(ItemInstance item)
	{
		_totalWeight += item.getTemplate().getWeight() * item.getCount();
	}

	@Override
	protected void onAddItem(ItemInstance item)
	{
		item.setOwnerId(getOwnerId());
		item.setLocation(getBaseLocation());
		item.setLocData(findSlot());
		if (item.getJdbcState().isSavable())
		{
			item.save();
		}
		else
		{
			item.setJdbcState(JdbcEntityState.UPDATED);
			item.update();
		}

		sendAddItem(item);
		refreshWeight();

		// Alexander - Add the new amount of adena to the stats. Will be registered only if bigger than the last
    	if (getActor() instanceof Player && item.getItemId() == ItemTemplate.ITEM_ID_ADENA)
    		getActor().getPlayer().addPlayerStats(Ranking.STAT_TOP_ADENA_ACQUIRED, item.getCount());
	}

	@Override
	protected void onModifyItem(ItemInstance item)
	{
		item.setJdbcState(JdbcEntityState.UPDATED);
		item.update();

		sendModifyItem(item);
		refreshWeight();

		// Alexander - Add the new amount of adena to the stats. Will be registered only if bigger than the last
    	if (getActor() instanceof Player && item.getItemId() == ItemTemplate.ITEM_ID_ADENA)
    		getActor().getPlayer().addPlayerStats(Ranking.STAT_TOP_ADENA_ACQUIRED, item.getCount());
	}

	@Override
	protected void onRemoveItem(ItemInstance item)
	{
		if (item.isEquipped())
			unEquipItem(item);

		sendRemoveItem(item);

		item.setLocData(-1);

		refreshWeight();
	}

	@Override
	protected void onDestroyItem(ItemInstance item)
	{
		item.setCount(0L);
		item.delete();
	}

	protected void onEquip(int slot, ItemInstance item)
	{
		_listeners.onEquip(slot, item);

		item.setLocation(getEquipLocation());
		item.setLocData(slot);
		item.setEquipped(true);
		item.setJdbcState(JdbcEntityState.UPDATED);

		sendModifyItem(item);

		_wearedMask |= item.getTemplate().getItemMask();
	}

	protected void onUnequip(int slot, ItemInstance item)
	{
		item.setLocation(getBaseLocation());
		item.setLocData(findSlot());
		item.setEquipped(false);
		item.setJdbcState(JdbcEntityState.UPDATED);

		item.setChargedSpiritshot(ItemInstance.CHARGED_NONE);
		item.setChargedSoulshot(ItemInstance.CHARGED_NONE);

		sendModifyItem(item);

		_wearedMask &= ~item.getTemplate().getItemMask();

		_listeners.onUnequip(slot, item);
	}

	/**
	 * Находит и возвращает пустой слот в инвентаре.
	 * @return
	 */
	private int findSlot()
	{
		ItemInstance item;
		int slot = 0;
		loop: for(slot = 0; slot < _items.size(); slot++)
		{
			for (int i = 0; i < _items.size(); i++)
			{
				item = _items.get(i);
				if (item.isEquipped() || item.getTemplate().isQuest()) // игнорируем надетое и квестовые вещи
					continue;
				if (item.getEquipSlot() == slot) // слот занят?
					continue loop;
			}
			break;
		}
		return slot; // слот не занят, возвращаем
	}

	public ItemInstance getPaperdollItem(int slot)
	{
		return _paperdoll[slot];
	}

	public ItemInstance[] getPaperdollItems()
	{
		return _paperdoll;
	}

	public int getPaperdollItemId(int slot)
	{
		ItemInstance item = getPaperdollItem(slot);
		if (item != null)
			return item.getItemId();
		else if (slot == PAPERDOLL_HAIR)
		{
			item = _paperdoll[PAPERDOLL_DHAIR];
			if (item != null)
				return item.getItemId();
		}

		return 0;
	}

	public int getPaperdollVisualItemId(int slot)
	{
		ItemInstance item = getPaperdollItem(slot);
		if (item != null)
		{
			return item.getItemId();
		}
		else if (slot == PAPERDOLL_HAIR)
		{
			item = _paperdoll[PAPERDOLL_DHAIR];
			if (item != null)
				return item.getItemId();
		}

		return 0;
	}

	public int getPaperdollObjectId(int slot)
	{
		ItemInstance item = _paperdoll[slot];
		if (item != null)
			return item.getObjectId();
		else if (slot == PAPERDOLL_HAIR)
		{
			item = _paperdoll[PAPERDOLL_DHAIR];
			if (item != null)
				return item.getObjectId();
		}
		return 0;
	}

	public void addListener(OnEquipListener listener)
	{
		_listeners.add(listener);
	}

	public void removeListener(OnEquipListener listener)
	{
		_listeners.remove(listener);
	}

	public ItemInstance setPaperdollItem(int slot, ItemInstance item)
	{
		ItemInstance old;
		writeLock();
		try
		{
			old = _paperdoll[slot];
			if (old != item)
			{
				if (old != null)
				{
					_paperdoll[slot] = null;
					onUnequip(slot, old);
				}
				if (item != null)
				{
					_paperdoll[slot] = item;
					onEquip(slot, item);
				}
			}
		}
		finally
		{
			writeUnlock();
		}
		return old;
	}

	public long getWearedMask()
	{
		return _wearedMask;
	}

	public void unEquipItem(ItemInstance item)
	{
		if (item.isEquipped())
			unEquipItemInBodySlot(item.getBodyPart(), item);
	}

	public void unEquipItemInBodySlot(int bodySlot)
	{
		unEquipItemInBodySlot(bodySlot, null);
	}

	private void unEquipItemInBodySlot(int bodySlot, ItemInstance item)
	{
		int pdollSlot = -1;

		switch (bodySlot)
		{
			case ItemTemplate.SLOT_NECK:
				pdollSlot = PAPERDOLL_NECK;
				break;
			case ItemTemplate.SLOT_L_EAR:
				pdollSlot = PAPERDOLL_LEAR;
				break;
			case ItemTemplate.SLOT_R_EAR:
				pdollSlot = PAPERDOLL_REAR;
				break;
			case ItemTemplate.SLOT_L_EAR | ItemTemplate.SLOT_R_EAR:
				if (item == null)
					return;
				if (getPaperdollItem(PAPERDOLL_LEAR) == item)
					pdollSlot = PAPERDOLL_LEAR;
				if (getPaperdollItem(PAPERDOLL_REAR) == item)
					pdollSlot = PAPERDOLL_REAR;
				break;
			case ItemTemplate.SLOT_L_FINGER:
				pdollSlot = PAPERDOLL_LFINGER;
				break;
			case ItemTemplate.SLOT_R_FINGER:
				pdollSlot = PAPERDOLL_RFINGER;
				break;
			case ItemTemplate.SLOT_L_FINGER | ItemTemplate.SLOT_R_FINGER:
				if (item == null)
					return;
				if (getPaperdollItem(PAPERDOLL_LFINGER) == item)
					pdollSlot = PAPERDOLL_LFINGER;
				if (getPaperdollItem(PAPERDOLL_RFINGER) == item)
					pdollSlot = PAPERDOLL_RFINGER;
				break;
			case ItemTemplate.SLOT_HAIR:
				pdollSlot = PAPERDOLL_HAIR;
				break;
			case ItemTemplate.SLOT_DHAIR:
				pdollSlot = PAPERDOLL_DHAIR;
				break;
			case ItemTemplate.SLOT_HAIRALL:
				setPaperdollItem(PAPERDOLL_DHAIR, null); // This should be the same as in DHAIR
				pdollSlot = PAPERDOLL_HAIR;
				break;
			case ItemTemplate.SLOT_HEAD:
				pdollSlot = PAPERDOLL_HEAD;
				break;
			case ItemTemplate.SLOT_R_HAND:
				pdollSlot = PAPERDOLL_RHAND;
				break;
			case ItemTemplate.SLOT_L_HAND:
				pdollSlot = PAPERDOLL_LHAND;
				break;
			case ItemTemplate.SLOT_GLOVES:
				pdollSlot = PAPERDOLL_GLOVES;
				break;
			case ItemTemplate.SLOT_LEGS:
				pdollSlot = PAPERDOLL_LEGS;
				break;
			case ItemTemplate.SLOT_CHEST:
			case ItemTemplate.SLOT_FULL_ARMOR:
			case ItemTemplate.SLOT_FORMAL_WEAR:
				pdollSlot = PAPERDOLL_CHEST;
				break;
			case ItemTemplate.SLOT_BACK:
				pdollSlot = PAPERDOLL_BACK;
				break;
			case ItemTemplate.SLOT_FEET:
				pdollSlot = PAPERDOLL_FEET;
				break;
			case ItemTemplate.SLOT_UNDERWEAR:
				pdollSlot = PAPERDOLL_UNDER;
				break;
			case ItemTemplate.SLOT_BELT:
				pdollSlot = PAPERDOLL_BELT;
				break;
			case ItemTemplate.SLOT_LR_HAND:
				setPaperdollItem(PAPERDOLL_LHAND, null);
				pdollSlot = PAPERDOLL_RHAND;
				break;
			case ItemTemplate.SLOT_L_BRACELET:
				pdollSlot = PAPERDOLL_LBRACELET;
				break;
			case ItemTemplate.SLOT_R_BRACELET:
				pdollSlot = PAPERDOLL_RBRACELET;
				// При снятии правого браслета, снимаем и талисманы тоже
				setPaperdollItem(Inventory.PAPERDOLL_DECO1, null);
				setPaperdollItem(Inventory.PAPERDOLL_DECO2, null);
				setPaperdollItem(Inventory.PAPERDOLL_DECO3, null);
				setPaperdollItem(Inventory.PAPERDOLL_DECO4, null);
				setPaperdollItem(Inventory.PAPERDOLL_DECO5, null);
				setPaperdollItem(Inventory.PAPERDOLL_DECO6, null);
				break;
			case ItemTemplate.SLOT_DECO:
				if (item == null)
					return;
				else if (getPaperdollItem(PAPERDOLL_DECO1) == item)
					pdollSlot = PAPERDOLL_DECO1;
				else if (getPaperdollItem(PAPERDOLL_DECO2) == item)
					pdollSlot = PAPERDOLL_DECO2;
				else if (getPaperdollItem(PAPERDOLL_DECO3) == item)
					pdollSlot = PAPERDOLL_DECO3;
				else if (getPaperdollItem(PAPERDOLL_DECO4) == item)
					pdollSlot = PAPERDOLL_DECO4;
				else if (getPaperdollItem(PAPERDOLL_DECO5) == item)
					pdollSlot = PAPERDOLL_DECO5;
				else if (getPaperdollItem(PAPERDOLL_DECO6) == item)
					pdollSlot = PAPERDOLL_DECO6;
				break;
			default:
				_log.warn("Requested invalid body slot: " + bodySlot + ", Item: " + item + ", ownerId: '" + getOwnerId() + "'");
				return;
		}

		if (pdollSlot >= 0)
			setPaperdollItem(pdollSlot, null);
	}

	public void equipItem(ItemInstance item)
	{
		int bodySlot = item.getBodyPart();

		//TODO [G1ta0] затычка на статы повышающие HP/MP/CP
		double hp = getActor().getCurrentHp();
		double mp = getActor().getCurrentMp();
		double cp = getActor().getCurrentCp();

		switch (bodySlot)
		{
			case ItemTemplate.SLOT_LR_HAND:
			{
				setPaperdollItem(PAPERDOLL_LHAND, null);
				setPaperdollItem(PAPERDOLL_RHAND, item);
				break;
			}
			case ItemTemplate.SLOT_L_HAND:
			{
				final ItemInstance rHandItem = getPaperdollItem(PAPERDOLL_RHAND);

				final ItemTemplate rHandItemTemplate = rHandItem == null ? null : rHandItem.getTemplate();
				final ItemTemplate newItem = item.getTemplate();

				if (newItem.getItemType() == EtcItemType.ARROW)
				{
					// arrows can be equipped only with bow
					if (rHandItemTemplate == null)
						return;
					if (rHandItemTemplate.getItemType() != WeaponType.BOW)
						return;
					if (rHandItemTemplate.getCrystalType() != newItem.getCrystalType())
						return;
				}
				else if (newItem.getItemType() == EtcItemType.BOLT)
				{
					// bolts can be equipped only with crossbow
					if (rHandItemTemplate == null)
						return;
					if (rHandItemTemplate.getItemType() != WeaponType.CROSSBOW)
						return;
					if (rHandItemTemplate.getCrystalType() != newItem.getCrystalType())
						return;
				}
				else if (newItem.getItemType() == EtcItemType.BAIT)
				{
					// baits can be equipped only with rods
					if (rHandItemTemplate == null)
						return;
					if (rHandItemTemplate.getItemType() != WeaponType.ROD)
						return;
					if (!getActor().isPlayer())
						return;

					Player owner = (Player) getActor();
					owner.setVar("LastLure", String.valueOf(item.getObjectId()), -1);
				}
				else
				{
					// unequip two-hand weapon
					if (rHandItemTemplate != null && rHandItemTemplate.getBodyPart() == ItemTemplate.SLOT_LR_HAND)
						setPaperdollItem(PAPERDOLL_RHAND, null);
				}

				setPaperdollItem(PAPERDOLL_LHAND, item);
				break;
			}
			case ItemTemplate.SLOT_R_HAND:
			{
				setPaperdollItem(PAPERDOLL_RHAND, item);
				break;
			}
			case ItemTemplate.SLOT_L_EAR:
			case ItemTemplate.SLOT_R_EAR:
			case ItemTemplate.SLOT_L_EAR | ItemTemplate.SLOT_R_EAR:
			{
				if (_paperdoll[PAPERDOLL_LEAR] == null)
					setPaperdollItem(PAPERDOLL_LEAR, item);
				else if (_paperdoll[PAPERDOLL_REAR] == null)
					setPaperdollItem(PAPERDOLL_REAR, item);
				else
					setPaperdollItem(PAPERDOLL_LEAR, item);
				break;
			}
			case ItemTemplate.SLOT_L_FINGER:
			case ItemTemplate.SLOT_R_FINGER:
			case ItemTemplate.SLOT_L_FINGER | ItemTemplate.SLOT_R_FINGER:
			{
				if (_paperdoll[PAPERDOLL_LFINGER] == null)
					setPaperdollItem(PAPERDOLL_LFINGER, item);
				else if (_paperdoll[PAPERDOLL_RFINGER] == null)
					setPaperdollItem(PAPERDOLL_RFINGER, item);
				else
					setPaperdollItem(PAPERDOLL_LFINGER, item);
				break;
			}
			case ItemTemplate.SLOT_NECK:
				setPaperdollItem(PAPERDOLL_NECK, item);
				break;
			case ItemTemplate.SLOT_FULL_ARMOR:
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			case ItemTemplate.SLOT_CHEST:
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			case ItemTemplate.SLOT_LEGS:
			{
				// handle full armor
				ItemInstance chest = getPaperdollItem(PAPERDOLL_CHEST);
				if (chest != null && chest.getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_FORMAL_WEAR)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_SCHOOL_UNIFORM_A)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_SCHOOL_UNIFORM_B)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_CHRISTMAS_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_KNIGHT_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_QUIPAO_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_NAVI_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_PIRAT_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_MUSKETEER_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_MAGICIAN_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_MAGICIAN_UPGRADED_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_NINJA_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_DARK_ASSASIN_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_METAL_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				setPaperdollItem(PAPERDOLL_LEGS, item);
				break;
			}

			case ItemTemplate.SLOT_FEET:
				if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_FORMAL_WEAR)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_SCHOOL_UNIFORM_A)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_SCHOOL_UNIFORM_B)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_CHRISTMAS_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_KNIGHT_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_QUIPAO_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_NAVI_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_PIRAT_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_MUSKETEER_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_MAGICIAN_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_MAGICIAN_UPGRADED_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_NINJA_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_DARK_ASSASIN_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_METAL_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				setPaperdollItem(PAPERDOLL_FEET, item);
				break;

			case ItemTemplate.SLOT_GLOVES:
				if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_FORMAL_WEAR)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_SCHOOL_UNIFORM_A)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_SCHOOL_UNIFORM_B)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_CHRISTMAS_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_KNIGHT_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_QUIPAO_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_NAVI_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_PIRAT_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_MUSKETEER_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_MAGICIAN_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_MAGICIAN_UPGRADED_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_NINJA_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_DARK_ASSASIN_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_METAL_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				setPaperdollItem(PAPERDOLL_GLOVES, item);
				break;

			case ItemTemplate.SLOT_HEAD:
				if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_FORMAL_WEAR)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_SCHOOL_UNIFORM_A)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_SCHOOL_UNIFORM_B)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_CHRISTMAS_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_KNIGHT_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_QUIPAO_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_NAVI_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_PIRAT_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_MUSKETEER_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_MAGICIAN_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_MAGICIAN_UPGRADED_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_NINJA_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_DARK_ASSASIN_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				else if (getPaperdollItemId(PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_METAL_UNIFORM)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				setPaperdollItem(PAPERDOLL_HEAD, item);
				break;

			case ItemTemplate.SLOT_HAIR:
				ItemInstance old = getPaperdollItem(PAPERDOLL_DHAIR);
				if (old != null && old.getBodyPart() == ItemTemplate.SLOT_HAIRALL)
					setPaperdollItem(PAPERDOLL_DHAIR, null);
				setPaperdollItem(PAPERDOLL_HAIR, item);
				break;

			case ItemTemplate.SLOT_DHAIR:
				ItemInstance slot2 = getPaperdollItem(PAPERDOLL_DHAIR);
				if (slot2 != null && slot2.getBodyPart() == ItemTemplate.SLOT_HAIRALL)
					setPaperdollItem(PAPERDOLL_HAIR, null);
				setPaperdollItem(PAPERDOLL_DHAIR, item);
				break;

			case ItemTemplate.SLOT_HAIRALL:
				setPaperdollItem(PAPERDOLL_HAIR, null);
				setPaperdollItem(PAPERDOLL_DHAIR, item);
				break;
			case ItemTemplate.SLOT_R_BRACELET:
				setPaperdollItem(PAPERDOLL_RBRACELET, item);
				break;
			case ItemTemplate.SLOT_L_BRACELET:
				setPaperdollItem(PAPERDOLL_LBRACELET, item);
				break;
			case ItemTemplate.SLOT_UNDERWEAR:
				setPaperdollItem(PAPERDOLL_UNDER, item);
				break;
			case ItemTemplate.SLOT_BACK:
				setPaperdollItem(PAPERDOLL_BACK, item);
				break;
			case ItemTemplate.SLOT_BELT:
				setPaperdollItem(PAPERDOLL_BELT, item);
				break;
			case ItemTemplate.SLOT_DECO:
				if (_paperdoll[PAPERDOLL_DECO1] == null)
					setPaperdollItem(PAPERDOLL_DECO1, item);
				else if (_paperdoll[PAPERDOLL_DECO2] == null)
					setPaperdollItem(PAPERDOLL_DECO2, item);
				else if (_paperdoll[PAPERDOLL_DECO3] == null)
					setPaperdollItem(PAPERDOLL_DECO3, item);
				else if (_paperdoll[PAPERDOLL_DECO4] == null)
					setPaperdollItem(PAPERDOLL_DECO4, item);
				else if (_paperdoll[PAPERDOLL_DECO5] == null)
					setPaperdollItem(PAPERDOLL_DECO5, item);
				else if (_paperdoll[PAPERDOLL_DECO6] == null)
					setPaperdollItem(PAPERDOLL_DECO6, item);
				else
					setPaperdollItem(PAPERDOLL_DECO1, item);
				break;

			case ItemTemplate.SLOT_FORMAL_WEAR:
				// When dressing the wedding dress hands do not touch
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_HEAD, null);
				setPaperdollItem(PAPERDOLL_FEET, null);
				setPaperdollItem(PAPERDOLL_GLOVES, null);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			case ItemTemplate.ITEM_ID_SCHOOL_UNIFORM_A:
				// When dressing the wedding dress hands do not touch
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_HEAD, null);
				setPaperdollItem(PAPERDOLL_FEET, null);
				setPaperdollItem(PAPERDOLL_GLOVES, null);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			case ItemTemplate.ITEM_ID_SCHOOL_UNIFORM_B:
				// When dressing the wedding dress hands do not touch
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_HEAD, null);
				setPaperdollItem(PAPERDOLL_FEET, null);
				setPaperdollItem(PAPERDOLL_GLOVES, null);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			case ItemTemplate.ITEM_ID_CHRISTMAS_UNIFORM:
				// When dressing the wedding dress hands do not touch
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_HEAD, null);
				setPaperdollItem(PAPERDOLL_FEET, null);
				setPaperdollItem(PAPERDOLL_GLOVES, null);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			case ItemTemplate.ITEM_ID_KNIGHT_UNIFORM:
				// When dressing the wedding dress hands do not touch
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_HEAD, null);
				setPaperdollItem(PAPERDOLL_FEET, null);
				setPaperdollItem(PAPERDOLL_GLOVES, null);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			case ItemTemplate.ITEM_ID_QUIPAO_UNIFORM:
				// When dressing the wedding dress hands do not touch
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_HEAD, null);
				setPaperdollItem(PAPERDOLL_FEET, null);
				setPaperdollItem(PAPERDOLL_GLOVES, null);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			case ItemTemplate.ITEM_ID_NAVI_UNIFORM:
				// When dressing the wedding dress hands do not touch
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_HEAD, null);
				setPaperdollItem(PAPERDOLL_FEET, null);
				setPaperdollItem(PAPERDOLL_GLOVES, null);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			case ItemTemplate.ITEM_ID_PIRAT_UNIFORM:
				// When dressing the wedding dress hands do not touch
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_HEAD, null);
				setPaperdollItem(PAPERDOLL_FEET, null);
				setPaperdollItem(PAPERDOLL_GLOVES, null);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			case ItemTemplate.ITEM_ID_MUSKETEER_UNIFORM:
				// When dressing the wedding dress hands do not touch
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_HEAD, null);
				setPaperdollItem(PAPERDOLL_FEET, null);
				setPaperdollItem(PAPERDOLL_GLOVES, null);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			case ItemTemplate.ITEM_ID_MAGICIAN_UNIFORM:
				// When dressing the wedding dress hands do not touch
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_HEAD, null);
				setPaperdollItem(PAPERDOLL_FEET, null);
				setPaperdollItem(PAPERDOLL_GLOVES, null);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			case ItemTemplate.ITEM_ID_MAGICIAN_UPGRADED_UNIFORM:
				// When dressing the wedding dress hands do not touch
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_HEAD, null);
				setPaperdollItem(PAPERDOLL_FEET, null);
				setPaperdollItem(PAPERDOLL_GLOVES, null);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			case ItemTemplate.ITEM_ID_NINJA_UNIFORM:
				// When dressing the wedding dress hands do not touch
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_HEAD, null);
				setPaperdollItem(PAPERDOLL_FEET, null);
				setPaperdollItem(PAPERDOLL_GLOVES, null);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			case ItemTemplate.ITEM_ID_DARK_ASSASIN_UNIFORM:
				// When dressing the wedding dress hands do not touch
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_HEAD, null);
				setPaperdollItem(PAPERDOLL_FEET, null);
				setPaperdollItem(PAPERDOLL_GLOVES, null);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			case ItemTemplate.ITEM_ID_METAL_UNIFORM:
				// When dressing the wedding dress hands do not touch
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_HEAD, null);
				setPaperdollItem(PAPERDOLL_FEET, null);
				setPaperdollItem(PAPERDOLL_GLOVES, null);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			default:
				_log.warn("unknown body slot:" + bodySlot + " for item id: " + item.getItemId());
				return;
		}

		//TODO [G1ta0] затычка на статы повышающие HP/MP/CP
		getActor().setCurrentHp(hp, false);
		getActor().setCurrentMp(mp);
		getActor().setCurrentCp(cp);

		if (getActor().isPlayer())
			((Player) getActor()).autoShot();
	}

	protected abstract void sendAddItem(ItemInstance item);

	protected abstract void sendModifyItem(ItemInstance item);

	protected abstract void sendRemoveItem(ItemInstance item);

	/**
	 * Refresh the weight of equipment loaded
	 */
	protected void refreshWeight()
	{
		int weight = 0;

		readLock();
		try
		{
			ItemInstance item;
			for (int i = 0; i < _items.size(); i++)
			{
				item = _items.get(i);
				weight += item.getTemplate().getWeight() * item.getCount();
			}
		}
		finally
		{
			readUnlock();
		}

		if (_totalWeight == weight)
			return;

		_totalWeight = weight;

		onRefreshWeight();
	}

	protected abstract void onRefreshWeight();

	public int getTotalWeight()
	{
		return _totalWeight;
	}

	public boolean validateCapacity(ItemInstance item)
	{
		long slots = 0;
		if (!item.isStackable() || getItemByItemId(item.getItemId()) == null)
			slots++;
		return validateCapacity(slots);
	}

	public boolean validateCapacity(int itemId, long count)
	{
		ItemTemplate item = ItemHolder.getInstance().getTemplate(itemId);
		return validateCapacity(item, count);
	}

	public boolean validateCapacity(ItemTemplate item, long count)
	{
		long slots = 0;
		if (!item.isStackable() || getItemByItemId(item.getItemId()) == null)
			slots = count;
		return validateCapacity(slots);
	}

	public boolean validateCapacity(long slots)
	{
		if (slots == 0)
			return true;
		if (slots < Integer.MIN_VALUE || slots > Integer.MAX_VALUE)
			return false;
		if (getSize() + (int) slots < 0)
			return false;
		return getSize() + slots <= getActor().getInventoryLimit();
	}

	public boolean validateWeight(ItemInstance item)
	{
		long weight = item.getTemplate().getWeight() * item.getCount();
		return validateWeight(weight);
	}

	public boolean validateWeight(int itemId, long count)
	{
		ItemTemplate item = ItemHolder.getInstance().getTemplate(itemId);
		return validateWeight(item, count);
	}

	public boolean validateWeight(ItemTemplate item, long count)
	{
		long weight = item.getWeight() * count;
		return validateWeight(weight);
	}

	public boolean validateWeight(long weight)
	{
		if (weight == 0L)
			return true;
		if (weight < Integer.MIN_VALUE || weight > Integer.MAX_VALUE)
			return false;
		if (getTotalWeight() + (int) weight < 0)
			return false;
		return getTotalWeight() + weight <= getActor().getMaxLoad();
	}

	public abstract void restore();

	public abstract void store();

	public static int getPaperdollIndex(int slot)
	{
		switch (slot)
		{
			case ItemTemplate.SLOT_UNDERWEAR:
				return PAPERDOLL_UNDER;
			case ItemTemplate.SLOT_R_EAR:
				return PAPERDOLL_REAR;
			case ItemTemplate.SLOT_L_EAR:
				return PAPERDOLL_LEAR;
			case ItemTemplate.SLOT_NECK:
				return PAPERDOLL_NECK;
			case ItemTemplate.SLOT_R_FINGER:
				return PAPERDOLL_RFINGER;
			case ItemTemplate.SLOT_L_FINGER:
				return PAPERDOLL_LFINGER;
			case ItemTemplate.SLOT_HEAD:
				return PAPERDOLL_HEAD;
			case ItemTemplate.SLOT_R_HAND:
				return PAPERDOLL_RHAND;
			case ItemTemplate.SLOT_L_HAND:
				return PAPERDOLL_LHAND;
			case ItemTemplate.SLOT_LR_HAND:
				return PAPERDOLL_LRHAND;
			case ItemTemplate.SLOT_GLOVES:
				return PAPERDOLL_GLOVES;
			case ItemTemplate.SLOT_CHEST:
			case ItemTemplate.SLOT_FULL_ARMOR:
			case ItemTemplate.SLOT_FORMAL_WEAR:
				return PAPERDOLL_CHEST;
			case ItemTemplate.SLOT_LEGS:
				return PAPERDOLL_LEGS;
			case ItemTemplate.SLOT_FEET:
				return PAPERDOLL_FEET;
			case ItemTemplate.SLOT_BACK:
				return PAPERDOLL_BACK;
			case ItemTemplate.SLOT_HAIR:
			case ItemTemplate.SLOT_HAIRALL:
				return PAPERDOLL_HAIR;
			case ItemTemplate.SLOT_DHAIR:
				return PAPERDOLL_DHAIR;
			case ItemTemplate.SLOT_R_BRACELET:
				return PAPERDOLL_RBRACELET;
			case ItemTemplate.SLOT_L_BRACELET:
				return PAPERDOLL_LBRACELET;
			case ItemTemplate.SLOT_DECO:
				return PAPERDOLL_DECO1; //return first we deal with it later
			case ItemTemplate.SLOT_BELT:
				return PAPERDOLL_BELT;
		}
		return -1;
	}

	@Override
	public int getSize()
	{
		return super.getSize() - getQuestSize();
	}

	public int getAllSize()
	{
		return super.getSize();
	}

	public int getQuestSize()
	{
		int size = 0;
		for (ItemInstance item : getItems())
			if (item.getTemplate().isQuest())
				size++;
		return size;
	}
}