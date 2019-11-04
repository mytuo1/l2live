//package l2f.gameserver.handler.voicecommands.impl;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.HashMap;
//
//import l2f.gameserver.Config;
//import l2f.gameserver.ThreadPoolManager;
//import l2f.gameserver.handler.voicecommands.IVoicedCommandHandler;
//import l2f.gameserver.handler.voicecommands.impl.Donate.Attempt;
//import l2f.gameserver.model.Player;
//import l2f.gameserver.model.Creature;
//import l2f.gameserver.model.entity.json.JsonObject;
//import l2f.gameserver.network.serverpackets.MagicSkillUse;
//import l2f.gameserver.scripts.Functions;
//import l2f.gameserver.tables.SkillTable;
//
//public class Stream implements IVoicedCommandHandler
//{
//	private final HashMap<Integer, Attempt> commandAttempts = new HashMap<>();
//
//	private static int checkTime = 60 * 1000 * 2;
//		
//	private static final String[] COMMANDS = {
//		"stream"
//	};
//		
//	@Override
//	public boolean useVoicedCommand(String command, Player player, String args)
//	{
//
//		if (args == null || args.length() == 0)
//		{
//			player.sendMessage("Usage: .stream username");
//			return false;
//		}
//		if (args.contains(" "))
//		{
//			Functions.sendDebugMessage(player, args + " is not a valid user name!");
//			return false;
//		}
//
//		if (isStreamLive(args) && getTitle(args))
//		{
//			player.sendMessage("Thank you for streaming with L2 Mutiny! You are awesome!");
//			reward(player, args);
//			player.sendMessage("Great job streaming! You have more then 0 viewers, so you get a big reward now!");
//			getReward(player, args);
//		}
//		return false;
//		
//	}
//
//
//	private static boolean isStreamLive(String args)
//	{					
//		try {
//					String url = ("https://api.twitch.tv/helix/streams?user_login=" + args);
//					URL obj = new URL(url);
//					HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
//					conn.setRequestMethod("GET");
//					conn.setRequestProperty("Accept", "application/vnd.twitchtv.v5+json");
//					conn.setRequestProperty("Client-ID", "3m7i0qd39kjkr0rp5bhifigtsaccgn");
//					BufferedReader in = new BufferedReader( new InputStreamReader( conn.getInputStream() ));
//					String InputLine;
//					StringBuffer response = new StringBuffer();
//					while ((InputLine = in.readLine()) != null) {
//					response.append(InputLine);
//					}    
//					String v = String.valueOf(response.toString().split(":")[6].replace(",\"title\"", ""));
//					
//					if (v.intern().matches("(.*)live(.*)"))
//					{
//					return true;
//					}
//		}
//			catch (IOException ex) 
//			{
//				ex.printStackTrace();
//			}
//		
//		return false;
//	
//	}
//	
//	private static boolean getTitle(String args)
//	{	
//					
//		try {
//					String url = ("https://api.twitch.tv/helix/streams?user_login=" + args);
//					URL obj = new URL(url);
//					HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
//					conn.setRequestMethod("GET");
//					conn.setRequestProperty("Accept", "application/vnd.twitchtv.v5+json");
//					conn.setRequestProperty("Client-ID", "3m7i0qd39kjkr0rp5bhifigtsaccgn");
//					BufferedReader in = new BufferedReader( new InputStreamReader( conn.getInputStream() ));
//					String InputLine;
//					StringBuffer response = new StringBuffer();
//					while ((InputLine = in.readLine()) != null) {
//					response.append(InputLine);
//					}    
//					String v = String.valueOf(response.toString().split(":")[7].replace(",\"viewer_count\"", ""));
//				
//				    if (v.intern().matches("(.*)L2Mutiny(.*)"))
//					{
//					return true;
//					}
//		}
//			catch (IOException ex) 
//			{
//				ex.printStackTrace();
//			}
//		return false;
//	}
//	
//	private static int getViewers(String args)
//	{	
//					
//		try {
//					String url = ("https://api.twitch.tv/helix/streams?user_login=" + args);
//					URL obj = new URL(url);
//					HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
//					conn.setRequestMethod("GET");
//					conn.setRequestProperty("Accept", "application/vnd.twitchtv.v5+json");
//					conn.setRequestProperty("Client-ID", "3m7i0qd39kjkr0rp5bhifigtsaccgn");
//					BufferedReader in = new BufferedReader( new InputStreamReader( conn.getInputStream() ));
//					String InputLine;
//					StringBuffer response = new StringBuffer();
//					while ((InputLine = in.readLine()) != null) {
//					response.append(InputLine);
//					}    
//					String g = String.valueOf(response.toString().split(":")[8].replace(",\"started_at\"", ""));
//					int v = Integer.parseInt(g);
//
//					return v;
//
//		}
//			catch (IOException ex) 
//			{
//				ex.printStackTrace();
//			}
//		return -1;
//	}
//	
//	private static void getReward(Player player, String args)
//	{
//		ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable()
//	{
//		@Override
//		public void run()
//		{
//				reward(player, args);
//		
//		}
//	}, checkTime / 2, checkTime);
//	}
//	
//	private static void reward(Player player, String args)
//	{
//		
//		if (isStreamLive(args) && getTitle(args))
//		{
//			if (getViewers(args) > 0)
//			{
//		
//    			MagicSkillUse MSU = new MagicSkillUse(player, player, 1363, 44, 1, 0);
//				player.broadcastPacket(MSU);
//				player.sendMessage("Checking stream actvity...");
//			}
//			else
//			{
//    			MagicSkillUse MSU = new MagicSkillUse(player, player, 1363, 46, 1, 0);
//				player.broadcastPacket(MSU);
//				player.sendMessage("Check stream activity... Viewers: 0");
//			}
//		
//		}
//		
//		else
//		return;
//		
//	}
//	
//	@Override
//	public String[] getVoicedCommandList()
//	{
//		return COMMANDS;
//	}
//}