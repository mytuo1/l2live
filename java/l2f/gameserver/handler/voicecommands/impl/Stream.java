package l2f.gameserver.handler.voicecommands.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import l2f.gameserver.Config;
import l2f.gameserver.ThreadPoolManager;
import l2f.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.Creature;
import l2f.gameserver.model.entity.json.JsonObject;
import l2f.gameserver.network.serverpackets.MagicSkillUse;
import l2f.gameserver.scripts.Functions;
import l2f.gameserver.tables.SkillTable;

public class Stream implements IVoicedCommandHandler
{
	private static int checkTime = 60 * 1000 * 60;
		
	private static final String[] COMMANDS = {
		"stream"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		if (args == null || args.length() == 0)
		{
			player.sendMessage("Usage: .stream username");
			return false;
		}
		if (args.contains(" "))
		{
			Functions.sendDebugMessage(player, args + " is not a valid user name!");
			return false;
		}

		if (isStreamLive(args) && getTitle(args))
		{
			ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable()
			{
				@Override
				public void run()
				{
					if (Config.ALLOW_TWITCH_VIEWS_REWARD)
					{
						reward(player);
					}
					else
					{
						return;
					}
				}
			}, checkTime / 2, checkTime);
		}
		if (!isStreamLive(args) && !getTitle(args))
		{
			player.sendMessage("Cheater! You arent streaming :)");
			return false;
		}
		return false;
		
	}


	private static boolean isStreamLive(String args)
	{					
		try {
					String url = ("https://api.twitch.tv/helix/streams?user_login=" + args);
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
//					JsonObject jsonObj = JsonObject.readFrom(response.toString());
//					String i = jsonObj.get("data").asObject().get("type").asString();
					String v = String.valueOf(response.toString().split(":")[6].replace(",\"title\"", ""));
					
					if ("\"live\"" != v.intern())
					{
					return false;
					}
		}
			catch (IOException ex) 
			{
				ex.printStackTrace();
			}
		
		return true;
	
	}
	
	private static boolean getTitle(String args)
	{	
					
		try {
					String url = ("https://api.twitch.tv/helix/streams?user_login=" + args);
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
//					JsonObject jsonObj = JsonObject.readFrom(response.toString());
//					String i = jsonObj.get("data").asObject().get("title").asString();
					String v = String.valueOf(response.toString().split(":")[7].replace(",\"viewer_count\"", ""));
				
					if ("\"Title!\"" != v.intern())
					{
					return false;
					}
		}
			catch (IOException ex) 
			{
				ex.printStackTrace();
			}
		return true;
	}
	
	private static void reward(Player player)
	{
		player.sendPacket(new MagicSkillUse(player, player, 1363, 46, 0, 0));
		player.sendMessage("Great job, you got CoV Now!");
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}
}