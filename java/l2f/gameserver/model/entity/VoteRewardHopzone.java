package l2f.gameserver.model.entity;

import javolution.util.FastMap;
import l2f.gameserver.Announcements;
import l2f.gameserver.Config;
import l2f.gameserver.ThreadPoolManager;
import l2f.gameserver.model.GameObjectsStorage;
import l2f.gameserver.model.Player;
import l2f.gameserver.network.serverpackets.components.ChatType;
import l2f.gameserver.utils.ItemFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class VoteRewardHopzone
{
	private static final Logger _log = LoggerFactory.getLogger(VoteRewardHopzone.class);
	
	// Configurations.
	private static String hopzoneUrl = Config.HOPZONE_SERVER_LINK;
//	private static String page1Url = Config.HOPZONE_FIRST_PAGE_LINK;
	private static int voteRewardVotesDifference = Config.HOPZONE_VOTES_DIFFERENCE;
	private static int firstPageRankNeeded = Config.HOPZONE_FIRST_PAGE_RANK_NEEDED;
	private static int checkTime = 60 * 1000 * Config.HOPZONE_REWARD_CHECK_TIME;
	
	// Don't-touch variables.
	private static int lastVotes = 0;
	private static FastMap<String, Integer> playerIps = new FastMap<String, Integer>();
	
	public static void updateConfigurations()
	{
		hopzoneUrl = Config.HOPZONE_SERVER_LINK;
//		page1Url = Config.HOPZONE_FIRST_PAGE_LINK;
		voteRewardVotesDifference = Config.HOPZONE_VOTES_DIFFERENCE;
		firstPageRankNeeded = Config.HOPZONE_FIRST_PAGE_RANK_NEEDED;
		checkTime = 60 * 1000 * Config.HOPZONE_REWARD_CHECK_TIME;
	}
	
	public static void getInstance()
	{
		_log.info("Hopzone: Vote reward system initialized.");
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				if (Config.ALLOW_HOPZONE_VOTE_REWARD)
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
		int firstPageVotes = getFirstPageRankVotes();
		int currentVotes = getVotes();
		
		if ((firstPageVotes == -1) || (currentVotes == -1))
		{
			if (firstPageVotes == -1)
			{
				_log.info("Hopzone: There was a problem on getting votes from server with rank " + firstPageRankNeeded + ".");
			}
			if (currentVotes == -1)
			{
				_log.info("Hopzone: There was a problem on getting server votes.");
			}
			
			return;
		}
		
		if (lastVotes == 0)
		{
			lastVotes = currentVotes;
			Announcements.getInstance().announceToAll("Hopzone: Current vote count is " + currentVotes + "." , ChatType.CRITICAL_ANNOUNCE);
			Announcements.getInstance().announceToAll("Hopzone: We need " + ((lastVotes + voteRewardVotesDifference) - currentVotes) + " vote(s) for reward." , ChatType.CRITICAL_ANNOUNCE);
			if (Config.ALLOW_HOPZONE_GAME_SERVER_REPORT)
			{
				_log.info("Server votes on Hopzone: " + currentVotes);
				_log.info("Votes needed for reward: " + ((lastVotes + voteRewardVotesDifference) - currentVotes));
			}
			if ((firstPageVotes - lastVotes) <= 0)
			{
//				Announcements.getInstance().announceToAll("Vote reward: We are in the first page of Hopzone, so the reward will be big.");
				if (Config.ALLOW_HOPZONE_GAME_SERVER_REPORT)
				{
					_log.info("Hopzone: Server is on the first page of hopzone.");
				}
			}
			else
			{
				Announcements.getInstance().announceToAll("Hopzone: We need " + (firstPageVotes - lastVotes) + " vote(s) to get to the first page of Hopzone for reward.");
				if (Config.ALLOW_HOPZONE_GAME_SERVER_REPORT)
				{
					_log.info("Hopzone: Server votes needed for first page: " + (firstPageVotes - lastVotes));
				}
			}
			return;
		}
		
		if (currentVotes >= (lastVotes + voteRewardVotesDifference))
		{
			if ((firstPageVotes - currentVotes) <= 0)
			{
				if (Config.ALLOW_HOPZONE_GAME_SERVER_REPORT)
				{
					_log.info("Server votes on hopzone: " + currentVotes);
					_log.info("Server is on the first page of hopzone.");
					_log.info("Votes needed for next reward: " + ((currentVotes + voteRewardVotesDifference) - currentVotes));
				}
				Announcements.getInstance().announceToAll("Hopzone: Everyone has been rewarded !" , ChatType.COMMANDCHANNEL_ALL);
				Announcements.getInstance().announceToAll("Hopzone: Current vote count is " + currentVotes + "." , ChatType.CRITICAL_ANNOUNCE);
				for (Player player : GameObjectsStorage.getAllPlayers())
				{
					boolean canReward = false;
					String pIp = player.getIP();
					if (playerIps.containsKey(pIp))
					{
						int count = playerIps.get(pIp);
						if (count < Config.HOPZONE_DUALBOXES_ALLOWED)
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
						addItem(player, Config.HOPZONE_REWARD_ID, Config.HOPZONE_REWARD_COUNT);
						player.sendMessage("You have been rewarded with" + Config.HOPZONE_REWARD_COUNT + "Vote Coins");
					}
					else
					{
						player.sendMessage("Already " + Config.HOPZONE_DUALBOXES_ALLOWED + " character(s) of your ip have been rewarded, so this character won't be rewarded.");
					}
				}
				playerIps.clear();
			}
			else
			{
				if (Config.ALLOW_HOPZONE_GAME_SERVER_REPORT)
				{
					_log.info("Server votes on hopzone: " + currentVotes);
					_log.info("Server votes needed for first page: " + (firstPageVotes - lastVotes));
					_log.info("Votes needed for next reward: " + ((currentVotes + voteRewardVotesDifference) - currentVotes));
				}
				Announcements.getInstance().announceToAll("Hopzone: Everyone has been rewarded!" , ChatType.COMMANDCHANNEL_ALL);
				Announcements.getInstance().announceToAll("Hopzone: Current vote count is " + currentVotes + "." , ChatType.CRITICAL_ANNOUNCE);
				Announcements.getInstance().announceToAll("Hopzone: We need " + (firstPageVotes - currentVotes) + " vote(s) to get to the first page of Hopzone for reward." , ChatType.CRITICAL_ANNOUNCE);
				for (Player player : GameObjectsStorage.getAllPlayers())
				{
					boolean canReward = false;
					String pIp = player.getIP();
					if (playerIps.containsKey(pIp))
					{
						int count = playerIps.get(pIp);
						if (count < Config.HOPZONE_DUALBOXES_ALLOWED)
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
						addItem(player, Config.HOPZONE_REWARD_ID, Config.HOPZONE_REWARD_COUNT);
						player.sendMessage("You have been rewarded with" + Config.HOPZONE_REWARD_COUNT + "Vote Coins");
					}
					else
					{
						player.sendMessage("Already " + Config.HOPZONE_DUALBOXES_ALLOWED + " character(s) of your ip have been rewarded, so this character won't be rewarded.");
					}
				}
				playerIps.clear();
			}
			
			lastVotes = currentVotes;
		}
		else
		{
			if ((firstPageVotes - currentVotes) <= 0)
			{
				if (Config.ALLOW_HOPZONE_GAME_SERVER_REPORT)
				{
					_log.info("Server votes on hopzone: " + currentVotes);
					_log.info("Server is on the first page of hopzone.");
					_log.info("Votes needed for next reward: " + ((lastVotes + voteRewardVotesDifference) - currentVotes));
				}
				Announcements.getInstance().announceToAll("Hopzone: Current vote count is " + currentVotes + "." , ChatType.CRITICAL_ANNOUNCE);
				Announcements.getInstance().announceToAll("Hopzone: We need " + ((lastVotes + voteRewardVotesDifference) - currentVotes) + " vote(s) for reward." , ChatType.CRITICAL_ANNOUNCE);
			}
			else
			{
				if (Config.ALLOW_HOPZONE_GAME_SERVER_REPORT)
				{
					_log.info("Server votes on hopzone: " + currentVotes);
					_log.info("Server votes needed for first page: " + (firstPageVotes - lastVotes));
					_log.info("Votes needed for next reward: " + ((lastVotes + voteRewardVotesDifference) - currentVotes));
				}
				Announcements.getInstance().announceToAll("Hopzone: Current vote count is " + currentVotes + "." , ChatType.CRITICAL_ANNOUNCE);
				Announcements.getInstance().announceToAll("Hopzone: We need " + ((lastVotes + voteRewardVotesDifference) - currentVotes) + " vote(s) for reward." , ChatType.CRITICAL_ANNOUNCE);
				Announcements.getInstance().announceToAll("Hopzone: We need " + (firstPageVotes - currentVotes) + " vote(s) to get to the first page of Hopzone for reward." , ChatType.CRITICAL_ANNOUNCE);
			}
		}
	}
	
	private static int getFirstPageRankVotes()
	{
		InputStreamReader isr = null;
		BufferedReader br = null;
		
		try
		{
			URLConnection con = new URL(hopzoneUrl).openConnection();
//			URLConnection con = new URL(page1Url).openConnection();
			con.addRequestProperty("User-Agent", "Mozilla/4.76");
			isr = new InputStreamReader(con.getInputStream());
			br = new BufferedReader(isr);
			
			String line;
//			int i = 0;
//			while ((line = br.readLine()) != null)
//			{
//				if (line.contains("<span class=\"no\">9</span>"))
////				if (line.contains("<span class=\"no\">" + firstPageRankNeeded + "</span>"))
//				{
//					i++;
//				}
//				if (line.contains("Total Votes") && (i == 1))
//				{
//					i = 0;
//					int votes = 10000;
////					int votes = Integer.valueOf(line.split(">")[1].replace("</span>", ""));
//					return votes;
//				}
//			}
			while ((line = br.readLine()) != null)
			{
				if (line.contains("totalvotes"))
				{
//					int votes = Integer.valueOf(line.split(">")[2].replace("</span", ""));
					int votes = Integer.valueOf(line.split(":")[2].replace(",\"status_code\"", ""));
					return votes;
				}
			}
			
			br.close();
			isr.close();
		}
		catch (Exception e)
		{
			_log.warn("Hopzone: Error while getting server vote count.", e);
		}
		
		return -1;
	}
	
	private static int getVotes()
	{
		InputStreamReader isr = null;
		BufferedReader br = null;
		
		try
		{
			URLConnection con = new URL(hopzoneUrl).openConnection();
			con.addRequestProperty("User-Agent", "Mozilla/4.76");
			isr = new InputStreamReader(con.getInputStream());
			br = new BufferedReader(isr);
			
			String line;
			while ((line = br.readLine()) != null)
			{
				if (line.contains("totalvotes"))
				{
//					int votes = Integer.valueOf(line.split(">")[2].replace("</span", ""));
					int votes = Integer.valueOf(line.split(":")[2].replace(",\"status_code\"", ""));
					return votes;
				}
			}
			
			br.close();
			isr.close();
		}
		catch (Exception e)
		{
			_log.warn("Hopzone: Error while getting server vote count.", e);
		}
		
		return -1;
	}
	
	public static void addItem(Player player, int itemId, long count)
	{
		ItemFunctions.addItem(player, itemId, count, true, "VoteRewardHopzone");
	}
}