//package services.community;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.util.Arrays;
//import java.util.Comparator;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.StringTokenizer;
//import java.util.concurrent.locks.ReentrantLock;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//
//import l2f.commons.dbutils.DbUtils;
//import l2f.gameserver.Config;
//import l2f.gameserver.cache.Msg;
//import l2f.gameserver.dao.ItemsDAO;
//import l2f.gameserver.database.DatabaseFactory;
//import l2f.gameserver.handler.bbs.CommunityBoardManager;
//import l2f.gameserver.handler.bbs.ICommunityBoardHandler;
//import l2f.gameserver.model.GameObjectsStorage;
//import l2f.gameserver.model.Player;
//import l2f.gameserver.model.base.Element;
//import l2f.gameserver.model.items.ItemInstance;
//import l2f.gameserver.network.serverpackets.ShowBoard;
//import l2f.gameserver.network.serverpackets.SystemMessage;
//import l2f.gameserver.scripts.ScriptFile;
//import l2f.gameserver.templates.item.ItemTemplate;
//import l2f.gameserver.templates.item.WeaponTemplate;
//import l2f.gameserver.utils.Files;
//import l2f.gameserver.utils.GArray;
//import l2f.gameserver.utils.GCSArray;
//import l2f.gameserver.utils.Util;
//
///**
// * @author: rage
// * @date: 30.05.2010 15:38:49
// */
//public class ColMarket implements ScriptFile, ICommunityBoardHandler
//{
//	private static final Logger _log = LoggerFactory.getLogger(ColMarket.class);
//	static final GCSArray<MarketContainer> _marketItems = new GCSArray<>();
//	private static final int ITEM_TIMEOUT = 7 * 24 * 60 * 60;
//	private static final int TRADERS_PER_PAGE = 8;
//	private static final long ITEM_PRICE = 10000000;
//	private static final long MIN_PRICE = 2;
//	private static final long MAX_ITEMS = 6;
//	private static final double TAX = 0.05;
//	static final int[] NO_TAX_ITEMS = new int[] {};
//	private static ReentrantLock addLock = new ReentrantLock();
//	private static ReentrantLock buyLock = new ReentrantLock();
//	
//	@Override
//	public void onLoad()
//	{
//		if (Config.COMMUNITYBOARD_ENABLED)
//		{
//			_log.info("CommunityBoard: CoL Market service loaded.");
//			CommunityBoardManager.getInstance().registerHandler(this);
//			cleanup();
//			restore();
//		}
//	}
//	
//	@Override
//	public void onReload()
//	{
//		if (Config.COMMUNITYBOARD_ENABLED)
//		{
//			CommunityBoardManager.getInstance().removeHandler(this);
//		}
//	}
//	
//	@Override
//	public void onShutdown()
//	{
//	}
//	
//	@Override
//	public String[] getBypassCommands()
//	{
//		return new String[]
//		{
//			"_cmlist_",
//			"_cmview_",
//			"_cmadd",
//			"_cmdelitem_",
//			"_cmsearch_",
//			"_cmaddpack",
//			"_cmdelpack_",
//			"_cmbuypack_",
//			"_cmgmreturn_",
//			"_cmgmremove_",
//			"_cmhelp"
//		};
//	}
//	
//	@Override
//	public void onBypassCommand(Player player, String bypass)
//	{
//		StringTokenizer st = new StringTokenizer(bypass, "_");
//		String cmd = st.nextToken();
//		
////		if (!player.getVarB("selected_language@") && Config.SHOW_LANG_SELECT_MENU)
////		{
////			String html = Files.read("data/scripts/services/community/html/langue_select.htm", player);
////			html = html.replace("<?page?>", bypass);
////			
////			ShowBoard.separateAndSend(html, player);
////		}
//	    if ("cmlist".equals(cmd))
//		{
//			int page = Integer.parseInt(st.nextToken());
//			int sort = Integer.parseInt(st.nextToken());
//			int desc = Integer.parseInt(st.nextToken());
//			int type = Integer.parseInt(st.nextToken());
//			String search = st.hasMoreTokens() ? st.nextToken().toLowerCase() : "";
//			player.setSessionVar("cmadd", null);
//			
//			GArray<MarketContainer> list = getList(sort, desc == 1, type, search);
//			
//			int start = (page - 1) * TRADERS_PER_PAGE;
//			int end = Math.min(page * TRADERS_PER_PAGE, list.size());
//			
//			HashMap<Integer, String> tpls;
//			tpls = Util.parseTemplate(Files.read(Config.BBS_HOME_DIR +  "cm_list.htm", player));
//			String html = tpls.get(0);
//			
//			if (page == 1)
//			{
//				html = html.replace("<?ACTION_GO_LEFT?>", "");
//				html = html.replace("<?GO_LIST?>", "");
//			}
//			else
//			{
//				html = html.replace("<?ACTION_GO_LEFT?>", "bypass -h _cmlist_" + (page - 1) + "_" + sort + "_" + desc + "_" + "_" + type + "_" + search);
//				StringBuilder goList = new StringBuilder("");
//				for (int i = page > 10 ? page - 10 : 1; i < page; i++)
//				{
//					goList.append("<td><a action=\"bypass -h _cmlist_").append(i).append("_").append(sort).append("_").append(desc).append("_").append(type).append("_").append(search).append("\"> ").append(i).append(" </a> </td>\n\n");
//				}
//				
//				html = html.replace("<?GO_LIST?>", goList.toString());
//			}
//			html = html.replace("<?NPAGE?>", String.valueOf(page));
//			
//			int pages = Math.max(list.size() / TRADERS_PER_PAGE, 1);
//			if (list.size() > (pages * TRADERS_PER_PAGE))
//			{
//				pages++;
//			}
//			
//			if (pages > page)
//			{
//				html = html.replace("<?ACTION_GO_RIGHT?>", "bypass -h _cmlist_" + (page + 1) + "_" + sort + "_" + desc + "_" + "_" + type + "_" + search);
//				int ep = Math.min(page + 10, pages);
//				StringBuilder goList = new StringBuilder("");
//				for (int i = page + 1; i <= ep; i++)
//				{
//					goList.append("<td><a action=\"bypass -h _cmlist_").append(i).append("_").append(sort).append("_").append(desc).append("_").append(type).append("_").append(search).append("\"> ").append(i).append(" </a> </td>\n\n");
//				}
//				
//				html = html.replace("<?GO_LIST2?>", goList.toString());
//			}
//			else
//			{
//				html = html.replace("<?ACTION_GO_RIGHT?>", "");
//				html = html.replace("<?GO_LIST2?>", "");
//			}
//			
//			StringBuilder mlist = new StringBuilder("");
//			String tpl = tpls.get(1);
//			
//			for (int i = start; i < end; i++)
//			{
//				MarketContainer mc = list.get(i);
//				String stpl = tpl.replace("<?icon?>", mc.getIcon());
//				stpl = stpl.replace("<?cm_view_bp?>", "_cmview_" + mc.getPosId() + "_" + page + "_" + sort + "_" + desc + "_" + type + "_" + search);
//				stpl = stpl.replace("<?cm_title?>", mc.getTitle());
//				stpl = stpl.replace("<?cm_descr?>", mc.getDescr());
//				stpl = stpl.replace("<?name?>", mc.getOwnerName());
//				stpl = stpl.replace("<?price?>", String.valueOf(mc.getPrice()));
//				stpl = stpl.replace("<?date?>", String.format("%1$te-%1$tm-%1$tY", new Date(mc.getPosDate())));
//				mlist.append(stpl);
//			}
//			
//			html = html.replace("<?CM_LIST?>", mlist.toString());
//			html = html.replace("<?search_bypass?>", "_cmsearch_" + sort + "_" + desc);
//			html = html.replace("<?cm_sort_p?>", "_cmlist_" + page + "_1_" + (desc == 0 ? 1 : 0) + "_" + type + "_" + search);
//			html = html.replace("<?cm_sort_d?>", "_cmlist_" + page + "_0_" + (desc == 0 ? 1 : 0) + "_" + type + "_" + search);
//			
//			ShowBoard.separateAndSend(html, player);
//		}
//		else if ("cmadd".equals(cmd))
//		{
//			GArray<ItemInstance> list = getItemList(player);
//			
//			HashMap<Integer, String> tpls;
//			tpls = Util.parseTemplate(Files.read(Config.BBS_HOME_DIR + "cm_add.htm", player));
//			String html = tpls.get(0);
//			String tpl = tpls.get(1);
//			StringBuilder sb = new StringBuilder("");
//			
//			for (ItemInstance item : list)
//			{
//				String stpl = tpl.replace("<?icon?>", item.getTemplate().getIcon());
//				stpl = stpl.replace("<?cm_title?>", item.getTemplate().getName() + (item.getEnchantLevel() > 0 ? " <font color=LEVEL>+" + item.getEnchantLevel() + "</font>" : ""));
//				stpl = stpl.replace("<?cm_descr?>", getItemDescr(item));
//				stpl = stpl.replace("<?object_id?>", String.valueOf(item.getObjectId()));
//				sb.append(stpl);
//			}
//			
//			html = html.replace("<?ITEM_LIST?>", sb.toString());
//			
//			sb = new StringBuilder("");
//			tpl = tpls.get(2);
//			String add = player.getSessionVar("cmadd");
//			if (add == null)
//			{
//				add = "";
//			}
//			
//			int c = 0;
//			for (String objId : add.split(";"))
//			{
//				if (objId.isEmpty())
//				{
//					continue;
//				}
//				int objectId = Integer.parseInt(objId);
//				ItemInstance item = player.getInventory().getItemByObjectId(objectId);
//				if (item == null)
//				{
//					add = add.replace(objId + ";", "");
//					continue;
//				}
//				
//				c++;
//				String stpl = tpl.replace("<?icon?>", item.getTemplate().getIcon());
//				stpl = stpl.replace("<?cm_title?>", item.getTemplate().getName() + (item.getEnchantLevel() > 0 ? " <font color=LEVEL>+" + item.getEnchantLevel() + "</font> " : ""));
//				stpl = stpl.replace("<?cm_descr?>", getItemDescr(item));
//				stpl = stpl.replace("<?object_id?>", String.valueOf(item.getObjectId()));
//				sb.append(stpl);
//			}
//			
//			html = html.replace("<?SEL_LIST?>", sb.toString());
//			html = html.replace("<?pack_price?>", String.format("%,3d", c * ITEM_PRICE).replace(" ", ","));
//			
//			ShowBoard.separateAndSend(html, player);
//		}
//		else if ("cmadditem".equals(cmd))
//		{
//			int objectId = Integer.parseInt(st.nextToken());
//			ItemInstance item = player.getInventory().getItemByObjectId(objectId);
//			addLock.lock();
//			try
//			{
//				if (item != null)
//				{
//					String add = player.getSessionVar("cmadd");
//					if (add == null)
//					{
//						add = "";
//					}
//					
//					if (add.split(";").length >= MAX_ITEMS)
//					{
//						String html = Files.read(Config.BBS_HOME_DIR + "cm_maxitems.htm", player);
//						ShowBoard.separateAndSend(html.replace("<?max_items?>", String.valueOf(MAX_ITEMS)), player);
//						return;
//					}
//					
//					if (!add.contains(String.valueOf(item.getObjectId())))
//					{
//						add += String.valueOf(item.getObjectId()) + ";";
//						player.setSessionVar("cmadd", add);
//					}
//				}
//			}
//			catch (Exception e)
//			{
//			}
//			finally
//			{
//				addLock.unlock();
//			}
//			
//			onBypassCommand(player, "_cmadd");
//		}
//		else if ("cmdelitem".equals(cmd))
//		{
//			String add = player.getSessionVar("cmadd");
//			if (add == null)
//			{
//				add = "";
//			}
//			add = add.replace(st.nextToken() + ";", "");
//			player.setSessionVar("cmadd", add);
//			onBypassCommand(player, "_cmadd");
//		}
//		else if ("cmview".equals(cmd))
//		{
//			int posId = Integer.parseInt(st.nextToken());
//			int page = Integer.parseInt(st.nextToken());
//			int sort = Integer.parseInt(st.nextToken());
//			int desc = Integer.parseInt(st.nextToken());
//			int type = Integer.parseInt(st.nextToken());
//			String search = st.hasMoreTokens() ? st.nextToken().toLowerCase() : "";
//			
//			MarketContainer mc = getMarketItemByPos(posId);
//			
//			if (mc != null)
//			{
//				HashMap<Integer, String> tpls;
//				tpls = Util.parseTemplate(Files.read(Config.BBS_HOME_DIR + "cm_view.htm", player));
//				String html = tpls.get(0);
//				String tpl = tpls.get(1);
//				StringBuilder sb = new StringBuilder("");
//				
//				for (ItemContainer ic : mc.getItems())
//				{
//					String stpl = tpl.replace("<?icon?>", ic.getIcon());
//					stpl = stpl.replace("<?cm_title?>", ic.getItemName() + (ic.enchant > 0 ? " <font color=LEVEL>+" + ic.enchant + "</font>" : ""));
//					stpl = stpl.replace("<?cm_descr?>", getItemDescr(ic));
//					sb.append(stpl);
//				}
//				
//				html = html.replace("<?ITEM_LIST?>", sb.toString());
//				html = html.replace("<?name?>", mc.getOwnerName());
//				html = html.replace("<?pos_date?>", String.format("%1$te-%1$tm-%1$tY", new Date(mc.getPosDate())));
//				html = html.replace("<?del_date?>", String.format("%1$te-%1$tm-%1$tY", new Date(mc.getPosDate() + (ITEM_TIMEOUT * 1000L))));
//				html = html.replace("<?del_date?>", String.format("%1$te-%1$tm-%1$tY", new Date(mc.getPosDate() + (ITEM_TIMEOUT * 1000L))));
//				html = html.replace("<?price?>", String.valueOf(mc.price));
//				
//				String btn;
//				if (mc.getOwnerId() == player.getObjectId())
//				{
//					btn = tpls.get(3).replace("<?cm_remove_bp?>", "_cmdelpack_" + mc.getPosId());
//				}
//				else
//				{
//					btn = tpls.get(2).replace("<?cm_buy_bp?>", "_cmbuypack_" + mc.getPosId());
//				}
//				
////				if (AdminTemplateManager.checkBoolean("marketAdmin", player))
////				{
////					String gm = tpls.get(4).replace("<?cm_gm_return?>", "_cmgmreturn_" + mc.getPosId());
////					gm = gm.replace("<?cm_gm_remove?>", "_cmgmremove_" + mc.getPosId());
////					btn = gm + btn;
////				}
//				
//				html = html.replace("<?button?>", btn);
//				html = html.replace("<?cm_list_bp?>", "_cmlist_" + page + "_" + sort + "_" + desc + "_" + type + "_" + search);
//				ShowBoard.separateAndSend(html, player);
//				return;
//			}
//			
//			onBypassCommand(player, "_cmlist_" + page + "_" + sort + "_" + desc + "_" + type + "_" + search);
//		}
//		else if ("cmdelpack".equals(cmd))
//		{
//			buyLock.lock();
//			try
//			{
//				int posId = Integer.parseInt(st.nextToken());
//				MarketContainer mc = getMarketItemByPos(posId);
//				if ((mc != null) && (mc.getOwnerId() == player.getObjectId()))
//				{
//					if (!player.getInventory().validateCapacity(1))
//					{
//						player.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
//						return;
//					}
//					
//					for (ItemContainer ic : mc.getItems())
//					{
//						ItemInstance item = ItemInstance.restoreFromDb(ic.objectId);
//						if (item == null)
//						{
//							continue;
//						}
//						
//						item.setLocation(ItemInstance.ItemLocation.INVENTORY);
//						ItemsDAO.getInstance().update(item);
//						player.sendPacket(getItemAddMessage(item));
//					}
//					MarketContainer.delete(mc);
//					_marketItems.remove(mc);
//					CommunityBoardManager.getInstance().setProperty("col_count", _marketItems.size());
//				}
//			}
//			catch (Exception e)
//			{
//			}
//			finally
//			{
//				buyLock.unlock();
//			}
//			onBypassCommand(player, "_cmlist_1_0_1_0_");
//		}
//		else if ("cmbuypack".equals(cmd))
//		{
//			buyLock.lock();
//			try
//			{
//				int posId = Integer.parseInt(st.nextToken());
//				MarketContainer mc = getMarketItemByPos(posId);
//				if (mc != null)
//				{
//					Player seller = GameObjectsStorage.getPlayer(mc.getOwnerId());
//					if (player.getInventory().destroyItemByItemId(4037, mc.getPrice(), "BuyMarket"))
//					{
//						long col = mc.getPrice();
//						if (seller != null)
//						{
//							seller.getInventory().addItem(4037, col, player.toString(), "MarketSell");
//						}
//						else
//						{
//							addOfflineItem(mc.getOwnerName(), 4037, col);
//						}
//						
//						for (ItemContainer ic : mc.getItems())
//						{
//							ItemInstance item = ItemInstance.restoreFromDb(ic.objectId);
//							if (item == null)
//							{
//								continue;
//							}
//							
//							item.setOwnerId(player.getObjectId());
//							item.setLocation(ItemInstance.ItemLocation.INVENTORY);
//							ItemsDAO.getInstance().update(item);
//							player.sendPacket(getItemAddMessage(item));
//						}
//						MarketContainer.delete(mc);
//						_marketItems.remove(mc);
//						CommunityBoardManager.getInstance().setProperty("col_count", _marketItems.size());
//					}
//				}
//			}
//			catch (Exception e)
//			{
//			}
//			finally
//			{
//				buyLock.unlock();
//			}
//			onBypassCommand(player, "_cmlist_1_0_1_0_");
//		}
////		else if ("cmgmreturn".equals(cmd))
////		{
////			int posId = Integer.parseInt(st.nextToken());
////			if (AdminTemplateManager.checkBoolean("marketAdmin", player))
////			{
////				MarketContainer mc = getMarketItemByPos(posId);
////				if (mc != null)
////				{
////					for (ItemContainer ic : mc.getItems())
////					{
////						L2ItemInstance item = L2ItemInstance.restoreFromDb(ic.objectId);
////						if (item == null)
////						{
////							continue;
////						}
////						
////						item.changeLocation("ReturnByGmMarket", L2ItemInstance.ItemLocation.WAREHOUSE, player, null);
////						item.updateDatabase(true);
////					}
////					_marketItems.remove(mc);
////					MarketContainer.delete(mc);
////				}
////			}
////			onBypassCommand(player, "_cmlist_1_0_1_0_");
////		}
////		else if ("cmgmremove".equals(cmd))
////		{
////			int posId = Integer.parseInt(st.nextToken());
////			if (AdminTemplateManager.checkBoolean("marketAdmin", player))
////			{
////				MarketContainer mc = getMarketItemByPos(posId);
////				if (mc != null)
////				{
////					for (ItemContainer ic : mc.getItems())
////					{
////						L2ItemInstance item = L2ItemInstance.restoreFromDb(ic.objectId);
////						if (item == null)
////						{
////							continue;
////						}
////						
////						item.setOwnerId(player.getObjectId());
////						item.changeLocation("RemoveByGmMarket", L2ItemInstance.ItemLocation.INVENTORY, player, null);
////						player.getInventory().resetItem(item);
////						player.sendPacket(getItemAddMessage(item));
////					}
////					_marketItems.remove(mc);
////					MarketContainer.delete(mc);
////				}
////			}
////			onBypassCommand(player, "_cmlist_1_0_1_0_");
////		}
//		else if ("cmhelp".equals(cmd))
//		{
//			String html = Files.read(Config.BBS_HOME_DIR + "cm_help.htm", player);
//			html = html.replace("<?item_price?>", String.format("%,3d", ITEM_PRICE).replace(" ", ","));
//			html = html.replace("<?tax?>", String.format("%.1f", 100 * TAX));
//			html = html.replace("<?days?>", String.valueOf(ITEM_TIMEOUT / 60 / 60 / 24));
//			html = html.replace("<?min_tax?>", "1");
//			ShowBoard.separateAndSend(html, player);
//		}
//	}
//	
//	@Override
//	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
//	{
//		StringTokenizer st = new StringTokenizer(bypass, "_");
//		String cmd = st.nextToken();
//		if ("cmsearch".equals(cmd))
//		{
//			int sort = Integer.parseInt(st.nextToken());
//			int desc = Integer.parseInt(st.nextToken());
//			int sType = "Item".equals(arg4) ? 0 : 1;
//			
//			if (arg3 == null)
//			{
//				arg3 = "";
//			}
//			
//			arg3 = arg3.replace("<", "");
//			arg3 = arg3.replace(">", "");
//			arg3 = arg3.replace("&", "");
//			arg3 = arg3.replace("$", "");
//			
//			if (arg3.length() > 30)
//			{
//				arg3 = arg3.substring(0, 30);
//			}
//			
//			onBypassCommand(player, "_cmlist_1_" + sort + "_" + desc + "_" + sType + "_" + arg3);
//		}
//		else if ("cmaddpack".equals(cmd))
//		{
//			long price = Long.parseLong(arg3);
//			GArray<ItemInstance> list = new GArray<>(1);
//			
//			addLock.lock();
//			try
//			{
//				String add = player.getSessionVar("cmadd");
//				if (add == null)
//				{
//					add = "";
//				}
//				
//				boolean noTax = true;
//				for (String objId : add.split(";"))
//				{
//					if (objId.isEmpty())
//					{
//						continue;
//					}
//					int objectId = Integer.parseInt(objId);
//					ItemInstance item = player.getInventory().getItemByObjectId(objectId);
//					if ((item == null) || item.isEquipped() || !item.canBeTraded(player) || list.contains(item))
//					{
//						continue;
//					}
//					
//					list.add(item);
////					if (noTax)
////					{
////						noTax = ArrayUtils.contains(NO_TAX_ITEMS, item.getTemplate().isDP());
////					}
//				}
//				
//				if (noTax && (price < 1))
//				{
//					String html = Files.read(Config.BBS_HOME_DIR + "cm_minprice.htm", player);
//					ShowBoard.separateAndSend(html.replace("<?min_price?>", "1"), player);
//					return;
//				}
//				
//				if ((price < MIN_PRICE) && !noTax)
//				{
//					String html = Files.read(Config.BBS_HOME_DIR + "cm_minprice.htm", player);
//					ShowBoard.separateAndSend(html.replace("<?min_price?>", String.valueOf(MIN_PRICE)), player);
//					return;
//				}
//				
//				if ((price > 0) && (list.size() > 0) && player.reduceAdena(list.size() * ITEM_PRICE, null))
//				{
//					MarketContainer mc = new MarketContainer(player, price);
//					for (ItemInstance item : list)
//					{
//						item.setLocation(ItemInstance.ItemLocation.AUCTION);
//						player.getInventory().removeItem(item, "removed");
//						player.sendPacket(getItemDelMessage(item));
//						mc.addItem(item);
//					}
//					MarketContainer.store(mc);
//					_marketItems.add(mc);
//					CommunityBoardManager.getInstance().setProperty("col_count", _marketItems.size());
//				}
//			}
//			catch (Exception e)
//			{
//			}
//			finally
//			{
//				addLock.unlock();
//			}
//			onBypassCommand(player, "_cmlist_1_0_1_0_");
//		}
//	}
//	
//	private MarketContainer getMarketItemByPos(int posId)
//	{
//		for (MarketContainer mc : _marketItems)
//		{
//			if (mc.getPosId() == posId)
//			{
//				return mc;
//			}
//		}
//		
//		return null;
//	}
//	
//	@SuppressWarnings("static-access")
//	public String getItemDescr(ItemInstance item)
//	{
//		String desc = "";
//		if (item.getTemplate().ATTRIBUTE_FIRE > 0)
//		{
//			desc += "&$1651;: &$1622; +" + item.getTemplate().ATTRIBUTE_FIRE + ";";
//		}
//		if (item.getTemplate().ATTRIBUTE_WATER > 0)
//		{
//			desc += "&$1651;: &$1623; +" + item.getTemplate().ATTRIBUTE_WATER + ";";
//		}
//		if (item.getTemplate().ATTRIBUTE_WIND > 0)
//		{
//			desc += "&$1651;: &$1624; +" + item.getTemplate().ATTRIBUTE_WIND + ";";
//		}
//		if (item.getTemplate().ATTRIBUTE_EARTH > 0)
//		{
//			desc += "&$1651;: &$1625; +" + item.getTemplate().ATTRIBUTE_EARTH + ";";
//		}
//		if (item.getTemplate().ATTRIBUTE_HOLY > 0)
//		{
//			desc += "&$1651;: &$1626; +" + item.getTemplate().ATTRIBUTE_HOLY + ";";
//		}
//		if (item.getTemplate().ATTRIBUTE_DARK > 0)
//		{
//			desc += "&$1651;: &$1627; +" + item.getTemplate().ATTRIBUTE_DARK + ";";
//		}
//		
//		if (desc.isEmpty())
//		{
//			desc = "no attribute enchant.";
//		}
//		
//		return desc;
//	}
//	
//	public String getItemDescr(ItemContainer ic)
//	{
//		String desc = "";
//		if (ic.fire > 0)
//		{
//			desc += (ic.itemTemplate instanceof WeaponTemplate ? " &$1620;: " : "&$1651;: ") + "&$1622; +" + ic.fire + ";";
//		}
//		if (ic.water > 0)
//		{
//			desc += (ic.itemTemplate instanceof WeaponTemplate ? " &$1620;: " : "&$1651;: ") + "&$1623; +" + ic.water + ";";
//		}
//		if (ic.wind > 0)
//		{
//			desc += (ic.itemTemplate instanceof WeaponTemplate ? " &$1620;: " : "&$1651;: ") + "&$1624; +" + ic.wind + ";";
//		}
//		if (ic.earth > 0)
//		{
//			desc += (ic.itemTemplate instanceof WeaponTemplate ? " &$1620;: " : "&$1651;: ") + "&$1625; +" + ic.earth + ";";
//		}
//		if (ic.holy > 0)
//		{
//			desc += (ic.itemTemplate instanceof WeaponTemplate ? " &$1620;: " : "&$1651;: ") + "&$1626; +" + ic.holy + ";";
//		}
//		if (ic.dark > 0)
//		{
//			desc += (ic.itemTemplate instanceof WeaponTemplate ? " &$1620;: " : "&$1651;: ") + "&$1627; +" + ic.dark + ";";
//		}
//		
//		if (desc.isEmpty())
//		{
//			desc = "no attribute enchant.";
//		}
//		
//		return desc;
//	}
//	
//	public GArray<ItemInstance> getItemList(Player player)
//	{
//		GArray<ItemInstance> list = new GArray<>();
//		String add = player.getSessionVar("cmadd");
//		if (add == null)
//		{
//			add = "";
//		}
//		
//		for (ItemInstance item : player.getInventory().getItems())
//		{
//			if (item.isEquipable() && !item.isEquipped() && item.canBeTraded(player) && !item.isStackable() && !item.getTemplate().getName().contains("Common") && !add.contains(String.valueOf(item.getObjectId())))
//			{
//				list.add(item);
//			}
//		}
//		
//		return list;
//	}
//	
//	private GArray<MarketContainer> getList(int sort, boolean desc, int sType, String search)
//	{
//		GArray<MarketContainer> list = new GArray<>();
//		
//		if (!search.isEmpty())
//		{
//			for (MarketContainer mc : _marketItems)
//			{
//				if (mc.search(sType, search))
//				{
//					list.add(mc);
//				}
//			}
//		}
//		else
//		{
//			list.addAll(_marketItems);
//		}
//		
//		if (!list.isEmpty())
//		{
//			if (sort == 0) // By date
//			{
//				MarketContainer[] sList = new MarketContainer[list.size()];
//				list.toArray(sList);
//				Arrays.sort(sList, new SortByDate<MarketContainer>(desc));
//				list.clear();
//				list.addAll(Arrays.asList(sList));
//			}
//			else if (sort == 1) // By price
//			{
//				MarketContainer[] sList = new MarketContainer[list.size()];
//				list.toArray(sList);
//				Arrays.sort(sList, new SortByPrice<MarketContainer>(desc));
//				list.clear();
//				list.addAll(Arrays.asList(sList));
//			}
//		}
//		
//		return list;
//	}
//	
//	private void addOfflineItem(String charName, int itemId, long count)
//	{
//		Connection con = null;
//		PreparedStatement statement = null;
//		ResultSet rset = null;
//		try
//		{
//			con = DatabaseFactory.getInstance().getConnection();
//			statement = con.prepareStatement("INSERT INTO transfer(itemid, amount, char_name, pay_id, status, stdt) VALUES(?, ?, ?, ?, ?, now())");
//			statement.setInt(1, itemId);
//			statement.setLong(2, count);
//			statement.setString(3, charName);
//			statement.setInt(4, 0);
//			statement.setInt(5, 0);
//			statement.execute();
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//		finally
//		{
//			DbUtils.closeQuietly(con, statement, rset);
//		}
//	}
//	
//	private MarketContainer getMarketContainerByPackage(int ownerId, int packId)
//	{
//		for (MarketContainer mc : _marketItems)
//		{
//			if ((mc.getOwnerId() == ownerId) && (mc.getPackageId() == packId))
//			{
//				return mc;
//			}
//		}
//		
//		return null;
//	}
//	
//	private void cleanup()
//	{
//		Connection con = null;
//		PreparedStatement statement = null;
//		ResultSet rset = null;
//		try
//		{
//			con = DatabaseFactory.getInstance().getConnection();
//			statement = con.prepareStatement("DELETE FROM col_market WHERE pos_date <= ?");
//			statement.setInt(1, (int) (System.currentTimeMillis() / 1000) - ITEM_TIMEOUT);
//			statement.execute();
//			statement.close();
//			
//			statement = con.prepareStatement("UPDATE items SET loc = 'WAREHOUSE' WHERE loc = 'MARKET' and object_id NOT IN (SELECT object_id FROM col_market)");
//			statement.execute();
//			statement.close();
//			
//			statement = con.prepareStatement("DELETE FROM col_market WHERE object_id NOT IN (SELECT object_id FROM items WHERE loc='MARKET')");
//			statement.execute();
//		}
//		catch (Exception e)
//		{
//		}
//		finally
//		{
//			DbUtils.closeQuietly(con, statement, rset);
//		}
//	}
//	
//	private SystemMessage getItemAddMessage(ItemInstance item)
//	{
//		SystemMessage sm;
//		if (item.getItemId() == 57)
//		{
//			sm = new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S1_ADENA);
//			sm.addNumber(item.getCount());
//		}
//		else if (item.getCount() == 1)
//		{
//			sm = new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S1);
//			sm.addItemName(item.getItemId());
//		}
//		else
//		{
//			sm = new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S2_S1S);
//			sm.addItemName(item.getItemId());
//			sm.addNumber(item.getCount());
//		}
//		return sm;
//	}
//	
//	private SystemMessage getItemDelMessage(ItemInstance item)
//	{
//		SystemMessage sm;
//		if (item.getCount() > 1)
//		{
//			sm = new SystemMessage(SystemMessage.S2_S1_HAS_DISAPPEARED);
//			sm.addItemName(item.getItemId());
//			sm.addNumber(item.getCount());
//		}
//		else
//		{
//			sm = new SystemMessage(SystemMessage.S1_HAS_DISAPPEARED);
//			sm.addItemName(item.getItemId());
//		}
//		return sm;
//	}
//	
//	private void restore()
//	{
//		Connection con = null;
//		PreparedStatement statement = null;
//		ResultSet rset = null;
//		try
//		{
//			con = DatabaseFactory.getInstance().getConnection();
//			statement = con.prepareStatement("SELECT\n" + "  cm.*,\n" + "  i.item_id,i.`count`,i.enchant_level,i.attribute_fire,i.attribute_water,i.attribute_wind,i.attribute_earth,i.attribute_unholy,i.attribute_holy,i.owner_id,\n" + "  c.char_name\n" + "FROM\n" + "  col_market cm\n" + "    INNER JOIN items i ON(cm.object_id = i.object_id)  \n" + "    INNER JOIN characters c on(i.owner_id = c.obj_id)\n" + "ORDER BY cm.pos_date DESC");
//			rset = statement.executeQuery();
//			
//			while (rset.next())
//			{
//				MarketContainer mc;
//				int packId = rset.getInt("package");
//				if (packId > 0)
//				{
//					mc = getMarketContainerByPackage(rset.getInt("owner_id"), packId);
//					if (mc == null)
//					{
//						mc = MarketContainer.restore(rset);
//					}
//					else
//					{
//						mc.addItem(rset.getInt("object_id"), rset.getInt("item_id"), rset.getInt("enchant_level"), rset.getInt("attribute_fire"), rset.getInt("attribute_water"), rset.getInt("attribute_wind"), rset.getInt("attribute_earth"), rset.getInt("attribute_unholy"), rset.getInt("attribute_holy"), rset.getLong("count"));
//						continue;
//					}
//					
//				}
//				else
//				{
//					mc = MarketContainer.restore(rset);
//				}
//				
//				if (mc != null)
//				{
//					_marketItems.add(mc);
//				}
//			}
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//		finally
//		{
//			DbUtils.closeQuietly(con, statement, rset);
//		}
//		_log.info("CommunityBoard: CoL Market restored " + _marketItems.size() + " packages.");
//		CommunityBoardManager.getInstance().setProperty("col_count", _marketItems.size());
//	}
//	
//	private static class MarketContainer
//	{
//		private int posId;
//		private long posDate;
//		private int ownerId;
//		long price;
//		private int packageId;
//		private String ownerName;
//		private final GArray<ItemContainer> items;
//		
//		public MarketContainer()
//		{
//			items = new GArray<>();
//		}
//		
//		public MarketContainer(Player owner, long _price)
//		{
//			ownerId = owner.getObjectId();
//			ownerName = owner.getName();
//			price = _price;
//			items = new GArray<>();
//			posDate = System.currentTimeMillis();
//		}
//		
//		public static MarketContainer restore(ResultSet rset)
//		{
//			MarketContainer mc = null;
//			try
//			{
//				mc = new MarketContainer();
//				mc.posId = rset.getInt("pos_id");
//				mc.posDate = rset.getInt("pos_date") * 1000L;
//				mc.ownerId = rset.getInt("owner_id");
//				mc.price = rset.getLong("price");
//				mc.packageId = rset.getInt("package");
//				mc.ownerName = rset.getString("char_name");
//				mc.items.add(new ItemContainer(rset.getInt("object_id"), rset.getInt("item_id"), rset.getInt("enchant_level"), rset.getInt("attribute_fire"), rset.getInt("attribute_water"), rset.getInt("attribute_wind"), rset.getInt("attribute_earth"), rset.getInt("attribute_unholy"), rset.getInt("attribute_holy"), rset.getLong("count")));
//			}
//			catch (Exception e)
//			{
//				_log.warn("ColMarket: restore error: " + e);
//				e.printStackTrace();
//			}
//			
//			return mc;
//		}
//		
//		public int getOwnerId()
//		{
//			return ownerId;
//		}
//		
//		public int getPackageId()
//		{
//			return packageId;
//		}
//		
//		public long getPosDate()
//		{
//			return posDate;
//		}
//		
//		public int getPosId()
//		{
//			return posId;
//		}
//		
////		public boolean isNoTax()
////		{
////			for (ItemContainer ic : items)
////			{
////				if (!ArrayUtils.contains(NO_TAX_ITEMS, ic.itemId))
////				{
////					return false;
////				}
////			}
////			
////			return true;
////		}
//		
//		public long getPrice()
//		{
//			return price;
//		}
//		
//		public String getTitle()
//		{
//			if (items.size() > 1)
//			{
//				String ret = "";
//				for (ItemContainer ic : items)
//				{
//					ret += ic.getItemName() + (ic.enchant > 0 ? " <font color=LEVEL>+" + ic.enchant + "</font>" : "") + ";";
//				}
//				return ret;
//			}
//			
//			ItemContainer ic = items.get(0);
//			return ic.getItemName() + (ic.enchant > 0 ? " <font color=LEVEL>+" + ic.enchant + "</font>" : "");
//		}
//		
//		public GArray<ItemContainer> getItems()
//		{
//			return items;
//		}
//		
//		public String getDescr()
//		{
//			if (items.size() > 1)
//			{
//				String ret = "";
//				for (ItemContainer ic : items)
//				{
//					if ((ic.fire > 0) || (ic.water > 0) || (ic.wind > 0) || (ic.earth > 0) || (ic.holy > 0) || (ic.dark > 0))
//					{
//						ret += ic.getItemName() + " ";
//					}
//					
//					if (ic.fire > 0)
//					{
//						ret += (ic.itemTemplate instanceof WeaponTemplate ? " &$1620;: " : "&$1651;: ") + "&$1622; +" + ic.fire + ";";
//					}
//					if (ic.water > 0)
//					{
//						ret += (ic.itemTemplate instanceof WeaponTemplate ? " &$1620;: " : "&$1651;: ") + "&$1623; +" + ic.water + ";";
//					}
//					if (ic.wind > 0)
//					{
//						ret += (ic.itemTemplate instanceof WeaponTemplate ? " &$1620;: " : "&$1651;: ") + "&$1624; +" + ic.wind + ";";
//					}
//					if (ic.earth > 0)
//					{
//						ret += (ic.itemTemplate instanceof WeaponTemplate ? " &$1620;: " : "&$1651;: ") + "&$1625; +" + ic.earth + ";";
//					}
//					if (ic.holy > 0)
//					{
//						ret += (ic.itemTemplate instanceof WeaponTemplate ? " &$1620;: " : "&$1651;: ") + "&$1626; +" + ic.holy + ";";
//					}
//					if (ic.dark > 0)
//					{
//						ret += (ic.itemTemplate instanceof WeaponTemplate ? " &$1620;: " : "&$1651;: ") + "&$1627; +" + ic.dark + ";";
//					}
//					
//					if ((ic.fire > 0) || (ic.water > 0) || (ic.wind > 0) || (ic.earth > 0) || (ic.holy > 0) || (ic.dark > 0))
//					{
//						ret += " ";
//					}
//				}
//				
//				return ret;
//			}
//			
//			ItemContainer ic = items.get(0);
//			String desc = "";
//			if (ic.fire > 0)
//			{
//				desc += (ic.itemTemplate instanceof WeaponTemplate ? " &$1620;: " : "&$1651;: ") + "&$1622; +" + ic.fire + ";";
//			}
//			if (ic.water > 0)
//			{
//				desc += (ic.itemTemplate instanceof WeaponTemplate ? " &$1620;: " : "&$1651;: ") + "&$1623; +" + ic.water + ";";
//			}
//			if (ic.wind > 0)
//			{
//				desc += (ic.itemTemplate instanceof WeaponTemplate ? " &$1620;: " : "&$1651;: ") + "&$1624; +" + ic.wind + ";";
//			}
//			if (ic.earth > 0)
//			{
//				desc += (ic.itemTemplate instanceof WeaponTemplate ? " &$1620;: " : "&$1651;: ") + "&$1625; +" + ic.earth + ";";
//			}
//			if (ic.holy > 0)
//			{
//				desc += (ic.itemTemplate instanceof WeaponTemplate ? " &$1620;: " : "&$1651;: ") + "&$1626; +" + ic.holy + ";";
//			}
//			if (ic.dark > 0)
//			{
//				desc += (ic.itemTemplate instanceof WeaponTemplate ? " &$1620;: " : "&$1651;: ") + "&$1627; +" + ic.dark + ";";
//			}
//			
//			if (desc.isEmpty())
//			{
//				desc = "no attribute enchant.";
//			}
//			
//			return desc;
//		}
//		
//		public boolean search(int sType, String search)
//		{
//			if (sType == 0)
//			{
//				for (ItemContainer ic : items)
//				{
//					if (ic.getItemName().toLowerCase().contains(search))
//					{
//						return true;
//					}
//				}
//				return false;
//			}
//			else if (ownerName.toLowerCase().contains(search))
//			{
//				return true;
//			}
//			
//			return false;
//		}
//		
//		public String getIcon()
//		{
//			return items.size() > 1 ? "icon.etc_pouch_yellow_i00" : items.get(0).getIcon();
//		}
//		
//		public void addItem(int _objectId, int _itemId, int _enchant, int _fire, int _water, int _wind, int _earth, int _dark, int _holy, long _count)
//		{
//			items.add(new ItemContainer(_objectId, _itemId, _enchant, _fire, _water, _wind, _earth, _dark, _holy, _count));
//		}
//		
//		public void addItem(ItemInstance item)
//		{
//			items.add(new ItemContainer(item));
//		}
//		
//		public String getOwnerName()
//		{
//			return ownerName;
//		}
//		
//		public static void store(MarketContainer mc)
//		{
//			if (mc.items.size() > 1)
//			{
//				int newPack = 1;
//				for (MarketContainer mcc : _marketItems)
//				{
//					if ((mcc.getOwnerId() == mc.getOwnerId()) && (mcc.packageId >= newPack))
//					{
//						newPack = mcc.packageId + 1;
//					}
//				}
//				mc.packageId = newPack;
//			}
//			
//			Connection con = null;
//			PreparedStatement stmt = null;
//			ResultSet rset = null;
//			try
//			{
//				con = DatabaseFactory.getInstance().getConnection();
//				stmt = con.prepareStatement("INSERT INTO col_market(`object_id`, `price`, `package`, `pos_date`) VALUES(?, ?, ?, ?)");
//				int ts = (int) (System.currentTimeMillis() / 1000);
//				for (ItemContainer ic : mc.items)
//				{
//					stmt.setInt(1, ic.objectId);
//					stmt.setLong(2, mc.price);
//					stmt.setInt(3, mc.packageId);
//					stmt.setInt(4, ts);
//					stmt.execute();
//				}
//				stmt.close();
//				
//				stmt = con.prepareStatement("SELECT pos_id FROM col_market WHERE object_id = ?");
//				stmt.setInt(1, mc.items.get(0).objectId);
//				rset = stmt.executeQuery();
//				if (rset.next())
//				{
//					mc.posId = rset.getInt("pos_id");
//				}
//			}
//			catch (Exception e)
//			{
//				e.printStackTrace();
//			}
//			finally
//			{
//				DbUtils.closeQuietly(con, stmt, rset);
//			}
//		}
//		
//		public static void delete(MarketContainer mc)
//		{
//			Connection con = null;
//			PreparedStatement stmt = null;
//			try
//			{
//				con = DatabaseFactory.getInstance().getConnection();
//				stmt = con.prepareStatement("DELETE FROM col_market WHERE object_id = ?");
//				for (ItemContainer ic : mc.items)
//				{
//					stmt.setInt(1, ic.objectId);
//					stmt.execute();
//				}
//			}
//			catch (Exception e)
//			{
//			}
//			finally
//			{
//				DbUtils.closeQuietly(con, stmt);
//			}
//		}
//	}
//			
//	@SuppressWarnings("unused")
//	private static class ItemContainer
//	{
//		public int objectId;
//		public int itemId;
//		public int enchant;
//		public int fire;
//		public int water;
//		public int wind;
//		public int earth;
//		public int dark;
//		public int holy;
//		public long count;
//		public ItemTemplate itemTemplate;
//		
//		public ItemContainer(ItemInstance item)
//		{
//			objectId = item.getObjectId();
//			itemId = item.getItemId();
//			enchant = item.getEnchantLevel();
//			fire = item.getAttributeElementValue(Element.FIRE, false);
//			water = item.getAttributeElementValue(Element.WATER, false);
//			wind = item.getAttributeElementValue(Element.WIND, false);
//			earth = item.getAttributeElementValue(Element.EARTH, false);
//			dark = item.getAttributeElementValue(Element.UNHOLY, false);
//			holy = item.getAttributeElementValue(Element.HOLY, false);
//			count = item.getCount();
//			itemTemplate = item.getTemplate();
//		}
//		
//		public ItemContainer(int _objectId, int _itemId, int _enchant, int _fire, int _water, int _wind, int _earth, int _dark, int _holy, long _count)
//		{
//			objectId = _objectId;
//			itemId = _itemId;
//			enchant = _enchant;
//			fire = _fire;
//			water = _water;
//			wind = _wind;
//			earth = _earth;
//			dark = _dark;
//			holy = _holy;
//			count = _count;
////			itemTemplate = ii.getTemplate();
//		}
//		
//		public String getItemName()
//		{
//			return itemTemplate.getName();
//		}
//		
//		public String getIcon()
//		{
//			return itemTemplate.getIcon();
//		}
//	}
//	
//	private static class SortByDate<T> implements Comparator<T>
//	{
//		public boolean _desc;
//		
//		public SortByDate(boolean desc)
//		{
//			_desc = desc;
//		}
//		
//		@Override
//		public int compare(Object o1, Object o2)
//		{
//			if ((o1 instanceof MarketContainer) && (o2 instanceof MarketContainer))
//			{
//				MarketContainer m1 = (MarketContainer) o1;
//				MarketContainer m2 = (MarketContainer) o2;
//				if (m1.getPosDate() > m2.getPosDate())
//				{
//					return _desc ? -1 : 1;
//				}
//				if (m1.getPosDate() < m2.getPosDate())
//				{
//					return _desc ? 1 : -1;
//				}
//				return 0;
//			}
//			return 0;
//		}
//	}
//	
//	private static class SortByPrice<T> implements Comparator<T>
//	{
//		public boolean _desc;
//		
//		public SortByPrice(boolean desc)
//		{
//			_desc = desc;
//		}
//		
//		@Override
//		public int compare(Object o1, Object o2)
//		{
//			if ((o1 instanceof MarketContainer) && (o2 instanceof MarketContainer))
//			{
//				MarketContainer m1 = (MarketContainer) o1;
//				MarketContainer m2 = (MarketContainer) o2;
//				if (m1.getPrice() > m2.getPrice())
//				{
//					return _desc ? -1 : 1;
//				}
//				if (m1.getPrice() < m2.getPrice())
//				{
//					return _desc ? 1 : -1;
//				}
//				return 0;
//			}
//			return 0;
//		}
//	}
//}
