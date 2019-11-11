package l2f.gameserver.model.entity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javolution.util.FastMap;
import l2f.gameserver.Announcements;
import l2f.gameserver.Config;
import l2f.gameserver.ThreadPoolManager;
import l2f.gameserver.model.GameObjectsStorage;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.entity.json.JsonObject;
import l2f.gameserver.network.serverpackets.components.ChatType;
import l2f.gameserver.utils.ItemFunctions;

public class MassRewardTwitch
{
	private static final Logger _log = LoggerFactory.getLogger(MassRewardTwitch.class);
	
	// Configurations.
	private static String twitchUrl = Config.TWITCH_SERVER_LINK;
	private static int viewRewardViewsDifference = Config.TWITCH_VIEWS_DIFFERENCE;
	private static int viewRewardViewsDifference2 = Config.TWITCH_VIEWS_DIFFERENCE2;
	private static int viewRewardViewsDifference3 = Config.TWITCH_VIEWS_DIFFERENCE3;

	private static int checkTime = 60 * 1000 * Config.TWITCH_REWARD_CHECK_TIME;
	
	// Don't-touch variables.
	private static int lastViews = 0;
	private static FastMap<String, Integer> playerIps = new FastMap<String, Integer>();
	
	public static void updateConfigurations()
	{
		twitchUrl = Config.TWITCH_SERVER_LINK;
		viewRewardViewsDifference = Config.TWITCH_VIEWS_DIFFERENCE;
		viewRewardViewsDifference2 = Config.TWITCH_VIEWS_DIFFERENCE2;
		viewRewardViewsDifference3 = Config.TWITCH_VIEWS_DIFFERENCE3;
		checkTime = 60 * 1000 * Config.TWITCH_REWARD_CHECK_TIME;
	}
	
