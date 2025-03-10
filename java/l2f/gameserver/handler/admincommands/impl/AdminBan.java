package l2f.gameserver.handler.admincommands.impl;

import l2f.gameserver.Announcements;
import l2f.gameserver.Config;
import l2f.gameserver.handler.admincommands.IAdminCommandHandler;
import l2f.gameserver.model.GameObjectsStorage;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.World;
import l2f.gameserver.model.items.ManufactureItem;
import l2f.gameserver.model.items.TradeItem;
import l2f.gameserver.network.GameClient;
import l2f.gameserver.network.loginservercon.AuthServerCommunication;
import l2f.gameserver.network.loginservercon.gspackets.ChangeAccessLevel;
import l2f.gameserver.network.serverpackets.NpcHtmlMessage;
import l2f.gameserver.network.serverpackets.Say2;
import l2f.gameserver.network.serverpackets.components.ChatType;
import l2f.gameserver.network.serverpackets.components.CustomMessage;
import l2f.gameserver.scripts.Functions;
import l2f.gameserver.security.HWIDBan;
import l2f.gameserver.utils.AutoBan;
import l2f.gameserver.utils.Log;
import l2f.gameserver.utils.TimeUtils;

import java.sql.SQLException;
import java.util.Collection;
import java.util.StringTokenizer;

import org.strixplatform.managers.ClientBanManager;
import org.strixplatform.utils.BannedHWIDInfo;

