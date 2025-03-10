package l2f.gameserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

import l2f.commons.net.nio.impl.SelectorThread;
import l2f.commons.time.cron.SchedulingPattern;
import l2f.commons.time.cron.SchedulingPattern.InvalidPatternException;
import l2f.gameserver.database.DatabaseFactory;
import l2f.gameserver.hwid.HwidEngine;
import l2f.gameserver.instancemanager.CoupleManager;
import l2f.gameserver.instancemanager.CursedWeaponsManager;
import l2f.gameserver.instancemanager.games.FishingChampionShipManager;
import l2f.gameserver.model.GameObjectsStorage;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.entity.Hero;
import l2f.gameserver.model.entity.SevenSigns;
import l2f.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2f.gameserver.model.entity.olympiad.OlympiadDatabase;
import l2f.gameserver.network.GameClient;
import l2f.gameserver.network.loginservercon.AuthServerCommunication;
import l2f.gameserver.network.serverpackets.SystemMessage2;
import l2f.gameserver.network.serverpackets.components.SystemMsg;
import l2f.gameserver.scripts.Scripts;
import l2f.gameserver.utils.Log;
import l2f.gameserver.utils.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Elemental.datatables.CharacterMonthlyRanking;

public class Shutdown extends Thread
{
	private static final Logger _log = LoggerFactory.getLogger(Shutdown.class);

	public static final int SHUTDOWN = 0;
	public static final int RESTART = 2;
	public static final int NONE = -1;

	private static final Shutdown _instance = new Shutdown();

	public static final Shutdown getInstance()
	{
		return _instance;
	}

	private Timer counter;

	private int shutdownMode;
	private int shutdownCounter;

	private class ShutdownCounter extends TimerTask
	{
		@Override
		public void run()
		{
			switch (shutdownCounter)
			{
				case 10800:
				case 8800:
				case 7800:
				case 7200:
				case 6400:
				case 5000:
				case 4000:
				case 3600:
				case 2700:
				case 1800:
				case 1200:
				case 900:
				case 600:
				case 300:
				case 240:
				case 180:
				case 120:
				case 60:
					Announcements.getInstance().announceToAll("Server is restarting in "+ shutdownCounter / 60 +" minutes!");
					break;
				case 30:
				case 20:
				case 10:
				case 5:
					Announcements.getInstance().announceToAll(new SystemMessage2(SystemMsg.THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS__PLEASE_FIND_A_SAFE_PLACE_TO_LOG_OUT).addInteger(shutdownCounter));
					break;
				case 0:
					switch (shutdownMode)
					{
						case SHUTDOWN:
							Runtime.getRuntime().exit(SHUTDOWN);
							break;
						case RESTART:
							Runtime.getRuntime().exit(RESTART);
							break;
					}
					cancel();
					return;
			}

			shutdownCounter--;
		}
	}

	private Shutdown()
	{
		setName(getClass().getSimpleName());
		setDaemon(true);

		shutdownMode = NONE;
	}

	/**
	 * Время в секундах до отключения.
	 *
	 * @return время в секундах до отключения сервера, -1 если отключение не запланировано
	 */
	public int getSeconds()
	{
		return shutdownMode == NONE ? -1 : shutdownCounter;
	}

	/**
	 * Режим отключения.
	 *
	 * @return <code>SHUTDOWN</code> или <code>RESTART</code>, либо <code>NONE</code>, если отключение не запланировано.
	 */
	public int getMode()
	{
		return shutdownMode;
	}

	/**
	 * Запланировать отключение сервера через определенный промежуток времени.
	 *
	 * @param seconds время в формате <code>hh:mm</code>
	 * @param shutdownMode  <code>SHUTDOWN</code> или <code>RESTART</code>
	 */
	public synchronized void schedule(int seconds, int shutdownMode)
	{
		if (seconds < 0)
			return;

		if (counter != null)
			counter.cancel();

		this.shutdownMode = shutdownMode;
		this.shutdownCounter = seconds;

		_log.info("Scheduled server " + (shutdownMode == SHUTDOWN ? "shutdown" : "restart") + " in " + Util.formatTime(seconds) + ".");

		counter = new Timer("ShutdownCounter", true);
		counter.scheduleAtFixedRate(new ShutdownCounter(), 0, 1000L);
	}

	/**
	 * Запланировать отключение сервера на определенное время.
	 *
	 * @param time время в формате cron
	 * @param shutdownMode <code>SHUTDOWN</code> или <code>RESTART</code>
	 */
	public void schedule(String time, int shutdownMode)
	{
		SchedulingPattern cronTime;
		try
		{
			cronTime = new SchedulingPattern(time);
		}
		catch (InvalidPatternException e)
		{
			return;
		}

		int seconds = (int)(cronTime.next(System.currentTimeMillis()) / 1000L - System.currentTimeMillis() / 1000L);
		schedule(seconds, shutdownMode);
	}

