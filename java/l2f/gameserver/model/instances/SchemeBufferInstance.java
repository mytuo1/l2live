package l2f.gameserver.model.instances;

import gnu.trove.list.array.TIntArrayList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import l2f.gameserver.Config;
import l2f.gameserver.ThreadPoolManager;
import l2f.gameserver.data.htm.HtmCache;
import l2f.gameserver.database.DatabaseFactory;
import l2f.gameserver.instancemanager.ReflectionManager;
import l2f.gameserver.listener.actor.player.OnAnswerListener;
import l2f.gameserver.model.Creature;
import l2f.gameserver.model.Playable;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.Skill;
import l2f.gameserver.model.Summon;
import l2f.gameserver.model.Zone.ZoneType;
import l2f.gameserver.model.entity.events.impl.AbstractFightClub;
import l2f.gameserver.model.entity.olympiad.Olympiad;
import l2f.gameserver.network.serverpackets.ConfirmDlg;
import l2f.gameserver.network.serverpackets.MagicSkillUse;
import l2f.gameserver.network.serverpackets.Say2;
import l2f.gameserver.network.serverpackets.ShowBoard;
import l2f.gameserver.network.serverpackets.SystemMessage;
import l2f.gameserver.network.serverpackets.components.ChatType;
import l2f.gameserver.network.serverpackets.components.SystemMsg;
import l2f.gameserver.scripts.Functions;
import l2f.gameserver.skills.effects.EffectCubic;
import l2f.gameserver.tables.SkillTable;
import l2f.gameserver.templates.npc.NpcTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemeBufferInstance extends NpcInstance
{
	/**
	 *  L2Mythras
	 */
	private static final long serialVersionUID = 1050954721426616212L;

	private static final Logger _log = LoggerFactory.getLogger(SchemeBufferInstance.class);

	private static final boolean DEBUG = false;
	private static final String TITLE_NAME = "Scheme Buffer";
	private static final boolean ENABLE_VIP_BUFFER = Config.NpcBuffer_VIP;
	private static final int VIP_ACCESS_LEVEL = Config.NpcBuffer_VIP_ALV;
	private static final boolean ENABLE_SCHEME_SYSTEM = Config.NpcBuffer_EnableScheme;
	private static final boolean ENABLE_HEAL = Config.NpcBuffer_EnableHeal;
	private static final boolean ENABLE_BUFFS = Config.NpcBuffer_EnableBuffs;
	private static final boolean ENABLE_RESIST = Config.NpcBuffer_EnableResist;
	private static final boolean ENABLE_SONGS = Config.NpcBuffer_EnableSong;
	private static final boolean ENABLE_DANCES = Config.NpcBuffer_EnableDance;
	private static final boolean ENABLE_CHANTS = Config.NpcBuffer_EnableChant;
	private static final boolean ENABLE_OTHERS = Config.NpcBuffer_EnableOther;
	private static final boolean ENABLE_SPECIAL = Config.NpcBuffer_EnableSpecial;
	private static final boolean ENABLE_CUBIC = Config.NpcBuffer_EnableCubic;
	private static final boolean ENABLE_BUFF_REMOVE = Config.NpcBuffer_EnableCancel;
	private static final boolean ENABLE_BUFF_SET = Config.NpcBuffer_EnableBuffSet;
	private static final boolean BUFF_WITH_KARMA = Config.NpcBuffer_EnableBuffPK;
	private static final boolean FREE_BUFFS = Config.NpcBuffer_EnableFreeBuffs;
	private static final int MIN_LEVEL = Config.NpcBuffer_MinLevel;
	private static final int BUFF_REMOVE_PRICE = Config.NpcBuffer_PriceCancel;
	private static final int HEAL_PRICE = Config.NpcBuffer_PriceHeal;
	private static final int BUFF_PRICE = Config.NpcBuffer_PriceBuffs;
	private static final int RESIST_PRICE = Config.NpcBuffer_PriceResist;
	private static final int SONG_PRICE = Config.NpcBuffer_PriceSong;
	private static final int DANCE_PRICE = Config.NpcBuffer_PriceDance;
	private static final int CHANT_PRICE = Config.NpcBuffer_PriceChant;
	private static final int OTHERS_PRICE = Config.NpcBuffer_PriceOther;
	private static final int SPECIAL_PRICE = Config.NpcBuffer_PriceSpecial;
	private static final int CUBIC_PRICE = Config.NpcBuffer_PriceCubic;
	private static final int BUFF_SET_PRICE = Config.NpcBuffer_PriceSet;
	private static final int SCHEME_BUFF_PRICE = Config.NpcBuffer_PriceScheme;
	private static final int SCHEMES_PER_PLAYER = Config.NpcBuffer_MaxScheme;
	private static final int MAX_SCHEME_BUFFS = Config.ALT_BUFF_LIMIT;
	private static final int MAX_SCHEME_DANCES = Config.ALT_MUSIC_LIMIT;
	private static final int CONSUMABLE_ID = 57;

	private static final String SET_FIGHTER = "Fighter";
	private static final String SET_MAGE = "Mage";
	private static final String SET_ALL = "All";
	private static final String SET_NONE = "None";

	private static final String[] SCHEME_ICONS = new String[]
	{
		"Icon.skill1331",
		"Icon.skill1332",
		"Icon.skill1316",
		"Icon.skill1264",
		"Icon.skill1254",
		"Icon.skill1178",
		"Icon.skill1085",
		"Icon.skill957",
		"Icon.skill0928",
		"Icon.skill0793",
		"Icon.skill0787",
		"Icon.skill0490",
		"Icon.skill0487",
		"Icon.skill0452",
		"Icon.skill0453",
		"Icon.skill0440",
		"Icon.skill0409",
		"Icon.skill0405",
		"Icon.skill0061",
		"Icon.skill0072",
		"Icon.skill0219",
		"Icon.skill0208",
		"Icon.skill0210",
		"Icon.skill0254",
		"Icon.skill0228",
		"Icon.skill0222",
		"Icon.skill0181",
		"Icon.skill0078",
		"Icon.skill0091",
		"Icon.skill0076",
		"Icon.skill0025",
		"Icon.skill0018",
		"Icon.skill0019",
		"Icon.skill0007",
		"Icon.skill1391",
		"Icon.skill1373",
		"Icon.skill1388",
		"Icon.skill1409",
		"Icon.skill1457",
		"Icon.skill1501",
		"Icon.skill1520",
		"Icon.skill1506",
		"Icon.skill1527",
		"Icon.skill5016",
		"Icon.skill5860",
		"Icon.skill5661",
		"Icon.skill6302",
		"Icon.skill6171",
		"Icon.skill6286",
		"Icon.skill4106",
		"Icon.skill4270_3"
	};

	private static boolean singleBuffsLoaded = false;
	private static List<SingleBuff> allSingleBuffs = null;

	public SchemeBufferInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);

