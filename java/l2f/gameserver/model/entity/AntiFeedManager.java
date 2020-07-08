/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2f.gameserver.model.entity;

import java.util.Map;

import javolution.util.FastMap;
import l2f.gameserver.Config;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.Creature;

public final class AntiFeedManager
{
	private Map<String, Long>	_lastDeathTimes;

	private static final class SingletonHolder
	{
		private static final AntiFeedManager	INSTANCE	= new AntiFeedManager();
	}

	public static final AntiFeedManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private AntiFeedManager()
	{
//		_lastDeathTimes = new FastMap<String, Long>().shared();
		_lastDeathTimes = new FastMap<String, Long>();

	}

	/**
	 * Set time of the last player's death to current
	 * @param objectId Player's objectId
	 */
	public final void setLastDeathTime(String HWID)
	{
		_lastDeathTimes.put(HWID, System.currentTimeMillis());
	}

	/**
	 * Check if current kill should be counted as non-feeded.
	 * @param attacker Attacker character
	 * @param target Target character
	 * @return True if kill is non-feeded.
	 */
	
	public final boolean checks(Creature attackers, Player targets)
	{
		if (!Config.ALT_ANTIFEED_ENABLE)
		{
			return true;
		}

//		final Player targetPlayers = targets; //removed getPlayer() from targets
//		if (targetPlayers == null)
//		{
//			return false;
//		}
		if (Config.ALT_ANTIFEED_INTERVAL > 0 && _lastDeathTimes.containsKey(targets.getNetConnection().getStrixClientData().getClientHWID().toString()))
		{
			if ((System.currentTimeMillis() - _lastDeathTimes.get(targets.getNetConnection().getStrixClientData().getClientHWID().toString())) > Config.ALT_ANTIFEED_INTERVAL)
			{
				return true;
			}
			else if ((System.currentTimeMillis() - _lastDeathTimes.get(targets.getNetConnection().getStrixClientData().getClientHWID().toString())) <= Config.ALT_ANTIFEED_INTERVAL)
			{
				return false;
			}
		}

		if (Config.ALT_ANTIFEED_DUALBOX && attackers.getPlayer() != null)
		{
//			final Player attackerPlayer = attackers.getPlayer();
//			if (attackerPlayer == null)
//			{
//				return false;
//			}
			
			final String targetHWID = targets.getNetConnection().getStrixClientData().getClientHWID().toString();
			final String attackerHWID = attackers.getPlayer().getNetConnection().getStrixClientData().getClientHWID().toString();
			if (attackerHWID.toString() == null || targetHWID.toString() == null || targets.isInOfflineMode() || attackers.getPlayer().isInOfflineMode())
			{
				attackers.sendMessage("You can't benefit from killing Offline Mode Players.");
				return false;
			}
			if (attackerHWID.equals(targetHWID))
			{
				attackers.getPlayer().sendMessage("You can't feed on DualBox.");
				return false;
			}
		}

		return true;
	}
}