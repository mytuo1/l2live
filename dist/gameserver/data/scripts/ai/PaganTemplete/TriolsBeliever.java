package ai.PaganTemplete;

import l2f.commons.util.Rnd;
import l2f.gameserver.ai.Mystic;
import l2f.gameserver.model.Creature;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.World;
import l2f.gameserver.model.instances.NpcInstance;
import l2f.gameserver.utils.Location;

/**
 * @author Grivesky
  * - AI for Rabe Boss Andreas Van Halter (29062).
  * - All the information about the AI ​​painted.
  * - AI is tested and works.
 */
public class TriolsBeliever extends Mystic
{
	private boolean _tele = true;

	public static final Location[] locs = {new Location( -16128, -35888, -10726), new Location( -16397, -44970, -10724), new Location( -15729, -42001, -10724)};

	public TriolsBeliever(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if (actor == null)
			return true;

		for (Player player : World.getAroundPlayers(actor, 500, 500))
		{
			if (player == null || !player.isInParty())
				continue;

			if (player.getParty().size() >= 5 && _tele)
			{
				_tele = false;
				player.teleToLocation(Rnd.get(locs));
			}
		}

		return true;
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		_tele = true;
		super.onEvtDead(killer);
	}
}