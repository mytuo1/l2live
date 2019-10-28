package l2f.gameserver.model.entity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javolution.util.FastMap;
import l2f.gameserver.Announcements;
import l2f.gameserver.Config;
import l2f.gameserver.ThreadPoolManager;
import l2f.gameserver.model.GameObjectsStorage;
import l2f.gameserver.model.Player;
import l2f.gameserver.utils.ItemFunctions;

public class ViewRewardTwitch
{
	private static final Logger _log = LoggerFactory.getLogger(ViewRewardTwitch.class);
	
	// Configurations.
	private static String twitchUrl = Config.TWITCH_SERVER_LINK;
	private static int viewRewardViewsDifference = Config.TWITCH_VIEWS_DIFFERENCE;
	private static int checkTime = 60 * 1000 * Config.TOPZONE_REWARD_CHECK_TIME;
	
	// Don't-touch variables.
	private static int lastViews = 0;
	private static FastMap<String, Integer> playerIps = new FastMap<String, Integer>();
	
	public static void updateConfigurations()
	{
		twitchUrl = Config.TWITCH_SERVER_LINK;
		viewRewardViewsDifference = Config.TWITCH_VIEWS_DIFFERENCE;
		checkTime = 60 * 1000 * Config.TWITCH_REWARD_CHECK_TIME;
	}
	
	public static void getInstance()
	{
		_log.info("Topzone: Vote reward system initialized.");
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				if (Config.ALLOW_TWITCH_VIEWS_REWARD)
				{
					reward();
				}
				else
				{
					return;
				}
			}
		}, checkTime / 2, checkTime);
	}
	
	private static void reward()
	{
		int currentViews = getViews();
		
		if ((currentViews == -1))
		{
			if (currentViews == -1)
			{
				_log.info("Twitch: There was a problem on getting channel views.");
			}
			
			return;
		}
		
		if (lastViews == 0)
		{
			lastViews = currentViews;
			Announcements.getInstance().announceToAll("Twitch: Current view count is " + currentViews + ".");
			Announcements.getInstance().announceToAll("Twitch: We need " + ((lastViews + viewRewardViewsDifference) - currentViews) + " view(s) for reward.");
			if (Config.ALLOW_TWITCH_GAME_SERVER_REPORT)
			{
				_log.info("Channel views on Twitch: " + currentViews);
				_log.info("Views needed for reward: " + ((lastViews + viewRewardViewsDifference) - currentViews));
			}
			return;
		}
		
		if (currentViews >= (lastViews + viewRewardViewsDifference))
		{
			if ((currentViews) <= 0)
			{
				if (Config.ALLOW_TWITCH_GAME_SERVER_REPORT)
				{
					_log.info("Channel views on twitch: " + currentViews);
					_log.info("Views needed for next reward: " + ((currentViews + viewRewardViewsDifference) - currentViews));
				}
				Announcements.getInstance().announceToAll("Twitch: Everyone has been rewarded with reward.");
				Announcements.getInstance().announceToAll("Twitch: Current views count is " + currentViews + ".");
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
						addItem(player, Config.TWITCH_REWARD_ID, Config.TWITCH_REWARD_COUNT);
						player.sendMessage("You have received an award for viewing our Twitch channel in the amount of " + Config.TWITCH_REWARD_COUNT);
					}
					else
					{
						player.sendMessage("Already " + Config.TWITCH_DUALBOXES_ALLOWED + " character(s) of your ip have been rewarded, so this character won't be rewarded.");
					}
				}
				playerIps.clear();
			}
			lastViews = currentViews;
		}
	}
	
	private static int getViews()
	{
		InputStreamReader isr = null;
		BufferedReader br = null;
		
		try
		{
			URLConnection con = new URL(twitchUrl).openConnection();
                       con.addRequestProperty("Accept", "application/vnd.twitchtv.v5+json");
                       con.addRequestProperty("Client-ID", "3m7i0qd39kjkr0rp5bhifigtsaccgn");
                       isr = new InputStreamReader(con.getInputStream());
                       br = new BufferedReader(isr);
                      
                       String line;
                       while ((line = br.readLine()) != null)
                       {
                               if (line.contains("</span> </small>"))
                               {
                                       int votes = Integer.valueOf(line.split(">")[9].replace("</span", ""));
                                       return votes;
                               }
                       }
			
			br.close();
			isr.close();
		}
		catch (Exception e)
		{
			_log.warn("Twitch: Error while getting server view count.", e);
		}
		
		return -1;
	}
	
	public static void addItem(Player player, int itemId, long count)
	{
		ItemFunctions.addItem(player, itemId, count, true, "ViewRewardTwitch");
	}
}