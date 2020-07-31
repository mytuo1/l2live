package l2f.gameserver.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import fandc.dailyquests.drops.Droplist;
import fandc.dailyquests.drops.DroplistGroup;
import fandc.dailyquests.drops.DroplistItem;
import l2f.commons.annotations.Nullable;
import l2f.commons.util.Rnd;
import l2f.gameserver.Config;
import l2f.gameserver.data.xml.holder.ItemHolder;
import l2f.gameserver.handler.bbs.CommunityBoardManager;
import l2f.gameserver.handler.bbs.ICommunityBoardHandler;
import l2f.gameserver.model.Creature;
import l2f.gameserver.model.GameObject;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.base.ClassId;
import l2f.gameserver.model.entity.events.impl.AbstractFightClub;
import l2f.gameserver.model.reward.RewardList;
import l2f.gameserver.network.serverpackets.ExShowScreenMessage;
import l2f.gameserver.stats.Env;
import l2f.gameserver.templates.item.ItemTemplate;

public class Util
{
	static final String PATTERN = "0.0000000000E00";
	static final DecimalFormat df;

	private static NumberFormat adenaFormatter;

	static
	{
		adenaFormatter = NumberFormat.getIntegerInstance(Locale.FRANCE);
		df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);
		df.applyPattern(PATTERN);
		df.setPositivePrefix("+");
	}

	public static boolean isMatchingRegexp(String text, String template)
	{
		Pattern pattern = null;
		try
		{
			pattern = Pattern.compile(template);
		}
		catch (PatternSyntaxException e) // invalid template
		{
			e.printStackTrace();
		}
		if (pattern == null)
			return false;
		Matcher regexp = pattern.matcher(text);
		return regexp.matches();
	}

	public static String formatDouble(double x, String nanString, boolean forceExponents)
	{
		if (Double.isNaN(x))
			return nanString;
		if (forceExponents)
			return df.format(x);
		if ((long) x == x)
			return String.valueOf((long) x);
		return String.valueOf(x);
	}

	/**
	 * Return amount of adena formatted with " " delimiter
	 * @param amount
	 * @return String formatted adena amount
	 */
	public static String formatAdena(long amount)
	{
		return adenaFormatter.format(amount);
	}

	public static String formatTime(int time)
	{
		if (time == 0)
			return "now";
		time = Math.abs(time);
		String ret = "";
		long numDays = time / 86400;
		time -= numDays * 86400;
		long numHours = time / 3600;
		time -= numHours * 3600;
		long numMins = time / 60;
		time -= numMins * 60;
		long numSeconds = time;
		if (numDays > 0)
			ret += numDays + "d ";
		if (numHours > 0)
			ret += numHours + "h ";
		if (numMins > 0)
			ret += numMins + "m ";
		if (numSeconds > 0)
			ret += numSeconds + "s";
		return ret.trim();
	}

	public static long rollDrop(long min, long max, double calcChance, boolean rate)
	{
		if (calcChance <= 0 || min <= 0 || max <= 0)
			return 0;
		int dropmult = 1;
		if (rate)
			calcChance *= Config.RATE_DROP_ITEMS;
		if (calcChance > RewardList.MAX_CHANCE)
			if (calcChance % RewardList.MAX_CHANCE == 0)
				dropmult = (int) (calcChance / RewardList.MAX_CHANCE);
			else
			{
				dropmult = (int) Math.ceil(calcChance / RewardList.MAX_CHANCE);
				calcChance = calcChance / dropmult;
			}
		return Rnd.chance(calcChance / 10000.) ? Rnd.get(min * dropmult, max * dropmult) : 0;
	}

	public static int packInt(int[] a, int bits) throws Exception
	{
		int m = 32 / bits;
		if (a.length > m)
			throw new Exception("Overflow");

		int result = 0;
		int next;
		int mval = (int) Math.pow(2, bits);
		for (int i = 0; i < m; i++)
		{
			result <<= bits;
			if (a.length > i)
			{
				next = a[i];
				if (next >= mval || next < 0)
					throw new Exception("Overload, value is out of range");
			}
			else
				next = 0;
			result += next;
		}
		return result;
	}

	public static long packLong(int[] a, int bits) throws Exception
	{
		int m = 64 / bits;
		if (a.length > m)
			throw new Exception("Overflow");

		long result = 0;
		int next;
		int mval = (int) Math.pow(2, bits);
		for (int i = 0; i < m; i++)
		{
			result <<= bits;
			if (a.length > i)
			{
				next = a[i];
				if (next >= mval || next < 0)
					throw new Exception("Overload, value is out of range");
			}
			else
				next = 0;
			result += next;
		}
		return result;
	}

	public static int[] unpackInt(int a, int bits)
	{
		int m = 32 / bits;
		int mval = (int) Math.pow(2, bits);
		int[] result = new int[m];
		int next;
		for (int i = m; i > 0; i--)
		{
			next = a;
			a = a >> bits;
			result[i - 1] = next - a * mval;
		}
		return result;
	}

	public static int[] unpackLong(long a, int bits)
	{
		int m = 64 / bits;
		int mval = (int) Math.pow(2, bits);
		int[] result = new int[m];
		long next;
		for (int i = m; i > 0; i--)
		{
			next = a;
			a = a >> bits;
			result[i - 1] = (int) (next - a * mval);
		}
		return result;
	}

	public static float[] parseCommaSeparatedFloatArray(String s)
	{
		if (s.isEmpty())
			return new float[0];
		String[] tmp = s.replaceAll(",", ";").replaceAll("\\n", ";").split(";");
		float[] val = new float[tmp.length];
		for (int i = 0; i < tmp.length; i++)
			val[i] = Float.parseFloat(tmp[i]);
		return val;
	}

	public static int[] parseCommaSeparatedIntegerArray(String s)
	{
		if (s.isEmpty())
			return new int[0];
		String[] tmp = s.replaceAll(",", ";").replaceAll("\\n", ";").split(";");
		int[] val = new int[tmp.length];
		for (int i = 0; i < tmp.length; i++)
			val[i] = Integer.parseInt(tmp[i]);
		return val;
	}

	public static long[] parseCommaSeparatedLongArray(String s)
	{
		if (s.isEmpty())
			return new long[0];
		String[] tmp = s.replaceAll(",", ";").replaceAll("\\n", ";").split(";");
		long[] val = new long[tmp.length];
		for (int i = 0; i < tmp.length; i++)
			val[i] = Long.parseLong(tmp[i]);
		return val;
	}

	public static long[][] parseStringForDoubleArray(String s)
	{
		String[] temp = s.replaceAll("\\n", ";").split(";");
		long[][] val = new long[temp.length][];

		for (int i = 0; i < temp.length; i++)
			val[i] = parseCommaSeparatedLongArray(temp[i]);
		return val;
	}

	/** Just alias
	 * @param glueStr
	 * @param strings
	 * @param startIdx
	 * @param maxCount
	 * @return */
	public static String joinStrings(String glueStr, String[] strings, int startIdx, int maxCount)
	{
		return Strings.joinStrings(glueStr, strings, startIdx, maxCount);
	}

	/** Just alias
	 * @param glueStr
	 * @param strings
	 * @param startIdx
	 * @return */
	public static String joinStrings(String glueStr, String[] strings, int startIdx)
	{
		return Strings.joinStrings(glueStr, strings, startIdx, -1);
	}

	public static boolean isNumber(String s)
	{
		try
		{
			Double.parseDouble(s);
		}
		catch (NumberFormatException e)
		{
			return false;
		}
		return true;
	}

	public static String dumpObject(Object o, boolean simpleTypes, boolean parentFields, boolean ignoreStatics)
	{
		Class<?> cls = o.getClass();
		String val, type, result = "[" + (simpleTypes ? cls.getSimpleName() : cls.getName()) + "\n";
		Object fldObj;
		List<Field> fields = new ArrayList<Field>();
		while (cls != null)
		{
			for (Field fld : cls.getDeclaredFields())
				if (!fields.contains(fld))
				{
					if (ignoreStatics && Modifier.isStatic(fld.getModifiers()))
						continue;
					fields.add(fld);
				}
			cls = cls.getSuperclass();
			if (!parentFields)
				break;
		}

		for (Field fld : fields)
		{
			fld.setAccessible(true);
			try
			{
				fldObj = fld.get(o);
				if (fldObj == null)
					val = "NULL";
				else
					val = fldObj.toString();
			}
			catch (Throwable e)
			{
				e.printStackTrace();
				val = "<ERROR>";
			}
			type = simpleTypes ? fld.getType().getSimpleName() : fld.getType().toString();

			result += String.format("\t%s [%s] = %s;\n", fld.getName(), type, val);
		}

		result += "]\n";
		return result;
	}

	private static Pattern _pattern = Pattern.compile("<!--TEMPLET(\\d+)(.*?)TEMPLET-->", Pattern.DOTALL);

	public static HashMap<Integer, String> parseTemplate(String html)
	{
		Matcher m = _pattern.matcher(html);
		HashMap<Integer, String> tpls = new HashMap<Integer, String>();
		while (m.find())
		{
			tpls.put(Integer.parseInt(m.group(1)), m.group(2));
			html = html.replace(m.group(0), "");
		}

		tpls.put(0, html);
		return tpls;
	}

	public static boolean isDigit(String text)
	{
		if (text == null)
			return false;
		return text.matches("[0-9]+");
	}

	/**
	 * @param raw
	 * @return
	 */
	public static String printData(byte[] raw)
	{
		return printData(raw, raw.length);
	}

	public static String fillHex(int data, int digits)
	{
		String number = Integer.toHexString(data);
		for (int i = number.length(); i < digits; i++)
		{
			number = "0" + number;
		}
		return number;
	}

	public static String printData(byte[] data, int len)
	{
		StringBuffer result = new StringBuffer();
		int counter = 0;
		for (int i = 0; i < len; i++)
		{
			if (counter % 16 == 0)
			{
				result.append(fillHex(i, 4) + ": ");
			}
			result.append(fillHex(data[i] & 0xff, 2) + " ");
			counter++;
			if (counter == 16)
			{
				result.append("   ");
				int charpoint = i - 15;
				for (int a = 0; a < 16; a++)
				{
					int t1 = data[charpoint++];
					if (t1 > 0x1f && t1 < 0x80)
					{
						result.append((char) t1);
					}
					else
					{
						result.append('.');
					}
				}
				result.append("\n");
				counter = 0;
			}
		}
		int rest = data.length % 16;
		if (rest > 0)
		{
			for (int i = 0; i < 17 - rest; i++)
			{
				result.append("   ");
			}
			int charpoint = data.length - rest;
			for (int a = 0; a < rest; a++)
			{
				int t1 = data[charpoint++];
				if (t1 > 0x1f && t1 < 0x80)
				{
					result.append((char) t1);
				}
				else
				{
					result.append('.');
				}
			}
			result.append("\n");
		}
		return result.toString();
	}

	public static byte[] generateHex(int size)
	{
		byte[] array = new byte[size];
		Random rnd = new Random();
		for (int i = 0; i < size; i++)
		{
			array[i] = (byte) rnd.nextInt(256);
		}
		return array;
	}

	/**
	 * @param number
	 * @return From 123123 returns 123,123
	 */
	public static String getNumberWithCommas(long number)
	{
		String text = String.valueOf(number);
		int size = text.length();
		for (int i = size; i > 0; i--)
		{
			if ((size - i) % 3 == 0 && i < size)
			{
				text = text.substring(0, i) + ',' + text.substring(i);
			}
		}
		return text;
	}

	public static String getFullClassName(ClassId classIndex)
	{
		switch (classIndex)
		{
			case phoenixKnight:
				return "Phoenix Knight";
			case hellKnight:
				return "Hell Knight";
			case arcanaLord:
				return "Arcana Lord";

			case evaTemplar:
				return "Eva's Templar";
			case swordMuse:
				return "Sword Muse";
			case windRider:
				return "Wind Rider";
			case moonlightSentinel:
				return "Moonlight Sentinel";
			case mysticMuse:
				return "Mystic Muse";
			case elementalMaster:
				return "Elemental Master";
			case evaSaint:
				return "Eva's Saint";

			case shillienTemplar:
				return "ShillenTemplar";
			case spectralDancer:
				return "Spectral Dancer";
			case ghostHunter:
				return "Ghost Hunter";
			case ghostSentinel:
				return "Ghost Sentinel";
			case stormScreamer:
				return "Storm Screamer";
			case spectralMaster:
				return "Spectral Master";
			case shillienSaint:
				return "Shillien Saint";

			case grandKhauatari:
				return "Grand Khauatari";

			case fortuneSeeker:
				return "Fortune Seeker";

			default:
				return classIndex.name().substring(0, 1).toUpperCase() + classIndex.name().substring(1);
		}
	}

	public static String boolToString(boolean b)
	{
		return b ? "True" : "False";
	}

	/**
	 * @param event
	 * @return
	 * @From FFADeathMatchEvent @Making FFADeathMatch
	 */
	public static String getChangedEventName(AbstractFightClub event)
	{
		String eventName = event.getClass().getSimpleName();//For example FFADeathMatchEvent
		eventName = eventName.substring(0, eventName.length() - 5);//Making it FFADeathMatch
		return eventName;
	}

	private static final char[] ALLOWED_CHARS = { '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' };

	public static boolean isInteger(char c)
	{
		for (char possibility : ALLOWED_CHARS)
			if (possibility == c)
				return true;
		return false;
	}

	public static boolean arrayContains(@Nullable Object[] array, @Nullable Object objectToLookFor)
	{
		if (array == null || objectToLookFor == null)
			return false;
		for (Object objectInArray : array)
			if (objectInArray != null && objectInArray.equals(objectToLookFor))
				return true;
		return false;
	}

	/**
	 * @param name
	 * @return Funcion que convierte el string en proper case, de cada palabra del string, la primera se hace mayuscula, y las demas todas minisculas
	 */
	public static String toProperCaseAll(String name)
	{
		StringTokenizer st = new StringTokenizer(name);
		String newString = "";

		newString = st.nextToken();
		name = newString.substring(0, 1).toUpperCase();
		if (newString.length() > 1)
			name += newString.substring(1).toLowerCase();

		while (st.hasMoreTokens())
		{
			newString = st.nextToken();

			if (newString.length() > 2)
			{
				name += " " + newString.substring(0, 1).toUpperCase();
				name += newString.substring(1).toLowerCase();
			}
			else
				name += " " + newString;
		}

		return name;
	}

	/**
	 * Si pasa los 9999a pasan a ser 10k sin decimales, si pasa los 1000k pasa a ser 1kk, con 2 decimales
	 * si pasa los 1000kk pasa a ser 1kkk con 3 decimales
	 *
	 * @param price
	 * @return Esta funcion convierte el precio actual en un formato mas amigable
	 */
	public static String convertToLineagePriceFormat(double price)
	{
		if (price < 10000)
			return Math.round(price) + " Adena";
		else if (price < 1000000)
			return Util.reduceDecimals(price / 1000, 1) + " k";
		else if (price < 1000000000)
			return Util.reduceDecimals(price / 1000 / 1000, 1) + " kk";
		else
			return Util.reduceDecimals(price / 1000 / 1000 / 1000, 1) + " kkk";
	}

	/**
	 * Funcion simple que devuelve el mismo numero solo que se asegura de que tenga maximo nDecim cantidad de decimales
	 * La idea esta en encontrar el . que separa el decimal y de ahi sumar tantos decimales como se quiera maximo
	 * 10.5912312, 2 = 10.59
	 * Ademas si por ejemplo termina en .0 o .00 se los quita
	 *
	 * @param original
	 * @param nDecim
	 * @return Devuelve el mismo numero solo que se asegura de que tenga maximo nDecim cantidad de decimales
	 */
	public static String reduceDecimals(double original, int nDecim)
	{
		return reduceDecimals(original, nDecim, false);
	}

	public static String reduceDecimals(double original, int nDecim, boolean round)
	{
		String decimals = "#";
		if (nDecim > 0)
		{
			decimals += ".";
			for (int i = 0; i < nDecim; i++)
				decimals += "#";
		}

		final DecimalFormat df = new DecimalFormat(decimals);
		return df.format((round ? Math.round(original) : original)).replace(",", ".");
	}

	/**
	 * @param obj1
	 * @param obj2
	 * @param includeZAxis - if true, includes also the Z axis in the calculation
	 * @return the distance between the two objects
	 */
	public static double calculateDistance(Creature obj1, Creature obj2, boolean includeZAxis)
	{
		if (obj1 == null || obj2 == null)
			return 1000000;

		return calculateDistance(obj1.getX(), obj1.getY(), obj1.getZ(), obj2.getX(), obj2.getY(), obj2.getZ(), includeZAxis);
	}

	/**
	 * @param x1
	 * @param y1
	 * @param z1
	 * @param x2
	 * @param y2
	 * @param z2
	 * @param includeZAxis - if true, includes also the Z axis in the calculation
	 * @return the distance between the two coordinates
	 */
	public static double calculateDistance(int x1, int y1, int z1, int x2, int y2, int z2, boolean includeZAxis)
	{
		double dx = (double) x1 - x2;
		double dy = (double) y1 - y2;

		if (includeZAxis)
		{
			final double dz = z1 - z2;
			return Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
		}
		return Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * @param classId
	 * @return
	 */
	public static String getFullClassName(int classId)
	{
		String name = null;
		switch (classId)
		{
			case 0:
				name = "Human Fighter";
				break;
			case 1:
				name = "Warrior";
				break;
			case 2:
				name = "Gladiator";
				break;
			case 3:
				name = "Warlord";
				break;
			case 4:
				name = "Human Knight";
				break;
			case 5:
				name = "Paladin";
				break;
			case 6:
				name = "Dark Avenger";
				break;
			case 7:
				name = "Rogue";
				break;
			case 8:
				name = "Treasure Hunter";
				break;
			case 9:
				name = "Hawkeye";
				break;
			case 10:
				name = "Human Mystic";
				break;
			case 11:
				name = "Human Wizard";
				break;
			case 12:
				name = "Sorcerer";
				break;
			case 13:
				name = "Necromancer";
				break;
			case 14:
				name = "Warlock";
				break;
			case 15:
				name = "Cleric";
				break;
			case 16:
				name = "Bishop";
				break;
			case 17:
				name = "Prophet";
				break;
			case 18:
				name = "Elven Fighter";
				break;
			case 19:
				name = "Elven Knight";
				break;
			case 20:
				name = "Temple Knight";
				break;
			case 21:
				name = "Sword Singer";
				break;
			case 22:
				name = "Elven Scout";
				break;
			case 23:
				name = "Plains Walker";
				break;
			case 24:
				name = "Silver Ranger";
				break;
			case 25:
				name = "Elven Mystic";
				break;
			case 26:
				name = "Elven Wizard";
				break;
			case 27:
				name = "Spellsinger";
				break;
			case 28:
				name = "Elemental Summoner";
				break;
			case 29:
				name = "Elven Oracle";
				break;
			case 30:
				name = "Elven Elder";
				break;
			case 31:
				name = "Dark Fighter";
				break;
			case 32:
				name = "Palus Knight";
				break;
			case 33:
				name = "Shillien Knight";
				break;
			case 34:
				name = "Bladedancer";
				break;
			case 35:
				name = "Assassin";
				break;
			case 36:
				name = "Abyss Walker";
				break;
			case 37:
				name = "Phantom Ranger";
				break;
			case 38:
				name = "Dark Mystic";
				break;
			case 39:
				name = "Dark Wizard";
				break;
			case 40:
				name = "Spellhowler";
				break;
			case 41:
				name = "Phantom Summoner";
				break;
			case 42:
				name = "Shillien Oracle";
				break;
			case 43:
				name = "Shillien Elder";
				break;
			case 44:
				name = "Orc Fighter";
				break;
			case 45:
				name = "Orc Raider";
				break;
			case 46:
				name = "Destroyer";
				break;
			case 47:
				name = "Monk";
				break;
			case 48:
				name = "Tyrant";
				break;
			case 49:
				name = "Orc Mystic";
				break;
			case 50:
				name = "Orc Shaman";
				break;
			case 51:
				name = "Overlord";
				break;
			case 52:
				name = "Warcryer";
				break;
			case 53:
				name = "Dwarven Fighter";
				break;
			case 54:
				name = "Scavenger";
				break;
			case 55:
				name = "Bounty Hunter";
				break;
			case 56:
				name = "Artisan";
				break;
			case 57:
				name = "Warsmith";
				break;
			case 88:
				name = "Duelist";
				break;
			case 89:
				name = "Dreadnought";
				break;
			case 90:
				name = "Phoenix Knight";
				break;
			case 91:
				name = "Hell Knight";
				break;
			case 92:
				name = "Sagittarius";
				break;
			case 93:
				name = "Adventurer";
				break;
			case 94:
				name = "Archmage";
				break;
			case 95:
				name = "Soultaker";
				break;
			case 96:
				name = "Arcana Lord";
				break;
			case 97:
				name = "Cardinal";
				break;
			case 98:
				name = "Hierophant";
				break;
			case 99:
				name = "Eva's Templar";
				break;
			case 100:
				name = "Sword Muse";
				break;
			case 101:
				name = "Wind Rider";
				break;
			case 102:
				name = "Moonlight Sentinel";
				break;
			case 103:
				name = "Mystic Muse";
				break;
			case 104:
				name = "Elemental Master";
				break;
			case 105:
				name = "Eva's Saint";
				break;
			case 106:
				name = "Shillien Templar";
				break;
			case 107:
				name = "Spectral Dancer";
				break;
			case 108:
				name = "Ghost Hunter";
				break;
			case 109:
				name = "Ghost Sentinel";
				break;
			case 110:
				name = "Storm Screamer";
				break;
			case 111:
				name = "Spectral Master";
				break;
			case 112:
				name = "Shillien Saint";
				break;
			case 113:
				name = "Titan";
				break;
			case 114:
				name = "Grand Khavatari";
				break;
			case 115:
				name = "Dominator";
				break;
			case 116:
				name = "Doom Cryer";
				break;
			case 117:
				name = "Fortune Seeker";
				break;
			case 118:
				name = "Maestro";
				break;
			case 123:
				name = "Kamael Soldier";
				break;
			case 124:
				name = "Kamael Soldier";
				break;
			case 125:
				name = "Trooper";
				break;
			case 126:
				name = "Warder";
				break;
			case 127:
				name = "Berserker";
				break;
			case 128:
				name = "Soul Breaker";
				break;
			case 129:
				name = "Soul Breaker";
				break;
			case 130:
				name = "Arbalester";
				break;
			case 131:
				name = "Doombringer";
				break;
			case 132:
				name = "Soul Hound";
				break;
			case 133:
				name = "Soul Hound";
				break;
			case 134:
				name = "Trickster";
				break;
			case 135:
				name = "Inspector";
				break;
			case 136:
				name = "Judicator";
				break;
			default:
				name = "Unknown";
		}
		return name;
	}

	public static String ArrayToString(String[] array, int start)
	{
		String text = "";

		if (array.length > 1)
		{
			int count = 1;
			for (int i = start; i < array.length; i++)
			{
				text += (count > 1 ? " " : "") + array[i];
				count++;
			}
		}
		else
			text = array[start];

		return text;
	}

	private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

	public static String time(long time)
	{
		return TIME_FORMAT.format(new Date(time));
	}

	public static void communityNextPage(Player player, String link)
	{
		ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(link);
		if (handler != null)
			handler.onBypassCommand(player, link);
	}

	public static String getItemName(int itemId)
	{
		if (itemId == ItemTemplate.ITEM_ID_FAME)
			return "Fame";
		else if (itemId == ItemTemplate.ITEM_ID_PC_BANG_POINTS)
			return "PC Bang point";
		else if (itemId == ItemTemplate.ITEM_ID_CLAN_REPUTATION_SCORE)
			return "Clan reputation";
		else
			return ItemHolder.getInstance().getTemplate(itemId).getName();
	}

	public static String getItemIcon(int itemId)
	{
		return ItemHolder.getInstance().getTemplate(itemId).getIcon();
	}

	public static String formatPay(Player player, long count, int item)
	{
		if (count > 0)
			return formatAdena(count) + " " + getItemName(item);
		else
			return "Free";
	}

	public static String declension(long count, DeclensionKey word)
	{
		String one = "";
		String two = "";
		String five = "";
		switch (word)
		{
			case DAYS:
				one = new String("Day");
				two = new String("Days");
				five = new String("Days");
				break;
			case HOUR:
				one = new String("Hour");
				two = new String("Hours");
				five = new String("Hours");
				break;
			case MINUTES:
				one = new String("Minute");
				two = new String("Minutes");
				five = new String("Minutes");
				break;
			case PIECE:
				one = new String("Piece");
				two = new String("Pieces");
				five = new String("Pieces");
				break;
			case POINT:
				one = new String("Point");
				two = new String("Points");
				five = new String("Points");
		}
		if (count > 100L)
		{
			count %= 100L;
		}
		if (count > 20L)
		{
			count %= 10L;
		}
		if (count == 1L)
		{
			return one.toString();
		}
		if ((count == 2L) || (count == 3L) || (count == 4L))
		{
			return two.toString();
		}
		return five.toString();
	}

	public static long addDay(long count)
	{
		long DAY = count * 1000 * 60 * 60 * 24;
		return DAY;
	}

	public static double cutOff(double num, int pow)
	{
		return (int) (num * Math.pow(10, pow)) / Math.pow(10, pow);
	}

	public static boolean getPay(Player player, int itemid, long count, boolean sendMessage)
	{
		if (count == 0)
		{
			return true;
		}
		boolean check = false;
		switch (itemid)
		{
			case -300:
				if (player.getFame() >= count)
				{
					player.setFame(player.getFame() - (int) count, "Disappeared: {0}.");
					check = true;
				}
				break;
			case -200:
				if ((player.getClan() != null) && (player.getClan().getLevel() >= 5) && (player.getClan().getLeader().isClanLeader()) && (player.getClan().getReputationScore() >= count))
				{
					player.getClan().incReputation((int) -count, false, "Disappeared: {0}.");
					check = true;
				}
				break;
			case -100:
				if (player.getPcBangPoints() >= count)
				{
					if (player.reducePcBangPoints((int) count))
					{
						check = true;
					}
				}
				break;
			default:
				if (player.getInventory().getCountOf(itemid) >= count)
				{
					if (player.getInventory().destroyItemByItemId(itemid, count, "deleted"))
					{
						check = true;
					}
				}
				break;
		}
		if (!check)
		{
			if (sendMessage)
			{
				enoughtItem(player, itemid, count);
			}
			return false;
		}
		if (sendMessage)
		{
			player.sendMessage("Disappeared: " + formatPay(player, count, itemid) + ".");
		}
		return true;
	}

	private static void enoughtItem(Player player, int itemid, long count)
	{
		player.sendPacket(new ExShowScreenMessage("You do not have " + formatPay(player, count, itemid) + ".", 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, 1, -1, false));
		player.sendMessage("You do not have " + formatPay(player, count, itemid) + ".");
	}
	
	public static List<DroplistItem> calculateDroplistItems(Env env, Collection<Droplist> drops)
	{
		List<DroplistItem> itemsToDrop = null;
		for (Droplist drop : drops)
		{
			if (!drop.verifyConditions(env))
			{
				continue;
			}

			final List<DroplistItem> items = calculateDroplistGroups(drop.getGroups());
			if (!items.isEmpty())
			{
				if (itemsToDrop == null)
				{
					itemsToDrop = new ArrayList<>();
				}
				itemsToDrop.addAll(items);
			}
		}
		return itemsToDrop != null ? itemsToDrop : Collections.<DroplistItem> emptyList();
	}

	/**
	 * @param drops
	 * @param env
	 * @return
	 */
	public static List<DroplistItem> calculateDroplistItems(Env env, Droplist... drops)
	{
		List<DroplistItem> itemsToDrop = null;
		for (Droplist drop : drops)
		{
			if (!drop.verifyConditions(env))
			{
				continue;
			}

			final List<DroplistItem> items = calculateDroplistGroups(drop.getGroups());
			if (!items.isEmpty())
			{
				if (itemsToDrop == null)
				{
					itemsToDrop = new ArrayList<>();
				}
				itemsToDrop.addAll(items);
			}
		}
		return itemsToDrop != null ? itemsToDrop : Collections.<DroplistItem> emptyList();
	}

	/**
	 * @param groups
	 * @return
	 */
	public static List<DroplistItem> calculateDroplistGroups(List<DroplistGroup> groups)
	{
		List<DroplistItem> itemsToDrop = null;
		for (DroplistGroup group : groups)
		{
			final double groupRandom = 100 * Rnd.nextDouble();
			if (groupRandom < (group.getChance()))
			{
				final double itemRandom = 100 * Rnd.nextDouble();
				float cumulativeChance = 0;
				for (DroplistItem item : group.getItems())
				{
					if (itemRandom < (cumulativeChance += item.getChance()))
					{
						if (itemsToDrop == null)
						{
							itemsToDrop = new ArrayList<>();
						}
						itemsToDrop.add(item);
						break;
					}
				}
			}
		}
		return itemsToDrop != null ? itemsToDrop : Collections.<DroplistItem> emptyList();
	}


	public static int getInteger(String args, int defaultValue)
	{
		if (args == null || args.isEmpty())
			return defaultValue;

		try
		{
			return Integer.parseInt(args);
		}
		catch (NumberFormatException e)
		{
		}
		return defaultValue;
	}
	
	/**
	 * Re-Maps a value from one range to another.
	 * @param input
	 * @param inputMin
	 * @param inputMax
	 * @param outputMin
	 * @param outputMax
	 * @return The mapped value
	 */
	public static int map(int input, int inputMin, int inputMax, int outputMin, int outputMax)
	{
		input = constrain(input, inputMin, inputMax);
		return (((input - inputMin) * (outputMax - outputMin)) / (inputMax - inputMin)) + outputMin;
	}

	/**
	 * Re-Maps a value from one range to another.
	 * @param input
	 * @param inputMin
	 * @param inputMax
	 * @param outputMin
	 * @param outputMax
	 * @return The mapped value
	 */
	public static long map(long input, long inputMin, long inputMax, long outputMin, long outputMax)
	{
		input = constrain(input, inputMin, inputMax);
		return (((input - inputMin) * (outputMax - outputMin)) / Math.max(inputMax - inputMin, 1)) + outputMin;
	}

	/**
	 * Re-Maps a value from one range to another.
	 * @param input
	 * @param inputMin
	 * @param inputMax
	 * @param outputMin
	 * @param outputMax
	 * @return The mapped value
	 */
	public static double map(double input, double inputMin, double inputMax, double outputMin, double outputMax)
	{
		input = constrain(input, inputMin, inputMax);
		return (((input - inputMin) * (outputMax - outputMin)) / (inputMax - inputMin)) + outputMin;
	}

	/**
	 * Constrains a number to be within a range.
	 * @param input the number to constrain, all data types
	 * @param min the lower end of the range, all data types
	 * @param max the upper end of the range, all data types
	 * @return input: if input is between min and max, min: if input is less than min, max: if input is greater than max
	 */
	public static long constrain(long input, long min, long max)
	{
		return (input < min) ? min : (input > max) ? max : input;
	}

	/**
	 * Constrains a number to be within a range.
	 * @param input the number to constrain, all data types
	 * @param min the lower end of the range, all data types
	 * @param max the upper end of the range, all data types
	 * @return input: if input is between min and max, min: if input is less than min, max: if input is greater than max
	 */
	public static double constrain(double input, double min, double max)
	{
		return (input < min) ? min : (input > max) ? max : input;
	}
	
	public static int constrain(int input, int min, int max)
	{
		return (input < min) ? min : (input > max) ? max : input;
	}
	
	public static boolean checkIfInRange(int range, GameObject obj1, GameObject obj2, boolean includeZAxis)
	{
		if ((obj1 == null) || (obj2 == null))
		{
			return false;
		}
		if (obj1.getReflectionId() != obj2.getReflectionId())
		{
			return false;
		}
		if (range == -1)
		{
			return true; // not limited
		}

		int rad = 0;
		if (obj1 instanceof Creature)
		{
			rad += ((Creature) obj1).getTemplate().getCollisionRadius();
		}
		if (obj2 instanceof Creature)
		{
			rad += ((Creature) obj2).getTemplate().getCollisionRadius();
		}

		double dx = obj1.getX() - obj2.getX();
		double dy = obj1.getY() - obj2.getY();
		double d = (dx * dx) + (dy * dy);

		if (includeZAxis)
		{
			double dz = obj1.getZ() - obj2.getZ();
			d += (dz * dz);
		}
		return d <= ((range * range) + (2 * range * rad) + (rad * rad));
	}
}