public class AdminBan implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_ban,
		admin_unban,
		admin_cban,
		admin_chatban,
		admin_chatunban,
		admin_banip,
		admin_accban,
		admin_accunban,
		admin_accban_hwid,
		admin_trade_ban,
		admin_trade_unban,
		admin_jail,
		admin_unjail,
		admin_permaban,
		admin_ban_hwid,
		admin_unban_hwid
	}

	@SuppressWarnings(
	{
		"incomplete-switch",
		"rawtypes"
	})
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		StringTokenizer st = new StringTokenizer(fullString);

		if (activeChar.getPlayerAccess().CanTradeBanUnban)
		{
			switch (command)
			{
				case admin_trade_ban:
					return tradeBan(st, activeChar);
				case admin_trade_unban:
					return tradeUnban(st, activeChar);
			}
		}

		if (activeChar.getPlayerAccess().CanBan)
		{
			switch (command)
			{
				case admin_ban:
					ban(st, activeChar);
					break;
				case admin_accban:
				{
					st.nextToken();

					int level = 0;
					int banExpire = 0;

					String account = st.nextToken();

					if (st.hasMoreTokens())
					{
						banExpire = (int) (System.currentTimeMillis() / 1000L) + (Integer.parseInt(st.nextToken()) * 60);
					}
					else
					{
						level = -100;
					}

					AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(account, level, banExpire));
					GameClient client = AuthServerCommunication.getInstance().getAuthedClient(account);
					if (client != null)
					{
						Player player = client.getActiveChar();
						if (player != null)
						{
							player.kick();
							activeChar.sendMessage("Player " + player.getName() + " kicked.");
						}
					}
					break;
				}
				case admin_accban_hwid:
				{
					st.nextToken();

					int level = 0;
					int banExpire = 0;

					String account = st.nextToken();

					if (st.hasMoreTokens())
					{
						banExpire = (int) (System.currentTimeMillis() / 1000L) + (Integer.parseInt(st.nextToken()) * 60);
					}
					else
					{
						level = -100;
					}
					AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(account, level, banExpire));
					GameClient client = AuthServerCommunication.getInstance().getAuthedClient(account);
					if (client != null)
					{
						Player player = client.getActiveChar();
						if (player != null)
						{
							HWIDBan.addBlackList(client.getHWID());
							player.kick();
							activeChar.sendMessage(new CustomMessage("common.Admin.Ban.Kicked", activeChar, new Object[0]).addString(player.getName()));
						}
					}
					break;
				}
				case admin_accunban:
				{
					st.nextToken();
					String account = st.nextToken();
					AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(account, 0, 0));
					break;
				}
				case admin_banip:
					String ip = activeChar.getTarget() != null && activeChar.isPlayable() ? activeChar.getTarget().getPlayer().getIP() : null;
					if (ip == null || ip.equals(Player.NOT_CONNECTED) || ip.equals(GameClient.NO_IP))
					{
						activeChar.sendMessage("No target or he have wrong IP Address!");
						return false;
					}
					else
					{
						for (Player player : GameObjectsStorage.getAllPlayersForIterate())
							if (player.getIP().equals(ip))
								player.kick();
						activeChar.sendMessage("Good Job!");
						return true;
					}
				case admin_trade_ban:
					return tradeBan(st, activeChar);

				case admin_trade_unban:
					return tradeUnban(st, activeChar);

				case admin_chatban:
				{
					try
					{
						st.nextToken();
						String player = st.nextToken();
						int period = Integer.parseInt(st.nextToken());
						String bmsg = "admin_chatban " + player + ' ' + period + ' ';
						String msg = fullString.substring(bmsg.length(), fullString.length());

						if (AutoBan.ChatBan(player, period, msg, activeChar.getName()))
						{
							activeChar.sendMessage("You ban chat for " + player + '.');

							//Announcements.getInstance().announceToAll("Player " + player + " muted for " + period + " minute.");

							Player p = World.getPlayer(player);
							if (p != null)
							{
								p.sendPacket(new Say2(activeChar.getObjectId(), ChatType.TELL, "->" + p.getName(), "Your chat ban duration " + TimeUtils.minutesToFullString(period) + ", cause: " + msg));
							}
						}
						else
						{
							activeChar.sendMessage("Can't find char " + player + '.');
						}
					}
					catch (NumberFormatException e)
					{
						activeChar.sendMessage("Command syntax: //chatban char_name period reason");
					}
					break;
				}

				case admin_chatunban:
					try
					{
						st.nextToken();
						String player = st.nextToken();

						if (AutoBan.ChatUnBan(player, activeChar.getName()))
						{
							activeChar.sendMessage("You unban chat for " + player + ".");
						}
						else
						{
							activeChar.sendMessage("Can't find char " + player + ".");
						}
					}
					catch (Exception e)
					{
						activeChar.sendMessage("Command syntax: //chatunban char_name");
					}
					break;
				case admin_jail:
					try
					{
						st.nextToken();
						String player = st.nextToken();
						int period = Integer.parseInt(st.nextToken());
						String reason = st.nextToken();

						if (AutoBan.Jail(player, period, reason, activeChar))
						{
							activeChar.sendMessage(player + " was jailed");
						}
					}
					catch (Exception e)
					{
						activeChar.sendMessage("Command syntax: //jail char_name period reason");
					}
					break;
				case admin_unjail:
					try
					{
						st.nextToken();
						String player = st.nextToken();

						if (AutoBan.RemoveFromJail(player, activeChar.getName()))
						{
							activeChar.sendMessage("You unjailed " + player + ".");
						}
					}
					catch (Exception e)
					{
						activeChar.sendMessage("Command syntax: //unjail char_name");
					}
					break;
				case admin_cban:
					activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/cban.htm"));
					break;
				case admin_permaban:
					if ((activeChar.getTarget() == null) || !activeChar.getTarget().isPlayer())
					{
						Functions.sendDebugMessage(activeChar, "Target should be set and be a player instance");
						return false;
					}
					Player banned = activeChar.getTarget().getPlayer();
					String banaccount = banned.getAccountName();
					AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(banaccount, -100, 0));
					if (banned.isInOfflineMode())
					{
						banned.setOfflineMode(false);
					}
					banned.kick();
					Functions.sendDebugMessage(activeChar, "Player account " + banaccount + " is banned, player " + banned.getName() + " kicked.");
					break;
				case admin_ban_hwid:
					st.nextToken();
					Player targetPlayer = null;
					if(activeChar.getTarget() != null)
					{
						targetPlayer = activeChar.getTarget().getPlayer();
					}
					else
					{
						final String playeraName = st.nextToken();
						targetPlayer = World.getPlayer(playeraName);
					}

					if(targetPlayer != null)
					{
						try
						{
							final Long time = Long.parseLong(st.nextToken());
							final String reason = st.nextToken();
							final BannedHWIDInfo bhi = new BannedHWIDInfo(targetPlayer.getNetConnection().getStrixClientData().getClientHWID(), (System.currentTimeMillis() + time * 60 * 1000), reason, activeChar.getName());
							ClientBanManager.getInstance().tryToStoreBan(bhi);
							final String bannedOut = "Player [Name:{" + targetPlayer.getName() + "}HWID:{" + targetPlayer.getNetConnection().getStrixClientData().getClientHWID() + "}] banned on [" + time + "] minutes from [" + reason + "] reason.";
							activeChar.sendMessage(bannedOut);
							org.strixplatform.logging.Log.audit(bannedOut);
							targetPlayer.sendMessage("You banned on [" + time + "] minutes. Reason: " + reason);
							targetPlayer.kick();
						}
						catch(final Exception e)
						{
							if(e instanceof SQLException)
							{
								activeChar.sendMessage("Unable to store ban in database. Please check Strix-Platform error log!");
								org.strixplatform.logging.Log.error("Exception on GM trying store ban. Exception: " + e.getLocalizedMessage());
							}
							else
							{
								activeChar.sendMessage("Command syntax: //ban_hwid PLAYER_NAME(or target) TIME(in minutes) REASON(255 max)");
							}
							break;
						}
					}
					else
					{
						activeChar.sendMessage("Unable to find this player.");
					}
					break;
				case admin_unban_hwid:
					st.nextToken();
					final String playeraHWID = st.nextToken();

					if(playeraHWID != null && playeraHWID.length() == 32)
					{
						try
						{
							ClientBanManager.getInstance().tryToDeleteBan(playeraHWID);
							activeChar.sendMessage("Player unbaned and delete from database.");
						}
						catch(final Exception e)
						{
							if(e instanceof SQLException)
							{
								activeChar.sendMessage("Unable to delete ban from database. Please check Strix-Platform error log!");
								org.strixplatform.logging.Log.error("Exception on GM trying delete ban. Exception: " + e.getLocalizedMessage());
							}
							break;
						}
					}
					else
					{
						activeChar.sendMessage("Command syntax: //unban_hwid HWID_STRING(size 32)");
					}
					break;

			}
		}

		return true;
	}

	private boolean tradeBan(StringTokenizer st, Player activeChar)
	{
		if ((activeChar.getTarget() == null) || !activeChar.getTarget().isPlayer())
		{
			return false;
		}
		st.nextToken();
		Player targ = (Player) activeChar.getTarget();
		long days = -1;
		long time = -1;
		if (st.hasMoreTokens())
		{
			days = Long.parseLong(st.nextToken());
			time = (days * 24 * 60 * 60 * 1000L) + System.currentTimeMillis();
		}
		targ.setVar("tradeBan", String.valueOf(time), -1);
		String msg = activeChar.getName() + " blocked the trade character " + targ.getName() + (days == -1 ? " for an indefinite period." : " on " + days + " days.");

		Log.add(targ.getName() + ":" + days + tradeToString(targ, targ.getPrivateStoreType()), "tradeBan", activeChar);

		if (targ.isInOfflineMode())
		{
			targ.setOfflineMode(false);
			targ.kick();
		}
		else if (targ.isInStoreMode())
		{
			targ.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
			targ.standUp();
			targ.broadcastCharInfo();
			targ.getBuyList().clear();
		}

		if (Config.BANCHAT_ANNOUNCE_FOR_ALL_WORLD)
		{
			//Announcements.getInstance().announceToAll(msg);
		}
		else
		{
			//Announcements.shout(activeChar, msg, ChatType.CRITICAL_ANNOUNCE);
		}
		return true;
	}

	@SuppressWarnings(
	{
		"unchecked",
		"rawtypes"
	})
	private static String tradeToString(Player targ, int trade)
	{
		String ret;
		Collection list;
		switch (trade)
		{
			case Player.STORE_PRIVATE_BUY:
				list = targ.getBuyList();
				if ((list == null) || list.isEmpty())
				{
					return "";
				}
				ret = ":buy:";
				for (TradeItem i : (Collection<TradeItem>) list)
				{
					ret += i.getItemId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
				}
				return ret;
			case Player.STORE_PRIVATE_SELL:
			case Player.STORE_PRIVATE_SELL_PACKAGE:
				list = targ.getSellList();
				if ((list == null) || list.isEmpty())
				{
					return "";
				}
				ret = ":sell:";
				for (TradeItem i : (Collection<TradeItem>) list)
				{
					ret += i.getItemId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
				}
				return ret;
			case Player.STORE_PRIVATE_MANUFACTURE:
				list = targ.getCreateList();
				if ((list == null) || list.isEmpty())
				{
					return "";
				}
				ret = ":mf:";
				for (ManufactureItem i : (Collection<ManufactureItem>) list)
				{
					ret += i.getRecipeId() + ";" + i.getCost() + ":";
				}
				return ret;
			default:
				return "";
		}
	}

	private boolean tradeUnban(StringTokenizer st, Player activeChar)
	{
		if ((activeChar.getTarget() == null) || !activeChar.getTarget().isPlayer())
		{
			return false;
		}
		Player targ = (Player) activeChar.getTarget();

		targ.unsetVar("tradeBan");

		if (Config.BANCHAT_ANNOUNCE_FOR_ALL_WORLD)
		{
			//Announcements.getInstance().announceToAll(activeChar + " trade unlocked character " + targ + ".");
		}
		else
		{
			Announcements.shout(activeChar, activeChar + " trade unlocked character " + targ + ".", ChatType.CRITICAL_ANNOUNCE);
		}

		Log.add(activeChar + " trade unlocked character " + targ + ".", "tradeBan", activeChar);
		return true;
	}

	private boolean ban(StringTokenizer st, Player activeChar)
	{
		try
		{
			st.nextToken();

			String player = st.nextToken();

			int time = 0;
			String msg = "";

			if (st.hasMoreTokens())
			{
				time = Integer.parseInt(st.nextToken());
			}

			if (st.hasMoreTokens())
			{
				msg = "admin_ban " + player + " " + time + " ";
				while (st.hasMoreTokens())
				{
					msg += st.nextToken() + " ";
				}
				msg.trim();
			}

			Player plyr = World.getPlayer(player);
			if (plyr != null)
			{
				plyr.sendMessage(new CustomMessage("admincommandhandlers.YoureBannedByGM", plyr));
				plyr.setAccessLevel(-100);
				AutoBan.Banned(plyr, time, msg, activeChar.getName());
				plyr.kick();
				activeChar.sendMessage("You banned " + plyr.getName());
			}
			else if (AutoBan.Banned(player, -100, time, msg, activeChar.getName()))
			{
				activeChar.sendMessage("You banned " + player);
			}
			else
			{
				activeChar.sendMessage("Can't find char: " + player);
			}
		}
		catch (Exception e)
		{
			activeChar.sendMessage("Command syntax: //ban char_name days reason");
		}
		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}