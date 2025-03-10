package l2f.gameserver.model.entity;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2f.commons.util.Rnd;
import l2f.gameserver.ThreadPoolManager;
import l2f.gameserver.instancemanager.UnderGroundColliseumManager;
import l2f.gameserver.model.Party;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.Zone;
import l2f.gameserver.model.base.TeamType;
import l2f.gameserver.network.serverpackets.SystemMessage;
import l2f.gameserver.utils.Location;
import l2f.gameserver.utils.ReflectionUtils;

public class Coliseum
{
	private static final Logger _log = LoggerFactory.getLogger(Coliseum.class);
	
	private Party previusWinners = null;
	private boolean isWaitingRoom1Free = true;
	private boolean isWaitingRoom2Free = true;
	private boolean isInUse;
	private int winCount = 0;
	private Party partyInRoom1 = null;
	private Party partyInRoom2 = null;
	private final int minlevel;
	private final int maxlevel;
	private final ArrayList<Party> party_waiting_list = new ArrayList<Party>();
	
	private final ArrayList<Party> party_inbattle_list = new ArrayList<Party>();
	private Zone _zone;
	private static Integer _event_cycle = Integer.valueOf(0);
	private int _id = 0;
	
	public int getMinLevel()
	{
		return minlevel;
	}
	
	public int getMaxLevel()
	{
		return maxlevel;
	}
	
	private boolean checkOffline(Party party)
	{
		party_waiting_list.add(party);
		return party.size() == 0;
	}
	