	/**
	 * Отменить запланированное отключение сервера.
	 */
	public synchronized void cancel()
	{
		shutdownMode = NONE;
		if (counter != null)
			counter.cancel();
		counter = null;
	}

	@Override
	public void run()
	{
		_log.info("Shutting down LS/GS communication...");
		AuthServerCommunication.getInstance().shutdown();

		_log.info("Shutting down scripts...");
		Scripts.getInstance().shutdown();

		_log.info("Disconnecting players...");
		disconnectAllPlayers();

		_log.info("Saving data...");
		saveData();

		_log.info("Deleting Void Items...");
		removeVoidItems();

		try
		{
			_log.info("Shutting down thread pool...");
			ThreadPoolManager.getInstance().shutdown();
		}
		catch (InterruptedException e)
		{
			_log.error("Shut down Interrupted ", e);
		}

		_log.info("Shutting down selector...");
		if (GameServer.getInstance() != null)
			for (SelectorThread<GameClient> st : GameServer.getInstance().getSelectorThreads())
				try
				{
					st.shutdown();
				}
				catch (RuntimeException e)
				{
					_log.error("Error on shut down! ", e);
				}

		try
		{
			_log.info("Shutting down database communication...");
			DatabaseFactory.getInstance().shutdown();
		}
		catch (SQLException e)
		{
			_log.error("Error while closing DatabaseFactory! ", e);
		}
		catch (Exception e)
		{
			_log.error("Exception while closing DatabaseFactory! ", e);
		}

		_log.info("Shutdown finished.");
	}

	private void saveData()
	{
		try
		{
			// Seven Signs data is now saved along with Festival data.
			if (!SevenSigns.getInstance().isSealValidationPeriod())
			{
				SevenSignsFestival.getInstance().saveFestivalData(false);
				_log.info("SevenSignsFestival: Data saved.");
			}
		}
		catch (RuntimeException e)
		{
			_log.error("Error while saving Seven Signs Period! ", e);
		}

		try
		{
			SevenSigns.getInstance().saveSevenSignsData(0, true);
			_log.info("SevenSigns: Data saved.");
		}
		catch (RuntimeException e)
		{
			_log.error("Error while saving Seven Signs Data! ", e);
		}

		if (Config.ENABLE_OLYMPIAD)
			try
			{
				OlympiadDatabase.save();
				_log.info("Olympiad: Data saved.");
			}
			catch (RuntimeException e)
			{
				_log.error("Error while saving Olympiad Database! ", e);
			}

		if (Config.ALLOW_WEDDING)
			try
			{
				CoupleManager.getInstance().store();
				_log.info("CoupleManager: Data saved.");
			}
			catch (RuntimeException e)
			{
				_log.error("Error while saving Couple Manager! ", e);
			}

		try
		{
			FishingChampionShipManager.getInstance().shutdown();
			_log.info("FishingChampionShipManager: Data saved.");
		}
		catch (RuntimeException e)
		{
			_log.error("Error while saving Fishing Championship Manager! ", e);
		}

		try
		{
			Hero.getInstance().shutdown();
			_log.info("Hero: Data saved.");
		}
		catch (RuntimeException e)
		{
			_log.error("Error while saving Heroes! ", e);
		}

		try
		{
			HwidEngine.getInstance().saveAllData();
			_log.info("All HWID data saved.");
		}
		catch (RuntimeException e)
		{
			_log.error("Error while saving HWID data! ", e);
		}

		if (Config.ALLOW_CURSED_WEAPONS)
			try
			{
				CursedWeaponsManager.getInstance().saveData();
				_log.info("CursedWeaponsManager: Data saved,");
			}
			catch (RuntimeException e)
			{
				_log.error("Error while saving Cursed Weapons! ", e);
			}
		
		// Alexander - Save monthly stats for each character
		try
		{
			CharacterMonthlyRanking.getInstance().saveMonthlyStats();
			_log.info("Monthly Stats: Data saved.");
		}
		catch (RuntimeException e)
		{
			_log.error("Monthly Stats: Error while saving.", e);
		}

		Log.closeLogger();
	}

	private static void disconnectAllPlayers()
	{
		for (Player player : GameObjectsStorage.getAllPlayersForIterate())
			try
			{
				player.logout();
			}
			catch (RuntimeException e)
			{
				_log.info("Error while disconnecting: " + player + '!', e);
			}
	}

	private static void removeVoidItems()
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("DELETE items FROM items LEFT JOIN clan_data ON clan_data.clan_id = items.owner_id WHERE items.loc='VOID' AND items.owner_id > 0 AND clan_data.clan_id is NULL"))
		{
			statement.executeUpdate();
		}
		catch (SQLException e)
		{
			_log.error("Error while removing void items!", e);
		}
	}
}