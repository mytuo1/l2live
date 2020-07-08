package l2f.gameserver.handler.voicecommands.impl;

import l2f.gameserver.data.xml.holder.ResidenceHolder;
import l2f.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.entity.residence.Fortress;
import l2f.gameserver.network.serverpackets.ExShowFortressSiegeInfo;
import l2f.gameserver.network.serverpackets.NpcHtmlMessage;

public class FortressManagerVC implements IVoicedCommandHandler {
	private static final String[] VOICED_COMMANDS = { "fort"

	};

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target) {

		if (!target.isEmpty()) {
			int fortId = Integer.parseInt(target);
			Fortress fort = ResidenceHolder.getInstance().getResidence(fortId);
			activeChar.sendPacket(new ExShowFortressSiegeInfo(fort));
		}
			sendHtml(activeChar);
		return true;
	}

	private void sendHtml(Player activeChar) {

		activeChar.sendPacket(new NpcHtmlMessage(0).setFile("command/fortress.htm"));
	}

	@Override
	public String[] getVoicedCommandList() {

		return VOICED_COMMANDS;
	}
}