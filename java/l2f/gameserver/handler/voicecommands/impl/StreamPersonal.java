package l2f.gameserver.handler.voicecommands.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import l2f.gameserver.Config;
import l2f.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2f.gameserver.model.Player;
import l2f.gameserver.network.GameClient;
import l2f.gameserver.network.GameClient.GameClientState;
import l2f.gameserver.network.serverpackets.ExBR_PremiumState;
import l2f.gameserver.network.serverpackets.ExShowScreenMessage;
import l2f.gameserver.network.serverpackets.MagicSkillUse;
import l2f.gameserver.scripts.Functions;
import l2f.gameserver.tables.SkillTable;
import l2f.commons.net.nio.impl.ReceivablePacket;


public class StreamPersonal implements IVoicedCommandHandler
{
	
//	private HashMap<String, Thread> userStreamMap = new HashMap<String, Thread>();
//	private HashMap<String, Thread> TwitchUserMap = new HashMap<String, Thread>();
//	private static HashMap<String[], Thread> StreamInfoMap = new HashMap<String[], Thread>();
	public HashMap<String, String> UserMap = new HashMap<String, String>();

//    Map<String, String> HOSTING = new HashMap<>();



	private static int PremiumBuff1ID = Config.PREMIUM_BUFF_1_ID;
	private static int PremiumBuff1Level = Config.PREMIUM_BUFF_1_LEVEL;

	private static int PremiumBuff2ID = Config.PREMIUM_BUFF_2_ID;
	private static int PremiumBuff2Level = Config.PREMIUM_BUFF_2_LEVEL;
//	private GameClient _connection;

