//package l2f.gameserver.model.entity;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.Reader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.net.URLConnection;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javolution.util.FastMap;
//import l2f.gameserver.Announcements;
//import l2f.gameserver.Config;
//import l2f.gameserver.ThreadPoolManager;
//import l2f.gameserver.model.GameObjectsStorage;
//import l2f.gameserver.model.Player;
//import l2f.gameserver.model.entity.json.JsonObject;
//import l2f.gameserver.utils.ItemFunctions;
//
//public class ViewRewardTwitch
//{
//	private static final Logger _log = LoggerFactory.getLogger(ViewRewardTwitch.class);
//	
//	// Configurations.
//	private static String twitchUrl = Config.TWITCH_SERVER_LINK;
//	private static int viewRewardViewsDifference = Config.TWITCH_VIEWS_DIFFERENCE;
//	private static int checkTime = 60 * 1000 * Config.TWITCH_REWARD_CHECK_TIME;
////	private static int firstPageRankNeeded = Config.HOPZONE_FIRST_PAGE_RANK_NEEDED;
//	
//	// Don't-touch variables.
//	private static int lastViews = 0;
//	private static FastMap<String, Integer> playerIps = new FastMap<String, Integer>();
//	
//	public static void updateConfigurations()
//	{
//		twitchUrl = Config.TWITCH_SERVER_LINK;
//		viewRewardViewsDifference = Config.TWITCH_VIEWS_DIFFERENCE;
//		checkTime = 60 * 1000 * Config.TWITCH_REWARD_CHECK_TIME;
////		firstPageRankNeeded = Config.HOPZONE_FIRST_PAGE_RANK_NEEDED;
//	}
//	
//	public static void getInstance()
//	{
//		_log.info("Twitch: View reward system initialized.");
//		ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				if (Config.ALLOW_TWITCH_VIEWS_REWARD)
//				{
//			        _log.info("Twitch: Views Loaded Successfully.");
//					reward();
//				}
//				else
//				{
//			        _log.info("Twitch: Views Failed to be Loaded.");
//					return;
//				}
//			}
//		}, checkTime / 2, checkTime);
//	}
//	
//	private static void reward()
//	{
//		int firstPageViews = getFirstPageRankViews();
//		int currentViews = getViews();
//		
//		if ((firstPageViews == -1) || (currentViews == -1))
//		{
//			if (firstPageViews == -1)
//			{
//				_log.info("Twitch: There was a problem on getting views from server first page.");
//			}
//			if (currentViews == -1)
//			{
//				_log.info("Twitch: There was a problem on getting channel views.");
//			}
//			
//			return;
//		}
//		
//		if (lastViews == 0)
//		{
//			lastViews = currentViews;
//			Announcements.getInstance().announceToAll("Twitch: Current view count is " + currentViews + ".");
//			Announcements.getInstance().announceToAll("Twitch: We need " + ((lastViews + viewRewardViewsDifference) - currentViews) + " view(s) for reward.");
//			if (Config.ALLOW_TWITCH_GAME_SERVER_REPORT)
//			{
//				_log.info("Server votes on Hopzone: " + currentViews);
//				_log.info("Votes needed for reward: " + ((lastViews + viewRewardViewsDifference) - currentViews));
//			}
//			if ((firstPageViews - lastViews) <= 0)
//			{
////				Announcements.getInstance().announceToAll("Vote reward: We are in the first page of Hopzone, so the reward will be big.");
//				if (Config.ALLOW_TWITCH_GAME_SERVER_REPORT)
//				{
//					_log.info("Twitch: Server is on the first page of Twitch!.");
//				}
//			}
//			else
//			{
//				Announcements.getInstance().announceToAll("Twitch: We need " + (firstPageViews - lastViews) + " view(s) to get to the first page of Twitch for reward.");
//				if (Config.ALLOW_TWITCH_GAME_SERVER_REPORT)
//				{
//					_log.info("Twitch: Server views needed for first page: " + (firstPageViews - lastViews));
//				}
//			}
//			return;
//		}
//		
//		if (currentViews >= (lastViews + viewRewardViewsDifference))
//		{
//			if ((firstPageViews - currentViews) <= 0)
//			{
//				if (Config.ALLOW_TWITCH_GAME_SERVER_REPORT)
//				{
//					_log.info("Server views on Twitch: " + currentViews);
//					_log.info("Server is on the first page of Twitch.");
//					_log.info("Views needed for next reward: " + ((currentViews + viewRewardViewsDifference) - currentViews));
//				}
//				Announcements.getInstance().announceToAll("Twitch: Everyone has been rewarded !");
//				Announcements.getInstance().announceToAll("Twitch: Current vote count is " + currentViews + ".");
//				for (Player player : GameObjectsStorage.getAllPlayers())
//				{
//					boolean canReward = false;
//					String pIp = player.getIP();
//					if (playerIps.containsKey(pIp))
//					{
//						int count = playerIps.get(pIp);
//						if (count < Config.TWITCH_DUALBOXES_ALLOWED)
//						{
//							playerIps.remove(pIp);
//							playerIps.put(pIp, count + 1);
//							canReward = true;
//						}
//					}
//					else
//					{
//						canReward = true;
//						playerIps.put(pIp, 1);
//					}
//					if (canReward)
//					{
//						_log.warn("Can Reward!");
//						addItem(player, Config.TWITCH_REWARD_ID, Config.TWITCH_REWARD_COUNT);
//						player.sendMessage("You have been rewarded with" + Config.TWITCH_REWARD_COUNT + "Festival Adena");
//					}
//					else
//					{
//						player.sendMessage("Already " + Config.TWITCH_DUALBOXES_ALLOWED + " character(s) of your ip have been rewarded, so this character won't be rewarded.");
//					}
//				}
//				playerIps.clear();
//			}
//			else
//			{
//				if (Config.ALLOW_TWITCH_GAME_SERVER_REPORT)
//				{
//					_log.info("Server views on Twitch: " + currentViews);
//					_log.info("Server views needed for first page: " + (firstPageViews - lastViews));
//					_log.info("Views needed for next reward: " + ((currentViews + viewRewardViewsDifference) - currentViews));
//				}
//				Announcements.getInstance().announceToAll("Twitch: Everyone has been rewarded!");
//				Announcements.getInstance().announceToAll("Twitch: Current view count is " + currentViews + ".");
//				Announcements.getInstance().announceToAll("Twitch: We need " + (firstPageViews - currentViews) + " view(s) to get to the first page of Twitch for reward.");
//				for (Player player : GameObjectsStorage.getAllPlayers())
//				{
//					boolean canReward = false;
//					String pIp = player.getIP();
//					if (playerIps.containsKey(pIp))
//					{
//						int count = playerIps.get(pIp);
//						if (count < Config.TWITCH_DUALBOXES_ALLOWED)
//						{
//							playerIps.remove(pIp);
//							playerIps.put(pIp, count + 1);
//							canReward = true;
//						}
//					}
//					else
//					{
//						canReward = true;
//						playerIps.put(pIp, 1);
//					}
//					if (canReward)
//					{
//						_log.warn("Can Reward!");
//						addItem(player, Config.TWITCH_REWARD_ID, Config.TWITCH_REWARD_COUNT);
//						player.sendMessage("You have been rewarded with" + Config.TWITCH_REWARD_COUNT + "Festival Adena");
//					}
//					else
//					{
//						player.sendMessage("Already " + Config.HOPZONE_DUALBOXES_ALLOWED + " character(s) of your ip have been rewarded, so this character won't be rewarded.");
//					}
//				}
//				playerIps.clear();
//			}
//			
//			lastViews = currentViews;
//		}
//		else
//		{
//			if ((firstPageViews - currentViews) <= 0)
//			{
//				if (Config.ALLOW_TWITCH_GAME_SERVER_REPORT)
//				{
//					_log.info("Server views on Twitch: " + currentViews);
//					_log.info("Server is on the first page of Twitch.");
//					_log.info("Views needed for next reward: " + ((lastViews + viewRewardViewsDifference) - currentViews));
//				}
//				Announcements.getInstance().announceToAll("Twitch: Current vote count is " + currentViews + ".");
//				Announcements.getInstance().announceToAll("Twitch: We need " + ((lastViews + viewRewardViewsDifference) - currentViews) + " view(s) for reward.");
//			}
//			else
//			{
//				if (Config.ALLOW_TWITCH_GAME_SERVER_REPORT)
//				{
//					_log.info("Server views on Twitch: " + currentViews);
//					_log.info("Server views needed for first page: " + (firstPageViews - lastViews));
//					_log.info("Views needed for next reward: " + ((lastViews + viewRewardViewsDifference) - currentViews));
//				}
//				Announcements.getInstance().announceToAll("Twitch: Current view count is " + currentViews + ".");
//				Announcements.getInstance().announceToAll("Twitch: We need " + ((lastViews + viewRewardViewsDifference) - currentViews) + " view(s) for reward.");
//				Announcements.getInstance().announceToAll("Twitch: We need " + (firstPageViews- currentViews) + " view(s) to get to the first page of Twitch for reward.");
//			}
//		}
//	}
//	
//	private static int getFirstPageRankViews()
//	
//	{	
//		try {
//				String url = (twitchUrl);
//				URL obj = new URL(url);
//				HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
//				conn.setRequestMethod("GET");
//				conn.setRequestProperty("Accept", "application/vnd.twitchtv.v5+json");
//				conn.setRequestProperty("Client-ID", "3m7i0qd39kjkr0rp5bhifigtsaccgn");
//    
//				BufferedReader in = new BufferedReader( new InputStreamReader( conn.getInputStream() ));
//				String InputLine;
//				StringBuffer response = new StringBuffer();
//				while ((InputLine = in.readLine()) != null) {
//				response.append(InputLine);
//				}    
//				System.out.println(response.toString());
//				JsonObject jsonObj = JsonObject.readFrom(response.toString());
//				int i = jsonObj.get("stream").asObject().get("viewers").asInt();
//				return i;
//    }
//		catch (IOException ex) 
//		{
//			ex.printStackTrace();
//		}
//		return -1;
//    }
//	
//	private static int getViews() 
//	{	
//		try {
//				String url = (twitchUrl);
//				URL obj = new URL(url);
//				HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
//				conn.setRequestMethod("GET");
//				conn.setRequestProperty("Accept", "application/vnd.twitchtv.v5+json");
//				conn.setRequestProperty("Client-ID", "3m7i0qd39kjkr0rp5bhifigtsaccgn");
//    
//				BufferedReader in = new BufferedReader( new InputStreamReader( conn.getInputStream() ));
//				String InputLine;
//				StringBuffer response = new StringBuffer();
//				while ((InputLine = in.readLine()) != null) {
//				response.append(InputLine);
//				}    
//				System.out.println(response.toString());
//				JsonObject jsonObj = JsonObject.readFrom(response.toString());
//				int i = jsonObj.get("stream").asObject().get("viewers").asInt();
//				return i;
//    }
//		catch (IOException ex) 
//		{
//			ex.printStackTrace();
//		}
//		return -1;
//    }
//	
//	public static void addItem(Player player, int itemId, long count)
//	{
//		ItemFunctions.addItem(player, itemId, count, true, "ViewRewardTwitch");
//	}
//}