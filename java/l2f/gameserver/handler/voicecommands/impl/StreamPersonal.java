package l2f.gameserver.handler.voicecommands.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import l2f.gameserver.Config;
import l2f.gameserver.ThreadPoolManager;
import l2f.gameserver.handler.items.IItemHandler;
import l2f.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.Creature;
import l2f.gameserver.model.entity.json.JsonObject;
import l2f.gameserver.model.items.ItemInstance;
import l2f.gameserver.network.serverpackets.MagicSkillUse;
import l2f.gameserver.scripts.Functions;
import l2f.gameserver.tables.SkillTable;

public class StreamPersonal implements IVoicedCommandHandler
{
	private static HashMap<String, Thread> userStreamMap = new HashMap<String, Thread>();

		
	private static final String[] COMMANDS = {
		"stream",
		"streamoff"
	};
		
	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{

	if (command.equals("stream"))
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
		if (userStreamMap.containsKey(player.toString()))
		{
			player.sendMessage("Twitch Stream: Already enabled!");
		}
		else
		if (isStreamLive(player, args))
		{
			player.sendMessage("Your stream is not live.");
			return false;
		}
		else
		if ((getViewers(args) >= 0) && !getTitle(args))
		{
		player.sendMessage("Your stream is live, but your channel name does not contain \"L2Mutiny\" in it.");
		return false;
		}
		else
		if ((getViewers(args) >= 0) && getTitle(args))
//		if (isStreamLive(player, args) && getTitle(args))
		{ 
			Thread t = new Thread(new StreamReward(player, args));
			player.sendMessage("Twitch Personal Stream: System has been enabled!");
			userStreamMap.put(player.toString(), t);
			t.start();
			return true;
		}
		return false;
	}
	else if (command.equals("streamoff"))
	{
		if (!userStreamMap.containsKey(player.toString()))
			{
				player.sendMessage("Twitch Personal Stream: System has not been enabled!");
			}
		else
			{
				userStreamMap.remove(player.toString()) // here we get thread and remove it from map
				.interrupt(); // and interrupt it
				player.sendMessage("Twitch Personal Stream: System has been disabled!");
			}
	}
	return false;	
	}


	private static boolean isStreamLive(Player player, String args)
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
//					String v = String.valueOf(response.toString().split(":")[6].replace(",\"title\"", ""));
					String off = String.valueOf(response.toString().split(":")[1].replace("[],", ""));
					
//					if (v.intern().matches("(.*)live(.*)"))
//					{
//					return true;
//					}
//					else
					if (off.intern().matches("(.*)pagination(.*)"))
					{
						return true;
					}
		}
			catch (IOException ex) 
			{
				ex.printStackTrace();
			}
		return false;
	
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
					String v = String.valueOf(response.toString().split(":")[7].replace(",\"viewer_count\"", ""));
				
				    if (v.intern().matches("(.*)L2Mutiny(.*)"))
					{
					return true;
					}
		}
			catch (IOException ex) 
			{
				ex.printStackTrace();
			}
		return false;
	}
	
	private static int getViewers(String args)
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
					String g = String.valueOf(response.toString().split(":")[8].replace(",\"started_at\"", ""));
					int v = Integer.parseInt(g);

					return v;

		}
			catch (IOException ex) 
			{
				ex.printStackTrace();
			}
		return -1;
	}
		
	
	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}
	
	private class StreamReward implements Runnable
	{
		private final Player _activeChar;
		private String _args;

		public StreamReward(Player activeChar, String args)
		{
			this._activeChar = activeChar;
			this._args = args;
		}

		@Override
		public void run()
		{
			try
			{
				while (true)
				{
					// Check if the stream is live
					if (isStreamLive(_activeChar, _args))
					{
						_activeChar.sendMessage("Your stream is offline or has the wrong title.");
						userStreamMap.remove(_activeChar.toString()) // here we get thread and remove it from map
						.interrupt(); // and interrupt it
						return;
					}
					else
					if ((getViewers(_args) >= 0) && getTitle(_args))
//					if (isStreamLive(_activeChar, _args) && getTitle(_args))
					{
					    _activeChar.sendMessage("You are streaming ! Thank you for playing L2Mutiny! ");
					}
					Thread.sleep(60000);
				}
			}
			catch (InterruptedException e)
			{
				// nothing
			}
			catch (Exception e)
			{
				Thread.currentThread().interrupt();
			}
			finally
			{
				_activeChar.sendMessage("You stopped streaming so the rewards will cease.");
				userStreamMap.remove(_activeChar.toString());
			}
		}
	}
}