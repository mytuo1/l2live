package services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2f.gameserver.model.instances.NpcInstance;
import l2f.gameserver.data.htm.HtmCache;
import l2f.gameserver.model.GameObjectsStorage;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.World;
import l2f.gameserver.network.GameClient;

import l2f.gameserver.network.serverpackets.CreatureSay;
import l2f.gameserver.network.serverpackets.NpcHtmlMessage;
import l2f.gameserver.scripts.Functions;
import l2f.gameserver.scripts.ScriptFile;


public class BugReport extends Functions implements ScriptFile
{
private Logger _log = LoggerFactory.getLogger(BugReport.class);
private static final int NpcId = 37095; // npc id here maybe config
private static String htmlLoc = "data/html-en/merchant/37095.htm"; // maybe add config ?

@Override
public void onLoad() {}
@Override
public void onReload() {}
@Override
public void onShutdown() {}

public BugReport()
{
	Player player = getSelf();
	NpcInstance npc = getNpc();
	if (player == null || npc == null || !NpcInstance.canBypassCheck(player, player.getLastNpc()))
		return;
	
	for (NpcInstance n : World.getAroundNpc(npc))
		if (n.getNpcId() == NpcId)
		{
			show(htmlLoc, player, npc);
			return;
		}
}

public String isEventStarted(String event, Player player, NpcInstance npc)
{
	if (event.startsWith("report"))
	{
		sendReport(event, npc, player, event);
	}
	return "";
}

public String onFirstTalk(NpcInstance npc, Player player)
{
	final int npcId = npc.getNpcId();
	if (npcId == NpcId)
	{
		String html = HtmCache.getInstance().getNotNull(htmlLoc, player);
		html = html.replaceAll("%player%", player.getName());

		NpcHtmlMessage npcHtml = new NpcHtmlMessage(0);
		npcHtml.setHtml(html);
		player.sendPacket(npcHtml);
	}
	return "";
}

public void sendReport(String event, NpcInstance npc, Player player, String command)
{
	StringTokenizer st = new StringTokenizer(command);
	st.nextToken();

	String message = "";
	String _type = null; // General, Fatal, Misuse, Balance, Other
	GameClient info = player.getNetConnection().getConnection().getClient();

	try
	{
		_type = st.nextToken();
		while(st.hasMoreTokens())
			message = message + st.nextToken() + " ";

		if (message.equals(""))
		{
			player.sendMessage("Message box cannot be empty.");
			return;
		}

		String fname = "data/BugReports/" + player.getName() + ".txt";
		File file = new File(fname);
		boolean exist = file.createNewFile();
		if(!exist)
		{
			player.sendMessage("You have already sent a bug report, GMs must check it first. You are going to be able to create a new report after the team examines your previous report.");
			return;
		}

		FileWriter fstream = new FileWriter(fname);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write("Character Info: " + info + "\r\nBug Type: " + _type + "\r\nMessage: " + message);
		player.sendMessage("Report sent succesfully. Thank you!");

		for ( Player allgms : GameObjectsStorage.getAllPlayersForIterate())	
		{
			if (allgms.getAccessLevel() > 0)
			{
			allgms.sendMessage(player.getPlayer().getName().toString() + "has sent a bug report.");
			allgms.sendMessage("Report Type: " + _type + ".");
			allgms.sendPacket(new CreatureSay(0, 0, "Bug Report Manager", player.getPlayer().getName().toString() + " sent a bug report."));
			allgms.sendPacket(new CreatureSay(0, 0, "Report Type", _type + "."));
			}
		}
		_log.info("Character: " + player.getName() + " sent a bug report.");
		out.close();
	}
	catch (Exception e)
	{
		player.sendMessage("Something went wrong try again.");
	}
	}
}