	public static void getInstance()
	{
		_log.info("Twitch: View reward system initialized.");
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				if (Config.ALLOW_TWITCH_VIEWS_REWARD)
				{
					reward();
			        _log.info("Twitch: Views Loaded Successfully" + getViews() + ".");
				}
				else
				{
			        _log.info("Twitch: Views Failed to be Loaded.");
					return;
				}
			}
		}, checkTime / 2, checkTime);
	}
	
	private static void reward()
	{
		int currentViews = getViews();
		
		if (currentViews == -1)
		{
			{
				_log.info("Twitch: There was a problem on getting channel views.");
			}			
			return;
		}
		
		if (lastViews == 0)
		{
			lastViews = currentViews;
			Announcements.getInstance().announceToAll("Twitch: Current view count is " + currentViews + "." , ChatType.CRITICAL_ANNOUNCE);
			Announcements.getInstance().announceToAll("Twitch: We need " + ((lastViews + viewRewardViewsDifference) - currentViews) + " view(s) for the next Reward stage." , ChatType.CRITICAL_ANNOUNCE);
			if (Config.ALLOW_TWITCH_GAME_SERVER_REPORT)
			{
				_log.info("Server views on Twitch: " + currentViews);
				_log.info("Views needed for Twitch Mass reward: " + ((lastViews + viewRewardViewsDifference) - currentViews));
			}
			return;
		}
		
		if (currentViews >= viewRewardViewsDifference && currentViews <= viewRewardViewsDifference2)
//		if (currentViews >= (lastViews + viewRewardViewsDifference) && currentViews <= (lastViews + viewRewardViewsDifference2))
		{
			
				if (Config.ALLOW_TWITCH_GAME_SERVER_REPORT)
				{
					_log.info("Server views on Twitch: " + currentViews);
					_log.info("Views needed for next reward: " + ((currentViews + viewRewardViewsDifference2) - currentViews));
				}
				Announcements.getInstance().announceToAll("We will now give the First Stage rewards!" , ChatType.CRITICAL_ANNOUNCE);
				Announcements.getInstance().announceToAll("Twitch: Everyone has been rewarded !" , ChatType.CRITICAL_ANNOUNCE);
				Announcements.getInstance().announceToAll("Twitch: Current view count is " + currentViews + "." , ChatType.CRITICAL_ANNOUNCE);
				for (Player player : GameObjectsStorage.getAllPlayers())
				{
					boolean canReward = false;
					String pIp = player.getIP();
					if (playerIps.containsKey(pIp))
					{
						int count = playerIps.get(pIp);
						if (count < Config.TWITCH_DUALBOXES_ALLOWED)
						{
							playerIps.remove(pIp);
							playerIps.put(pIp, count + 1);
							canReward = true;
						}
					}
					else
					{
						canReward = true;
						playerIps.put(pIp, 1);
					}
					if (canReward)
					{
						_log.warn("Can Reward!");
						addItem(player, Config.TWITCH_REWARD_ID, Config.TWITCH_REWARD_COUNT);
						player.sendMessage("You have been rewarded with" + Config.TWITCH_REWARD_COUNT + "Festival Adena");
					}
					else
					{
						player.sendMessage("Already " + Config.TWITCH_DUALBOXES_ALLOWED + " character(s) of your ip have been rewarded, so this character won't be rewarded.");
					}
				}
				playerIps.clear();			
			lastViews = currentViews;
		}
		else
			
		if (currentViews >= viewRewardViewsDifference2 && currentViews <= viewRewardViewsDifference3)
//		if (currentViews >= (lastViews + viewRewardViewsDifference2) && currentViews <= (lastViews + viewRewardViewsDifference3))
		{
			
				if (Config.ALLOW_TWITCH_GAME_SERVER_REPORT)
				{
					_log.info("Server views on Twitch: " + currentViews + ".");
					_log.info("Views needed for next reward: " + ((currentViews + viewRewardViewsDifference3) - currentViews));
				}
				Announcements.getInstance().announceToAll("We will now give the Second Stage rewards!" , ChatType.CRITICAL_ANNOUNCE);
				Announcements.getInstance().announceToAll("Twitch: Everyone has been rewarded !" , ChatType.CRITICAL_ANNOUNCE);
				Announcements.getInstance().announceToAll("Twitch: Current view count is " + currentViews + "." , ChatType.CRITICAL_ANNOUNCE);
				for (Player player : GameObjectsStorage.getAllPlayers())
				{
					boolean canReward = false;
					String pIp = player.getIP();
					if (playerIps.containsKey(pIp))
					{
						int count = playerIps.get(pIp);
						if (count < Config.TWITCH_DUALBOXES_ALLOWED)
						{
							playerIps.remove(pIp);
							playerIps.put(pIp, count + 1);
							canReward = true;
						}
					}
					else
					{
						canReward = true;
						playerIps.put(pIp, 1);
					}
					if (canReward)
					{
						_log.warn("Can Reward!");
						addItem(player, Config.TWITCH_REWARD_ID2, Config.TWITCH_REWARD_COUNT2);
						player.sendMessage("You have been rewarded with" + Config.TWITCH_REWARD_COUNT2 + "Festival Adena");
					}
					else
					{
						player.sendMessage("Already " + Config.TWITCH_DUALBOXES_ALLOWED + " character(s) of your ip have been rewarded, so this character won't be rewarded.");
					}
				}
				playerIps.clear();			
			lastViews = currentViews;
		}
		
		else
			
		if (currentViews >= viewRewardViewsDifference3)
//		if (currentViews >= (lastViews + viewRewardViewsDifference3))
		{
			
				if (Config.ALLOW_TWITCH_GAME_SERVER_REPORT)
				{
					_log.info("Server views on Twitch: " + currentViews);
					_log.info("Getting 3rd Stage rewards.");
				}
				Announcements.getInstance().announceToAll("We will now give the biggest rewards!" , ChatType.CRITICAL_ANNOUNCE);
				Announcements.getInstance().announceToAll("Twitch: Everyone has been rewarded !" , ChatType.CRITICAL_ANNOUNCE);
				Announcements.getInstance().announceToAll("Twitch: Current view count is " + currentViews + "." , ChatType.CRITICAL_ANNOUNCE);

				for (Player player : GameObjectsStorage.getAllPlayers())
				{
					boolean canReward = false;
					String pIp = player.getIP();
					if (playerIps.containsKey(pIp))
					{
						int count = playerIps.get(pIp);
						if (count < Config.TWITCH_DUALBOXES_ALLOWED)
						{
							playerIps.remove(pIp);
							playerIps.put(pIp, count + 1);
							canReward = true;
						}
					}
					else
					{
						canReward = true;
						playerIps.put(pIp, 1);
					}
					if (canReward)
					{
						_log.warn("Can Reward!");
						addItem(player, Config.TWITCH_REWARD_ID3, Config.TWITCH_REWARD_COUNT3);
						player.sendMessage("You have been rewarded with" +  Config.TWITCH_REWARD_COUNT3  + "Festival Adena");
					}
					else
					{
						player.sendMessage("Already " + Config.TWITCH_DUALBOXES_ALLOWED + " character(s) of your ip have been rewarded, so this character won't be rewarded.");
					}
				}
				playerIps.clear();			
			lastViews = currentViews;
		}

	}
	
	private static int getViews() 
	{	
		try {
				String url = (twitchUrl);
				URL obj = new URL(url);
				HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Accept", "application/vnd.twitchtv.v5+json");
				conn.setRequestProperty("Client-ID", "3m7i0qd39kjkr0rp5bhifigtsaccgn");
				BufferedReader in = new BufferedReader( new InputStreamReader( conn.getInputStream() ));
				String InputLine;
				StringBuffer response = new StringBuffer();
				while ((InputLine = in.readLine()) != null) {
				response.append(InputLine);
				}    
				JsonObject jsonObj = JsonObject.readFrom(response.toString());
				int i = jsonObj.get("stream").asObject().get("viewers").asInt();
				return i;
    }
		catch (IOException ex) 
		{
			ex.printStackTrace();
		}
		return -1;
    }
	
	public static void addItem(Player player, int itemId, long count)
	{
		ItemFunctions.addItem(player, itemId, count, true, "ViewRewardTwitch");
	}
}