	public Coliseum()
	{
		_id += 1;
		minlevel = 1;
		maxlevel = 85;
		try
		{
			load();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (_event_cycle.intValue() == 0)
		{
			init();
		}
	}
	
	public int getId()
	{
		return _id;
	}
	
	private void load()
	{
		_event_cycle = Integer.valueOf(getColiseumMatchNumber());
		if (_event_cycle.intValue() == 2147483647)
		{
			_event_cycle = Integer.valueOf(1);
		}
		else
		{
			Coliseum._event_cycle = Integer.valueOf(_event_cycle.intValue() + 1);
		}
	}
	
	private void init()
	{
		setcoliseummatchnumber(1);
	}
	
	private void startBattle(Party party, Party party2)
	{
		if (!isInUse())
		{
			if (getPreviusWinners() == null)
			{
				ThreadPoolManager.getInstance().schedule(new StartBattle(party, party2), 10000L);
			}
			else
			{
				ThreadPoolManager.getInstance().schedule(new StartBattle(party, getPreviusWinners()), 10000L);
				setIsWaitingRoom1Free(true);
				Location teleloc = getFreeWaitingRoom();
				setIsWaitingRoom2Free(true);
				teleportToWaitingRoom(party2, teleloc);
			}
		}
		else
		{
			ThreadPoolManager.getInstance().schedule(new TryStart(party, party2), 300000L);
		}
	}
	
	public static void register(Player player, int minLevel, int maxLevel)
	{
		Coliseum coli = UnderGroundColliseumManager.getInstance().getColiseumByLevelLimit(maxLevel);
		if (coli == null)
		{
			player.sendMessage("this is not work now, if you have any information about it, contact as");
			return;
		}
		Location teleloc = coli.getFreeWaitingRoom();
		if (teleloc == null)
		{
			player.sendMessage("this is not work now, if you have any information about it, contact as");
		}
		coli.party_waiting_list.add(player.getParty());
		coli.teleportToWaitingRoom(player.getParty(), teleloc);
	}
	
	public void teleportToWaitingRoom(Party party, Location teleloc)
	{
		if (isWaitingRoom1Free())
		{
			setIsWaitingRoom1Free(false);
			setPartyInRoom1(party);
		}
		else if (isWaitingRoom2Free())
		{
			setIsWaitingRoom2Free(false);
			setPartyInRoom2(party);
		}
		else
		{
			party.getLeader().sendMessage("rooms are not free you has been registred try to use teleport function later");
			return;
		}
		for (Player member : party.getMembers())
		{
			member.teleToLocation(teleloc);
		}
		if (!isWaitingRoom2Free)
		{
			startBattle(getPartyInRoom1(), getPartyInRoom2());
		}
	}
	
	public Location getFreeWaitingRoom()
	{
		if (isWaitingRoom1Free())
		{
			return _zone.getRestartPoints().get(0);
		}
		if (isWaitingRoom2Free())
		{
			return _zone.getRestartPoints().get(1);
		}
		return null;
	}
	
	public int getColiseumMatchNumber()
	{
		return _event_cycle.intValue();
	}
	
	public void setcoliseummatchnumber(int number)
	{
		_event_cycle = Integer.valueOf(number);
	}
	
	public boolean checkIfInZone(int x, int y)
	{
		return getZone().checkIfInZone(x, y);
	}
	
	public final Zone getZone()
	{
		if (_zone == null)
		{
			_zone = ReflectionUtils.getZone("UnderGroundColiseum");
		}
		return _zone;
	}
	
	public void StopBattle(Party party, Party party2, TeamType winner, long period)
	{
		ThreadPoolManager.getInstance().schedule(new StopBattle(party, party2, winner), period);
	}
	
	public void teleportPlayers(Party party, Party party2)
	{
		if (((party == null) && (party2 == null)) || ((party2 == null) && (party != null) && party.getMembers().isEmpty()) || ((party == null) && (party2 != null) && party2.getMembers().isEmpty()) || ((party2 != null) && (party != null) && party.getMembers().isEmpty() && party2.getMembers().isEmpty()))
		{
			StopBattle(party, party2, TeamType.NONE, 20000L);
			party_inbattle_list.remove(party);
			party_inbattle_list.remove(party2);
		}
		if ((party2 != null) && ((party == null) || party.getMembers().isEmpty()))
		{
			StopBattle(party, party2, party2.getLeader().getTeam(), 20000L);
			party_inbattle_list.remove(party);
		}
		else if ((party != null) && ((party2 == null) || party2.getMembers().isEmpty()))
		{
			StopBattle(party, party2, party.getLeader().getTeam(), 20000L);
			party_inbattle_list.remove(party2);
		}
		else
		{
			if (party != null)
			{
				for (Player member : party.getMembers())
				{
					member.setTeam(TeamType.BLUE);
					
					Location locOnBGToTP = _zone.getRestartPoints().get(3);
					member.teleToLocation(locOnBGToTP);
				}
			}
			if (party2 != null)
			{
				for (Player member : party2.getMembers())
				{
					member.setTeam(TeamType.RED);
					Location locOnBGToTP = _zone.getRestartPoints().get(4);
					member.teleToLocation(locOnBGToTP);
				}
			}
		}
	}
	
	public void setPreviusWinner(Party previusWinners)
	{
		this.previusWinners = previusWinners;
	}
	
	public Party getPreviusWinners()
	{
		return previusWinners;
	}
	
	public void setIsWaitingRoom1Free(boolean isWaitingRoom1Free)
	{
		this.isWaitingRoom1Free = isWaitingRoom1Free;
	}
	
	public boolean isWaitingRoom1Free()
	{
		return isWaitingRoom1Free;
	}
	
	public void setIsWaitingRoom2Free(boolean isWaitingRoom2Free)
	{
		this.isWaitingRoom2Free = isWaitingRoom2Free;
	}
	
	public boolean isWaitingRoom2Free()
	{
		return isWaitingRoom2Free;
	}
	
	public void setIsInUse(boolean isInUse)
	{
		this.isInUse = isInUse;
	}
	
	public boolean isInUse()
	{
		return isInUse;
	}
	
	public ArrayList<Party> getPartysInBattleGround()
	{
		return party_inbattle_list;
	}
	
	public ArrayList<Party> getWaitingPartys()
	{
		return party_waiting_list;
	}
	
	public void setWinCount(int winCount)
	{
		this.winCount = winCount;
	}
	
	public void incWinCount()
	{
		winCount += 1;
	}
	
	public int getWinCount()
	{
		return winCount;
	}
	
	public Party getPartyInRoom1()
	{
		return partyInRoom1;
	}
	
	public Party getPartyInRoom2()
	{
		return partyInRoom2;
	}
	
	public void setPartyInRoom1(Party p1)
	{
		partyInRoom1 = p1;
	}
	
	public void setPartyInRoom2(Party p2)
	{
		partyInRoom2 = p2;
	}
	
	public class TryStart implements Runnable
	{
		Party _party1;
		Party _party2;
		
		public TryStart(Party party, Party party2)
		{
			_party1 = party;
			_party2 = party2;
		}
		
		@Override
		public void run()
		{
			startBattle(_party1, _party2);
		}
	}
	
	public class StopBattle implements Runnable
	{
		Party _party1;
		Party _party2;
		TeamType _winner = TeamType.NONE;
		
		public StopBattle(Party party, Party party2, TeamType winner)
		{
			_party1 = party;
			_party2 = party2;
			_winner = winner;
		}
		
		@Override
		public void run()
		{
			party_inbattle_list.remove(_party1);
			party_inbattle_list.remove(_party2);
			if (_winner == TeamType.NONE)
			{
				setPreviusWinner(null);
				setWinCount(0);
			}
			else
			{
				if (_party1.getLeader().getTeam() == _winner)
				{
					if (!getPreviusWinners().equals(_party1))
					{
						setPreviusWinner(_party1);
						setWinCount(1);
						for (Player member : _party1.getMembers())
						{
							member.setFame(member.getFame() + 80, "Coliseum");
						}
					}
					else
					{
						incWinCount();
						for (Player member : _party1.getMembers())
						{
							member.setFame(member.getFame() + 80 + (getWinCount() * 5), "Coliseum");
						}
					}
					Location teleloc = getFreeWaitingRoom();
					if (teleloc == null)
					{
						teleloc = _zone.getRestartPoints().get(4);
					}
					else if (isWaitingRoom2Free() && isWaitingRoom1Free())
					{
						setPreviusWinner(null);
						for (Player member : _party1.getMembers())
						{
							if (!member.isDead())
							{
								member.teleToClosestTown();
							}
						}
					}
					else if (!isWaitingRoom1Free())
					{
						teleloc = _zone.getRestartPoints().get(1);
						startBattle(getPartyInRoom1(), _party1);
					}
				}
				if (_party2.getLeader().getTeam() == _winner)
				{
					if (!getPreviusWinners().equals(_party2))
					{
						setPreviusWinner(_party2);
						setWinCount(1);
						for (Player member : _party2.getMembers())
						{
							member.setFame(member.getFame() + 80, "Coliseum");
						}
					}
					else
					{
						incWinCount();
						for (Player member : _party2.getMembers())
						{
							member.setFame(member.getFame() + 80 + (getWinCount() * 5), "Coliseum");
						}
					}
					Location teleloc = getFreeWaitingRoom();
					if (teleloc == null)
					{
						teleloc = _zone.getRestartPoints().get(4);
					}
					else if (isWaitingRoom2Free() && isWaitingRoom1Free())
					{
						setPreviusWinner(null);
						for (Player member : _party2.getMembers())
						{
							if (!member.isDead())
							{
								member.teleToClosestTown();
							}
						}
					}
					else if (!isWaitingRoom1Free())
					{
						teleloc = _zone.getRestartPoints().get(1);
						startBattle(getPartyInRoom1(), _party2);
					}
				}
				setIsInUse(false);
			}
		}
	}
	
	public class StartBattle implements Runnable
	{
		int _number;
		Party _party1;
		Party _party2;
		
		public StartBattle(Party party, Party party2)
		{
			_number = getColiseumMatchNumber();
			_party1 = party;
			_party2 = party2;
		}
		
		@Override
		public void run()
		{
			party_waiting_list.remove(_party1);
			party_waiting_list.remove(_party2);
			if (getPartyInRoom1().equals(_party1) || getPartyInRoom1().equals(_party2))
			{
				setPartyInRoom1(null);
			}
			if (getPartyInRoom2().equals(_party1) || getPartyInRoom2().equals(_party2))
			{
				setPartyInRoom2(null);
			}
			
			boolean isParty1Ready = checkOffline(_party1);
			boolean isParty2Ready = checkOffline(_party2);
			if (!isParty1Ready)
			{
				party_waiting_list.remove(_party1);
				setIsWaitingRoom1Free(true);
				_party2.sendMessage("opponents party is offline, wait for next opponent");
				return;
			}
			if (!isParty2Ready)
			{
				party_waiting_list.remove(_party2);
				setIsWaitingRoom2Free(true);
				_party1.sendMessage("opponents party is offline, wait for next opponent");
				return;
			}
			
			party_inbattle_list.addAll(party_waiting_list);
			party_waiting_list.remove(_party2);
			party_waiting_list.remove(_party1);
			setIsWaitingRoom1Free(true);
			setIsWaitingRoom2Free(true);
			teleportPlayers(_party1, _party2);
			setIsInUse(true);
			Party next;
			if (party_waiting_list.size() > 0)
			{
				ArrayList<Party> toDel = new ArrayList<Party>();
				for (Party party : party_waiting_list)
				{
					if (party.getMembers().size() > 6)
					{
						party.sendMessage("Free room at coliseum append");
					}
					else
					{
						toDel.add(party);
					}
					
				}
				
				if (party_waiting_list.size() == 0)
				{
					return;
				}
				next = party_waiting_list.get(Rnd.get(party_waiting_list.size() - 1));
				for (Player member : next.getMembers())
				{
					if ((member.getLevel() > getMaxLevel()) || (member.getLevel() < getMinLevel()))
					{
						next.sendPacket(member, new SystemMessage(2097).addName(member));
						return;
					}
					if (member.isCursedWeaponEquipped())
					{
						next.sendPacket(member, new SystemMessage(2098).addName(member));
						return;
					}
					Location teleloc = getFreeWaitingRoom();
					if (teleloc == null)
					{
						_log.info("bug cannot find teleloc for coliseum id: " + getId(), "UC");
						return;
					}
					teleportToWaitingRoom(next, teleloc);
				}
				if (party_waiting_list.size() < 2)
				{
					return;
				}
				Party next2 = party_waiting_list.get(Rnd.get(party_waiting_list.size() - 1));
				while (next2 == next)
				{
					next2 = party_waiting_list.get(Rnd.get(party_waiting_list.size() - 1));
				}
				next = next2;
				for (Player member : next.getMembers())
				{
					if ((member.getLevel() > getMaxLevel()) || (member.getLevel() < getMinLevel()))
					{
						next.sendPacket(member, new SystemMessage(2097).addName(member));
						return;
					}
					Location teleloc = getFreeWaitingRoom();
					if (teleloc == null)
					{
						_log.info("bug cannot find teleloc for coliseum id: " + getId(), "UC");
						return;
					}
					teleportToWaitingRoom(next, teleloc);
				}
			}
		}
	}
}