package quests;

import java.util.StringTokenizer;

import l2f.gameserver.data.xml.holder.NpcHolder;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.instances.NpcInstance;
import l2f.gameserver.model.quest.Quest;
import l2f.gameserver.model.quest.QuestState;
import l2f.gameserver.scripts.ScriptFile;

/**
 * @author pchayka
 */
public class _254_LegendaryTales extends Quest implements ScriptFile
{
	private static final int Gilmore = 30754;
	private static final int LargeBone = 17249;
	private static final int[] raids =
	{ 25718, 25719, 25720, 25721, 25722, 25723, 25724 };

	public _254_LegendaryTales()
	{
		super(PARTY_ALL);
		addStartNpc(Gilmore);
		addKillId(raids);
		addQuestItem(LargeBone);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if (event.equalsIgnoreCase("gilmore_q254_05.htm"))
		{
			st.setState(STARTED);
			st.setCond(1);
			st.playSound(SOUND_ACCEPT);
		} else if (event.startsWith("gilmore_q254_09.htm"))
		{
			st.takeAllItems(LargeBone);
			StringTokenizer tokenizer = new StringTokenizer(event);
			tokenizer.nextToken();
			switch (Integer.parseInt(tokenizer.nextToken()))
			{
				case 1:
					st.giveItems(13467, 1);
					break;
				case 2:
					st.giveItems(13462, 1);
					break;
				case 3:
					st.giveItems(13464, 1);
					break;
				case 4:
					st.giveItems(13461, 1);
					break;
				case 5:
					st.giveItems(13465, 1);
					break;
				case 6:
					st.giveItems(13463, 1);
					break;
				case 7:
					st.giveItems(13460, 1);
					break;
				case 8:
					st.giveItems(13466, 1);
					break;
				case 9:
					st.giveItems(13459, 1);
					break;
				case 10:
					st.giveItems(13457, 1);
					break;
				case 11:
					st.giveItems(13458, 1);
					break;
				default:
					break;
			}
			st.playSound(SOUND_FINISH);
			st.setState(COMPLETED);
			st.exitCurrentQuest(false);
			htmltext = "gilmore_q254_09.htm";
		}

		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();
		if (npc.getNpcId() == Gilmore)
		{
			if (cond == 0)
			{
				if (st.getPlayer().getLevel() >= 80)
					htmltext = "gilmore_q254_01.htm";
				else
				{
					htmltext = "gilmore_q254_00.htm";
					st.exitCurrentQuest(true);
				}
			} else if (cond == 1)
				htmltext = "gilmore_q254_06.htm";
			else if (cond == 2)
				htmltext = "gilmore_q254_07.htm";
		}

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if (cond == 1)
		{
			int mask = 1;
			int var = npc.getNpcId();
			for (int i = 0; i < raids.length; i++)
			{
				if (raids[i] == var)
					break;
				mask = mask << 1;
			}

			var = st.getInt("RaidsKilled");
			if ((var & mask) == 0) // этого босса еще не убивали
			{
				var |= mask;
				st.set("RaidsKilled", var);
				st.giveItems(LargeBone, 1);
				if (st.getQuestItemsCount(LargeBone) >= 7)
					st.setCond(2);

			}
			checkKilledRaids(st.getPlayer(), var);
		}
		return null;
	}

	public static void checkKilledRaids(Player player, int var)
	{
		player.sendMessage("=== Remaining Dragon(s) ===");
		for (int i : raids)
		{
			int mask = 1;
			for (int b = 0; b < raids.length; b++)
			{
				if (raids[b] == i)
				{
					break;
				}
				mask = mask << 1;
			}

			if ((var & mask) == 0) // этого босса еще не убивали
			{
				String name = NpcHolder.getInstance().getTemplate(i).getName();
				player.sendMessage(name);
			}
		}
	}

	@Override
	public void onLoad()
	{
	}

	@Override
	public void onReload()
	{
	}

	@Override
	public void onShutdown()
	{
	}
}