//		if (!singleBuffsLoaded)
//		{
//			singleBuffsLoaded = true;
//			loadSingleBuffs();
//		}
	}

	private static void loadSingleBuffsPremium()
	{
		allSingleBuffs = new LinkedList<>();
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM npcbuffer_premium_list WHERE canUse = 1 ORDER BY Buff_Class ASC, id");
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				int id = rset.getInt("id");
				int buffClass = rset.getInt("buff_class");
				String buffType = rset.getString("buffType");
				int buffId = rset.getInt("buffId");
				int buffLevel = rset.getInt("buffLevel");
				int forClass = rset.getInt("forClass");
				boolean canUse = rset.getInt("canUse") == 1;

				allSingleBuffs.add(new SingleBuff(id, buffClass, buffType, buffId, buffLevel, forClass, canUse));
			}
		}
		catch (SQLException e)
		{
			_log.error("Error while loading Single Buffs", e);
		}
	}
	private static void loadSingleBuffs()
	{
		allSingleBuffs = new LinkedList<>();
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM npcbuffer_buff_list WHERE canUse = 1 ORDER BY Buff_Class ASC, id");
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				int id = rset.getInt("id");
				int buffClass = rset.getInt("buff_class");
				String buffType = rset.getString("buffType");
				int buffId = rset.getInt("buffId");
				int buffLevel = rset.getInt("buffLevel");
				int forClass = rset.getInt("forClass");
				boolean canUse = rset.getInt("canUse") == 1;

				allSingleBuffs.add(new SingleBuff(id, buffClass, buffType, buffId, buffLevel, forClass, canUse));
			}
		}
		catch (SQLException e)
		{
			_log.error("Error while loading Single Buffs", e);
		}
	}

	private static class SingleBuff
	{
		public final int _buffClass;
		public final String _buffType;
		public final int _buffId;
		public final int _buffLevel;
		public final String _buffName;
		public int _forClass;
		public boolean _canUse;

		private SingleBuff(int id, int buffClass, String buffType, int buffId, int buffLevel, int forClass, boolean canUse)
		{
			_buffClass = buffClass;
			_buffType = buffType;
			_buffId = buffId;
			_buffLevel = buffLevel;
			_forClass = forClass;
			_canUse = canUse;
			_buffName = SkillTable.getInstance().getInfo(buffId, buffLevel).getName();
		}
	}

	public static class PlayerScheme
	{
		public final int schemeId;
		public String schemeName;
		public int iconId;
		public final List<SchemeBuff> schemeBuffs;

		private PlayerScheme(int schemeId, String schemeName, int iconId)
		{
			this.schemeId = schemeId;
			this.schemeName = schemeName;
			this.iconId = iconId;
			schemeBuffs = new ArrayList<>();
		}
	}

	private static class SchemeBuff
	{
		public final int skillId;
		public final int skillLevel;
		public final int forClass;

		private SchemeBuff(int skillId, int skillLevel, int forClass)
		{
			this.skillId = skillId;
			this.skillLevel = skillLevel;
			this.forClass = forClass;
		}
	}

	public static void loadSchemes(Player player, Connection con)
	{
		// Loading Scheme Templates
		try (PreparedStatement statement = con.prepareStatement("SELECT id, scheme_name, icon FROM npcbuffer_scheme_list WHERE player_id=?"))
		{
			statement.setInt(1, player.getObjectId());
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					int schemeId = rset.getInt("id");
					String schemeName = rset.getString("scheme_name");
					int iconId = rset.getInt("icon");
					player.getBuffSchemes().add(new PlayerScheme(schemeId, schemeName, iconId));
				}
			}
		}
		catch (SQLException e)
		{
			_log.error("Error while loading Scheme Content of the Player", e);
		}

		// Loading Scheme Contents
		for (PlayerScheme scheme : player.getBuffSchemes())
		{
			try (PreparedStatement statement = con.prepareStatement("SELECT skill_id, skill_level, buff_class FROM npcbuffer_scheme_contents WHERE scheme_id=?"))
			{
				statement.setInt(1, scheme.schemeId);
				try (ResultSet rset = statement.executeQuery())
				{
					while (rset.next())
					{
						int skillId = rset.getInt("skill_id");
						int skillLevel = rset.getInt("skill_level");
						int forClass = rset.getInt("buff_class");
						scheme.schemeBuffs.add(new SchemeBuff(skillId, skillLevel, forClass));
					}
				}
			}
			catch (SQLException e)
			{
				_log.error("Error while loading Scheme Content of the Player", e);
			}
		}
	}

	private static void setPetBuff(Player player, String eventParam1)
	{
		player.addQuickVar("SchemeBufferPet", Integer.valueOf(eventParam1));
	}

	private static boolean isPetBuff(Player player)
	{
		int value = player.getQuickVarI("SchemeBufferPet");
		return value > 0;
	}

	public static void showWindow(Player player)
	{
		if (!checkConditions(player))
			return;

		if (!ENABLE_VIP_BUFFER || (ENABLE_VIP_BUFFER && (player.getAccessLevel() == VIP_ACCESS_LEVEL)))
		{

		}
		else
		{
			sendErrorMessageToPlayer(player, "This buffer is for VIPs only.");
		}
		showCommunity(player, main(player));
	}

	private static boolean checkConditions(Player player)
	{
		String msg = null;
		int playerReflectionId = player.getReflection().getInstancedZoneId();
		if (playerReflectionId != ReflectionManager.DEFAULT.getId() && playerReflectionId != ReflectionManager.FIGHT_CLUB_REFLECTION_ID)
		{
			msg = "You cannot receive buffs outside the default instance.";
		}
		else if (player.isInOlympiadMode() || Olympiad.isRegistered(player))
		{
			msg = "You cannot receive buffs while registered in the Grand Olympiad.";
		}
		else if (player.getLevel() >= 10 && (player.getPvpFlag() > 0 && !player.isInPeaceZone() || player.isInCombat()))
		{
			msg = "You cannot receive buffs while in combat.";
		}
		else if (!BUFF_WITH_KARMA && (player.getKarma() > 0))
		{
			msg = "Chaotic players may not use the buffer.";
		}
		else if (Olympiad.isRegisteredInComp(player))
		{
			msg = "You cannot use the buffer while participating in the Grand Olympiad.";
		}
		else if (Olympiad.isRegistered(player))
		{
			msg = "You cannot use the buffer while participating in the Grand Olympiad.";
		}
		else if (player.getLevel() < MIN_LEVEL)
		{
			msg = "Your level is too low. You have to be at least level " + MIN_LEVEL + ", to use the buffer.";
		}
		else if ((player.getPvpFlag() > 0) && !Config.SCHEME_ALLOW_FLAG)
		{
			msg = "You cannot receive buffs while flagged. Please, try again later.";
		}
		else if (player.isInCombat() && !Config.SCHEME_ALLOW_FLAG)
		{
			msg = "You cannot receive buffs while in combat.<br>Please, try again later.";
		}
		// Ady - Block time that the player cannot use the community buffer
		else if (player.getResurrectionBuffBlockedTime() > System.currentTimeMillis())
		{
			msg = "You must wait 10 seconds after being resurrected to use the buffer.";
		}

		if (msg == null)
		{
			return true;
		}
		else
		{
			sendErrorMessageToPlayer(player, msg);
			showCommunity(player, main(player));
			return false;
		}
	}

	private static String main(final Player player)
	{
		String dialog = HtmCache.getInstance().getNotNull("scripts/services/communityPVP/buffer_main.htm", player);

		final String bottonA, bottonB, bottonC;
		if (isPetBuff(player))
		{
			bottonA = "Auto Buff Pet";
			bottonB = "Heal My Pet";
			bottonC = "Remove Pet Buffs";
			dialog = dialog.replace("%topbtn%", "<button value=\"" + (player.getPet() != null ? player.getPet().getName() : "You don't have Pet") + "\" action=\"bypass _bbsbufferbypass_buffpet 0 0 0\" width=200 height=30 back=\"L2UI_ct1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">");
		}
		else
		{
			bottonA = "Auto Buff";
			bottonB = "Heal";
			bottonC = "Remove Buffs";

			dialog = dialog.replace("%topbtn%", "<button value=" + player.getName() + " action=\"bypass _bbsbufferbypass_buffpet 1 0 0\" width=200 height=30 back=\"L2UI_ct1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">");
		}

		if (ENABLE_BUFF_SET)
		{
			dialog = dialog.replace("%autobuff%", "<button value=\"" + bottonA + "\" action=\"bypass _bbsbufferbypass_castBuffSet 0 0 0\" width=200 height=30 back=\"L2UI_ct1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">");
		}

		if (ENABLE_HEAL)
		{
			dialog = dialog.replace("%heal%", "<button value=\"" + bottonB + "\" action=\"bypass _bbsbufferbypass_heal 0 0 0\" width=200 height=30 back=\"L2UI_ct1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">");
		}

		if (ENABLE_BUFF_REMOVE)
		{
			dialog = dialog.replace("%removebuffs%", "<button value=\"" + bottonC + "\" action=\"bypass _bbsbufferbypass_removeBuffs 0 0 0\" width=200 height=30 back=\"L2UI_ct1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">");
		}

		if (ENABLE_SCHEME_SYSTEM)
		{
			dialog = dialog.replace("%schemePart%", generateScheme(player));
		}

		if (player.isGM())
		{
			dialog = dialog.replace("%gm%", "<button value=\"Manage Schemes\" action=\"bypass _bbsbufferbypass_redirect manage_buffs 0 0\" width=135 height=30 back=\"L2UI_ct1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">");
		}
		else
		{
			dialog = dialog.replace("%gm%", "");
		}

		dialog = dialog.replace("\r\n", "");
		dialog = dialog.replace("\t", "");

		return dialog;
	}

	private static String viewAllSchemeBuffs(Player player, String scheme, String page)
	{
		int pageN = Integer.parseInt(page);
		int schemeId = Integer.parseInt(scheme);
		String dialog = HtmCache.getInstance().getNotNull("scripts/services/communityPVP/buffer_scheme_buffs.htm", player);

		int[] buffCount = getBuffCount(player, schemeId);
		int TOTAL_BUFF = buffCount[0];
		int BUFF_COUNT = buffCount[1];
		int DANCE_SONG = buffCount[2];

		if (isPetBuff(player))
		{
			dialog = dialog.replace("%topbtn%", (player.getPet() != null ? player.getPet().getName() : "You don't have Pet"));
		}
		else
		{
			dialog = dialog.replace("%topbtn%", player.getName());
		}

		// Buff count
		dialog = dialog.replace("%bcount%", String.valueOf(MAX_SCHEME_BUFFS - BUFF_COUNT));
		dialog = dialog.replace("%dscount%", String.valueOf(MAX_SCHEME_DANCES - DANCE_SONG));

		// Current selected buffs
		final List<SchemeBuff> schemeBuffs = new ArrayList<>();
		final List<SchemeBuff> schemeDances = new ArrayList<>();
		for (SchemeBuff buff : player.getBuffSchemeById(schemeId).schemeBuffs)
		{
			switch (getBuffType(buff.skillId))
			{
				case "song":
				case "dance":
					schemeDances.add(buff);
					break;
				default:
					schemeBuffs.add(buff);
					break;
			}
		}

		final int MAX_ROW_SIZE = 16;
		final int[] ROW_SIZES = new int[] { 12, 12 + 16, 12 + 16 + 12 };

		final StringBuilder addedBuffs = new StringBuilder();
		int row = 0;
		for (int i = 0; i < ROW_SIZES[2]; i++)
		{
			// Open row
			if (i == 0 || (i + 1) - ROW_SIZES[Math.max(row - 1, 0)] == 1)
			{
				addedBuffs.append("<tr>");
			}

			if (row < 2 && schemeBuffs.size() > i)
			{
				final Skill skill = SkillTable.getInstance().getInfo(schemeBuffs.get(i).skillId, schemeBuffs.get(i).skillLevel);
				addedBuffs.append("<td width=34>");
				addedBuffs.append("<table cellspacing=0 cellpadding=0 width=34 height=34 background=" + skill.getIcon() + ">");
				addedBuffs.append("<tr>");
				addedBuffs.append("<td width=34>");
				addedBuffs.append("<button action=\"bypass _bbsbufferbypass_remove_buff " + schemeId + "_" + skill.getId() + "_" + skill.getLevel() + " " + pageN + " x\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"/>");
				addedBuffs.append("</td>");
				addedBuffs.append("</tr>");
				addedBuffs.append("</table>");
				addedBuffs.append("</td>");
			}
			else if (row >= 2 && schemeDances.size() > i - ROW_SIZES[row - 1])
			{
				final Skill skill = SkillTable.getInstance().getInfo(schemeDances.get(i - ROW_SIZES[row - 1]).skillId, schemeDances.get(i - ROW_SIZES[row - 1]).skillLevel);
				addedBuffs.append("<td width=34>");
				addedBuffs.append("<table cellspacing=0 cellpadding=0 width=34 height=34 background=" + skill.getIcon() + ">");
				addedBuffs.append("<tr>");
				addedBuffs.append("<td width=34>");
				addedBuffs.append("<button action=\"bypass _bbsbufferbypass_remove_buff " + schemeId + "_" + skill.getId() + "_" + skill.getLevel() + " " + pageN + " x\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"/>");
				addedBuffs.append("</td>");
				addedBuffs.append("</tr>");
				addedBuffs.append("</table>");
				addedBuffs.append("</td>");
			}
			else
			{
				addedBuffs.append("<td width=34>");
				addedBuffs.append("<table cellspacing=0 cellpadding=0 width=34 height=34 background=L2UI_CH3.multisell_plusicon>");
				addedBuffs.append("<tr>");
				addedBuffs.append("<td width=34>");
				addedBuffs.append("&nbsp;");
				addedBuffs.append("</td>");
				addedBuffs.append("</tr>");
				addedBuffs.append("</table>");
				addedBuffs.append("</td>");
			}

			if (ROW_SIZES[row] < MAX_ROW_SIZE * (row + 1) && i + 1 > ROW_SIZES[row])
			{
				for (int z = ROW_SIZES[row]; z < MAX_ROW_SIZE * (row + 1); z++)
				{
					addedBuffs.append("<td width=1>");
					addedBuffs.append("&nbsp;");
					addedBuffs.append("</td>");
				}
			}

			// Close row
			if ((i + 1) - ROW_SIZES[row] == 0)
			{
				addedBuffs.append("</tr>");
				row++;
			}
		}

		// Current available buffs to add
		final List<Skill> availableSkills = new ArrayList<>();
		for (SingleBuff singleBuff : allSingleBuffs)
		{
			if (!singleBuff._canUse)
				continue;

			// Check if we already added this buff
			boolean hasAddedThisBuff = false;
			for (SchemeBuff buff : schemeBuffs)
			{
				if (buff.skillId == singleBuff._buffId)
				{
					hasAddedThisBuff = true;
					break;
				}
			}
			for (SchemeBuff buff : schemeDances)
			{
				if (buff.skillId == singleBuff._buffId)
				{
					hasAddedThisBuff = true;
					break;
				}
			}
			if (hasAddedThisBuff)
				continue;

			// If we reached the limit dont add dances or buffs
			switch (singleBuff._buffType)
			{
				case "song":
				case "dance":
					if (DANCE_SONG >= MAX_SCHEME_DANCES)
						continue;
					break;
				default:
					if (BUFF_COUNT >= MAX_SCHEME_BUFFS)
						continue;
					break;
			}

			availableSkills.add(SkillTable.getInstance().getInfo(singleBuff._buffId, singleBuff._buffLevel));
		}

		final int SKILLS_PER_ROW = 4;
		final int MAX_SKILLS_ROWS = 3;

		final StringBuilder availableBuffs = new StringBuilder();
		final int maxPage = (int)Math.ceil((double)availableSkills.size() / (SKILLS_PER_ROW * MAX_SKILLS_ROWS) - 1);
		final int currentPage = Math.max(Math.min(maxPage, pageN), 0);
		final int startIndex = currentPage * SKILLS_PER_ROW * MAX_SKILLS_ROWS;
		for (int i = startIndex; i < startIndex + SKILLS_PER_ROW * MAX_SKILLS_ROWS; i++)
		{
			// Open row
			if (i == 0 || i % SKILLS_PER_ROW == 0)
				availableBuffs.append("<tr>");

			if (availableSkills.size() > i)
			{
				final Skill skill = availableSkills.get(i);
				availableBuffs.append("<td>");
				availableBuffs.append("<table cellspacing=2 cellpadding=2 width=140 height=40 bgcolor=000000>");
				availableBuffs.append("<tr>");
				availableBuffs.append("<td>");
				availableBuffs.append("<table border=0 cellspacing=0 cellpadding=0 width=34 height=34 background=" + skill.getIcon() + ">");
				availableBuffs.append("<tr>");
				availableBuffs.append("<td>");
				availableBuffs.append("<table cellspacing=0 cellpadding=0 width=34 height=34 background=L2UI.item_click>");
				availableBuffs.append("<tr>");
				availableBuffs.append("<td>");
				availableBuffs.append("<br>");
				availableBuffs.append("</td>");
				availableBuffs.append("<td height=34>");
				availableBuffs.append("<button action=\"bypass _bbsbufferbypass_add_buff ").append(scheme).append("_").append(skill.getId()).append("_").append(skill.getLevel()).append(" ").append(currentPage).append(" ").append(TOTAL_BUFF).append("\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\">");
				availableBuffs.append("</td>");
				availableBuffs.append("</tr>");
				availableBuffs.append("</table>");
				availableBuffs.append("</td>");
				availableBuffs.append("</tr>");
				availableBuffs.append("</table>");
				availableBuffs.append("</td>");
				availableBuffs.append("<td width=100 align=center>");
				availableBuffs.append("<font name=CREDITTEXTSMALL>" + skill.getName() + "</font>");
				availableBuffs.append("</td>");
				availableBuffs.append("</tr>");
				availableBuffs.append("</table>");
				availableBuffs.append("</td>");
			}
			else
			{
				availableBuffs.append("<td>");
				availableBuffs.append("<table cellspacing=2 cellpadding=2 width=140 height=40>");
				availableBuffs.append("<tr>");
				availableBuffs.append("<td>");
				availableBuffs.append("&nbsp;");
				availableBuffs.append("</td>");
				availableBuffs.append("</tr>");
				availableBuffs.append("</table>");
				availableBuffs.append("</td>");
			}

			// Close row
			if ((i + 1) % SKILLS_PER_ROW == 0 || (i - startIndex) >= SKILLS_PER_ROW * MAX_SKILLS_ROWS)
				availableBuffs.append("</tr>");
		}

		dialog = dialog.replace("%scheme%", scheme);
		dialog = dialog.replace("%addedBuffs%", addedBuffs.toString());
		dialog = dialog.replace("%availableBuffs%", availableBuffs.toString());
		dialog = dialog.replace("%prevPage%", (currentPage > 0 ? "bypass _bbsbufferbypass_manage_scheme_1 " + scheme + " " + (currentPage - 1) + " x" : ""));
		dialog = dialog.replace("%nextPage%", (currentPage < maxPage ? "bypass _bbsbufferbypass_manage_scheme_1 " + scheme + " " + (currentPage + 1) + " x" : ""));
		dialog = dialog.replace("\r\n", "");
		dialog = dialog.replace("\t", "");
		return dialog;
	}

	private static boolean canHeal(Player player)
	{
		if (player.isInFightClub() && player.getFightClubEvent().getState() != AbstractFightClub.EVENT_STATE.PREPARATION)
			return false;
		if (!player.isInFightClub() && (!checkConditions(player) || (!player.isInPeaceZone() && !player.isInZone(ZoneType.RESIDENCE))))
			return false;
		return true;
	}

	private static void heal(Player player, boolean isPet)
	{
		if (!canHeal(player))
			return;
		if (!isPet)
		{
			player.setCurrentHp(player.getMaxHp(), false);
			player.setCurrentMp(player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
			player.broadcastSkillOrSocialAnimation(22217, 1, 0, 0);
		}
		else if (player.getPet() != null)
		{
			Summon pet = player.getPet();
			pet.setCurrentHp(pet.getMaxHp(), false);
			pet.setCurrentMp(pet.getMaxMp());
			pet.setCurrentCp(pet.getMaxCp());
			pet.broadcastPacket(new MagicSkillUse(pet, 22217, 1, 0, 0));
		}
	}

	private static String getSkillIconHtml(int id, int level)
	{
		String iconNumber = getSkillIconNumber(id, level);
		return "<img width=32 height=32 src=\"Icon.skill" + iconNumber + "\">";
	}

	private static String getSkillIconNumber(int id, int level)
	{
		String formato;
		if (id == 4)
		{
			formato = "0004";
		}
		else if ((id > 9) && (id < 100))
		{
			formato = "00" + id;
		}
		else if ((id > 99) && (id < 1000))
		{
			formato = "0" + id;
		}
		else if (id == 1517)
		{
			formato = "1536";
		}
		else if (id == 1518)
		{
			formato = "1537";
		}
		else if (id == 1547)
		{
			formato = "0065";
		}
		else if (id == 2076)
		{
			formato = "0195";
		}
		else if ((id > 4550) && (id < 4555))
		{
			formato = "5739";
		}
		else if ((id > 4698) && (id < 4701))
		{
			formato = "1331";
		}
		else if ((id > 4701) && (id < 4704))
		{
			formato = "1332";
		}
		else if (id == 6049)
		{
			formato = "0094";
		}
		else
		{
			formato = String.valueOf(id);
		}
		return formato;
	}

	private static String getDeleteSchemePage(Player player)
	{
		StringBuilder builder = new StringBuilder();
		String dialog = HtmCache.getInstance().getNotNull("scripts/services/communityPVP/buffer_scheme_delete.htm", player);
		//builder.append("<html><head><title>").append(TITLE_NAME).append("</title></head><body><br><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br><font name=\"hs12\" color=LEVEL>Available schemes:</font><br><br>");

		for (PlayerScheme scheme : player.getBuffSchemes())
		{
			builder.append("<button value=\"").append(scheme.schemeName).append("\" action=\"bypass _bbsbufferbypass_delete ").append(scheme.schemeId).append(" ").append(scheme.schemeName).append(" x\" width=200 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br1>");
		}

		//builder.append("<br><button value=\"Back\" action=\"bypass _bbsbufferbypass_redirect main 0 0\" width=200 height=30 back=\"L2UI_ct1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\"></center>");
		dialog = dialog.replace("%schemes%", builder.toString());
		return dialog;
	}

	private static String getItemNameHtml(Player st, int itemval)
	{
		return "&#" + itemval + ";";
	}

	private static String buildHtml(String buffType, Player player)
	{
		StringBuilder builder = new StringBuilder();
		String dialog = HtmCache.getInstance().getNotNull("scripts/services/communityPVP/buffer_scheme_indbuffs.htm", player);

		//builder.append("<html><head><title>").append(TITLE_NAME).append("</title></head><body><br><center><br>");

		Collection<String> availableBuffs = new ArrayList<>();

		for (SingleBuff buff : allSingleBuffs)
		{
			if (!buff._canUse)
				continue;

			if (buff._buffType.equals(buffType))
			{
				String bName = SkillTable.getInstance().getInfo(buff._buffId, buff._buffLevel).getName();
				bName = bName.replace(" ", "+");
				availableBuffs.add(bName + "_" + buff._buffId + "_" + buff._buffLevel);
			}
		}

		if (availableBuffs.isEmpty())
		{
			builder.append("There are no available buffs at the moment.");
		}
		else
		{
			//builder.append("<font name=\"hs12\" color=LEVEL>Buffer</font>");
			builder.append("<BR1><table width=650>");
			int index = 0;
			for (String buff : availableBuffs)
			{
				if (index % 2 == 0)
				{
					if (index > 0)
						builder.append("</tr>");
					builder.append("<tr>");
				}
				buff = buff.replace("_", " ");
				String[] buffSplit = buff.split(" ");
				String name = buffSplit[0];
				int id = Integer.parseInt(buffSplit[1]);
				int level = Integer.parseInt(buffSplit[2]);
				name = name.replace("+", " ");
				builder.append("<td align=center><table cellspacing=0 cellpadding=0><tr><td align=right>").append(getSkillIconHtml(id, level)).append("</td><td><button value=\"").append(name).append("\" action=\"bypass _bbsbufferbypass_giveBuffs ").append(id).append(" ").append(level).append(" ").append(buffType).append("\" width=190 height=32 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td align=left>").append(getSkillIconHtml(id, level)).append("</td></tr></table></td>");
				index++;
			}
			builder.append("</table>");
		}

		//builder.append("<br><center><button value=\"Back\" action=\"bypass _bbsbufferbypass_redirect main 0 0\" width=200 height=30 back=\"L2UI_ct1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\"></center>");
		dialog = dialog.replace("%buffs%", builder.toString());
		return dialog;
	}

	private static String getEditSchemePage(Player player)
	{
		StringBuilder builder = new StringBuilder();
		String dialog = HtmCache.getInstance().getNotNull("scripts/services/communityPVP/buffer_scheme_menu.htm", player);

		//builder.append("<html><head><title>").append(TITLE_NAME).append("</title></head><body><br><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br><font name=\"hs12\" color=LEVEL>Select a scheme that you would like to manage:</font><br><br>");

		for (PlayerScheme scheme : player.getBuffSchemes())
		{
			builder.append("<button value=\"").append(scheme.schemeName).append("\" action=\"bypass _bbsbufferbypass_manage_scheme_select ").append(scheme.schemeId).append(" x x\" width=200 height=30 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br1>");
		}

		//builder.append("<br><button value=\"Back\" action=\"bypass _bbsbufferbypass_redirect main 0 0\" width=200 height=30 back=\"L2UI_ct1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\"></center>");

		dialog = dialog.replace("%schemes%", builder.toString());
		return dialog;
	}

	private static int[] getBuffCount(Player player, int schemeId)
	{
		int count = 0;
		int D_S_Count = 0;
		int B_Count = 0;

		for (SchemeBuff buff : player.getBuffSchemeById(schemeId).schemeBuffs)
		{
			++count;
			int val = buff.forClass;
			if ((val == 1) || (val == 2))
			{
				++D_S_Count;
			}
			else
			{
				++B_Count;
			}
		}

		return new int[] { count, B_Count, D_S_Count };
	}

	private static String getOptionList(Player player, int schemeId)
	{
		final PlayerScheme scheme = player.getBuffSchemeById(schemeId);
		int[] buffCount = getBuffCount(player, schemeId);
		String dialog = HtmCache.getInstance().getNotNull("scripts/services/communityPVP/buffer_scheme_options.htm", player);

		if (isPetBuff(player))
			dialog = dialog.replace("%topbtn%", (player.getPet() != null ? player.getPet().getName() : "You don't have Pet"));
		else
			dialog = dialog.replace("%topbtn%", player.getName());

		dialog = dialog.replace("%name%", (scheme != null ? scheme.schemeName : ""));
		dialog = dialog.replace("%bcount%", String.valueOf(buffCount[1]));
		dialog = dialog.replace("%dscount%", String.valueOf(buffCount[2]));

		dialog = dialog.replace("%manageBuffs%", "bypass _bbsbufferbypass_manage_scheme_1 " + schemeId + " 0 x");
		dialog = dialog.replace("%changeName%", "bypass _bbsbufferbypass_changeName_1 " + schemeId + " x x");
		dialog = dialog.replace("%changeIcon%", "bypass _bbsbufferbypass_changeIcon_1 " + schemeId + " x x");
		dialog = dialog.replace("%deleteScheme%", "bypass _bbsbufferbypass_delete " + schemeId + " x x");

		return dialog;
	}

	private static String viewAllBuffTypes()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("<html><head><title>").append(TITLE_NAME).append("</title></head><body><br><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br>");
		builder.append("<font color=LEVEL>[Buff management]</font><br>");
		if (ENABLE_BUFFS)
		{
			builder.append("<button value=\"Buffs\" action=\"bypass _bbsbufferbypass_edit_buff_list buff Buffs 1\" width=200 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		}
		if (ENABLE_RESIST)
		{
			builder.append("<button value=\"Resist Buffs\" action=\"bypass _bbsbufferbypass_edit_buff_list resist Resists 1\" width=200 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		}
		if (ENABLE_SONGS)
		{
			builder.append("<button value=\"Songs\" action=\"bypass _bbsbufferbypass_edit_buff_list song Songs 1\" width=200 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		}
		if (ENABLE_DANCES)
		{
			builder.append("<button value=\"Dances\" action=\"bypass _bbsbufferbypass_edit_buff_list dance Dances 1\" width=200 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		}
		if (ENABLE_CHANTS)
		{
			builder.append("<button value=\"Chants\" action=\"bypass _bbsbufferbypass_edit_buff_list chant Chants 1\" width=200 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		}
		if (ENABLE_SPECIAL)
		{
			builder.append("<button value=\"Special Buffs\" action=\"bypass _bbsbufferbypass_edit_buff_list special Special_Buffs 1\" width=200 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		}
		if (ENABLE_OTHERS)
		{
			builder.append("<button value=\"Others Buffs\" action=\"bypass _bbsbufferbypass_edit_buff_list others Others_Buffs 1\" width=200 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		}
		if (ENABLE_CUBIC)
		{
			builder.append("<button value=\"Cubics\" action=\"bypass _bbsbufferbypass_edit_buff_list cubic cubic_Buffs 1\" width=200 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		}
		if (ENABLE_BUFF_SET)
		{
			builder.append("<button value=\"Buff Sets\" action=\"bypass _bbsbufferbypass_edit_buff_list set Buff_Sets 1\" width=200 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br>");
		}
		builder.append("<button value=\"Back\" action=\"bypass _bbsbufferbypass_redirect main 0 0\" width=200 height=30 back=\"L2UI_ct1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\"></center>");
		return builder.toString();
	}

	private static String createScheme(Player player, int iconId)
	{
		String dialog = HtmCache.getInstance().getNotNull("scripts/services/communityPVP/buffer_scheme_create.htm", player);

		if (isPetBuff(player))
		{
			dialog = dialog.replace("%topbtn%", (player.getPet() != null ? player.getPet().getName() : "You don't have Pet"));
		}
		else
		{
			dialog = dialog.replace("%topbtn%", player.getName());
		}

		// Now we assemble the icon list
		final StringBuilder icons = new StringBuilder();
		final int MAX_ICONS_PER_ROW = 17;

		for (int i = 0; i < SCHEME_ICONS.length; i++)
		{
			// Open the new row
			if (i == 0 || (i + 1) % MAX_ICONS_PER_ROW == 1)
				icons.append("<tr>");

			// Draw the icon
			icons.append("<td width=60 align=center valign=top>");
			icons.append("<table border=0 cellspacing=0 cellpadding=0 width=32 height=32 background=" + SCHEME_ICONS[i] + ">");
			icons.append("<tr>");
			icons.append("<td width=32 height=32 align=center valign=top>");
			if (iconId == i)
			{
				icons.append("<table cellspacing=0 cellpadding=0 width=34 height=34 background=L2UI_CT1.ItemWindow_DF_Frame_Over>");
				icons.append("<tr><td align=left>");
				icons.append("&nbsp;");
				icons.append("</td></tr>");
				icons.append("</table>");
			}
			else
				icons.append("<button action=\"bypass _bbsbufferbypass_create_1 " + i + " x x\" width=34 height=34 back=L2UI_CT1.ItemWindow_DF_Frame_Down fore=L2UI_CT1.ItemWindow_DF_Frame />");
			icons.append("</td>");
			icons.append("</tr>");
			icons.append("</table>");
			icons.append("</td>");

			// Close the row
			if ((i + 1) == SCHEME_ICONS.length || (i + 1) % MAX_ICONS_PER_ROW == 0)
				icons.append("</tr>");
		}

		dialog = dialog.replace("%iconList%", icons.toString());
		dialog = dialog.replace("%iconId%", String.valueOf(iconId));

		return dialog;
	}

	private static String changeSchemeIcon(Player player, int schemeId)
	{
		String dialog = HtmCache.getInstance().getNotNull("scripts/services/communityPVP/buffer_scheme_change_icon.htm", player);

		if (isPetBuff(player))
		{
			dialog = dialog.replace("%topbtn%", (player.getPet() != null ? player.getPet().getName() : "You don't have Pet"));
		}
		else
		{
			dialog = dialog.replace("%topbtn%", player.getName());
		}

		// Now we assemble the icon list
		final StringBuilder icons = new StringBuilder();
		final int MAX_ICONS_PER_ROW = 17;

		for (int i = 0; i < SCHEME_ICONS.length; i++)
		{
			// Open the new row
			if (i == 0 || (i + 1) % MAX_ICONS_PER_ROW == 1)
				icons.append("<tr>");

			// Draw the icon
			icons.append("<td width=60 align=center valign=top>");
			icons.append("<table border=0 cellspacing=0 cellpadding=0 width=32 height=32 background=" + SCHEME_ICONS[i] + ">");
			icons.append("<tr>");
			icons.append("<td width=32 height=32 align=center valign=top>");
			icons.append("<button action=\"bypass _bbsbufferbypass_changeIcon " + schemeId + " " + i + " x x\" width=34 height=34 back=L2UI_CT1.ItemWindow_DF_Frame_Down fore=L2UI_CT1.ItemWindow_DF_Frame />");
			icons.append("</td>");
			icons.append("</tr>");
			icons.append("</table>");
			icons.append("</td>");

			// Close the row
			if ((i + 1) == SCHEME_ICONS.length || (i + 1) % MAX_ICONS_PER_ROW == 0)
				icons.append("</tr>");
		}

		dialog = dialog.replace("%iconList%", icons.toString());

		return dialog;
	}

	private static Collection<String> generateQuery(int case1, int case2)
	{
		Collection<String> buffTypes = new ArrayList<>();
		if (ENABLE_BUFFS)
		{
			if (case1 < MAX_SCHEME_BUFFS)
			{
				buffTypes.add("buff");
			}
		}
		if (ENABLE_RESIST)
		{
			if (case1 < MAX_SCHEME_BUFFS)
			{
				buffTypes.add("resist");
			}
		}
		if (ENABLE_SONGS)
		{
			if (case2 < MAX_SCHEME_DANCES)
			{
				buffTypes.add("song");
			}
		}
		if (ENABLE_DANCES)
		{
			if (case2 < MAX_SCHEME_DANCES)
			{
				buffTypes.add("dance");
			}
		}
		if (ENABLE_CHANTS)
		{
			if (case1 < MAX_SCHEME_BUFFS)
			{
				buffTypes.add("chant");
			}
		}
		if (ENABLE_OTHERS)
		{
			if (case1 < MAX_SCHEME_BUFFS)
			{
				buffTypes.add("others");
			}
		}
		if (ENABLE_SPECIAL)
		{
			if (case1 < MAX_SCHEME_BUFFS)
			{
				buffTypes.add("special");
			}
		}
		return buffTypes;
	}

	private static String generateScheme(Player player)
	{
		StringBuilder mainBuilder = new StringBuilder();

		// Create Scheme
		mainBuilder.append("<tr>");
		mainBuilder.append("<td width=240 height=30 valign=top align=center>");
		mainBuilder.append("<table border=0 width=240 height=40 cellspacing=4 cellpadding=3 bgcolor=10100E>");
		mainBuilder.append("<tr>");
		mainBuilder.append("<td align=right valign=top>");
		mainBuilder.append("<table border=0 cellspacing=0 cellpadding=0 width=32 height=32 background=Icon.skill1510>");
		mainBuilder.append("<tr>");
		mainBuilder.append("<td width=32 height=32 align=center valign=top>");
		mainBuilder.append("<button action=\"bypass _bbsbufferbypass_create_1 0 x x\" width=34 height=34 back=L2UI_CT1.ItemWindow_DF_Frame_Down fore=L2UI_CT1.ItemWindow_DF_Frame />");
		mainBuilder.append("</td>");
		mainBuilder.append("</tr>");
		mainBuilder.append("</table>");
		mainBuilder.append("</td>");
		mainBuilder.append("<td width=150 valign=top>");
		mainBuilder.append("<font name=hs12 color=ADA71B>New Scheme</font><br1>");
		mainBuilder.append("<font color=FFFFFF name=__SYSTEMWORLDFONT>Free of Choice</font>");
		mainBuilder.append("</td>");
		mainBuilder.append("</tr>");
		mainBuilder.append("</table>");
		mainBuilder.append("<br>");
		mainBuilder.append("</td>");
		mainBuilder.append("</tr>");

		// Player Schemes
		final Iterator<PlayerScheme> it = player.getBuffSchemes().iterator();
		for (int i = 0; i < SCHEMES_PER_PLAYER; i++)
		{
			if (it.hasNext())
			{
				final PlayerScheme scheme = it.next();
				mainBuilder.append("<tr>");
				mainBuilder.append("<td width=240 height=30 valign=top align=center>");
				mainBuilder.append("<table border=0 width=240 height=40 cellspacing=4 cellpadding=3 bgcolor=10100E>");
				mainBuilder.append("<tr>");
				mainBuilder.append("<td align=right valign=top>");
				mainBuilder.append("<table border=0 cellspacing=0 cellpadding=0 width=32 height=32 background=" + SCHEME_ICONS[scheme.iconId] + ">");
				mainBuilder.append("<tr>");
				mainBuilder.append("<td width=32 height=32 align=center valign=top>");
				mainBuilder.append("<button action=\"bypass _bbsbufferbypass_cast " + scheme.schemeId + " x x\" width=34 height=34 back=L2UI_CT1.ItemWindow_DF_Frame_Down fore=L2UI_CT1.ItemWindow_DF_Frame />");
				mainBuilder.append("</td>");
				mainBuilder.append("</tr>");
				mainBuilder.append("</table>");
				mainBuilder.append("</td>");
				mainBuilder.append("<td width=120 valign=top>");
				mainBuilder.append("<font name=hs12 color=ADA71B>" + scheme.schemeName + "</font><br1>");
				mainBuilder.append("<font color=FFFFFF name=__SYSTEMWORLDFONT>Price: 60,000 Adena</font>");
				mainBuilder.append("</td>");
				mainBuilder.append("<td width=30 align=center>");
				mainBuilder.append("<br>");
				mainBuilder.append("<button action=\"bypass _bbsbufferbypass_manage_scheme_select " + scheme.schemeId + " x x\" width=32 height=32 back=L2UI_CT1.RadarMap_DF_OptionBtn_Down fore=L2UI_CT1.RadarMap_DF_OptionBtn />");
				mainBuilder.append("</td>");
				mainBuilder.append("</tr>");
				mainBuilder.append("</table>");
				mainBuilder.append("<br>");
				mainBuilder.append("</td>");
				mainBuilder.append("</tr>");
			}
			else
			{
				mainBuilder.append("<tr>");
				mainBuilder.append("<td width=240 height=50 valign=top align=center></td>");
				mainBuilder.append("</tr>");
			}
		}

		return mainBuilder.toString();
	}

	private static String getBuffType(int id)
	{
		for (SingleBuff singleBuff : allSingleBuffs)
		{
			if (!singleBuff._canUse)
				continue;

			if (singleBuff._buffId == id)
			{
				return singleBuff._buffType;
			}
		}
		return "none";
	}

	private static boolean isEnabled(int id, int level)
	{
		for (SingleBuff singleBuff : allSingleBuffs)
		{
			if (singleBuff._buffId != id || singleBuff._buffLevel != level)
				continue;

			return singleBuff._canUse;
		}

		return false;
	}

	private static int getClassBuff(int id)
	{
		for (SingleBuff singleBuff : allSingleBuffs)
		{
			if (!singleBuff._canUse)
				continue;

			if (singleBuff._buffId == id)
				return singleBuff._buffClass;
		}

		return 0;
	}

	private static String viewAllBuffs(String type, String typeName, String page)
	{
		final List<SingleBuff> buffList = new ArrayList<SingleBuff>();
		StringBuilder builder = new StringBuilder();
		builder.append("<html><head><title>").append(TITLE_NAME).append("</title></head><body><br><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br>");

		typeName = typeName.replace("_", " ");

		Collection<String> types;
		if (type.equals("set"))
			types = generateQuery(0, 0);
		else
		{
			types = new ArrayList<>();
			types.add(type);
		}

		for (SingleBuff singleBuff : allSingleBuffs)
		{
			if (types.contains(singleBuff._buffType))
			{
				buffList.add(singleBuff);
			}
		}

		Collections.sort(buffList, new Comparator<SingleBuff>()
		{
			@Override
			public int compare(SingleBuff left, SingleBuff right)
			{
				return left._buffName.compareToIgnoreCase(right._buffName);
			}

		});

		builder.append("<font color=LEVEL>[Buff management - ").append(typeName).append(" - Page ").append(page).append("]</font><br><table border=0><tr>");
		final int buffsPerPage;
		if (type.equals("set"))
		{
			buffsPerPage = 12;
		}
		else
		{
			buffsPerPage = 20;
		}
		final String width, pageName;
		int pc = ((buffList.size() - 1) / buffsPerPage) + 1;
		if (pc > 5)
		{
			width = "25";
			pageName = "P";
		}
		else
		{
			width = "50";
			pageName = "Page ";
		}
		typeName = typeName.replace(" ", "_");
		for (int ii = 1; ii <= pc; ++ii)
		{
			if (ii == Integer.parseInt(page))
			{
				builder.append("<td width=").append(width).append(" align=center><font color=LEVEL>").append(pageName).append(ii).append("</font></td>");
			}
			else
			{
				builder.append("<td width=").append(width).append("><button value=\"").append(pageName).append(ii).append("\" action=\"bypass _bbsbufferbypass_edit_buff_list ").append(type).append(" ").append(typeName).append(" ").append(ii).append("\" width=").append(width).append(" height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			}
		}
		builder.append("</tr></table><br>");

		int limit = buffsPerPage * Integer.parseInt(page);
		int start = limit - buffsPerPage;
		int end = Math.min(limit, buffList.size());
		for (int i = start; i < end; ++i)
		{
			final SingleBuff buff = buffList.get(i);
			if ((i % 2) != 0)
			{
				builder.append("<BR1><table border=0 bgcolor=333333>");
			}
			else
			{
				builder.append("<BR1><table border=0 bgcolor=292929>");
			}
			if (type.equals("set"))
			{
				String listOrder = null;
				if (buff._forClass == 0)
				{
					listOrder = "List=\"" + SET_FIGHTER + ";" + SET_MAGE + ";" + SET_ALL + ";" + SET_NONE + ";\"";
				}
				else if (buff._forClass == 1)
				{
					listOrder = "List=\"" + SET_MAGE + ";" + SET_FIGHTER + ";" + SET_ALL + ";" + SET_NONE + ";\"";
				}
				else if (buff._forClass == 2)
				{
					listOrder = "List=\"" + SET_ALL + ";" + SET_FIGHTER + ";" + SET_MAGE + ";" + SET_NONE + ";\"";
				}
				else if (buff._forClass == 3)
				{
					listOrder = "List=\"" + SET_NONE + ";" + SET_FIGHTER + ";" + SET_MAGE + ";" + SET_ALL + ";\"";
				}
				builder.append("<tr><td fixwidth=145>").append(buff._buffName).append("</td><td width=70><combobox var=\"newSet").append(i).append("\" width=70 ").append(listOrder).append("></td><td width=50><button value=\"Update\" action=\"bypass _bbsbufferbypass_changeBuffSet ").append(buff._buffId).append(" ").append(buff._buffLevel).append(" $newSet").append(i).append(" ").append(page).append(" ").append(buff._buffType).append("\" width=50 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
			}
			else
			{
				builder.append("<tr><td fixwidth=170>").append(buff._buffName).append("</td><td width=80>");
				if (buff._canUse)
				{
					builder.append("<button value=\"Disable\" action=\"bypass _bbsbufferbypass_editSelectedBuff ").append(buff._buffId).append(" ").append(buff._buffLevel).append(" 0 ").append(page).append(" ").append(type).append("\" width=80 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
				}
				else
				{
					builder.append("<button value=\"Enable\" action=\"bypass _bbsbufferbypass_editSelectedBuff ").append(buff._buffId).append(" ").append(buff._buffLevel).append(" 1 ").append(page).append(" ").append(type).append("\" width=80 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
				}
			}
			builder.append("</table>");
		}
		builder.append("<br><br><button value=\"Back\" action=\"bypass _bbsbufferbypass_redirect manage_buffs 0 0\" width=200 height=30 back=\"L2UI_ct1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\"><button value=\"Home\" action=\"bypass _bbsbufferbypass_redirect main 0 0\" width=200 height=30 back=\"L2UI_ct1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\"></center>");
		return builder.toString();
	}

	public static void onBypass(final Player player, String command)
	{
		/*Creature npc = player;
		if (!player.isInFightClub())
		{
			if (player.getTarget() != null)
			{
				if (player.getTarget().isNpc() && player.getTarget() instanceof SchemeBufferInstance)
				{
					npc = ((Creature) player.getTarget());
					if (!player.isInRange(npc, 240))
						return;
				}
				else
					return;
			}
			else
				return;
		}*/
		Creature npc = player;
		final Creature npc2 = npc;
		if (!checkConditions(player))
			return;

		String msg = null;

		String[] eventSplit = command.split(" ");
		if (eventSplit.length < 4)
			return;

		// If buffs were not loaded, load them now
		if (!singleBuffsLoaded)
		{
			singleBuffsLoaded = true;
			if (player.getNetConnection().getBonus() == 0) 
			{
				loadSingleBuffs();
			}
			else if (player.getNetConnection().getBonus() != 0)
			{
				loadSingleBuffsPremium();
			}
		}

		String eventParam0 = eventSplit[0];
		String eventParam1 = eventSplit[1];
		String eventParam2 = eventSplit[2];
		String eventParam3 = eventSplit[3];

		if (!eventParam0.equals("heal") && !canHeal(player) && !player.containsQuickVar("BackHpOn"))
		{
			player.addQuickVar("BackHpOn", true);
			Playable target = isPetBuff(player) ? player.getPet() : player;
			if (!isPetBuff(player))
				ThreadPoolManager.getInstance().schedule(new BackHp(target, target.getCurrentHp(), target.getCurrentMp(), target.getCurrentCp()), 250);
			if (player.getPet() != null)
				ThreadPoolManager.getInstance().schedule(new BackHp(player.getPet(), target.getCurrentHp(), target.getCurrentMp(), target.getCurrentCp()), 250);
		}

		if (!FREE_BUFFS)
		{
			if (player.getAdena() < SCHEME_BUFF_PRICE)
			{
				sendErrorMessageToPlayer(player, "You do not have enough Adena. You need at least " + SCHEME_BUFF_PRICE + " Adena.");
				showCommunity(player, main(player));
				return;
			}
		}

		if (eventParam0.equalsIgnoreCase("buffpet"))
		{
			setPetBuff(player, eventParam1);
			msg = main(player);
		}
		else if (eventParam0.equals("redirect"))
		{
			if (eventParam1.equals("main"))
			{
				msg = main(player);
			}
			else if (eventParam1.equals("manage_buffs"))
			{
				msg = viewAllBuffTypes();
			}
			else if (eventParam1.equals("view_buffs"))
			{
				msg = buildHtml("buff", player);
			}
			else if (eventParam1.equals("view_resists"))
			{
				msg = buildHtml("resist", player);
			}
			else if (eventParam1.equals("view_songs"))
			{
				msg = buildHtml("song", player);
			}
			else if (eventParam1.equals("view_dances"))
			{
				msg = buildHtml("dance", player);
			}
			else if (eventParam1.equals("view_chants"))
			{
				msg = buildHtml("chant", player);
			}
			else if (eventParam1.equals("view_others"))
			{
				msg = buildHtml("others", player);
			}
			else if (eventParam1.equals("view_special"))
			{
				msg = buildHtml("special", player);
			}
			else if (eventParam1.equals("view_cubic"))
			{
				msg = buildHtml("cubic", player);
			}
			else if (DEBUG)
			{
				throw new RuntimeException();
			}
		}
		else if (eventParam0.equalsIgnoreCase("edit_buff_list"))
		{
			msg = viewAllBuffs(eventParam1, eventParam2, eventParam3);
		}
		else if (eventParam0.equalsIgnoreCase("changeBuffSet"))
		{
			final int skillId = Integer.parseInt(eventParam1);
			final int skillLevel = Integer.parseInt(eventParam2);
			final String page = eventSplit[4];
			final String type = eventSplit[5];
			int forClass = 0;
			if (eventParam3.equals(SET_FIGHTER))
			{
				forClass = 0;
			}
			else if (eventParam3.equals(SET_MAGE))
			{
				forClass = 1;
			}
			else if (eventParam3.equals(SET_ALL))
			{
				forClass = 2;
			}
			else if (eventParam3.equals(SET_NONE))
			{
				forClass = 3;
			}
			else if (DEBUG)
			{
				throw new RuntimeException();
			}

			// Alexander - First update the buff class on the array
			for (SingleBuff buff : allSingleBuffs)
			{
				if (!buff._buffType.equals(type))
					continue;

				if (buff._buffId != skillId || buff._buffLevel != skillLevel)
					continue;

				buff._forClass = forClass;
			}

			// Alexander - Then update the db
			try (Connection con = DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("UPDATE npcbuffer_buff_list SET forClass=? WHERE buffId=? AND buffLevel=? AND buffType=?"))
			{
				statement.setInt(1, forClass);
				statement.setInt(2, skillId);
				statement.setInt(3, skillLevel);
				statement.setString(4, type);
				statement.executeUpdate();
			}
			catch (Exception e)
			{
				_log.error("Error while updating forClass on selected buff", e);
			}

			msg = viewAllBuffs("set", "Buff_Sets", page);
		}
		else if (eventParam0.equalsIgnoreCase("editSelectedBuff"))
		{
			final int skillId = Integer.parseInt(eventParam1);
			final int skillLevel = Integer.parseInt(eventParam2);
			final int mustEnable = Integer.parseInt(eventParam3);
			final String page = eventSplit[4];
			String typeName = eventSplit[5];

			if (typeName.equalsIgnoreCase("buff"))
			{
				typeName = "Buffs";
			}
			else if (typeName.equalsIgnoreCase("resist"))
			{
				typeName = "Resists";
			}
			else if (typeName.equalsIgnoreCase("song"))
			{
				typeName = "Songs";
			}
			else if (typeName.equalsIgnoreCase("dance"))
			{
				typeName = "Dances";
			}
			else if (typeName.equalsIgnoreCase("chant"))
			{
				typeName = "Chants";
			}
			else if (typeName.equalsIgnoreCase("others"))
			{
				typeName = "Others_Buffs";
			}
			else if (typeName.equalsIgnoreCase("special"))
			{
				typeName = "Special_Buffs";
			}
			else if (typeName.equalsIgnoreCase("cubic"))
			{
				typeName = "Cubics";
			}
			else if (DEBUG)
			{
				throw new RuntimeException();
			}

			// Alexander - First remove or add the buff on the array
			for (SingleBuff buff : allSingleBuffs)
			{
				if (!buff._buffType.equals(eventSplit[5]))
					continue;

				if (buff._buffId != skillId || buff._buffLevel != skillLevel)
					continue;

				buff._canUse = (mustEnable == 1);
			}

			// Alexander - Then update the db
			try (Connection con = DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("UPDATE npcbuffer_buff_list SET canUse=? WHERE buffId=? AND buffLevel=? AND buffType=?"))
			{
				statement.setInt(1, mustEnable);
				statement.setInt(2, skillId);
				statement.setInt(3, skillLevel);
				statement.setString(4, eventSplit[5]);
				statement.executeUpdate();
			}
			catch (Exception e)
			{
				_log.error("Error while updating canUse on selected buff", e);
			}

			msg = viewAllBuffs(eventSplit[5], typeName, page);
		}
		else if (eventParam0.equalsIgnoreCase("giveBuffs"))
		{
			int cost = 0;
			if (eventParam3.equalsIgnoreCase("special"))
			{
				cost = BUFF_PRICE;
			}
			else if (eventParam3.equalsIgnoreCase("resist"))
			{
				cost = RESIST_PRICE;
			}
			else if (eventParam3.equalsIgnoreCase("song"))
			{
				cost = SONG_PRICE;
			}
			else if (eventParam3.equalsIgnoreCase("dance"))
			{
				cost = DANCE_PRICE;
			}
			else if (eventParam3.equalsIgnoreCase("chant"))
			{
				cost = CHANT_PRICE;
			}
			else if (eventParam3.equalsIgnoreCase("others"))
			{
				cost = OTHERS_PRICE;
			}
			else if (eventParam3.equalsIgnoreCase("others"))
			{
				cost = OTHERS_PRICE;
			}
			else if (eventParam3.equalsIgnoreCase("special"))
			{
				cost = SPECIAL_PRICE;
			}
			else if (eventParam3.equalsIgnoreCase("cubic"))
			{
				cost = CUBIC_PRICE;
			}
			else if (DEBUG)
			{
				throw new RuntimeException();
			}

			if (!FREE_BUFFS)
			{
				if (Functions.getItemCount(player, CONSUMABLE_ID) < cost)
				{
					sendErrorMessageToPlayer(player, "You do not have the necessary items. You need: " + cost + " " + getItemNameHtml(player, CONSUMABLE_ID) + ".");
					showCommunity(player, main(player));
					return;
				}
			}
			if (!isEnabled(Integer.parseInt(eventParam1), Integer.parseInt(eventParam2)) || player.isBlocked())
				return;

			final boolean getpetbuff = isPetBuff(player);
			if (!getpetbuff)
			{
				if (eventParam3.equals("cubic"))
				{
					if (player.getCubics() != null)
					{
						for (EffectCubic cubic : player.getCubics())
						{
							cubic.exit();
							player.getCubic(cubic.getId()).exit();
						}
					}
					player.onMagicUseTimer(player, SkillTable.getInstance().getInfo(Integer.parseInt(eventParam1), Integer.parseInt(eventParam2)), false);
				}
				else
				{
					SkillTable.getInstance().getInfo(Integer.parseInt(eventParam1), Integer.parseInt(eventParam2)).getEffects(player, player, false, false);
					npc.broadcastPacket(new MagicSkillUse(npc, player, Integer.parseInt(eventParam1), Integer.parseInt(eventParam2), 2, 0));
				}
			}
			else
			{
				if (eventParam3.equals("cubic"))
				{
					if (player.getCubics() != null)
					{
						for (EffectCubic cubic : player.getCubics())
						{
							cubic.exit();
							player.getCubic(cubic.getId()).exit();
						}
					}
					player.onMagicUseTimer(player, SkillTable.getInstance().getInfo(Integer.parseInt(eventParam1), Integer.parseInt(eventParam2)), false);
				}
				else
				{
					if (player.getPet() != null)
					{
						SkillTable.getInstance().getInfo(Integer.parseInt(eventParam1), Integer.parseInt(eventParam2)).getEffects(player.getPet(), player.getPet(), false, false);
						npc.broadcastPacket(new MagicSkillUse(npc, player.getPet(), Integer.parseInt(eventParam1), Integer.parseInt(eventParam2), 0, 0));
					}
					else
					{
						sendErrorMessageToPlayer(player, "You do not have a servitor. Summon your pet first!");
						showCommunity(player, main(player));
						return;
					}
				}
			}
			//Functions.removeItem(player, CONSUMABLE_ID, cost, "Scheme Buffer");

			msg = buildHtml(eventParam3, player);
		}
		else if (eventParam0.equalsIgnoreCase("castBuffSet"))
		{
			if (player.isBlocked())
				return;
			if (!FREE_BUFFS)
			{
				if (Functions.getItemCount(player, CONSUMABLE_ID) < BUFF_SET_PRICE)
				{
					sendErrorMessageToPlayer(player, "You do not have enough necessary items. You need: " + BUFF_SET_PRICE + " " + getItemNameHtml(player, CONSUMABLE_ID) + ".");
					return;
				}
			}
			final List<int[]> buff_sets = new ArrayList<int[]>();
			final int player_class;
			if (player.isMageClass())
			{
				player_class = 1;
			}
			else
			{
				player_class = 0;
			}
			final boolean getpetbuff = isPetBuff(player);
			if (!getpetbuff)
			{
				for (SingleBuff buff : allSingleBuffs)
				{
					if (!buff._canUse)
						continue;

					if (buff._forClass == player_class || buff._forClass == 2)
					{
						buff_sets.add(new int[] { buff._buffId, buff._buffLevel });
					}
				}

				ThreadPoolManager.getInstance().execute(new Runnable()
				{
					@Override
					public void run()
					{
						for (int[] i : buff_sets)
						{
							SkillTable.getInstance().getInfo(i[0], i[1]).getEffects(player, player, false, false, false, false);
							 npc2.broadcastPacket(new MagicSkillUse(npc2, player, i[0], i[1], 0, 0));
						}
					}
				});
			}
			else
			{
				if (player.getPet() != null)
				{
					for (SingleBuff buff : allSingleBuffs)
					{
						if (!buff._canUse)
							continue;

						if (buff._forClass == 0 || buff._forClass == 2)
						{
							buff_sets.add(new int[] { buff._buffId, buff._buffLevel });
						}
					}

					ThreadPoolManager.getInstance().execute(new Runnable()
					{
						@Override
						public void run()
						{
							for (int[] i : buff_sets)
							{
								SkillTable.getInstance().getInfo(i[0], i[1]).getEffects(player.getPet(), player.getPet(), false, false, false, false);
								npc2.broadcastPacket(new MagicSkillUse(npc2, player, i[0], i[1], 0, 0));
							}
						}
					});
				}
				else
				{
					sendErrorMessageToPlayer(player, "You do not have a servitor summoned. Please summon your servitor and try again.");
					showCommunity(player, main(player));
					return;
				}
			}
			Functions.removeItem(player, CONSUMABLE_ID, BUFF_SET_PRICE, "SchemeBuffer");

			msg = main(player);
		}
		else if (eventParam0.equalsIgnoreCase("heal"))
		{
			if (Functions.getItemCount(player, CONSUMABLE_ID) < HEAL_PRICE)
			{
				sendErrorMessageToPlayer(player, "You don't have enough Adena!");
				showCommunity(player, main(player)); // Resend the main page or the cb will get stucked
				return;
			}
			if (!canHeal(player))
			{
				sendErrorMessageToPlayer(player, "You cannot heal outside of town!");
				showCommunity(player, main(player)); // Resend the main page or the cb will get stucked
				return;
			}
			final boolean getpetbuff = isPetBuff(player);
			if (getpetbuff)
			{
				if (player.getPet() != null)
				{
					heal(player, getpetbuff);
				}
				else
				{
					sendErrorMessageToPlayer(player, "You do not have a servitor summoned. Please summon your servitor and try again.");
					showCommunity(player, main(player)); // Resend the main page or the cb will get stucked
					return;
				}
			}
			else
			{
				heal(player, getpetbuff);
			}
			Functions.removeItem(player, CONSUMABLE_ID, HEAL_PRICE, "SchemeBuffer");

			msg = main(player);
		}
		else if (eventParam0.equalsIgnoreCase("removeBuffs"))
		{
			if (Functions.getItemCount(player, CONSUMABLE_ID) < BUFF_REMOVE_PRICE)
			{
				sendErrorMessageToPlayer(player, "You don't have enough Adena!");
				showCommunity(player, main(player)); // Resend the main page or the cb will get stucked
				return;
			}
			final boolean getpetbuff = isPetBuff(player);
			if (getpetbuff)
			{
				if (player.getPet() != null)
				{
					player.getPet().getEffectList().stopAllEffects();
				}
				else
				{
					sendErrorMessageToPlayer(player, "You do not have a servitor summoned. Please summon your servitor and try again.");
					showCommunity(player, main(player)); // Resend the main page or the cb will get stucked
					return;
				}
			}
			else
			{
				player.getEffectList().stopAllEffects();
				if (player.getCubics() != null)
				{
					for (EffectCubic cubic : player.getCubics())
					{
						cubic.exit();
						player.getCubic(cubic.getId()).exit();
					}
				}
			}
			Functions.removeItem(player, CONSUMABLE_ID, BUFF_REMOVE_PRICE, "SchemeBuffer");

			msg = main(player);
		}
		else if (eventParam0.equalsIgnoreCase("cast"))
		{
			if (!FREE_BUFFS)
			{
				if (Functions.getItemCount(player, CONSUMABLE_ID) < SCHEME_BUFF_PRICE)
				{
					sendErrorMessageToPlayer(player, "You don't have enough Adena!");
					return;
				}
			}

			final int schemeId = Integer.parseInt(eventParam1);
			if(player.getBuffSchemeById(schemeId) == null || player.getBuffSchemeById(schemeId).schemeBuffs == null)
			{
				player.sendMessage("First you have to Create scheme.");
				return;
			}

			final TIntArrayList buffs = new TIntArrayList();
			final TIntArrayList levels = new TIntArrayList();


			for (SchemeBuff buff : player.getBuffSchemeById(schemeId).schemeBuffs)
			{
				int id = buff.skillId;
				int level = buff.skillLevel;
				switch (getBuffType(id))
				{
					case "buff":
						if (ENABLE_BUFFS)
						{
							if (isEnabled(id, level))
							{
								buffs.add(id);
								levels.add(level);
							}
						}
						break;
					case "resist":
						if (ENABLE_RESIST)
						{
							if (isEnabled(id, level))
							{
								buffs.add(id);
								levels.add(level);
							}
						}
						break;
					case "song":
						if (ENABLE_SONGS)
						{
							if (isEnabled(id, level))
							{
								buffs.add(id);
								levels.add(level);
							}
						}
						break;
					case "dance":
						if (ENABLE_DANCES)
						{
							if (isEnabled(id, level))
							{
								buffs.add(id);
								levels.add(level);
							}
						}
						break;
					case "chant":
						if (ENABLE_CHANTS)
						{
							if (isEnabled(id, level))
							{
								buffs.add(id);
								levels.add(level);
							}
						}
						break;
					case "others":
						if (ENABLE_OTHERS)
						{
							if (isEnabled(id, level))
							{
								buffs.add(id);
								levels.add(level);
							}
						}
						break;
					case "special":
						if (ENABLE_SPECIAL)
						{
							if (isEnabled(id, level))
							{
								buffs.add(id);
								levels.add(level);
							}
						}
						break;
				}
			}

			final boolean getpetbuff = isPetBuff(player);

			if (player.isBlocked())
				return;

			if (buffs.size() == 0)
			{
				msg = viewAllSchemeBuffs(player, eventParam1, "0");
			}
			else if (getpetbuff && player.getPet() == null)
			{
				sendErrorMessageToPlayer(player, "You do not have a servitor summoned. Please summon your servitor and try again.");
				showCommunity(player, main(player));
				return;
			}
			else
			{
				ThreadPoolManager.getInstance().execute(new Runnable() {
					@Override
					public void run()
					{
						for (int i = 0; i < buffs.size(); ++i)
						{
							if (!getpetbuff)
							{
								SkillTable.getInstance().getInfo(buffs.get(i), levels.get(i)).getEffects(player, player, false, false);
								npc2.broadcastPacket(new MagicSkillUse(npc2, player, buffs.get(i), levels.get(i), 0, 0));
							}
							else
							{
								SkillTable.getInstance().getInfo(buffs.get(i), levels.get(i)).getEffects(player.getPet(), player.getPet(), false, false);
							   // npc2.getPet().broadcastPacket(new MagicSkillUse(npc2, player.getPet(), buffs.get(i), levels.get(i), 0, 0));
							}
							try
							{
								Thread.sleep(5);
								if (player.isBlocked())
									return;
							}
							catch (InterruptedException e)
							{}

						}
					}
				});
			}
			Functions.removeItem(player, CONSUMABLE_ID, SCHEME_BUFF_PRICE, "SchemeBuffer");

			msg = main(player);
		}
		else if (eventParam0.equalsIgnoreCase("manage_scheme_1"))
		{
			msg = viewAllSchemeBuffs(player, eventParam1, eventParam2);
		}
		else if (eventParam0.equalsIgnoreCase("remove_buff"))
		{
			String[] split = eventParam1.split("_");
			String scheme = split[0];
			String skill = split[1];
			String level = split[2];

			try (Connection con = DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("DELETE FROM npcbuffer_scheme_contents WHERE scheme_id=? AND skill_id=? AND skill_level=? LIMIT 1"))
			{
				statement.setString(1, scheme);
				statement.setString(2, skill);
				statement.setString(3, level);
				statement.executeUpdate();
			}
			catch (SQLException e)
			{
				_log.error("Error while deleting Scheme Content", e);
			}
			int skillId = Integer.parseInt(skill);
			for (SchemeBuff buff : player.getBuffSchemeById(Integer.parseInt(scheme)).schemeBuffs)
				if (buff.skillId == skillId)
				{
					player.getBuffSchemeById(Integer.parseInt(scheme)).schemeBuffs.remove(buff);
					break;
				}

			msg = viewAllSchemeBuffs(player, scheme, eventParam2);
		}
		else if (eventParam0.equalsIgnoreCase("add_buff"))
		{
			String[] split = eventParam1.split("_");
			String scheme = split[0];
			String skill = split[1];
			String level = split[2];
			if (!isEnabled(Integer.parseInt(skill), Integer.parseInt(level)))
				return;
			int idbuffclass = getClassBuff(Integer.parseInt(skill));

			try (Connection con = DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("INSERT INTO npcbuffer_scheme_contents (scheme_id,skill_id,skill_level,buff_class) VALUES (?,?,?,?)"))
			{
				statement.setString(1, scheme);
				statement.setString(2, skill);
				statement.setString(3, level);
				statement.setInt(4, idbuffclass);
				statement.executeUpdate();
			}
			catch (SQLException e)
			{
				_log.error("Error while deleting Scheme Content", e);
			}

			player.getBuffSchemeById(Integer.parseInt(scheme)).schemeBuffs.add(new SchemeBuff(Integer.parseInt(skill), Integer.parseInt(level), idbuffclass));

			/*
			int temp = Integer.parseInt(eventParam3) + 1;
			final String HTML;
			if (temp >= (MAX_SCHEME_BUFFS + MAX_SCHEME_DANCES))
			{
				HTML = getOptionList(player, Integer.parseInt(scheme));
			}
			else
			{
				HTML = viewAllSchemeBuffs(player, scheme, eventParam2);
			}
			msg = HTML;
			*/
			msg = viewAllSchemeBuffs(player, scheme, eventParam2);
		}
		else if (eventParam0.equalsIgnoreCase("create"))
		{
			String name = getCorrectName(eventParam2 + (eventParam3.equalsIgnoreCase("x") ? "" : " " + eventParam3));
			if (name.isEmpty() || name.equals("no_name"))
			{
				player.sendPacket(new SystemMessage(SystemMsg.INCORRECT_NAME));
				sendErrorMessageToPlayer(player, "Please, enter a scheme name.");
				return;
			}

			int iconId = 0;
			try
			{
				iconId = Integer.parseInt(eventParam1);
				if (iconId < 0 || iconId > SCHEME_ICONS.length - 1)
					throw new Exception();
			}
			catch (Exception e)
			{
				sendErrorMessageToPlayer(player, "Wrong icon selected!");
				showCommunity(player, main(player));
				return;
			}

			try (Connection con = DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("INSERT INTO npcbuffer_scheme_list (player_id,scheme_name,icon) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS))
			{
				statement.setInt(1, player.getObjectId());
				statement.setString(2, name);
				statement.setInt(3, iconId);
				statement.executeUpdate();
				try (ResultSet rset = statement.getGeneratedKeys())
				{
					if (rset.next())
					{
						int id = rset.getInt(1);
						player.getBuffSchemes().add(new PlayerScheme(id, name, iconId));

						msg = getOptionList(player, id);
					}
					else
					{
						_log.error("Couldn't get Generated Key while creating scheme!");
					}
				}
			}
			catch (SQLException e)
			{
				_log.error("Error while inserting Scheme List", e);
				msg = main(player);
			}
		}
		else if (eventParam0.equalsIgnoreCase("delete"))
		{
			final int schemeId = Integer.parseInt(eventParam1);
			final PlayerScheme scheme = player.getBuffSchemeById(schemeId);
			if (scheme == null)
			{
				sendErrorMessageToPlayer(player, "Invalid scheme selected.");
				showCommunity(player, main(player));
				return;
			}

			askQuestion(player, schemeId, scheme.schemeName);

			msg = main(player);
		}
		else if (eventParam0.equalsIgnoreCase("delete_c"))
		{
			msg = "<html><head><title>" + TITLE_NAME + "</title></head><body><br><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br><font name=\"hs12\" color=LEVEL>Do you really want to delete '" + eventParam2 + "' scheme?</font><br><br><button value=\"Yes\" action=\"bypass _bbsbufferbypass_delete " + eventParam1 + " x x\" width=50 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + "<button value=\"No\" action=\"bypass _bbsbufferbypass_delete_1 x x x\" width=50 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></body></html>";

		}
		else if (eventParam0.equalsIgnoreCase("create_1"))
		{
			if (player.getBuffSchemes().size() >= SCHEMES_PER_PLAYER)
			{
				sendErrorMessageToPlayer(player, "You have reached your max scheme count.");
				showCommunity(player, main(player)); // Resend the main page or the cb will get stucked
				return;
			}

			msg = createScheme(player, Integer.parseInt(eventParam1));
		}
		else if (eventParam0.equalsIgnoreCase("edit_1"))
		{
			msg = getEditSchemePage(player);
		}
		else if (eventParam0.equalsIgnoreCase("delete_1"))
		{
			msg = getDeleteSchemePage(player);
		}
		else if (eventParam0.equalsIgnoreCase("manage_scheme_select"))
		{
			msg = getOptionList(player, Integer.parseInt(eventParam1));
		}
		// Alexander - Function to cast a certain custom buff set
		else if (eventParam0.equalsIgnoreCase("giveBuffSet"))
		{
			if (player.isBlocked())
				return;
			if (!FREE_BUFFS)
			{
				if (Functions.getItemCount(player, CONSUMABLE_ID) < BUFF_SET_PRICE)
				{
					sendErrorMessageToPlayer(player, "You don't have enough Adena! You need" + BUFF_SET_PRICE + " Adena!");
					showCommunity(player, main(player)); // Resend the main page or the cb will get stucked
					return;
				}
			}
			final List<int[]> buff_sets;
			switch (eventParam1)
			{
				case "mage":
					buff_sets = Config.NpcBuffer_BuffSetMage;
					break;
				case "dagger":
					buff_sets = Config.NpcBuffer_BuffSetDagger;
					break;
				case "support":
					buff_sets = Config.NpcBuffer_BuffSetSupport;
					break;
				case "tank":
					buff_sets = Config.NpcBuffer_BuffSetTank;
					break;
				case "archer":
					buff_sets = Config.NpcBuffer_BuffSetArcher;
					break;
				default:
				case "fighter":
					buff_sets = Config.NpcBuffer_BuffSetFighter;
					break;
			}
			final List<int[]> buff_sets_premium;
			switch (eventParam1)
			{
				case "mage":
					buff_sets_premium = Config.NpcBuffer_BuffSetMagePremium;
					break;
				case "dagger":
					buff_sets_premium = Config.NpcBuffer_BuffSetDaggerPremium;
					break;
				case "support":
					buff_sets_premium = Config.NpcBuffer_BuffSetSupportPremium;
					break;
				case "tank":
					buff_sets_premium = Config.NpcBuffer_BuffSetTankPremium;
					break;
				case "archer":
					buff_sets_premium = Config.NpcBuffer_BuffSetArcherPremium;
					break;
				default:
				case "fighter":
					buff_sets_premium = Config.NpcBuffer_BuffSetFighterPremium;
					break;
			}

			final boolean getpetbuff = isPetBuff(player);
			if (!getpetbuff && player.getNetConnection().getBonus() == 0)
			{
				ThreadPoolManager.getInstance().execute(new Runnable()
				{
					@Override
					public void run()
					{
						for (int[] i : buff_sets)
						{
							SkillTable.getInstance().getInfo(i[0], i[1]).getEffects(player, player, false, false, false, false);
							 npc2.broadcastPacket(new MagicSkillUse(npc2, player, i[0], i[1], 0, 0));
						}
					}
				});
			}
			if (!getpetbuff && player.getNetConnection().getBonus() != 0)
			{
				ThreadPoolManager.getInstance().execute(new Runnable()
				{
					@Override
					public void run()
					{
						for (int[] i : buff_sets_premium)
						{
							SkillTable.getInstance().getInfo(i[0], i[1]).getEffects(player, player, false, false, false, false);
							 npc2.broadcastPacket(new MagicSkillUse(npc2, player, i[0], i[1], 0, 0));
						}
					}
				});
			}
			else
			{
				if (player.getPet() != null && player.getNetConnection().getBonus() == 0)
				{
					ThreadPoolManager.getInstance().execute(new Runnable()
					{
						@Override
						public void run()
						{
							for (int[] i : buff_sets)
							{
								SkillTable.getInstance().getInfo(i[0], i[1]).getEffects(player.getPet(), player.getPet(), false, false, false, false);
								npc2.broadcastPacket(new MagicSkillUse(npc2, player, i[0], i[1], 0, 0));
							}
						}
					});
				}
				if (player.getPet() != null && player.getNetConnection().getBonus() != 0)
				{
					ThreadPoolManager.getInstance().execute(new Runnable()
					{
						@Override
						public void run()
						{
							for (int[] i : buff_sets_premium)
							{
								SkillTable.getInstance().getInfo(i[0], i[1]).getEffects(player.getPet(), player.getPet(), false, false, false, false);
								npc2.broadcastPacket(new MagicSkillUse(npc2, player, i[0], i[1], 0, 0));
							}
						}
					});
				}
				else
				{
					sendErrorMessageToPlayer(player, "You do not have a servitor summoned. Please summon your servitor and try again.");
					showCommunity(player, main(player));
					return;
				}
			}
			Functions.removeItem(player, CONSUMABLE_ID, BUFF_SET_PRICE, "SchemeBuffer");

			msg = main(player);
		}
		// Alexander - Main page for changing scheme name
		else if (eventParam0.equalsIgnoreCase("changeName_1"))
		{
			String dialog = HtmCache.getInstance().getNotNull("scripts/services/communityPVP/buffer_scheme_change_name.htm", player);

			if (isPetBuff(player))
			{
				dialog = dialog.replace("%topbtn%", (player.getPet() != null ? player.getPet().getName() : "You don't have Pet"));
			}
			else
			{
				dialog = dialog.replace("%topbtn%", player.getName());
			}

			dialog = dialog.replace("%schemeId%", eventParam1);

			msg = dialog;
		}
		// Alexander - Change the scheme's name
		else if (eventParam0.equalsIgnoreCase("changeName"))
		{
			final int schemeId = Integer.parseInt(eventParam1);
			final PlayerScheme scheme = player.getBuffSchemeById(schemeId);
			if (scheme == null)
			{
				sendErrorMessageToPlayer(player, "Invalid scheme selected.");
				showCommunity(player, main(player));
				return;
			}

			String name = getCorrectName(eventParam2 + (eventParam3.equalsIgnoreCase("x") ? "" : " " + eventParam3));
			if (name.isEmpty() || name.equals("no_name"))
			{
				player.sendPacket(new SystemMessage(SystemMsg.INCORRECT_NAME));
				sendErrorMessageToPlayer(player, "Please, enter a scheme name.");
				showCommunity(player, getOptionList(player, schemeId));
				return;
			}

			scheme.schemeName = name;

			try (Connection con = DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("UPDATE npcbuffer_scheme_list SET scheme_name=? WHERE id=?"))
			{
				statement.setString(1, name);
				statement.setInt(2, schemeId);
				statement.executeUpdate();
			}
			catch (SQLException e)
			{
				_log.error("Error while updating Scheme List", e);
			}

			sendErrorMessageToPlayer(player, "Your scheme name changed successfully!");

			msg = getOptionList(player, schemeId);
		}
		// Alexander - Main page for changing scheme icon
		else if (eventParam0.equalsIgnoreCase("changeIcon_1"))
		{
			msg = changeSchemeIcon(player, Integer.parseInt(eventParam1));
		}
		// Alexander - Change the scheme's icon
		else if (eventParam0.equalsIgnoreCase("changeIcon"))
		{
			final int schemeId = Integer.parseInt(eventParam1);
			final PlayerScheme scheme = player.getBuffSchemeById(schemeId);
			if (scheme == null)
			{
				sendErrorMessageToPlayer(player, "Invalid scheme selected!");
				showCommunity(player, main(player));
				return;
			}

			int iconId = 0;
			try
			{
				iconId = Integer.parseInt(eventParam2);
				if (iconId < 0 || iconId > SCHEME_ICONS.length - 1)
					throw new Exception();
			}
			catch (Exception e)
			{
				sendErrorMessageToPlayer(player, "Wrong icon selected!");
				showCommunity(player, getOptionList(player, schemeId));
				return;
			}

			scheme.iconId = iconId;

			try (Connection con = DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("UPDATE npcbuffer_scheme_list SET icon=? WHERE id=?"))
			{
				statement.setInt(1, iconId);
				statement.setInt(2, schemeId);
				statement.executeUpdate();
			}
			catch (SQLException e)
			{
				_log.error("Error while updating Scheme List", e);
			}

			sendErrorMessageToPlayer(player, "Scheme Icon changed successfully!");

			msg = getOptionList(player, schemeId);
		}

		showCommunity(player, msg);
	}

	private static void sendErrorMessageToPlayer(Player player, String msg)
	{
		player.sendPacket(new Say2(player.getObjectId(), ChatType.CRITICAL_ANNOUNCE, "Error", msg));
	}

	private static void askQuestion(Player player, int id, String name)
	{
		ConfirmDlg packet = new ConfirmDlg(SystemMsg.S1, 60000).addString("Do you really want to delete your '" + name + "' scheme?");
		player.ask(packet, new AskQuestionAnswerListener(player));
		player.addQuickVar("schemeToDel", id);
	}

	private static class AskQuestionAnswerListener implements OnAnswerListener
	{
		private final Player _player;

		private AskQuestionAnswerListener(Player player)
		{
			_player = player;
		}

		@Override
		public void sayYes()
		{
			deleteScheme(_player.getQuickVarI("schemeToDel"), _player);
			_player.deleteQuickVar("schemeToDel");
			showCommunity(_player, main(_player));
		}

		@Override
		public void sayNo()
		{
			showCommunity(_player, main(_player));
		}

	}

	private static void deleteScheme(int eventParam1, Player player)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection())
		{

			try (PreparedStatement statement = con.prepareStatement("DELETE FROM npcbuffer_scheme_list WHERE id=? LIMIT 1"))
			{
				statement.setString(1, String.valueOf(eventParam1));
				statement.executeUpdate();
			}
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM npcbuffer_scheme_contents WHERE scheme_id=?"))
			{
				statement.setString(1, String.valueOf(eventParam1));
				statement.executeUpdate();
			}
		}
		catch (SQLException e)
		{
			_log.error("Error while deleting Scheme Content", e);
		}

		int realId = eventParam1;
		for (PlayerScheme scheme : player.getBuffSchemes())
		{
			if (scheme.schemeId == realId)
			{
				player.getBuffSchemes().remove(scheme);
				break;
			}
		}
	}

	private static class BackHp implements Runnable
	{
		private final Playable playable;
		private final double hp;
		private final double mp;
		private final double cp;
		private BackHp(Playable playable, double hp, double mp, double cp)
		{
			this.playable = playable;
			this.hp = hp;
			this.mp = mp;
			this.cp = cp;
		}

		@Override
		public void run()
		{
			playable.getPlayer().deleteQuickVar("BackHpOn");
			playable.setCurrentHp(hp, false);
			playable.setCurrentMp(mp);
			playable.setCurrentCp(cp);
		}
	}

	private static void showCommunity(Player player, String text)
	{
		if (text != null)
			ShowBoard.separateAndSend(text, player);
	}

	private static String getCorrectName(String currentName)
	{
		StringBuilder newNameBuilder = new StringBuilder();
		char[] chars = currentName.toCharArray();
		for (char c : chars)
			if (isCharFine(c))
				newNameBuilder.append(c);
		return newNameBuilder.toString();
	}

	private static final char[] FINE_CHARS = {'1','2','3','4','5','6','7','8','9','0','q','w','e','r','t','y','u','i','o','p','a','s','d','f','g','h','j','k','l',
	'z','x','c','v','b','n','m','Q','W','E','R','T','Y','U','I','O','P','A','S','D','F','G','H','J','K','L','Z','X','C','V','B','N','M',' '};

	private static boolean isCharFine(char c)
	{
		for (char fineChar : FINE_CHARS)
			if (fineChar == c)
				return true;
		return false;
	}
}
