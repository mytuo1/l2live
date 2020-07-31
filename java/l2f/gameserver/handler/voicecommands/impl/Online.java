package l2f.gameserver.handler.voicecommands.impl;

import l2f.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.entity.CCPHelpers.CCPSmallCommands;
import l2f.gameserver.scripts.Functions;

public class Online extends Functions implements IVoicedCommandHandler
{
	private static final String[] COMMANDS =
	{
		"online"
	};

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		String answer = CCPSmallCommands.showOnlineCount();
		if (answer != null)
			activeChar.sendMessage(answer);
			activeChar.sendMessage("Your HWID is " + activeChar.getNetConnection().getStrixClientData().getClientHWID().toString() + " !");
		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}
}