	private static int current = (int) (System.currentTimeMillis() / 1000L);
	private static int premtime = (int) (3600000 / 1000L);
	final List<String> accounts = new ArrayList<>();
	
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
		if (UserMap.containsKey(player.toString()))
		{
			player.sendMessage("You character is already being rewarded for a stream.");
			return false;
		}
//		if (userStreamMap.containsKey(player.toString()))
		if ((UserMap.containsKey(player.toString())) && (UserMap.containsValue(args)))
		{
			player.sendMessage("You are already being rewarded for a stream.");
			return false;
		}
//		if (TwitchUserMap.containsKey(args.toString())) removing these, adding a normal map without threads.
		if ( (UserMap.containsValue(args)))
		{
			player.sendMessage("Twitch Personal Stream: This Twitch Username is already being rewarded ingame.");
			return false;
		}
		if (isStreamLive(player, args))
		{
			player.sendMessage("Your stream is not live.");
			return false;
		}
		if ((getViewers(args) >= 0) && !getTitle(args))
		{
		player.sendMessage("Your stream is live, but your channel name does not contain \"L2Mutiny\" in it.");
		return false;
		}
		else
		if ((getViewers(args) >= 0) && getTitle(args))
		{ 
			Thread t = new Thread(new StreamReward(player, args));
//			Thread t2 = new Thread(new StreamReward(player, args));
			player.sendMessage("Twitch Personal Stream: System has been enabled!");
//			TwitchUserMap.put(args.toString(), t);
//			userStreamMap.put(player.toString(), t);
			UserMap.put(player.toString(), args);
			t.start();
//			t2.start();
			return true;
		}
		return false;
	}
	else if (command.equals("streamoff"))
	{

//		if (!userStreamMap.containsKey(player.toString()))
		if (!UserMap.containsKey(player.toString()))
		{
			player.sendMessage("Twitch Personal System: Not Enabled.");
			return false;
		}
		if (player.isInCombat())
		{
			player.sendMessage("Twitch Personal System: Stream can not be turned off while in combat.");
		}
		else
		{
			player.sendMessage("Twitch Personal System: Streaming reward has been stopped");
			if (player.getNetConnection().getBonus() > 7)
			{
       		player.sendPacket(new ExBR_PremiumState(player, false));
       		player.getEffectList().stopAllEffects();
       		player.getNetConnection().setBonus(0);
       		player.getNetConnection().setBonusExpire(current);
       		player.stopBonusTask(true);
			}
			player.getEffectList().stopEffect(PremiumBuff1ID);
			player.getEffectList().stopEffect(PremiumBuff2ID);
			UserMap.keySet().remove(player.toString());
			Thread.currentThread().interrupt();
//			TwitchUserMap.keySet().remove(args);
//			userStreamMap.remove(player.toString()).interrupt();
//			TwitchUserMap.keySet().remove(args);
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
				while ((InputLine = in.readLine()) != null) 
				{
					response.append(InputLine);
				}    
				String off = String.valueOf(response.toString().split(":")[1].replace("[],", ""));

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
	
	public void OnLogout(Player player)
	{
//		GameClientState client = GameClientState.AUTHED;
//		if (client.equals(GameClientState.AUTHED))
//		if  client.stax
//		{
		if (player.getNetConnection().getBonus() > 7)
			{
				player.sendPacket(new ExBR_PremiumState(player, false));
				player.getEffectList().stopAllEffects();
				player.getNetConnection().setBonus(8);
				player.getNetConnection().setBonusExpire(current);
				player.stopBonusTask(true);
			}
			player.getEffectList().stopEffect(PremiumBuff1ID);
			player.getEffectList().stopEffect(PremiumBuff2ID);
			UserMap.keySet().remove(player.toString());
			Thread.currentThread().interrupt();
//		}
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
		    			if (_activeChar.getNetConnection().getBonusExpire() <= (current + 400))
		    			{
		    				_activeChar.sendPacket(new ExBR_PremiumState(_activeChar, false));
		    				_activeChar.getEffectList().stopAllEffects();
		           		_activeChar.getNetConnection().setBonus(0);
		           		_activeChar.getNetConnection().setBonusExpire(current);
		           		_activeChar.stopBonusTask(true);
		    			}
		    			_activeChar.getEffectList().stopEffect(PremiumBuff1ID);
		    			_activeChar.getEffectList().stopEffect(PremiumBuff2ID);
		    			UserMap.keySet().remove(_activeChar.toString());
		    			Thread.currentThread().interrupt();
						return;
					}
					else
					if (_activeChar.isInOlympiadMode())
					{
						_activeChar.sendMessage("Your stream stopped because you're in the Olympiad Games.");
		    			if (_activeChar.getNetConnection().getBonusExpire() <= (current + 400))
		    			{
		    				_activeChar.sendPacket(new ExBR_PremiumState(_activeChar, false));
		    				_activeChar.getEffectList().stopAllEffects();
		           		_activeChar.getNetConnection().setBonus(0);
		           		_activeChar.getNetConnection().setBonusExpire(current);
		           		_activeChar.stopBonusTask(true);
		    			}
		    			_activeChar.getEffectList().stopEffect(PremiumBuff1ID);
		    			_activeChar.getEffectList().stopEffect(PremiumBuff2ID);
		    			UserMap.keySet().remove(_activeChar.toString());
		    			Thread.currentThread().interrupt();
						return;
					}
					else
					if ((getViewers(_args) >= 0) && getTitle(_args))
					{
						if ((getViewers(_args) >= 0) && (getViewers(_args) < 15))
						{
							SkillTable.getInstance().getInfo(PremiumBuff1ID, PremiumBuff1Level).getEffects(_activeChar, _activeChar, false, false);
							_activeChar.broadcastPacket(new MagicSkillUse(_activeChar, _activeChar, PremiumBuff1ID, PremiumBuff1Level, 2, 0));
							_activeChar.sendMessage("Thank you for streaming! You have less then 15 viewers right now.");
						}
						else
						if ((getViewers(_args) >= 15) && (getViewers(_args) <= 29))
						{
							SkillTable.getInstance().getInfo(PremiumBuff1ID, PremiumBuff1Level).getEffects(_activeChar, _activeChar, false, false);
							SkillTable.getInstance().getInfo(PremiumBuff2ID, PremiumBuff2Level).getEffects(_activeChar, _activeChar, false, false);
							_activeChar.broadcastPacket(new MagicSkillUse(_activeChar, _activeChar, PremiumBuff1ID, PremiumBuff1Level, 2, 0));
							_activeChar.broadcastPacket(new MagicSkillUse(_activeChar, _activeChar, PremiumBuff2ID, PremiumBuff2Level, 2, 0));
						    _activeChar.sendMessage("You are streaming ! Thank you for playing L2Mutiny! You have 15 or more Viewers! ");						
						}
						else
						if (getViewers(_args) >= 30)
						{
						SkillTable.getInstance().getInfo(PremiumBuff1ID, PremiumBuff1Level).getEffects(_activeChar, _activeChar, false, false);
						SkillTable.getInstance().getInfo(PremiumBuff2ID, PremiumBuff2Level).getEffects(_activeChar, _activeChar, false, false);
						_activeChar.broadcastPacket(new MagicSkillUse(_activeChar, _activeChar, PremiumBuff1ID, PremiumBuff1Level, 2, 0));
						_activeChar.broadcastPacket(new MagicSkillUse(_activeChar, _activeChar, PremiumBuff2ID, PremiumBuff2Level, 2, 0));
						if ((_activeChar.getNetConnection().getBonus() == 0) || (_activeChar.getNetConnection().getBonus() > 7))
							{
						
								_activeChar.getNetConnection().setBonus(8);
								_activeChar.getNetConnection().setBonusExpire(current + premtime);

								_activeChar.stopBonusTask();
								_activeChar.startBonusTask();

								if(_activeChar.getParty() != null)
									_activeChar.getParty().recalculatePartyData();	
						
								_activeChar.sendPacket(new ExBR_PremiumState(_activeChar, true));	
								_activeChar.sendPacket(new ExShowScreenMessage("You got a premium pack for streaming in L2Mutiny", 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false));
							}
	 					_activeChar.sendMessage("You are streaming for 30 or more people! Thank you for playing L2Mutiny! ");
						}
					}
					Thread.sleep(600000);
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
//				if (_activeChar.getNetConnection().getBonusExpire() <= (current + 400))
				if (_activeChar.getNetConnection().getBonus() > 7)
				{
					_activeChar.sendPacket(new ExBR_PremiumState(_activeChar, false));
					_activeChar.getEffectList().stopAllEffects();
					_activeChar.getNetConnection().setBonus(8);
					_activeChar.getNetConnection().setBonusExpire(current);
					_activeChar.stopBonusTask(true);
				}
				_activeChar.getEffectList().stopEffect(PremiumBuff1ID);
				_activeChar.getEffectList().stopEffect(PremiumBuff2ID);
				UserMap.keySet().remove(_activeChar.toString());
//				TwitchUserMap.keySet().remove(_args);
//				userStreamMap.keySet().remove(_activeChar.toString());	
			}
		}

	}
}