package l2f.gameserver.model;

import static l2f.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import l2f.commons.lang.reference.HardReference;
import l2f.commons.lang.reference.HardReferences;
import l2f.commons.listener.Listener;
import l2f.commons.threading.RunnableImpl;
import l2f.commons.util.Rnd;
import l2f.commons.util.concurrent.atomic.AtomicState;
import l2f.gameserver.BalancerConfig;
import l2f.gameserver.Config;
import l2f.gameserver.ThreadPoolManager;
import l2f.gameserver.ai.CharacterAI;
import l2f.gameserver.ai.CtrlEvent;
import l2f.gameserver.ai.CtrlIntention;
import l2f.gameserver.ai.DefaultAI;
import l2f.gameserver.ai.PlayableAI.nextAction;
import l2f.gameserver.geodata.GeoEngine;
import l2f.gameserver.geodata.GeoMove;
import l2f.gameserver.instancemanager.DimensionalRiftManager;
import l2f.gameserver.instancemanager.QuestManager;
import l2f.gameserver.instancemanager.ReflectionManager;
import l2f.gameserver.model.GameObjectTasks.AltMagicUseTask;
import l2f.gameserver.model.GameObjectTasks.CastEndTimeTask;
import l2f.gameserver.model.GameObjectTasks.HitTask;
import l2f.gameserver.model.GameObjectTasks.MagicGeoCheckTask;
import l2f.gameserver.model.GameObjectTasks.MagicLaunchedTask;
import l2f.gameserver.model.GameObjectTasks.MagicUseTask;
import l2f.gameserver.model.GameObjectTasks.NotifyAITask;
import l2f.gameserver.model.Skill.SkillTargetType;
import l2f.gameserver.model.Skill.SkillType;
import l2f.gameserver.model.Zone.ZoneType;
import l2f.gameserver.model.actor.listener.CharListenerList;
import l2f.gameserver.model.actor.recorder.CharStatsChangeRecorder;
import l2f.gameserver.model.base.InvisibleType;
import l2f.gameserver.model.base.TeamType;
import l2f.gameserver.model.entity.Reflection;
import l2f.gameserver.model.entity.events.GlobalEvent;
import l2f.gameserver.model.entity.olympiad.CompType;
import l2f.gameserver.model.instances.MinionInstance;
import l2f.gameserver.model.instances.MonsterInstance;
import l2f.gameserver.model.instances.NpcInstance;
import l2f.gameserver.model.items.ItemInstance;
import l2f.gameserver.model.pledge.Clan;
import l2f.gameserver.model.quest.Quest;
import l2f.gameserver.model.quest.QuestEventType;
import l2f.gameserver.model.quest.QuestState;
import l2f.gameserver.model.reference.L2Reference;
import l2f.gameserver.network.serverpackets.ActionFail;
import l2f.gameserver.network.serverpackets.Attack;
import l2f.gameserver.network.serverpackets.AutoAttackStart;
import l2f.gameserver.network.serverpackets.AutoAttackStop;
import l2f.gameserver.network.serverpackets.ChangeMoveType;
import l2f.gameserver.network.serverpackets.CharMoveToLocation;
import l2f.gameserver.network.serverpackets.FlyToLocation;
import l2f.gameserver.network.serverpackets.L2GameServerPacket;
import l2f.gameserver.network.serverpackets.MagicSkillCanceled;
import l2f.gameserver.network.serverpackets.MagicSkillLaunched;
import l2f.gameserver.network.serverpackets.MagicSkillUse;
import l2f.gameserver.network.serverpackets.MyTargetSelected;
import l2f.gameserver.network.serverpackets.SetupGauge;
import l2f.gameserver.network.serverpackets.StatusUpdate;
import l2f.gameserver.network.serverpackets.StopMove;
import l2f.gameserver.network.serverpackets.SystemMessage2;
import l2f.gameserver.network.serverpackets.TeleportToLocation;
import l2f.gameserver.network.serverpackets.ValidateLocation;
import l2f.gameserver.network.serverpackets.components.CustomMessage;
import l2f.gameserver.network.serverpackets.components.IStaticPacket;
import l2f.gameserver.network.serverpackets.components.SystemMsg;
import l2f.gameserver.skills.AbnormalEffect;
import l2f.gameserver.skills.EffectType;
import l2f.gameserver.skills.TimeStamp;
import l2f.gameserver.stats.Calculator;
import l2f.gameserver.stats.Env;
import l2f.gameserver.stats.Formulas;
import l2f.gameserver.stats.Formulas.AttackInfo;
import l2f.gameserver.stats.StatFunctions;
import l2f.gameserver.stats.StatTemplate;
import l2f.gameserver.stats.Stats;
import l2f.gameserver.stats.funcs.Func;
import l2f.gameserver.stats.triggers.TriggerInfo;
import l2f.gameserver.stats.triggers.TriggerType;
import l2f.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2f.gameserver.taskmanager.RegenTaskManager;
import l2f.gameserver.templates.CharTemplate;
import l2f.gameserver.templates.item.WeaponTemplate;
import l2f.gameserver.templates.item.WeaponTemplate.WeaponType;
import l2f.gameserver.utils.Location;
import l2f.gameserver.utils.Log;
import l2f.gameserver.utils.PositionUtils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Elemental.templates.Ranking;

public abstract class Creature extends GameObject
{
	/**
	 *
	 */
	protected long _reuseDelay = 0L;
	private static final long serialVersionUID = 1L;
	
	public class MoveNextTask extends RunnableImpl
	{
		private double alldist, donedist;
		
		public MoveNextTask setDist(double dist)
		{
			alldist = dist;
			donedist = 0.;
			return this;
		}
		
		@Override
		public void runImpl()
		{
			if (!isMoving)
				return;
			
			moveLock.lock();
			try
			{
				if (!isMoving)
					return;
				
				if (isMovementDisabled())
				{
					stopMove();
					return;
				}
				
				Creature follow = null;
				int speed = getMoveSpeed();
				if (speed <= 0)
				{
					stopMove();
					return;
				}
				long now = System.currentTimeMillis();
				
				if (isFollow)
				{
					follow = getFollowTarget();
					if (follow == null || follow.isInvisible())
					{
						stopMove();
						return;
					}
					if (isInRangeZ(follow, _offset) && GeoEngine.canSeeTarget(Creature.this, follow, false))
					{
						stopMove();
						ThreadPoolManager.getInstance().execute(new NotifyAITask(Creature.this, CtrlEvent.EVT_ARRIVED_TARGET));
						return;
					}
				}
				
				if (alldist <= 0)
				{
					moveNext(false);
					return;
				}
				
				donedist += (now - _startMoveTime) * _previousSpeed / 1000.;
				double done = donedist / alldist;
				
				if (done < 0)
					done = 0;
				if (done >= 1)
				{
					moveNext(false);
					return;
				}
				
				if (isMovementDisabled())
				{
					stopMove();
					return;
				}
				
				Location loc = null;
				
				int index = (int) (moveList.size() * done);
				if (index >= moveList.size())
					index = moveList.size() - 1;
				if (index < 0)
					index = 0;
				
				loc = moveList.get(index).clone().geo2world();
				
				if (!isFlying() && !isInBoat() && !isInWater() && !isBoat())
					if (loc.z - getZ() > 256)
					{
						String bug_text = "geo bug 1 at: " + getLoc() + " => " + loc.x + "," + loc.y + "," + loc.z + "\tAll path: " + moveList.get(0) + " => " + moveList.get(moveList.size() - 1);
						Log.add(bug_text, "geo");
						stopMove();
						return;
					}
				
				// РџСЂРѕРІРµСЂСЏРµРј, РЅР° РІСЃСЏРєРёР№ СЃР»СѓС‡Р°Р№
				if (loc == null || isMovementDisabled())
				{
					stopMove();
					return;
				}
				
				setLoc(loc, true);
				
				// Р’ РїСЂРѕС†РµСЃСЃРµ РёР·РјРµРЅРµРЅРёСЏ РєРѕРѕСЂРґРёРЅР°С‚, РјС‹ РѕСЃС‚Р°РЅРѕРІРёР»РёСЃСЊ
				if (isMovementDisabled())
				{
					stopMove();
					return;
				}
				
				if (isFollow && now - _followTimestamp > (_forestalling ? 500 : 1000) && follow != null && !follow.isInRange(movingDestTempPos, Math.max(100, _offset)))
				{
					if (Math.abs(getZ() - loc.z) > 1000 && !isFlying())
					{
						sendPacket(SystemMsg.CANNOT_SEE_TARGET);
						stopMove();
						return;
					}
					if (buildPathTo(follow.getX(), follow.getY(), follow.getZ(), _offset, follow, true, true))
						movingDestTempPos.set(follow.getX(), follow.getY(), follow.getZ());
					else
					{
						stopMove();
						return;
					}
					moveNext(true);
					return;
				}
				
				_previousSpeed = speed;
				_startMoveTime = now;
				_moveTask = ThreadPoolManager.getInstance().schedule(this, getMoveTickInterval());
			}
			catch (RuntimeException e)
			{
				_log.error("Error in Creature Moving! ", e);
			}
			finally
			{
				moveLock.unlock();
			}
		}
	}
	
	private static final Logger _log = LoggerFactory.getLogger(Creature.class);
	
	public static final double HEADINGS_IN_PI = 10430.378350470452724949566316381;
	public static final int INTERACTION_DISTANCE = 200;
	
	private Skill _castingSkill;
	
	private long _castInterruptTime;
	private long _animationEndTime;
	
	public int _scheduledCastCount;
	public int _scheduledCastInterval;
	
	public Future<?> _skillTask;
	public Future<?> _skillLaunchedTask;
	public Future<?> _skillGeoCheckTask;
	
	private Future<?> _stanceTask;
	private Runnable _stanceTaskRunnable;
	private long _stanceEndTime;
	
	public final static int CLIENT_BAR_SIZE = 352; // 352 - СЂР°Р·РјРµСЂ РїРѕР»РѕСЃРєРё CP/HP/MP РІ РєР»РёРµРЅС‚Рµ, РІ РїРёРєСЃРµР»СЏС…
	
	private int _lastCpBarUpdate = -1;
	private int _lastHpBarUpdate = -1;
	private int _lastMpBarUpdate = -1;
	
	protected double _currentCp = 0;
	protected double _currentHp = 1;
	protected double _currentMp = 1;
	
	protected boolean _isAttackAborted;
	protected long _attackEndTime;
	protected long _attackReuseEndTime;
	private int _poleAttackCount = 0;
	private static final double[] POLE_VAMPIRIC_MOD =
	{
		1.0,
		0.9,
		0.0,
		7.0,
		0.2,
		0.01
	};
	
	/** HashMap(Integer, L2Skill) containing all skills of the L2Character */
	protected final Map<Integer, Skill> _skills = new ConcurrentSkipListMap<>();
	protected Map<TriggerType, Set<TriggerInfo>> _triggers;
	
	protected IntObjectMap<TimeStamp> _skillReuses = new CHashIntObjectMap<>();
	
	protected volatile EffectList _effectList;
	
	protected volatile CharStatsChangeRecorder<? extends Creature> _statsRecorder;
	
	private List<Stats> _blockedStats;
	
	/** Map 32 bits (0x00000000) containing all abnormal effect in progress */
	private int _abnormalEffects;
	private int _abnormalEffects2;
	private int _abnormalEffects3;
	
	protected AtomicBoolean isDead = new AtomicBoolean();
	protected AtomicBoolean isTeleporting = new AtomicBoolean();
	
	private Map<Integer, Integer> _skillMastery;
	
	private final Map<Integer, Long[]> receivedDebuffs;
	
	protected boolean _isInvul;
	
	private boolean _fakeDeath;
	private boolean _isBlessedByNoblesse; // Р’РѕСЃСЃС‚Р°РЅР°РІР»РёРІР°РµС‚ РІСЃРµ Р±Р°С„С‹ РїРѕСЃР»Рµ СЃРјРµСЂС‚Рё
	private boolean _isSalvation; // Р’РѕСЃСЃС‚Р°РЅР°РІР»РёРІР°РµС‚ РІСЃРµ Р±Р°С„С‹ РїРѕСЃР»Рµ СЃРјРµСЂС‚Рё Рё РїРѕР»РЅРѕСЃС‚СЊСЋ CP, MP, HP
	
	private boolean _meditated;
	private boolean _lockedTarget;
	
	private boolean _blocked;
	
	private final AtomicState _afraid = new AtomicState();
	private final AtomicState _muted = new AtomicState();
	private final AtomicState _pmuted = new AtomicState();
	private final AtomicState _amuted = new AtomicState();
	private final AtomicState _paralyzed = new AtomicState();
	private final AtomicState _rooted = new AtomicState();
	private final AtomicState _sleeping = new AtomicState();
	private final AtomicState _stunned = new AtomicState();
	private final AtomicState _immobilized = new AtomicState();
	private final AtomicState _confused = new AtomicState();
	private final AtomicState _frozen = new AtomicState();
	
	private final AtomicState _healBlocked = new AtomicState();
	private final AtomicState _damageBlocked = new AtomicState();
	private final AtomicState _buffImmunity = new AtomicState(); // Р�РјРјСѓРЅРёС‚РµС‚ Рє Р±Р°С„Р°Рј
	private final AtomicState _debuffImmunity = new AtomicState(); // Р�РјРјСѓРЅРёС‚РµС‚ Рє РґРµР±Р°С„Р°Рј
	private final AtomicState _effectImmunity = new AtomicState(); // Р�РјРјСѓРЅРёС‚РµС‚ РєРѕ РІСЃРµРј СЌС„С„РµРєС‚Р°Рј
	
	private final AtomicState _weaponEquipBlocked = new AtomicState();
	
	private boolean _flying;
	
	private boolean _running;
	
	public boolean isMoving;
	public boolean isFollow;
	private final Lock moveLock = new ReentrantLock();
	private Future<?> _moveTask;
	private MoveNextTask _moveTaskRunnable;
	private List<Location> moveList;
	private Location destination;
	/**
	 * РїСЂРё moveToLocation РёСЃРїРѕР»СЊР·СѓРµС‚СЃСЏ РґР»СЏ С…СЂР°РЅРµРЅРёСЏ РіРµРѕРєРѕРѕСЂРґРёРЅР°С‚ РІ РєРѕС‚РѕСЂС‹Рµ РјС‹ РґРІРёРіР°РµРјСЃСЏ РґР»СЏ С‚РѕРіРѕ С‡С‚Рѕ Р±С‹ РёР·Р±РµР¶Р°С‚СЊ РїРѕРІС‚РѕСЂРЅРѕРіРѕ РїРѕСЃС‚СЂРѕРµРЅРёСЏ РѕРґРЅРѕРіРѕ Рё С‚РѕРіРѕ Р¶Рµ РїСѓС‚Рё РїСЂРё followToCharacter РёСЃРїРѕР»СЊР·СѓРµС‚СЃСЏ РґР»СЏ С…СЂР°РЅРµРЅРёСЏ РјРёСЂРѕРІС‹С… РєРѕРѕСЂРґРёРЅР°С‚ РІ РєРѕС‚РѕСЂС‹С… РЅР°С…РѕРґРёР»Р°СЃСЊ РїРѕСЃР»РµРґРЅРёР№ СЂР°Р· РїСЂРµСЃР»РµРґСѓРµРјР°СЏ С†РµР»СЊ РґР»СЏ РѕС‚СЃР»РµР¶РёРІР°РЅРёСЏ
	 * РЅРµРѕР±С…РѕРґРёРјРѕСЃС‚Рё РїРµСЂРµСЃС‚СЂР°РёРІР°РЅРёСЏ РїСѓС‚Рё
	 */
	private final Location movingDestTempPos = new Location();
	private int _offset;
	
	private boolean _forestalling;
	
	private volatile HardReference<? extends GameObject> target = HardReferences.emptyRef();
	private volatile HardReference<? extends Creature> castingTarget = HardReferences.emptyRef();
	private volatile HardReference<? extends Creature> followTarget = HardReferences.emptyRef();
	private volatile HardReference<? extends Creature> _aggressionTarget = HardReferences.emptyRef();
	
	private final List<List<Location>> _targetRecorder = new ArrayList<List<Location>>();
	private long _followTimestamp, _startMoveTime;
	private int _previousSpeed = 0;
	
	private int _heading;
	
	private final Calculator[] _calculators;
	
	protected CharTemplate _template;
	protected CharTemplate _baseTemplate;
	
	protected volatile CharacterAI _ai;
	
	protected String _name;
	protected String _title;
	protected TeamType _team = TeamType.NONE;
	
	private boolean _isRegenerating;
	private final Lock regenLock = new ReentrantLock();
	private Future<?> _regenTask;
	private Runnable _regenTaskRunnable;
	
	private final List<Zone> _zones = new ArrayList<Zone>();
	/** Р‘Р»РѕРєРёСЂРѕРІРєР° РґР»СЏ С‡С‚РµРЅРёСЏ/Р·Р°РїРёСЃРё РѕР±СЉРµРєС‚РѕРІ РёР· СЂРµРіРёРѕРЅР° */
	private final ReadWriteLock zonesLock = new ReentrantReadWriteLock();
	private final Lock zonesRead = zonesLock.readLock();
	private final Lock zonesWrite = zonesLock.writeLock();
	
	protected volatile CharListenerList listeners;
	
	/** РЎРїРёСЃРѕРє РёРіСЂРѕРєРѕРІ, РєРѕС‚РѕСЂС‹Рј РЅРµРѕР±С…РѕРґРёРјРѕ РѕС‚СЃС‹Р»Р°С‚СЊ РёРЅС„РѕСЂРјР°С†РёСЋ РѕР± РёР·РјРµРЅРµРЅРёРё СЃРѕСЃС‚РѕСЏРЅРёСЏ РїРµСЂСЃРѕРЅР°Р¶Р° */
	private List<Player> _statusListeners;
	private final Lock statusListenersLock = new ReentrantLock();
	
	protected Long _storedId;
	
	public final Long getStoredId()
	{
		return _storedId;
	}
	
	protected HardReference<? extends Creature> reference;
	
	public Creature(int objectId, CharTemplate template)
	{
		super(objectId);
		
		receivedDebuffs = new HashMap<>();
		
		_template = template;
		_baseTemplate = template;
		
		_calculators = new Calculator[Stats.NUM_STATS];
		
		StatFunctions.addPredefinedFuncs(this);
		
		reference = new L2Reference<Creature>(this);
		
		_storedId = GameObjectsStorage.put(this);
	}
	
	@Override
	public HardReference<? extends Creature> getRef()
	{
		return reference;
	}
	
	public boolean isAttackAborted()
	{
		return _isAttackAborted;
	}
	
	public final void abortAttack(boolean force, boolean message)
	{
		if (isAttackingNow())
		{
			_attackEndTime = 0;
			if (force)
				_isAttackAborted = true;
			
			getAI().setIntention(AI_INTENTION_ACTIVE);
			
			if (isPlayer() && message)
			{
				sendActionFailed();
				sendPacket(new SystemMessage2(SystemMsg.C1S_ATTACK_FAILED).addName(this));
			}
		}
	}
	
	public final void abortCast(boolean force, boolean message)
	{
		if (isCastingNow() && (force || canAbortCast()))
		{
			final Skill castingSkill = _castingSkill;
			final Future<?> skillTask = _skillTask;
			final Future<?> skillLaunchedTask = _skillLaunchedTask;
			final Future<?> skillGeoCheckTask = _skillGeoCheckTask;
			
			finishFly(); // Р‘СЂРѕР°РґРєР°СЃС‚ РїР°РєРµС‚Р° FlyToLoc СѓР¶Рµ РІС‹РїРѕР»РЅРµРЅ, СѓСЃС‚Р°РЅР°РІР»РёРІР°РµРј РєРѕРѕСЂРґРёРЅР°С‚С‹ С‡С‚РѕР±С‹ РЅРµ Р±С‹Р»Рѕ РІРёР·СѓР°Р»СЊРЅС‹С… РіР»СЋРєРѕРІ
			clearCastVars();
			
			if (skillTask != null)
				skillTask.cancel(false); // cancels the skill hit scheduled task
				
			if (skillLaunchedTask != null)
				skillLaunchedTask.cancel(false); // cancels the skill hit scheduled task
				
			if (skillGeoCheckTask != null)
			{
				skillGeoCheckTask.cancel(false); // cancels the skill GeoCheck scheduled task
			}
			
			if (castingSkill != null)
			{
				if (castingSkill.isUsingWhileCasting())
				{
					Creature target = getAI().getAttackTarget();
					if (target != null)
						target.getEffectList().stopEffect(castingSkill.getId());
				}
				
				removeSkillMastery(castingSkill.getId());
			}
			
			broadcastPacket(new MagicSkillCanceled(getObjectId())); // broadcast packet to stop animations client-side
			
			getAI().setIntention(AI_INTENTION_ACTIVE);
			
			if (isPlayer() && message)
				sendPacket(SystemMsg.YOUR_CASTING_HAS_BEEN_INTERRUPTED);
		}
	}
	
	public final boolean canAbortCast()
	{
		return _castInterruptTime > System.currentTimeMillis();
	}
	
	public boolean absorbAndReflect(Creature target, Skill skill, double damage)
	{
		if (target.isDead())
			return false;
		
		boolean bow = getActiveWeaponItem() != null && (getActiveWeaponItem().getItemType() == WeaponType.BOW || getActiveWeaponItem().getItemType() == WeaponType.CROSSBOW);
		
		double value = 0;
		
		if (skill != null && skill.isMagic())
			value = target.calcStat(Stats.REFLECT_AND_BLOCK_MSKILL_DAMAGE_CHANCE, 0, this, skill);
		else if (skill != null && skill.getCastRange() <= 200)
			value = target.calcStat(Stats.REFLECT_AND_BLOCK_PSKILL_DAMAGE_CHANCE, 0, this, skill);
		else if (skill == null && !bow)
			value = target.calcStat(Stats.REFLECT_AND_BLOCK_DAMAGE_CHANCE, 0, this, null);
		
		// The purpose of all damage recorded
		if (value > 0 && Rnd.chance(value))
		{
			reduceCurrentHp(damage, target, null, true, true, false, false, false, false, true);
			return true;
		}
		
		if (skill != null && skill.isMagic())
			value = target.calcStat(Stats.REFLECT_MSKILL_DAMAGE_PERCENT, 0, this, skill);
		else if (skill != null)
		{
			if (skill.getCastRange() >= 0 && skill.getCastRange() <= 40)
				value = target.calcStat(Stats.REFLECT_PSKILL_DAMAGE_PERCENT, 0, this, skill);
		}
		else if (!bow)
			value = target.calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0, this, null);
		
		if (value > 0)
		{
			// Purpose able to reflect some of the damage
			if (getCurrentHp() + getCurrentCp() > value * 0.01 * damage)
			{
				reduceCurrentHp(value * 0.01 * damage, target, null, true, true, false, false, false, false, true);
			}
			else
			{
				if (!(this instanceof Player && ((Player)this).isInOlympiadMode()) && !(this instanceof Player && ((Player)this).isInDuel()))
				if (!isPlayer() || !(getPlayer().isInOlympiadMode() || getPlayer().isInDuel()))
				{
					doDie(this);
				}
			}
		}
		
		if (skill != null || bow)
			return false;
		
		// РІР°РјРїРёСЂРёРє
		damage = (int) (damage - target.getCurrentCp());
		
		if (damage <= 0)
			return false;
		
		final double poleMod = _poleAttackCount < POLE_VAMPIRIC_MOD.length ? POLE_VAMPIRIC_MOD[_poleAttackCount] : 0;
		double absorb = poleMod * calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, target, null);
		double limit;
		if (absorb > 0 && !target.isDamageBlocked())
		{
			limit = calcStat(Stats.HP_LIMIT, null, null) * getMaxHp() / 100.;
			if (getCurrentHp() < limit)
				setCurrentHp(Math.min(_currentHp + damage * absorb * BalancerConfig.ALT_ABSORB_DAMAGE_MODIFIER / 100., limit), false);
		}
		
		absorb = poleMod * calcStat(Stats.ABSORB_DAMAGEMP_PERCENT, 0, target, null);
		if (absorb > 0 && !target.isDamageBlocked())
		{
			limit = calcStat(Stats.MP_LIMIT, null, null) * getMaxMp() / 100.;
			if (getCurrentMp() < limit)
				setCurrentMp(Math.min(_currentMp + damage * absorb * BalancerConfig.ALT_ABSORB_DAMAGE_MODIFIER / 100., limit));
		}
		
		return false;
	}
	
	public double absorbToEffector(Creature attacker, double damage)
	{
		double transferToEffectorDam = calcStat(Stats.TRANSFER_TO_EFFECTOR_DAMAGE_PERCENT, 0.);
		if (transferToEffectorDam > 0)
		{
			Effect effect = getEffectList().getEffectByType(EffectType.AbsorbDamageToEffector);
			if (effect == null)
				return damage;
			
			Creature effector = effect.getEffector();
			// on dead char, not online player - do not give ABSORB, and not for himself
			if (effector == this || effector.isDead() || !isInRange(effector, 1200))
				return damage;
			
			Player thisPlayer = getPlayer();
			Player effectorPlayer = effector.getPlayer();
			if (thisPlayer != null && effectorPlayer != null)
			{
				if (thisPlayer != effectorPlayer && (!thisPlayer.isOnline() || !thisPlayer.isInParty() || thisPlayer.getParty() != effectorPlayer.getParty()))
					return damage;
			}
			else
				return damage;
			
			double transferDamage = (damage * transferToEffectorDam) * .01;
			damage -= transferDamage;
			
			effector.reduceCurrentHp(transferDamage, effector, null, false, false, !attacker.isPlayable(), false, true, false, true);
		}
		return damage;
	}
	
	public double absorbToMp(Creature attacker, double damage)
	{
		double transferToMpDamPercent = calcStat(Stats.TRANSFER_TO_MP_DAMAGE_PERCENT, 0.);
		if(transferToMpDamPercent > 0)
		{
			double transferDamage = (damage * transferToMpDamPercent) * .01;
			
			double currentMp = getCurrentMp();
			if(currentMp > transferDamage)
			{
				setCurrentMp(getCurrentMp() - transferDamage);
				return 0;
			}
			else
			{
				if(currentMp > 0)
				{
					damage -= currentMp;
					setCurrentMp(0);
					sendPacket(SystemMsg.MP_BECAME_0_AND_THE_ARCANE_SHIELD_IS_DISAPPEARING);
				}
				getEffectList().stopEffects(EffectType.AbsorbDamageToMp);
			}
			
			return damage;
		}
		return damage;
	}
	
	public double absorbToSummon(Creature attacker, double damage)
	{
		double transferToSummonDam = calcStat(Stats.TRANSFER_TO_SUMMON_DAMAGE_PERCENT, 0.);
		if(transferToSummonDam > 0)
		{
			Summon summon = getPet();
			double transferDamage = (damage * transferToSummonDam) * .01;
			if(summon == null || summon.isDead() || summon.getCurrentHp() < transferDamage)
				getEffectList().stopEffects(EffectType.AbsorbDamageToSummon);
			else if (summon.isSummon() && summon.isInRangeZ(this, 1200))
			{
				damage -= transferDamage;
				
				summon.reduceCurrentHp(transferDamage, summon, null, false, false, false, false, true, false, true);
			}
		}
		return damage;
	}
	
	public void addBlockStats(List<Stats> stats)
	{
		if(_blockedStats == null)
			_blockedStats = new ArrayList<Stats>();
		_blockedStats.addAll(stats);
	}
	
	public Skill addSkill(Skill newSkill)
	{
		if (newSkill == null)
			return null;
		
		Skill oldSkill = _skills.get(newSkill.getId());
		
		if (oldSkill != null && oldSkill.getLevel() == newSkill.getLevel())
			return newSkill;
		
		// Replace oldSkill by newSkill or Add the newSkill
		_skills.put(newSkill.getId(), newSkill);
		
		// FIX for /useskill re-use exploit
		if (oldSkill != null)
		{
			TimeStamp sts = _skillReuses.get(oldSkill.hashCode());
			if (sts != null && sts.hasNotPassed())
			{
				_skillReuses.put(newSkill.hashCode(), sts);
			}
		}
		
		if (oldSkill != null)
		{
			removeStatsOwner(oldSkill);
			removeTriggers(oldSkill);
		}
		
		addTriggers(newSkill);
		
		// Add Func objects of newSkill to the calculator set of the L2Character
		addStatFuncs(newSkill.getStatFuncs());
		
		return oldSkill;
	}
	
	public Calculator[] getCalculators()
	{
		return _calculators;
	}
	
	public final void addStatFunc(Func f)
	{
		if (f == null)
			return;
		int stat = f.stat.ordinal();
		synchronized (_calculators)
		{
			if (_calculators[stat] == null)
				_calculators[stat] = new Calculator(f.stat, this);
			_calculators[stat].addFunc(f);
		}
	}
	
	public final void addStatFuncs(Func[] funcs)
	{
		for (Func f : funcs)
			addStatFunc(f);
	}
	
	public final void removeStatFunc(Func f)
	{
		if (f == null)
			return;
		int stat = f.stat.ordinal();
		synchronized (_calculators)
		{
			if (_calculators[stat] != null)
				_calculators[stat].removeFunc(f);
		}
	}
	
	public final void removeStatFuncs(Func[] funcs)
	{
		for (Func f : funcs)
			removeStatFunc(f);
	}
	
	public final void removeStatsOwner(Object owner)
	{
		synchronized (_calculators)
		{
			for (Calculator _calculator : _calculators)
				if (_calculator != null)
					_calculator.removeOwner(owner);
		}
	}
	
	public void altOnMagicUseTimer(Creature aimingTarget, Skill skill)
	{
		if (isAlikeDead())
			return;
		int magicId = skill.getDisplayId();
		int level = Math.max(1, getSkillDisplayLevel(skill.getId()));
		List<Creature> targets = skill.getTargets(this, aimingTarget, true);
		broadcastPacket(new MagicSkillLaunched(getObjectId(), magicId, level, Collections.unmodifiableList(targets)));
		double mpConsume2 = skill.getMpConsume2();
		if (mpConsume2 > 0)
		{
			if (_currentMp < mpConsume2)
			{
				sendPacket(SystemMsg.NOT_ENOUGH_MP);
				return;
			}
			if (skill.isMagic())
				reduceCurrentMp(calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, aimingTarget, skill), null);
			else
				reduceCurrentMp(calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, aimingTarget, skill), null);
		}
		callSkill(skill, targets, false);
	}
	
	public void altUseSkill(Skill skill, Creature target)
	{
		if (skill == null)
			return;
		
		int magicId = skill.getId();
		if (isUnActiveSkill(magicId))
			return;
		if (isSkillDisabled(skill))
		{
			sendReuseMessage(skill);
			return;
		}
		if (target == null)
		{
			target = skill.getAimingTarget(this, getTarget());
			if (target == null)
				return;
		}
		
		getListeners().onMagicUse(skill, target, true);
		
		int itemConsume[] = skill.getItemConsume();
		
		if (itemConsume[0] > 0)
			for (int i = 0; i < itemConsume.length; i++)
				if (!consumeItem(skill.getItemConsumeId()[i], itemConsume[i]))
				{
					sendPacket(skill.isHandler() ? SystemMsg.INCORRECT_ITEM_COUNT : SystemMsg.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
					return;
				}
		
		if (skill.getReferenceItemId() > 0)
			if (!consumeItemMp(skill.getReferenceItemId(), skill.getReferenceItemMpConsume()))
				return;
		
		if (skill.getSoulsConsume() > getConsumedSouls())
		{
			sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_SOULS);
			return;
		}
		
		if (skill.getEnergyConsume() > getAgathionEnergy())
		{
			sendPacket(SystemMsg.THE_SKILL_HAS_BEEN_CANCELED_BECAUSE_YOU_HAVE_INSUFFICIENT_ENERGY);
			return;
		}
		
		if (skill.getSoulsConsume() > 0)
			setConsumedSouls(getConsumedSouls() - skill.getSoulsConsume(), null);
		if (skill.getEnergyConsume() > 0)
			setAgathionEnergy(getAgathionEnergy() - skill.getEnergyConsume());
		
		int level = Math.max(1, getSkillDisplayLevel(magicId));
		Formulas.calcSkillMastery(skill, this);
		long reuseDelay = Formulas.calcSkillReuseDelay(this, skill);
		if (!skill.isToggle())
			broadcastPacket(new MagicSkillUse(this, target, skill.getDisplayId(), level, skill.getHitTime(), reuseDelay));
		// РќРµ РїРѕРєР°Р·С‹РІР°С‚СЊ СЃРѕРѕР±С‰РµРЅРёРµ РґР»СЏ С…РµСЂР±РѕРІ Рё РєСѓР±РёРєРѕРІ
		if (!skill.isHideUseMessage())
			if (skill.getSkillType() == SkillType.PET_SUMMON)
				sendPacket(new SystemMessage2(SystemMsg.SUMMONING_YOUR_PET));
			else if (!skill.isHandler())
				sendPacket(new SystemMessage2(SystemMsg.YOU_USE_S1).addSkillName(magicId, level));
			else
				sendPacket(new SystemMessage2(SystemMsg.YOU_USE_S1).addItemName(skill.getItemConsumeId()[0]));
		
		if (!skill.isHandler())
			disableSkill(skill, reuseDelay);
		
		ThreadPoolManager.getInstance().schedule(new AltMagicUseTask(this, target, skill), skill.getHitTime());
	}
	
	public void sendReuseMessage(Skill skill)
	{
	}
	
	public void broadcastPacket(L2GameServerPacket... packets)
	{
		sendPacket(packets);
		broadcastPacketToOthers(packets);
	}
	
	public void broadcastPacket(List<L2GameServerPacket> packets)
	{
		sendPacket(packets);
		broadcastPacketToOthers(packets);
	}
	
	public void broadcastPacketToOthers(L2GameServerPacket... packets)
	{
		if (!isVisible() || packets.length == 0)
			return;
		
		List<Player> players = World.getAroundPlayers(this);
		Player target;
		for (int i = 0; i < players.size(); i++)
		{
			target = players.get(i);
			target.sendPacket(packets);
		}
	}
	
	public void broadcastPacketToOthers(List<L2GameServerPacket> packets)
	{
		if (!isVisible() || packets.isEmpty())
			return;
		
		List<Player> players = World.getAroundPlayers(this);
		Player target;
		for (int i = 0; i < players.size(); i++)
		{
			target = players.get(i);
			target.sendPacket(packets);
		}
	}
	
	public void broadcastToStatusListeners(L2GameServerPacket... packets)
	{
		if (!isVisible() || packets.length == 0)
			return;
		
		statusListenersLock.lock();
		try
		{
			if (_statusListeners == null || _statusListeners.isEmpty())
				return;
			
			Player player;
			for (int i = 0; i < _statusListeners.size(); i++)
			{
				player = _statusListeners.get(i);
				player.sendPacket(packets);
			}
		}
		finally
		{
			statusListenersLock.unlock();
		}
	}
	
	public void addStatusListener(Player cha)
	{
		if (cha == this)
			return;
		
		statusListenersLock.lock();
		try
		{
			if (_statusListeners == null)
				_statusListeners = new ArrayList<Player>();
			if (!_statusListeners.contains(cha))
				_statusListeners.add(cha);
		}
		finally
		{
			statusListenersLock.unlock();
		}
	}
	
	public void removeStatusListener(Creature cha)
	{
		statusListenersLock.lock();
		try
		{
			if (_statusListeners == null)
				return;
			_statusListeners.remove(cha);
		}
		finally
		{
			statusListenersLock.unlock();
		}
	}
	
	public void clearStatusListeners()
	{
		statusListenersLock.lock();
		try
		{
			if (_statusListeners == null)
				return;
			_statusListeners.clear();
		}
		finally
		{
			statusListenersLock.unlock();
		}
	}
	
	public StatusUpdate makeStatusUpdate(int... fields)
	{
		StatusUpdate su = new StatusUpdate(getObjectId());
		for (int field : fields)
			switch (field)
			{
				case StatusUpdate.CUR_HP:
					su.addAttribute(field, (int) getCurrentHp());
					break;
				case StatusUpdate.MAX_HP:
					su.addAttribute(field, getMaxHp());
					break;
				case StatusUpdate.CUR_MP:
					su.addAttribute(field, (int) getCurrentMp());
					break;
				case StatusUpdate.MAX_MP:
					su.addAttribute(field, getMaxMp());
					break;
				case StatusUpdate.KARMA:
					su.addAttribute(field, getKarma());
					break;
				case StatusUpdate.CUR_CP:
					su.addAttribute(field, (int) getCurrentCp());
					break;
				case StatusUpdate.MAX_CP:
					su.addAttribute(field, getMaxCp());
					break;
				case StatusUpdate.PVP_FLAG:
					su.addAttribute(field, getPvpFlag());
					break;
			}
		return su;
	}
	
	public void broadcastStatusUpdate()
	{
		if (!needStatusUpdate())
			return;
		
		StatusUpdate su = makeStatusUpdate(StatusUpdate.MAX_HP, StatusUpdate.MAX_MP, StatusUpdate.CUR_HP, StatusUpdate.CUR_MP);
		broadcastToStatusListeners(su);
	}
	
	public int calcHeading(int x_dest, int y_dest)
	{
		return (int) (Math.atan2(getY() - y_dest, getX() - x_dest) * HEADINGS_IN_PI) + 32768;
	}
	
	public final double calcStat(Stats stat, double init)
	{
		return calcStat(stat, init, null, null);
	}
	
	public final double calcStat(Stats stat, double init, Creature target, Skill skill)
	{
		int id = stat.ordinal();
		Calculator c = _calculators[id];
		if (c == null)
			return init;
		Env env = new Env();
		env.character = this;
		
		if ((skill != null) && (skill.getId() == 1557))
			env.target = getPlayer().getPet();
		else
		{
			env.target = target;
		}
		env.skill = skill;
		env.value = init;
		c.calc(env);
		return env.value;
	}
	
	public final double calcStat(Stats stat, Creature target, Skill skill)
	{
		if ((skill != null) && (skill.getId() == 1557))
		{
			target = getPlayer().getPet();
		}
		Env env = new Env(this, target, skill);
		if (stat == null)
		{
			_log.warn("FIX ME FAST(!) My name IS " + getName() + " and I'm a player?(" + isPlayer() + ") my target is " + target.getName() + " skill:" + skill.getName() + "");
			return 0;
		}
		env.value = stat.getInit();
		int id = stat.ordinal();
		Calculator c = _calculators[id];
		if (c != null)
			c.calc(env);
		return env.value;
	}
	
	/**
	 * Return the Attack Speed of the L2Character (delay (in milliseconds) before next attack).
	 * @return
	 */
	public int calculateAttackDelay()
	{
		return Formulas.calcPAtkSpd(getPAtkSpd());
	}
	
	public void callSkill(Skill skill, List<Creature> targets, boolean useActionSkills)
	{
		try
		{
			if (useActionSkills && !skill.isUsingWhileCasting() && _triggers != null)
				if (skill.isOffensive())
				{
					if (skill.isMagic())
						useTriggers(getTarget(), TriggerType.OFFENSIVE_MAGICAL_SKILL_USE, null, skill, 0);
					else
						useTriggers(getTarget(), TriggerType.OFFENSIVE_PHYSICAL_SKILL_USE, null, skill, 0);
				}
				else if (skill.isMagic()) // РґР»СЏ РђРѕР•, РїР°С‚Рё/РєР»Р°РЅ Р±Р°С„РѕРІ Рё СЃРµР»С„РѕРІ С‚СЂРёРіРіРµСЂ РЅР°РєР»Р°РґС‹РІР°РµС‚СЃСЏ РЅР° РєР°СЃС‚РµСЂР°
				{
					final boolean targetSelf = skill.isAoE() || skill.isNotTargetAoE() || skill.getTargetType() == Skill.SkillTargetType.TARGET_SELF;
					useTriggers(targetSelf ? this : getTarget(), TriggerType.SUPPORT_MAGICAL_SKILL_USE, null, skill, 0);
				}
			
			Player pl = getPlayer();
			Creature target;
			Iterator<Creature> itr = targets.iterator();
			while (itr.hasNext())
			{
				target = itr.next();
				
				// Р¤РёР»СЊС‚СЂСѓРµРј РЅРµСѓСЏР·РІРёРјС‹Рµ С†РµР»Рё
				if (skill.isOffensive() && target.isInvul())
				{
					Player pcTarget = target.getPlayer();
					if ((!skill.isIgnoreInvul() || (pcTarget != null && pcTarget.isGM())) && !target.isArtefact())
					{
						itr.remove();
						continue;
					}
				}
				
				// Р Р°СЃСЃС‡РёС‚С‹РІР°РµРј РёРіСЂРѕСЂРёСЂСѓРµРјС‹Рµ СЃРєРёР»С‹ РёР· СЃРїРµС†.СЌС„С„РµРєС‚Р°
				Effect ie = target.getEffectList().getEffectByType(EffectType.IgnoreSkill);
				if (ie != null)
					if (ArrayUtils.contains(ie.getTemplate().getParam().getIntegerArray("skillId"), skill.getId()))
					{
						itr.remove();
						continue;
					}
				
				target.getListeners().onMagicHit(skill, this);
				
				if (pl != null)
					if (target != null && target.isNpc())
					{
						NpcInstance npc = (NpcInstance) target;
						List<QuestState> ql = pl.getQuestsForEvent(npc, QuestEventType.MOB_TARGETED_BY_SKILL);
						if (ql != null)
							for (QuestState qs : ql)
								qs.getQuest().notifySkillUse(npc, skill, qs);
					}
				
				if (skill.getNegateSkill() > 0)
					for (Effect e : target.getEffectList().getAllEffects())
					{
						Skill efs = e.getSkill();
						if (efs.getId() == skill.getNegateSkill() && e.isCancelable() && (skill.getNegatePower() <= 0 || efs.getPower() <= skill.getNegatePower()))
							e.exit();
					}
				
				if (skill.getCancelTarget() > 0)
					if (Rnd.chance(skill.getCancelTarget()))
						if ((target.getCastingSkill() == null || !(target.getCastingSkill().getSkillType() == SkillType.TAKECASTLE || target.getCastingSkill().getSkillType() == SkillType.TAKEFORTRESS || target.getCastingSkill().getSkillType() == SkillType.TAKEFLAG)) && !target.isRaid())
						{
							target.abortAttack(true, true);
							target.abortCast(true, true);
							target.setTarget(null);
						}
			}
			
			if (skill.isOffensive())
				startAttackStanceTask();
			
			// РџСЂРёРјРµРЅСЏРµРј СЃРµР»С„СЌС„С„РµРєС‚С‹ РЅР° РєР°СЃС‚РµСЂР°
			// РћСЃРѕР±РѕРµ СѓСЃР»РѕРІРёРµ РґР»СЏ Р°С‚Р°РєСѓСЋС‰РёС… Р°СѓСЂР°-СЃРєРёР»Р»РѕРІ (Vengeance 368):
			// РµСЃР»Рё РЅРё РѕРґРЅР° С†РµР»СЊ РЅРµ Р·Р°РґРµС‚Р° С‚Рѕ СЃРµР»С„СЌС„С„РµРєС‚С‹ РЅРµ РЅР°РєР»Р°РґС‹РІР°СЋС‚СЃСЏ
			if (!(skill.isNotTargetAoE() && skill.isOffensive() && targets.size() == 0))
				skill.getEffects(this, this, false, true);
			
			skill.useSkill(this, targets);
		}
		catch (Exception e)
		{
			_log.warn("Error while Calling Skill! ");
			e.printStackTrace();
		}
	}
	
	public void useTriggers(GameObject target, TriggerType type, Skill ex, Skill owner, double damage)
	{
		if (_triggers == null)
			return;
		
		Set<TriggerInfo> SkillsOnSkillAttack = _triggers.get(type);
		if (SkillsOnSkillAttack != null)
		{
			for (TriggerInfo t : SkillsOnSkillAttack)
			{
				if (t.getSkill() != ex)
					useTriggerSkill(target == null ? getTarget() : target, null, t, owner, damage);
			}
		}
	}
	
	public void useTriggerSkill(GameObject target, List<Creature> targets, TriggerInfo trigger, Skill owner, double damage)
	{
		Skill skill = trigger.getSkill();
		if (skill.getReuseDelay(this) > 0 && isSkillDisabled(skill))
			return;
		
		Creature aimTarget = skill.getAimingTarget(this, target);
		// DS: Р”Р»СЏ С€Р°РЅСЃРѕРІС‹С… СЃРєРёР»Р»РѕРІ СЃ TARGET_SELF Рё СѓСЃР»РѕРІРёРµРј "РїРІРї" СЃР°Рј РєР°СЃС‚РµСЂ Р±СѓРґРµС‚ СЏРІР»СЏС‚СЊСЃСЏ aimTarget,
		// РїРѕСЌС‚РѕРјСѓ РІ СѓСЃР»РѕРІРёСЏС… РґР»СЏ С‚СЂРёРіРіРµСЂР° РїСЂРѕРІРµСЂСЏРµРј СЂРµР°Р»СЊРЅСѓСЋ С†РµР»СЊ.
		Creature realTarget = target != null && target.isCreature() ? (Creature) target : null;
		if (Rnd.get(140) < trigger.getChance() && trigger.checkCondition(this, realTarget, aimTarget, owner, damage) && skill.checkCondition(this, aimTarget, false, true, true))
		{
			if (targets == null)
				targets = skill.getTargets(this, aimTarget, false);
			
			int displayId = 0, displayLevel = 0;
			
			if (skill.hasEffects())
			{
				displayId = skill.getEffectTemplates()[0]._displayId;
				displayLevel = skill.getEffectTemplates()[0]._displayLevel;
			}
			
			if (displayId == 0)
				displayId = skill.getDisplayId();
			if (displayLevel == 0)
				displayLevel = skill.getDisplayLevel();
			
			if (trigger.getType() != TriggerType.SUPPORT_MAGICAL_SKILL_USE)
			{
				for (Creature cha : targets)
					broadcastPacket(new MagicSkillUse(this, cha, displayId, displayLevel, 0, 0));
			}
			
			Formulas.calcSkillMastery(skill, this);
			callSkill(skill, targets, false);
			disableSkill(skill, skill.getReuseDelay(this));
		}
	}
	
	public boolean checkBlockedStat(Stats stat)
	{
		return _blockedStats != null && _blockedStats.contains(stat);
	}
	
	public boolean checkReflectSkill(Creature attacker, Skill skill)
	{
		if (!skill.isReflectable())
			return false;
		// Does not reflect if there is invulnerable, or it may cancel
		if (isInvul() || attacker.isInvul() || !skill.isOffensive())
			return false;
		// Of the magical skills are reflected only damaging skills for CPs.
		if (skill.isMagic() && skill.getSkillType() != SkillType.MDAM)
			return false;
		if (Rnd.chance(calcStat(skill.isMagic() ? Stats.REFLECT_MAGIC_SKILL : Stats.REFLECT_PHYSIC_SKILL, 0, attacker, skill)))
		{
			sendPacket(new SystemMessage2(SystemMsg.YOU_COUNTERED_C1S_ATTACK).addName(attacker));
			attacker.sendPacket(new SystemMessage2(SystemMsg.C1_DODGES_THE_ATTACK).addName(this));
			return true;
		}
		return false;
	}
	
	public void doCounterAttack(Skill skill, Creature attacker, boolean blow)
	{
		if (isDead()) // if the character is already dead, counter should not be
			return;
		if (isDamageBlocked() || attacker.isDamageBlocked()) // РќРµ РєРѕРЅС‚СЂР°С‚Р°РєСѓРµРј, РµСЃР»Рё РµСЃС‚СЊ РЅРµСѓСЏР·РІРёРјРѕСЃС‚СЊ, РёРЅР°С‡Рµ РѕРЅР° РјРѕР¶РµС‚ РѕС‚РјРµРЅРёС‚СЊСЃСЏ
			return;
		if (skill == null || skill.hasEffects() || skill.isMagic() || !skill.isOffensive() || skill.getCastRange() > 200)
			return;
		if (Rnd.chance(calcStat(Stats.COUNTER_ATTACK, 0, attacker, skill)))
		{
			double damage = 1189 * getPAtk(attacker) / Math.max(attacker.getPDef(this), 1);
			attacker.sendPacket(new SystemMessage2(SystemMsg.C1_IS_PERFORMING_A_COUNTERATTACK).addName(this));
			if (blow) // СѓСЂРѕРЅ С…2 РґР»СЏ РѕС‚СЂР°Р¶РµРЅРёСЏ blow СЃРєРёР»Р»РѕРІ
			{
				sendPacket(new SystemMessage2(SystemMsg.C1_IS_PERFORMING_A_COUNTERATTACK).addName(this));
				sendPacket(new SystemMessage2(SystemMsg.C1_HAS_GIVEN_C2_DAMAGE_OF_S3).addName(this).addName(attacker).addInteger((long) damage));
				attacker.reduceCurrentHp(damage, this, skill, true, true, false, false, false, false, true);
			}
			else
				sendPacket(new SystemMessage2(SystemMsg.C1_IS_PERFORMING_A_COUNTERATTACK).addName(this));
			sendPacket(new SystemMessage2(SystemMsg.C1_HAS_GIVEN_C2_DAMAGE_OF_S3).addName(this).addName(attacker).addInteger((long) damage));
			attacker.reduceCurrentHp(damage, this, skill, true, true, false, false, false, false, true);
		}
	}
	
	/**
	 * Disable this skill id for the duration of the delay in milliseconds.
	 * @param skill
	 * @param delay (seconds * 1000)
	 */
	public void disableSkill(Skill skill, long delay)
	{
		_skillReuses.put(skill.hashCode(), new TimeStamp(skill, delay));
	}
	
	public abstract boolean isAutoAttackable(Creature attacker);
	
	public void doAttack(Creature target)
	{
		if (target == null || isAMuted() || isAttackingNow() || isAlikeDead() || target.isAlikeDead() || !isInRange(target, 2000) || isPlayer() && getPlayer().isInMountTransform())
			return;
		
		if (target.isInvisible() && getAI() instanceof DefaultAI)
		{
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
			return;
		}
		
		getListeners().onAttack(target);
		
		final WeaponTemplate weaponItem = getActiveWeaponItem();
		
		// Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack)
		final int sAtk = calculateTimeBetweenAttacks(target, weaponItem);
		final int timeToHit = sAtk / 2;
		
		// Get the Attack Reuse Delay of the L2Weapon
		final int reuse = calculateReuseTime(target, weaponItem);
		
		final int ssGrade = (weaponItem != null ? weaponItem.getCrystalType().externalOrdinal : 0);
		
		// Reuse time
		_attackReuseEndTime = System.currentTimeMillis() + sAtk + reuse - 10;
		
		// Ready to act
		ThreadPoolManager.getInstance().schedule(new NotifyAITask(this, CtrlEvent.EVT_READY_TO_ACT, null, null), sAtk + reuse);
		
		// DS: adjusted by 1/100 of a second since the AI task is called with a small error
		// Especially on slower machines and is broken by autoattacks isAttackingNow () == true
		_attackEndTime = sAtk + System.currentTimeMillis() - 10;
		_isAttackAborted = false;
		
		Attack attack = new Attack(this, target, getChargedSoulShot(), ssGrade);
		
		setHeading(PositionUtils.calculateHeadingFrom(this, target));
		
		// Select the type of attack to
		if (weaponItem == null)
			doAttackHitSimple(attack, target, 1., !isPlayer(), timeToHit, true);
		else
			switch (weaponItem.getItemType())
			{
				case BOW:
				case CROSSBOW:
					// Gauge
					sendPacket(new SetupGauge(this, SetupGauge.RED, sAtk + reuse));
					
					doAttackHitByBow(attack, target, timeToHit);
					break;
				case POLE:
					doAttackHitByPole(attack, target, timeToHit);
					break;
				case DUAL:
				case DUALFIST:
				case DUALDAGGER:
					doAttackHitByDual(attack, target, timeToHit);
					break;
				default:
					doAttackHitSimple(attack, target, 1., true, timeToHit, true);
			}
		
		if (attack.hasHits())
			broadcastPacket(attack);
	}
	
	public int calculateTimeBetweenAttacks(Creature target, WeaponTemplate weapon)
	{
		if (weapon != null)
		{
			switch (weapon.getItemType())
			{
				case BOW:
					return (1500 * 345) / getPAtkSpd();
				case CROSSBOW:
					return (1200 * 345) / getPAtkSpd();
			}
		}
		return Formulas.calcPAtkSpd(getPAtkSpd());
	}
	
	public int calculateReuseTime(Creature target, WeaponTemplate weapon)
	{
		if (weapon == null)
		{
			return 0;
		}
		
		int reuse = weapon.getAttackReuseDelay();
		// only bows should continue for now
		if (reuse == 0)
		{
			return 0;
		}
		
		reuse *= getReuseModifier(target);
		double atkSpd = getPAtkSpd();
		switch (weapon.getItemType())
		{
			case BOW:
			case CROSSBOW:
				return (int) ((reuse * 345) / atkSpd);
			default:
				return (int) ((reuse * 312) / atkSpd);
		}
	}
	
	private void doAttackHitSimple(Attack attack, Creature target, double multiplier, boolean unchargeSS, int sAtk, boolean notify)
	{
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;
		boolean miss1 = Formulas.calcHitMiss(this, target);
		
		if (!miss1)
		{
			AttackInfo info = Formulas.calcPhysDam(this, target, null, false, false, attack._soulshot, false);
			damage1 = (int) (info.damage * multiplier);
			shld1 = info.shld;
			crit1 = info.crit;
		}
		
		ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, unchargeSS, notify), sAtk);
		
		attack.addHit(target, damage1, miss1, crit1, shld1);
	}
	
	private void doAttackHitByBow(Attack attack, Creature target, int sAtk)
	{
		WeaponTemplate activeWeapon = getActiveWeaponItem();
		if (activeWeapon == null)
			return;
		
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;
		
		// Calculate if hit is missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);
		
		reduceArrowCount();
		
		if (!miss1)
		{
			AttackInfo info = Formulas.calcPhysDam(this, target, null, false, false, attack._soulshot, false);
			damage1 = (int) info.damage;
			shld1 = info.shld;
			crit1 = info.crit;
			
			int range = activeWeapon.getAttackRange();
			damage1 *= Math.min(range, getDistance(target)) / range * .4 + 0.8; // СЂР°Р·Р±СЂРѕСЃ 20% РІ РѕР±Рµ СЃС‚РѕСЂРѕРЅС‹
		}
		
		ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, true, true), sAtk);
		
		attack.addHit(target, damage1, miss1, crit1, shld1);
	}
	
	private void doAttackHitByDual(Attack attack, Creature target, int sAtk)
	{
		int damage1 = 0;
		int damage2 = 0;
		boolean shld1 = false;
		boolean shld2 = false;
		boolean crit1 = false;
		boolean crit2 = false;
		
		boolean miss1 = Formulas.calcHitMiss(this, target);
		boolean miss2 = Formulas.calcHitMiss(this, target);
		
		if (!miss1)
		{
			AttackInfo info = Formulas.calcPhysDam(this, target, null, true, false, attack._soulshot, false);
			damage1 = (int) info.damage;
			shld1 = info.shld;
			crit1 = info.crit;
		}
		
		if (!miss2)
		{
			AttackInfo info = Formulas.calcPhysDam(this, target, null, true, false, attack._soulshot, false);
			damage2 = (int) info.damage;
			shld2 = info.shld;
			crit2 = info.crit;
		}
		
		// Create a new hit task with Medium priority for hit 1 and for hit 2 with a higher delay
		ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, true, false), sAtk / 2);
		ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage2, crit2, miss2, attack._soulshot, shld2, false, true), sAtk);
		
		attack.addHit(target, damage1, miss1, crit1, shld1);
		attack.addHit(target, damage2, miss2, crit2, shld2);
	}
	
	private void doAttackHitByPole(Attack attack, Creature target, int sAtk)
	{
		int angle = (int) calcStat(Stats.POLE_ATTACK_ANGLE, 90, target, null);
		int range = (int) calcStat(Stats.POWER_ATTACK_RANGE, getTemplate().baseAtkRange, target, null);
		
		// Р�СЃРїРѕР»СЊР·СѓРµРј Math.round С‚.Рє. РѕР±С‹С‡РЅС‹Р№ РєР°СЃС‚РёРЅРі РѕР±СЂРµР·Р°РµС‚ Рє РјРµРЅСЊС€РµРјСѓ
		// double d = 2.95. int i = (int)d, РІС‹Р№РґРµС‚ С‡С‚Рѕ i = 2
		// РµСЃР»Рё 1% СѓРіР»Р° РёР»Рё 1 РґРёСЃС‚Р°РЅС†РёРё РЅРµ РёРіСЂР°РµС‚ РѕРіСЂРѕРјРЅРѕР№ СЂРѕР»Рё, С‚Рѕ РґР»СЏ
		// РєРѕР»РёС‡РµСЃС‚РІР° С†РµР»РµР№ СЌС‚Рѕ РєСЂРёС‚РёС‡РЅРѕ
		int attackcountmax = (int) Math.round(calcStat(Stats.POLE_TARGET_COUNT, 0, target, null));
		
		if (isBoss())
			attackcountmax += 27;
		else if (isRaid())
			attackcountmax += 12;
		else if (isMonster() && getLevel() > 0)
			attackcountmax += getLevel() / 7.5;
		
		double mult = 1.;
		_poleAttackCount = 1;
		
		if (!isInZonePeace())// Guard with a lance will attack only the single target in
			for (Creature t : getAroundCharacters(range, 200))
				if (_poleAttackCount <= attackcountmax)
				{
					if (t == target || t.isDead() || !PositionUtils.isFacing(this, t, angle))
						continue;
					
					if (t.isAutoAttackable(this))
					{
						doAttackHitSimple(attack, t, mult, false, sAtk, false);
						mult *= BalancerConfig.ALT_POLE_DAMAGE_MODIFIER;
						_poleAttackCount++;
					}
				}
				else
					break;
		
		_poleAttackCount = 0;
		doAttackHitSimple(attack, target, 1., true, sAtk, true);
	}
	
	public long getAnimationEndTime()
	{
		return _animationEndTime;
	}
	
	@SuppressWarnings("incomplete-switch")
	public void doCast(Skill skill, Creature target, boolean forceUse)
	{
		if (skill == null)
			return;
		
		boolean disallowSkill = false;
		if (getPlayer() != null && getPlayer().isInTvT())
		{
			for (String skillId : Config.EVENT_TvT_DISALLOWED_SKILLS)
			{
				if (skill.getId() == Integer.parseInt(skillId))
					disallowSkill = true;
				else
					continue;
			}
		}
		
		if (getPlayer() != null && getPlayer().isInCtF())
		{
			for (String skillId : Config.EVENT_CtF_DISALLOWED_SKILLS)
			{
				if (skill.getId() == Integer.parseInt(skillId))
					disallowSkill = true;
				else
					continue;
			}
		}
		
		if (disallowSkill)
		{
			getPlayer().sendMessage("Action is not allowed");
			return;
		}
		
		for (GlobalEvent e : getEvents())
		{
			if (!e.canUseSkill(this, target, skill))
			{
				sendMessage("The skill you are trying to cast is not allowed in your current state");
				return;
			}
		}
		
		int itemConsume[] = skill.getItemConsume();
		
		if (itemConsume[0] > 0)
			for (int i = 0; i < itemConsume.length; i++)
				if (!consumeItem(skill.getItemConsumeId()[i], itemConsume[i]))
				{
					sendPacket(skill.isHandler() ? SystemMsg.INCORRECT_ITEM_COUNT : SystemMsg.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
					return;
				}
		
		if (skill.getReferenceItemId() > 0)
			if (!consumeItemMp(skill.getReferenceItemId(), skill.getReferenceItemMpConsume()))
				return;
		
		int magicId = skill.getId();
		
		if (target == null)
			target = skill.getAimingTarget(this, getTarget());
		if (target == null)
			return;
		
		getListeners().onMagicUse(skill, target, false);
		
		if (this != target)
			setHeading(PositionUtils.calculateHeadingFrom(this, target));
		
		int level = Math.max(1, getSkillDisplayLevel(magicId));
		
		int skillTime = skill.isSkillTimePermanent() ? skill.getHitTime() : Formulas.calcMAtkSpd(this, skill, skill.getHitTime());
		int skillInterruptTime = skill.isMagic() ? Formulas.calcMAtkSpd(this, skill, skill.getSkillInterruptTime()) : 0;
		
		int minCastTime = Math.min(BalancerConfig.SKILLS_CAST_TIME_MIN, skill.getHitTime());
		if (skillTime < minCastTime)
		{
			skillTime = minCastTime;
			skillInterruptTime = 0;
		}
		
		_animationEndTime = System.currentTimeMillis() + skillTime;
		
		if (skill.isMagic() && !skill.isSkillTimePermanent() && getChargedSpiritShot() > 0)
		{
			skillTime = (int) (0.70 * skillTime);
			skillInterruptTime = (int) (0.70 * skillInterruptTime);
		}
		
		Formulas.calcSkillMastery(skill, this); // Calculate skill mastery for current cast
		long reuseDelay = Math.max(0, Formulas.calcSkillReuseDelay(this, skill));
		
		broadcastPacket(new MagicSkillUse(this, target, skill.getDisplayId(), level, skillTime, reuseDelay));
		
		if (!skill.isHandler())
			disableSkill(skill, reuseDelay);
		
		if (isPlayer())
			if (skill.getSkillType() == SkillType.PET_SUMMON)
				sendPacket(SystemMsg.SUMMONING_YOUR_PET);
			else if (!skill.isHandler())
				sendPacket(new SystemMessage2(SystemMsg.YOU_USE_S1).addSkillName(magicId, level));
			else
				sendPacket(new SystemMessage2(SystemMsg.YOU_USE_S1).addItemName(skill.getItemConsumeId()[0]));
		
		if (skill.getTargetType() == SkillTargetType.TARGET_HOLY)
			target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this, 1);
		
		switch (skill.getFlyType())
		{
			case DUMMY:
			case CHARGE:
				Location flyLoc = getFlyLocation(target, skill);
				if (flyLoc != null)
				{
					_flyLoc = flyLoc;
				}
				else
				{
					sendPacket(SystemMsg.CANNOT_SEE_TARGET);
					return;
				}
				break;
			default:
				_flyLoc = null;
				break;
		}
		
		double mpConsume1 = skill.isUsingWhileCasting() ? skill.getMpConsume() : skill.getMpConsume1();
		if (mpConsume1 > 0)
		{
			if (_currentMp < mpConsume1)
			{
				sendPacket(SystemMsg.NOT_ENOUGH_MP);
				onCastEndTime();
				return;
			}
			reduceCurrentMp(mpConsume1, null);
		}
		
		if (_flyLoc != null)
		{
			broadcastPacket(new FlyToLocation(this, _flyLoc, skill.getFlyType()));
		}
		
		_castingSkill = skill;
		_castInterruptTime = System.currentTimeMillis() + skillInterruptTime;
		setCastingTarget(target);
		
		if (skill.isUsingWhileCasting())
			callSkill(skill, skill.getTargets(this, target, forceUse), true);
		
		if (isPlayer())
			sendPacket(new SetupGauge(this, SetupGauge.BLUE, skillTime));
		
		_scheduledCastCount = skill.getCastCount();
		_scheduledCastInterval = skill.getCastCount() > 0 ? skillTime / _scheduledCastCount : skillTime;
		
		// Create a task MagicUseTask with Medium priority to launch the MagicSkill at the end of the casting time
		_skillLaunchedTask = ThreadPoolManager.getInstance().schedule(new MagicLaunchedTask(this, forceUse), skillInterruptTime);
		_skillTask = ThreadPoolManager.getInstance().schedule(new MagicUseTask(this, forceUse), skill.getCastCount() > 0 ? skillTime / skill.getCastCount() : skillTime);
		
		_skillGeoCheckTask = null;
		if ((skill.getCastRange() < 32767) && (skill.getSkillType() != SkillType.TAKECASTLE) && (skill.getSkillType() != SkillType.TAKEFORTRESS) && (_scheduledCastInterval > 600))
		{
			_skillGeoCheckTask = ThreadPoolManager.getInstance().schedule(new MagicGeoCheckTask(this), (long) (_scheduledCastInterval * 0.5));
		}
	}
	
	public void addReceivedDebuff(int skillId, long period)
	{
		if (!Config.DEBUFF_PROTECTION_SYSTEM)
			return;
		
		Long[] debuffInfo = new Long[2];
		debuffInfo[0] = System.currentTimeMillis() + period;// Time of Debuff Effect End
		debuffInfo[1] = period;
		receivedDebuffs.put(skillId, debuffInfo);
	}
	
	public double getReceivedDebuffMod(int skillId, double currentChance)
	{
		if (!Config.DEBUFF_PROTECTION_SYSTEM)
			return 1.0;
		
		if (currentChance >= 100.0)
			return 1.0;
		
		if (!receivedDebuffs.containsKey(skillId))
			return 1.0;
		
		long dateOfDebuffEnd = receivedDebuffs.get(skillId)[0].longValue();
		double lastDebuffPeriod = receivedDebuffs.get(skillId)[1].longValue() / 1000L;
		
		if (dateOfDebuffEnd == 0L)
			return 1.0;
		
		long secondsDifference = (System.currentTimeMillis() - dateOfDebuffEnd) / 1000L;
		
		if (secondsDifference <= 0L)
			return 0.0;
		
		return Math.min(1.0, (currentChance / 100.0) * (secondsDifference / lastDebuffPeriod));
	}
	
	private Location _flyLoc;
	
	private Location getFlyLocation(GameObject target, Skill skill)
	{
		if (target != null && target != this)
		{
			Location loc;
			
			double radian = PositionUtils.convertHeadingToRadian(target.getHeading());
			if (skill.isFlyToBack())
				loc = new Location(target.getX() + (int) (Math.sin(radian) * 40), target.getY() - (int) (Math.cos(radian) * 40), target.getZ());
			else
				loc = new Location(target.getX() - (int) (Math.sin(radian) * 40), target.getY() + (int) (Math.cos(radian) * 40), target.getZ());
			
			if (isFlying())
			{
				if (isPlayer() && ((Player) this).isInFlyingTransform() && (loc.z <= 0 || loc.z >= 6000))
					return null;
				if (GeoEngine.moveCheckInAir(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getColRadius(), getGeoIndex()) == null)
					return null;
			}
			else
			{
				loc.correctGeoZ();
				
				if (!GeoEngine.canMoveToCoord(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getGeoIndex()))
				{
					loc = target.getLoc(); // Р•СЃР»Рё РЅРµ РїРѕР»СѓС‡Р°РµС‚СЃСЏ РІСЃС‚Р°С‚СЊ СЂСЏРґРѕРј СЃ РѕР±СЉРµРєС‚РѕРј, РїСЂРѕР±СѓРµРј РІСЃС‚Р°С‚СЊ РїСЂСЏРјРѕ РІ РЅРµРіРѕ
					if (!GeoEngine.canMoveToCoord(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getGeoIndex()))
						return null;
				}
			}
			
			return loc;
		}
		
		double radian = PositionUtils.convertHeadingToRadian(getHeading());
		int x1 = -(int) (Math.sin(radian) * skill.getFlyRadius());
		int y1 = (int) (Math.cos(radian) * skill.getFlyRadius());
		
		if (isFlying())
			return GeoEngine.moveCheckInAir(getX(), getY(), getZ(), getX() + x1, getY() + y1, getZ(), getColRadius(), getGeoIndex());
		return GeoEngine.moveCheck(getX(), getY(), getZ(), getX() + x1, getY() + y1, getGeoIndex());
	}
	
	public final void doDie(Creature killer)
	{
		// killing is only possible one time
		if (!isDead.compareAndSet(false, true))
			return;
		 for (Player player : World.getAroundPlayers(this))
		 {
		 if (player.getTarget() == this && player.isCastingNow())
		 player.abortCast(true, false);
		 }
		onDeath(killer);
	}
	
	protected void onDeath(Creature killer)
	{
		if (killer != null)
		{
			Player killerPlayer = killer.getPlayer();
			if (killerPlayer != null)
				killerPlayer.getListeners().onKillIgnorePetOrSummon(this);
			
			// Alexander - Add a new mob death to the stats
			 if (isPlayer() && killer.isMonster())
			 {
			 getPlayer().addPlayerStats(Ranking.STAT_TOP_MOBS_DEATHS);
			 }
			
			killer.getListeners().onKill(this);
			
			if (isPlayer() && killer.isPlayable())
				_currentCp = 0;
		}
		
		setTarget(null);
		stopMove();
		stopAttackStanceTask();
		stopRegeneration();
		
		_currentHp = 0;
		
		  // unsummon pet when player dies
        if (isPlayer() && getPlayer().getPet() != null)	
        {	
            getPlayer().getPet().unSummon();	
        }
		
		boolean fightClubKeepBuffs = isPlayable() && getPlayer().isInFightClub() && !getPlayer().getFightClubEvent().loseBuffsOnDeath(getPlayer());
		// Stop all active skills effects in progress on the L2Character
		if (isBlessedByNoblesse() || isSalvation() || fightClubKeepBuffs)
		{
			if (isSalvation() && isPlayer() && !getPlayer().isInOlympiadMode())
			{
				getPlayer().reviveRequest(getPlayer(), 100, false);
			}
			for (Effect e : getEffectList().getAllEffects())
			{
				// Noblesse Blessing Buff/debuff effects are retained after
				// death. However, Noblesse Blessing and Lucky Charm are lost as normal.
				if (e.getEffectType() == EffectType.BlessNoblesse || e.getSkill().getId() == Skill.SKILL_FORTUNE_OF_NOBLESSE || e.getSkill().getId() == Skill.SKILL_RAID_BLESSING)
					e.exit();
				else if (e.getEffectType() == EffectType.AgathionResurrect && !(isPlayable() && getPlayer().isInFightClub()))
				{
					if (isPlayer())
						getPlayer().setAgathionRes(true);
					e.exit();
				}
			}
		}
		else
		 if (!isPlayable() || !getPlayer().isInFightClub()) // Cancel all effects unless in event.
		{
			for (Effect e : getEffectList().getAllEffects())
			{
				// Some effects persist at death
				if (e.getEffectType() != EffectType.Transformation && !e.getSkill().isPreservedOnDeath())
					e.exit();
			}
		}
		
		// Fight Club
		// Killer is in Fight Club
		if (killer != null && killer.isPlayable() && killer.getPlayer().isInFightClub())
			killer.getPlayer().getFightClubEvent().onKilled(killer, this);
		// Dead guy killed by monster in fight club
		else if (isPlayable() && getPlayer().isInFightClub())
			getPlayer().getFightClubEvent().onKilled(killer, this);
		
		ThreadPoolManager.getInstance().execute(new NotifyAITask(this, CtrlEvent.EVT_DEAD, killer, null));
		
		getListeners().onDeath(killer);
		
		updateEffectIcons();
		updateStats();
		broadcastStatusUpdate();
	}
	
	protected void onRevive()
	{
		
	}
	
	public void enableSkill(Skill skill)
	{
		_skillReuses.remove(skill.hashCode());
	}
	
	/**
	 * Return a map of 32 bits (0x00000000) containing all abnormal effects
	 * @return
	 */
	public int getAbnormalEffect()
	{
		return _abnormalEffects;
	}
	
	/**
	 * Return a map of 32 bits (0x00000000) containing all special effects
	 * @return
	 */
	public int getAbnormalEffect2()
	{
		return _abnormalEffects2;
	}
	
	/**
	 * Return a map of 32 bits (0x00000000) containing all event effects
	 * @return
	 */
	public int getAbnormalEffect3()
	{
		return _abnormalEffects3;
	}
	
	public int getAccuracy()
	{
		return (int) calcStat(Stats.ACCURACY_COMBAT, 0, null, null);
	}
	
	/**
	 * Р’РѕР·РІСЂР°С‰Р°РµС‚ РєРѕР»Р»РµРєС†РёСЋ СЃРєРёР»Р»РѕРІ РґР»СЏ Р±С‹СЃС‚СЂРѕРіРѕ РїРµСЂРµР±РѕСЂР°
	 * @return
	 */
	public Collection<Skill> getAllSkills()
	{
		return _skills.values();
	}
	
	/**
	 * Р’РѕР·РІСЂР°С‰Р°РµС‚ РјР°СЃСЃРёРІ СЃРєРёР»Р»РѕРІ РґР»СЏ Р±РµР·РѕРїР°СЃРЅРѕРіРѕ РїРµСЂРµР±РѕСЂР°
	 * @return
	 */
	public final Skill[] getAllSkillsArray()
	{
		Collection<Skill> vals = _skills.values();
		return vals.toArray(new Skill[vals.size()]);
	}
	
	public final double getAttackSpeedMultiplier()
	{
		return 1.1 * getPAtkSpd() / getTemplate().basePAtkSpd;
	}
	
	public int getBuffLimit()
	{
		return (int) calcStat(Stats.BUFF_LIMIT, Config.ALT_BUFF_LIMIT, null, null);
	}
	
	public Skill getCastingSkill()
	{
		return _castingSkill;
	}
	
	public int getCON()
	{
		return (int) calcStat(Stats.STAT_CON, _template.baseCON, null, null);
	}
	
	/**
	 * Р’РѕР·РІСЂР°С‰Р°РµС‚ С€Р°РЅСЃ С„РёР·РёС‡РµСЃРєРѕРіРѕ РєСЂРёС‚Р° (1000 == 100%)
	 * @param target
	 * @param skill
	 * @return
	 */
	public int getCriticalHit(Creature target, Skill skill)
	{
		return (int) calcStat(Stats.CRITICAL_BASE, _template.baseCritRate, target, skill);
	}
	
	/**
	 * Р’РѕР·РІСЂР°С‰Р°РµС‚ С€Р°РЅСЃ РјР°РіРёС‡РµСЃРєРѕРіРѕ РєСЂРёС‚Р° РІ РїСЂРѕС†РµРЅС‚Р°С…
	 * @param target
	 * @param skill
	 * @return
	 */
	public double getMagicCriticalRate(Creature target, Skill skill)
	{
		return calcStat(Stats.MCRITICAL_RATE, target, skill);
	}
	
	/**
	 * Return the current CP of the L2Character.
	 * @return
	 */
	public final double getCurrentCp()
	{
		return _currentCp;
	}
	
	public final double getCurrentCpRatio()
	{
		return getCurrentCp() / getMaxCp();
	}
	
	public final double getCurrentCpPercents()
	{
		return getCurrentCpRatio() * 100.;
	}
	
	public final boolean isCurrentCpFull()
	{
		return getCurrentCp() >= getMaxCp();
	}
	
	public final boolean isCurrentCpZero()
	{
		return getCurrentCp() < 1;
	}
	
	public final double getCurrentHp()
	{
		return _currentHp;
	}
	
	public final double getCurrentHpRatio()
	{
		return getCurrentHp() / getMaxHp();
	}
	
	public final double getCurrentHpPercents()
	{
		return getCurrentHpRatio() * 100.;
	}
	
	public final boolean isCurrentHpFull()
	{
		return getCurrentHp() >= getMaxHp();
	}
	
	public final boolean isCurrentHpZero()
	{
		return getCurrentHp() < 1;
	}
	
	public final double getCurrentMp()
	{
		return _currentMp;
	}
	
	public final double getCurrentMpRatio()
	{
		return getCurrentMp() / getMaxMp();
	}
	
	public final double getCurrentMpPercents()
	{
		return getCurrentMpRatio() * 100.;
	}
	
	public final boolean isCurrentMpFull()
	{
		return getCurrentMp() >= getMaxMp();
	}
	
	public final boolean isCurrentMpZero()
	{
		return getCurrentMp() < 1;
	}
	
	public Location getDestination()
	{
		return destination;
	}
	
	public int getDEX()
	{
		return (int) calcStat(Stats.STAT_DEX, _template.baseDEX, null, null);
	}
	
	public int getEvasionRate(Creature target)
	{
		return (int) calcStat(Stats.EVASION_RATE, 0, target, null);
	}
	
	public int getINT()
	{
		return (int) calcStat(Stats.STAT_INT, _template.baseINT, null, null);
	}
	
	public List<Creature> getAroundCharacters(int radius, int height)
	{
		if (!isVisible())
			return Collections.emptyList();
		return World.getAroundCharacters(this, radius, height);
	}
	
	public List<NpcInstance> getAroundNpc(int range, int height)
	{
		if (!isVisible())
			return Collections.emptyList();
		return World.getAroundNpc(this, range, height);
	}
	
	public boolean knowsObject(GameObject obj)
	{
		return World.getAroundObjectById(this, obj.getObjectId()) != null;
	}
	
	public final Skill getKnownSkill(int skillId)
	{
		return _skills.get(skillId);
	}
	
	public final int getMagicalAttackRange(Skill skill)
	{
		if (skill != null)
			return (int) calcStat(Stats.MAGIC_ATTACK_RANGE, skill.getCastRange(), null, skill);
		return getTemplate().baseAtkRange;
	}
	
	public int getMAtk(Creature target, Skill skill)
	{
		if (skill != null && skill.getMatak() > 0)
			return skill.getMatak();
		return (int) calcStat(Stats.MAGIC_ATTACK, _template.baseMAtk, target, skill);
	}
	
	public int getMAtkSpd()
	{
		return (int) (calcStat(Stats.MAGIC_ATTACK_SPEED, _template.baseMAtkSpd, null, null));
	}
	
	public final int getMaxCp()
	{
		return (int) calcStat(Stats.MAX_CP, _template.baseCpMax, null, null);
	}
	
	public int getMaxHp()
	{
		return (int) calcStat(Stats.MAX_HP, _template.baseHpMax, null, null);
	}
	
	public int getMaxMp()
	{
		return (int) calcStat(Stats.MAX_MP, _template.baseMpMax, null, null);
	}
	
	public int getMDef(Creature target, Skill skill)
	{
		return Math.max((int) calcStat(Stats.MAGIC_DEFENCE, _template.baseMDef, target, skill), 1);
	}
	
	public int getMEN()
	{
		return (int) calcStat(Stats.STAT_MEN, _template.baseMEN, null, null);
	}
	
	public double getMinDistance(GameObject obj)
	{
		double distance = getTemplate().collisionRadius;
		
		if (obj != null && obj.isCreature())
			distance += ((Creature) obj).getTemplate().collisionRadius;
		
		return distance;
	}
	
	public double getMovementSpeedMultiplier()
	{
		return getRunSpeed() * 1. / _template.baseRunSpd;
	}
	
	@Override
	public int getMoveSpeed()
	{
		if (isRunning())
			return getRunSpeed();
		
		return getWalkSpeed();
	}
	
	@Override
	public String getName()
	{
		return StringUtils.defaultString(_name);
	}
	
	public int getPAtk(Creature target)
	{
		return (int) calcStat(Stats.POWER_ATTACK, _template.basePAtk, target, null);
	}
	
	public int getPAtkSpd()
	{
		return (int) calcStat(Stats.POWER_ATTACK_SPEED, _template.basePAtkSpd, null, null);
	}
	
	public int getPDef(Creature target)
	{
		return (int) calcStat(Stats.POWER_DEFENCE, _template.basePDef, target, null);
	}
	
	public final int getPhysicalAttackRange()
	{
		return (int) calcStat(Stats.POWER_ATTACK_RANGE, getTemplate().baseAtkRange, null, null);
	}
	
	public final int getRandomDamage()
	{
		WeaponTemplate weaponItem = getActiveWeaponItem();
		if (weaponItem == null)
			return 5 + (int) Math.sqrt(getLevel());
		return weaponItem.getRandomDamage();
	}
	
	public double getReuseModifier(Creature target)
	{
		return calcStat(Stats.ATK_REUSE, 1, target, null);
	}
	
	public int getRunSpeed()
	{
		return getSpeed(_template.baseRunSpd);
	}
	
	public final int getShldDef()
	{
		if (isPlayer())
			return (int) calcStat(Stats.SHIELD_DEFENCE, 0, null, null);
		return (int) calcStat(Stats.SHIELD_DEFENCE, _template.baseShldDef, null, null);
	}
	
	public final int getSkillDisplayLevel(Integer skillId)
	{
		Skill skill = _skills.get(skillId);
		if (skill == null)
			return -1;
		return skill.getDisplayLevel();
	}
	
	public final int getSkillLevel(Integer skillId)
	{
		return getSkillLevel(skillId, -1);
	}
	
	public final int getSkillLevel(Integer skillId, int def)
	{
		Skill skill = _skills.get(skillId);
		if (skill == null)
			return def;
		return skill.getLevel();
	}
	
	public int getSkillMastery(Integer skillId)
	{
		if (_skillMastery == null)
			return 0;
		Integer val = _skillMastery.get(skillId);
		return val == null ? 0 : val.intValue();
	}
	
	public void removeSkillMastery(Integer skillId)
	{
		if (_skillMastery != null)
			_skillMastery.remove(skillId);
	}
	
	public int getSpeed(int baseSpeed)
	{
		if (isInWater())
			return getSwimSpeed();
		return (int) calcStat(Stats.RUN_SPEED, baseSpeed, null, null);
	}
	
	public int getSTR()
	{
		return (int) calcStat(Stats.STAT_STR, _template.baseSTR, null, null);
	}
	
	public int getSwimSpeed()
	{
		return (int) calcStat(Stats.RUN_SPEED, Config.SWIMING_SPEED, null, null);
	}
	
	public GameObject getTarget()
	{
		return target.get();
	}
	
	public final int getTargetId()
	{
		GameObject target = getTarget();
		return target == null ? -1 : target.getObjectId();
	}
	
	public CharTemplate getTemplate()
	{
		return _template;
	}
	
	public CharTemplate getBaseTemplate()
	{
		return _baseTemplate;
	}
	
	public String getTitle()
	{
		return StringUtils.defaultString(_title);
	}
	
	public final int getWalkSpeed()
	{
		if (isInWater())
			return getSwimSpeed();
		return getSpeed(_template.baseWalkSpd);
	}
	
	public int getWIT()
	{
		return (int) calcStat(Stats.STAT_WIT, _template.baseWIT, null, null);
	}
	
	public double headingToRadians(int heading)
	{
		return (heading - 32768) / HEADINGS_IN_PI;
	}
	
	public boolean isAlikeDead()
	{
		return _fakeDeath || isDead();
	}
	
	public final boolean isAttackingNow()
	{
		return _attackEndTime > System.currentTimeMillis();
	}
	
	public final boolean isBlessedByNoblesse()
	{
		return _isBlessedByNoblesse;
	}
	
	public final boolean isSalvation()
	{
		return _isSalvation;
	}
	
	public boolean isEffectImmune()
	{
		return _effectImmunity.get();
	}
	
	public boolean isBuffImmune()
	{
		return _buffImmunity.get();
	}
	
	public boolean isDebuffImmune()
	{
		return _debuffImmunity.get();
	}
	
	public boolean isDead()
	{
		return _currentHp < 0.5 || isDead.get();
	}
	
	@Override
	public final boolean isFlying()
	{
		return _flying;
	}
	
	/**
	 * РќР°С…РѕРґРёС‚СЃСЏ Р»Рё РїРµСЂСЃРѕРЅР°Р¶ РІ Р±РѕРµРІРѕР№ РїРѕР·Рµ
	 * @return true, РµСЃР»Рё РїРµСЂСЃРѕРЅР°Р¶ РІ Р±РѕРµРІРѕР№ РїРѕР·Рµ, Р°С‚Р°РєРѕРІР°РЅ РёР»Рё Р°С‚Р°РєСѓРµС‚
	 */
	public final boolean isInCombat()
	{
		return System.currentTimeMillis() < _stanceEndTime;
	}
	
	public boolean isInvul()
	{
		return _isInvul;
	}
	
	public boolean isMageClass()
	{
		return getTemplate().baseMAtk > 3;
	}
	
	public final boolean isRunning()
	{
		return _running;
	}
	
	public final void setReuseDelay(long newReuseDelay)
	{
		_reuseDelay = (newReuseDelay + System.currentTimeMillis());
	}
	
	public final long getReuseDelay()
	{
		return _reuseDelay;
	}
	
	public boolean isSkillDisabled(Skill skill)
	{
		TimeStamp sts = _skillReuses.get(skill.hashCode());
		if (sts == null)
			return false;
		if (sts.hasNotPassed())
			return true;
		_skillReuses.remove(skill.hashCode());
		return false;
	}
	
	public final boolean isTeleporting()
	{
		return isTeleporting.get();
	}
	
	/**
	 * Р’РѕР·РІСЂР°С‰Р°РµС‚ РїРѕР·РёС†РёСЋ С†РµР»Рё, РІ РєРѕС‚РѕСЂРѕР№ РѕРЅР° Р±СѓРґРµС‚ С‡РµСЂРµР· РїРѕР» СЃРµРєСѓРЅРґС‹.
	 * @param target
	 * @return
	 */
	public Location getIntersectionPoint(Creature target)
	{
		if (!PositionUtils.isFacing(this, target, 90))
			return new Location(target.getX(), target.getY(), target.getZ());
		double angle = PositionUtils.convertHeadingToDegree(target.getHeading()); // СѓРіРѕР» РІ РіСЂР°РґСѓСЃР°С…
		double radian = Math.toRadians(angle - 90); // СѓРіРѕР» РІ СЂР°РґРёР°РЅР°С…
		double range = target.getMoveSpeed() / 2; // СЂР°СЃСЃС‚РѕСЏРЅРёРµ, РїСЂРѕР№РґРµРЅРЅРѕРµ Р·Р° 1 СЃРµРєСѓРЅРґСѓ, СЂР°РІРЅРѕ СЃРєРѕСЂРѕСЃС‚Рё. Р‘РµСЂРµРј РїРѕР»РѕРІРёРЅСѓ.
		return new Location((int) (target.getX() - range * Math.sin(radian)), (int) (target.getY() + range * Math.cos(radian)), target.getZ());
	}
	
	public Location applyOffset(Location point, int offset)
	{
		if (offset <= 0)
			return point;
		
		long dx = point.x - getX();
		long dy = point.y - getY();
		long dz = point.z - getZ();
		
		double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
		
		if (distance <= offset)
		{
			point.set(getX(), getY(), getZ());
			return point;
		}
		
		if (distance >= 1)
		{
			double cut = offset / distance;
			point.x -= (int) (dx * cut + 0.5);
			point.y -= (int) (dy * cut + 0.5);
			point.z -= (int) (dz * cut + 0.5);
			
			if (!isFlying() && !isInBoat() && !isInWater() && !isBoat())
				point.correctGeoZ();
		}
		
		return point;
	}
	
	public List<Location> applyOffset(List<Location> points, int offset)
	{
		offset = offset >> 4;
		if (offset <= 0)
			return points;
		
		long dx = points.get(points.size() - 1).x - points.get(0).x;
		long dy = points.get(points.size() - 1).y - points.get(0).y;
		long dz = points.get(points.size() - 1).z - points.get(0).z;
		
		double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (distance <= offset)
		{
			Location point = points.get(0);
			points.clear();
			points.add(point);
			return points;
		}
		
		if (distance >= 1)
		{
			double cut = offset / distance;
			int num = (int) (points.size() * cut + 0.5);
			for (int i = 1; i <= num && points.size() > 0; i++)
				points.remove(points.size() - 1);
		}
		
		return points;
	}
	
	private boolean setSimplePath(Location dest)
	{
		List<Location> moveList = GeoMove.constructMoveList(getLoc(), dest);
		if (moveList.isEmpty())
			return false;
		_targetRecorder.clear();
		_targetRecorder.add(moveList);
		return true;
	}
	
	private boolean buildPathTo(int x, int y, int z, int offset, boolean pathFind)
	{
		return buildPathTo(x, y, z, offset, null, false, pathFind);
	}
	
	private boolean buildPathTo(int x, int y, int z, int offset, Creature follow, boolean forestalling, boolean pathFind)
	{
		int geoIndex = getGeoIndex();
		
		Location dest;
		
		if (forestalling && follow != null && follow.isMoving)
			dest = getIntersectionPoint(follow);
		else
			dest = new Location(x, y, z);
		
		if (isInBoat() || isBoat() || !Config.ALLOW_GEODATA)
		{
			applyOffset(dest, offset);
			return setSimplePath(dest);
		}
		
		if (isFlying() || isInWater())
		{
			applyOffset(dest, offset);
			
			Location nextloc;
			
			if (isFlying())
			{
				if (GeoEngine.canSeeCoord(this, dest.x, dest.y, dest.z, true))
					return setSimplePath(dest);
				
				nextloc = GeoEngine.moveCheckInAir(getX(), getY(), getZ(), dest.x, dest.y, dest.z, getColRadius(), geoIndex);
				if (nextloc != null && !nextloc.equals(getX(), getY(), getZ()))
					return setSimplePath(nextloc);
			}
			else
			{
				int waterZ = getWaterZ();
				nextloc = GeoEngine.moveInWaterCheck(getX(), getY(), getZ(), dest.x, dest.y, dest.z, waterZ, geoIndex);
				if (nextloc == null)
					return false;
				
				List<Location> moveList = GeoMove.constructMoveList(getLoc(), nextloc.clone());
				_targetRecorder.clear();
				if (!moveList.isEmpty())
					_targetRecorder.add(moveList);
				
				int dz = dest.z - nextloc.z;
				// РµСЃР»Рё РїС‹С‚Р°РµРјСЃСЏ РІС‹Р±СЂР°С‚СЃСЏ РЅР° Р±РµСЂРµРі, СЃС‡РёС‚Р°РµРј РїСѓС‚СЊ СЃ С‚РѕС‡РєРё РІС‹С…РѕРґР° РґРѕ С‚РѕС‡РєРё РЅР°Р·РЅР°С‡РµРЅРёСЏ
				if (dz > 0 && dz < 128)
				{
					moveList = GeoEngine.MoveList(nextloc.x, nextloc.y, nextloc.z, dest.x, dest.y, geoIndex, false);
					 try
					 {
					 moveList = GeoEngine.MoveList(nextloc.x, nextloc.y, nextloc.z, dest.x, dest.y, geoIndex, false);
					 }
					 catch (Exception e)
					 {
//					 if (_moveExceptionsThrown < 3)
//					 _log.error("Building path has failed for character: " + this + " at location " + getLoc() + ". Character will be teleported automatically nearby.", e);
//					
//					 _moveExceptionsThrown++;
					 teleToLocation(GeoEngine.moveCheck(getX(), getY(), getZ(), dest.x, dest.y, geoIndex));
					 return false;
					 }
					if (moveList != null) // null - РґРѕ РєРѕРЅС†Р° РїСѓС‚Рё РґРѕР№С‚Рё РЅРµР»СЊР·СЏ
					{
						if (!moveList.isEmpty()) // СѓР¶Рµ СЃС‚РѕРёРј РЅР° РЅСѓР¶РЅРѕР№ РєР»РµС‚РєРµ
							_targetRecorder.add(moveList);
					}
				}
				
				return !_targetRecorder.isEmpty();
			}
			return false;
		}
		
		List<Location> moveList = GeoEngine.MoveList(getX(), getY(), getZ(), dest.x, dest.y, geoIndex, true); // onlyFullPath = true - РїСЂРѕРІРµСЂСЏРµРј РІРµСЃСЊ РїСѓС‚СЊ РґРѕ РєРѕРЅС†Р°
		
		if (moveList != null) // null - РґРѕ РєРѕРЅС†Р° РїСѓС‚Рё РґРѕР№С‚Рё РЅРµР»СЊР·СЏ
		{
			if (moveList.isEmpty()) // СѓР¶Рµ СЃС‚РѕРёРј РЅР° РЅСѓР¶РЅРѕР№ РєР»РµС‚РєРµ
				return false;
			applyOffset(moveList, offset);
			if (moveList.isEmpty()) // СѓР¶Рµ СЃС‚РѕРёРј РЅР° РЅСѓР¶РЅРѕР№ РєР»РµС‚РєРµ
				return false;
			_targetRecorder.clear();
			_targetRecorder.add(moveList);
			return true;
		}
		
		if (pathFind)
		{
			List<List<Location>> targets = GeoMove.findMovePath(getX(), getY(), getZ(), dest.clone(), this, true, geoIndex);
			if (!targets.isEmpty())
			{
				moveList = targets.remove(targets.size() - 1);
				applyOffset(moveList, offset);
				if (!moveList.isEmpty())
					targets.add(moveList);
				if (!targets.isEmpty())
				{
					_targetRecorder.clear();
					_targetRecorder.addAll(targets);
					return true;
				}
			}
		}
		
		if (follow != null)
			return false;
		
		applyOffset(dest, offset);
		
		moveList = GeoEngine.MoveList(getX(), getY(), getZ(), dest.x, dest.y, geoIndex, false); // onlyFullPath = false - РёРґРµРј РґРѕ РєСѓРґР° РјРѕР¶РµРј
		if (moveList != null && !moveList.isEmpty()) // null - РЅРµС‚ РіРµРѕРґР°С‚С‹, empty - СѓР¶Рµ СЃС‚РѕРёРј РЅР° РЅСѓР¶РЅРѕР№ РєР»РµС‚РєРµ
		{
			_targetRecorder.clear();
			_targetRecorder.add(moveList);
			return true;
		}
		
		return false;
	}
	
	public Creature getFollowTarget()
	{
		return followTarget.get();
	}
	
	public void setFollowTarget(Creature target)
	{
		followTarget = target == null ? HardReferences.<Creature> emptyRef() : target.getRef();
	}
	
	public boolean followToCharacter(Creature target, int offset, boolean forestalling)
	{
		return followToCharacter(target.getLoc(), target, offset, forestalling);
	}
	
	public boolean followToCharacter(Location loc, Creature target, int offset, boolean forestalling)
	{
		moveLock.lock();
		try
		{
			if (isMovementDisabled() || target == null || isInBoat() || target.isInvisible())
				return false;
			
			offset = Math.max(offset, 10);
			if (isFollow && target == getFollowTarget() && offset == _offset)
				return true;
			
			if (Math.abs(getZ() - target.getZ()) > 1000 && !isFlying())
			{
				sendPacket(SystemMsg.CANNOT_SEE_TARGET);
				return false;
			}
			
			if (getAI() != null)
				getAI().clearNextAction();
			
			stopMove(false, false);
			
			if (buildPathTo(loc.x, loc.y, loc.z, offset, target, forestalling, !target.isDoor()))
				movingDestTempPos.set(loc.x, loc.y, loc.z);
			else
				return false;
			
			isMoving = true;
			isFollow = true;
			_forestalling = forestalling;
			_offset = offset;
			setFollowTarget(target);
			
			moveNext(true);
			
			return true;
		}
		finally
		{
			moveLock.unlock();
		}
	}
	
	public boolean moveToLocation(Location loc, int offset, boolean pathfinding)
	{
		return moveToLocation(loc.x, loc.y, loc.z, offset, pathfinding);
	}
	
	public boolean moveToLocation(int x_dest, int y_dest, int z_dest, int offset, boolean pathfinding)
	{
		moveLock.lock();
		try
		{
			offset = Math.max(offset, 0);
			Location dst_geoloc = new Location(x_dest, y_dest, z_dest).world2geo();
			if (isMoving && !isFollow && movingDestTempPos.equals(dst_geoloc))
			{
				sendActionFailed();
				return true;
			}
			
			if (isMovementDisabled())
			{
				getAI().setNextAction(nextAction.MOVE, new Location(x_dest, y_dest, z_dest), offset, pathfinding, false);
				sendActionFailed();
				return false;
			}
			
			getAI().clearNextAction();
			
			if (isPlayer())
				getAI().changeIntention(AI_INTENTION_ACTIVE, null, null);
			
			stopMove(false, false);
			
			if (buildPathTo(x_dest, y_dest, z_dest, offset, pathfinding))
				movingDestTempPos.set(dst_geoloc);
			else
			{
				sendActionFailed();
				return false;
			}
			
			isMoving = true;
			
			moveNext(true);
			
			return true;
		}
		finally
		{
			moveLock.unlock();
		}
	}
	
	private void moveNext(boolean firstMove)
	{
		if (!isMoving || isMovementDisabled())
		{
			stopMove();
			return;
		}
		
		_previousSpeed = getMoveSpeed();
		if (_previousSpeed <= 0)
		{
			stopMove();
			return;
		}
		
		if (!firstMove)
		{
			Location dest = destination;
			if (dest != null)
				setLoc(dest, true);
		}
		
		if (_targetRecorder.isEmpty())
		{
			CtrlEvent ctrlEvent = isFollow ? CtrlEvent.EVT_ARRIVED_TARGET : CtrlEvent.EVT_ARRIVED;
			stopMove(false, true);
			ThreadPoolManager.getInstance().execute(new NotifyAITask(this, ctrlEvent));
			return;
		}
		
		moveList = _targetRecorder.remove(0);
		Location begin = moveList.get(0).clone().geo2world();
		Location end = moveList.get(moveList.size() - 1).clone().geo2world();
		destination = end;
		double distance = (isFlying() || isInWater()) ? begin.distance3D(end) : begin.distance(end); // РєР»РёРµРЅС‚ РїСЂРё РїРµСЂРµРґРІРёР¶РµРЅРёРё РЅРµ СѓС‡РёС‚С‹РІР°РµС‚ РїРѕРІРµСЂС…РЅРѕСЃС‚СЊ
		
		if (distance != 0)
			setHeading(PositionUtils.calculateHeadingFrom(getX(), getY(), destination.x, destination.y));
		
		if (isPlayer())
			getPlayer().getCounters().distanceWalked += distance;
		
		broadcastMove();
		
		_startMoveTime = _followTimestamp = System.currentTimeMillis();
		if (_moveTaskRunnable == null)
			_moveTaskRunnable = new MoveNextTask();
		_moveTask = ThreadPoolManager.getInstance().schedule(_moveTaskRunnable.setDist(distance), getMoveTickInterval());
	}
	
	public int getMoveTickInterval()
	{
		return (isPlayer() ? 16000 : 32000) / Math.max(getMoveSpeed(), 1);
	}
	
	private void broadcastMove()
	{
		validateLocation(isPlayer() ? 2 : 1);
		broadcastPacket(movePacket());
	}
	
	/**
	 * РћСЃС‚Р°РЅР°РІР»РёРІР°РµС‚ РґРІРёР¶РµРЅРёРµ Рё СЂР°СЃСЃС‹Р»Р°РµС‚ StopMove, ValidateLocation
	 */
	public void stopMove()
	{
		stopMove(true, true);
	}
	
	/**
	 * РћСЃС‚Р°РЅР°РІР»РёРІР°РµС‚ РґРІРёР¶РµРЅРёРµ Рё СЂР°СЃСЃС‹Р»Р°РµС‚ StopMove
	 * @param validate - СЂР°СЃСЃС‹Р»Р°С‚СЊ Р»Рё ValidateLocation
	 */
	public void stopMove(boolean validate)
	{
		stopMove(true, validate);
	}
	
	/**
	 * РћСЃС‚Р°РЅР°РІР»РёРІР°РµС‚ РґРІРёР¶РµРЅРёРµ
	 * @param stop - СЂР°СЃСЃС‹Р»Р°С‚СЊ Р»Рё StopMove
	 * @param validate - СЂР°СЃСЃС‹Р»Р°С‚СЊ Р»Рё ValidateLocation
	 */
	public void stopMove(boolean stop, boolean validate)
	{
		if (!isMoving)
			return;
		
		moveLock.lock();
		try
		{
			if (!isMoving)
				return;
			
			isMoving = false;
			isFollow = false;
			
			if (_moveTask != null)
			{
				_moveTask.cancel(false);
				_moveTask = null;
			}
			
			destination = null;
			moveList = null;
			
			_targetRecorder.clear();
			
			if (validate)
				validateLocation(isPlayer() ? 2 : 1);
			if (stop)
				broadcastPacket(stopMovePacket());
		}
		finally
		{
			moveLock.unlock();
		}
	}
	
	/**
	 * Р’РѕР·РІСЂР°С‰Р°РµС‚ РєРѕРѕСЂРґРёРЅР°С‚С‹ РїРѕРІРµСЂС…РЅРѕСЃС‚Рё РІРѕРґС‹, РµСЃР»Рё РјС‹ РЅР°С…РѕРґРёРјСЃСЏ РІ РЅРµР№, РёР»Рё РЅР°Рґ РЅРµР№.
	 * @return
	 */
	public int getWaterZ()
	{
		if (!isInWater())
			return Integer.MIN_VALUE;
		
		int waterZ = Integer.MIN_VALUE;
		zonesRead.lock();
		try
		{
			Zone zone;
			for (int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if (zone.getType() == ZoneType.water)
					if (waterZ == Integer.MIN_VALUE || waterZ < zone.getTerritory().getZmax())
						waterZ = zone.getTerritory().getZmax();
			}
		}
		finally
		{
			zonesRead.unlock();
		}
		
		return waterZ;
	}
	
	protected L2GameServerPacket stopMovePacket()
	{
		return new StopMove(this);
	}
	
	public L2GameServerPacket movePacket()
	{
		return new CharMoveToLocation(this);
	}
	
	public void updateZones()
	{
		if (isInObserverMode())
			return;
		
		Zone[] zones = isVisible() ? getCurrentRegion().getZones() : Zone.EMPTY_L2ZONE_ARRAY;
		
		ArrayList<Zone> entering = null;
		ArrayList<Zone> leaving = null;
		
		Zone zone;
		
		zonesWrite.lock();
		try
		{
			if (!_zones.isEmpty())
			{
				leaving = new ArrayList<>();
				for (int i = 0; i < _zones.size(); i++)
				{
					zone = _zones.get(i);
					// Р·РѕРЅС‹ Р±РѕР»СЊС€Рµ РЅРµС‚ РІ СЂРµРіРёРѕРЅРµ, Р»РёР±Рѕ РІС‹С€Р»Рё Р·Р° С‚РµСЂСЂРёС‚РѕСЂРёСЋ Р·РѕРЅС‹
					if (!ArrayUtils.contains(zones, zone) || !zone.checkIfInZone(getX(), getY(), getZ(), getReflection()))
						leaving.add(zone);
				}
				
				// РџРѕРєРёРЅСѓР»Рё Р·РѕРЅС‹, СѓР±РёСЂР°РµРј РёР· СЃРїРёСЃРєР° Р·РѕРЅ РїРµСЂСЃРѕРЅР°Р¶Р°
				if (!leaving.isEmpty())
				{
					for (int i = 0; i < leaving.size(); i++)
					{
						zone = leaving.get(i);
						_zones.remove(zone);
					}
				}
			}
			
			if (zones.length > 0)
			{
				entering = new ArrayList<>();
				for (Zone zone2 : zones)
				{
					zone = zone2;
					// РІ Р·РѕРЅСѓ РµС‰Рµ РЅРµ Р·Р°С…РѕРґРёР»Рё Рё Р·Р°С€Р»Рё РЅР° С‚РµСЂСЂРёС‚РѕСЂРёСЋ Р·РѕРЅС‹
					if (!_zones.contains(zone) && zone.checkIfInZone(getX(), getY(), getZ(), getReflection()))
						entering.add(zone);
				}
				
				// Р’РѕС€Р»Рё РІ Р·РѕРЅС‹, РґРѕР±Р°РІРёРј РІ СЃРїРёСЃРѕРє Р·РѕРЅ РїРµСЂСЃРѕРЅР°Р¶Р°
				if (!entering.isEmpty())
				{
					for (int i = 0; i < entering.size(); i++)
					{
						zone = entering.get(i);
						_zones.add(zone);
					}
				}
			}
		}
		finally
		{
			zonesWrite.unlock();
		}
		
		onUpdateZones(leaving, entering);
	}
	
	protected void onUpdateZones(List<Zone> leaving, List<Zone> entering)
	{
		Zone zone;
		
		if (leaving != null && !leaving.isEmpty())
		{
			for (int i = 0; i < leaving.size(); i++)
			{
				zone = leaving.get(i);
				zone.doLeave(this);
			}
		}
		
		if (entering != null && !entering.isEmpty())
		{
			for (int i = 0; i < entering.size(); i++)
			{
				zone = entering.get(i);
				zone.doEnter(this);
			}
		}
	}
	
	public boolean isInZonePeace()
	{
		return isInZone(ZoneType.peace_zone) && !isInZoneBattle();
	}
	
	public boolean isInZoneBattle()
	{
		return isInZone(ZoneType.battle_zone);
	}
	
	/**
	 * In this zone, players are not getting PvP Flags or Karma Neutral or Enemy players are Auto Attackable
	 * @return is Inside Valakas, Antharas or Baium zone
	 */
	public boolean isInZonePvP()
	{
		for (Zone zone : _zones)
			if (zone.getTemplate().isEpicPvP())
				return true;
		return false;
	}
	
	public boolean isInWater()
	{
		return isInZone(ZoneType.water) && !(isInBoat() || isBoat() || isFlying());
	}
	
	public boolean isInZone(ZoneType type)
	{
		zonesRead.lock();
		try
		{
			Zone zone;
			for (int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if (zone.getType() == type)
					return true;
			}
		}
		finally
		{
			zonesRead.unlock();
		}
		
		return false;
	}
	
	public boolean isInZone(String name)
	{
		zonesRead.lock();
		try
		{
			Zone zone;
			for (int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if (zone.getName().equals(name))
					return true;
			}
		}
		finally
		{
			zonesRead.unlock();
		}
		
		return false;
	}
	
	public boolean isInZone(Zone zone)
	{
		zonesRead.lock();
		try
		{
			return _zones.contains(zone);
		}
		finally
		{
			zonesRead.unlock();
		}
	}
	
	public Zone getZone(ZoneType type)
	{
		zonesRead.lock();
		try
		{
			Zone zone;
			for (int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if (zone.getType() == type)
					return zone;
			}
		}
		finally
		{
			zonesRead.unlock();
		}
		return null;
	}
	
	public Location getRestartPoint()
	{
		zonesRead.lock();
		try
		{
			Zone zone;
			for (int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if (zone.getRestartPoints() != null)
				{
					ZoneType type = zone.getType();
					if (type == ZoneType.battle_zone || type == ZoneType.peace_zone || type == ZoneType.offshore || type == ZoneType.dummy)
						return zone.getSpawn();
				}
			}
		}
		finally
		{
			zonesRead.unlock();
		}
		
		return null;
	}
	
	public Location getPKRestartPoint()
	{
		zonesRead.lock();
		try
		{
			Zone zone;
			for (int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if (zone.getRestartPoints() != null)
				{
					ZoneType type = zone.getType();
					if (type == ZoneType.battle_zone || type == ZoneType.peace_zone || type == ZoneType.offshore || type == ZoneType.dummy)
						return zone.getPKSpawn();
				}
			}
		}
		finally
		{
			zonesRead.unlock();
		}
		
		return null;
	}
	
	@Override
	public int getGeoZ(Location loc)
	{
		if (isFlying() || isInWater() || isInBoat() || isBoat() || isDoor())
			return loc.z;
		
		return super.getGeoZ(loc);
	}
	
	protected boolean needStatusUpdate()
	{
		if (!isVisible())
			return false;
		
		boolean result = false;
		
		int bar;
		bar = (int) (getCurrentHp() * CLIENT_BAR_SIZE / getMaxHp());
		if (bar == 0 || bar != _lastHpBarUpdate)
		{
			_lastHpBarUpdate = bar;
			result = true;
		}
		
		bar = (int) (getCurrentMp() * CLIENT_BAR_SIZE / getMaxMp());
		if (bar == 0 || bar != _lastMpBarUpdate)
		{
			_lastMpBarUpdate = bar;
			result = true;
		}
		
		if (isPlayer())
		{
			bar = (int) (getCurrentCp() * CLIENT_BAR_SIZE / getMaxCp());
			if (bar == 0 || bar != _lastCpBarUpdate)
			{
				_lastCpBarUpdate = bar;
				result = true;
			}
		}
		
		return result;
	}
	
	@Override
	public void onForcedAttack(Player player, boolean shift)
	{
		player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
		
		if (!isAttackable(player) || player.isConfused() || player.isBlocked())
		{
			player.sendActionFailed();
			return;
		}
		
		player.getAI().Attack(this, true, shift);
	}
	
	public void onHitTimer(Creature target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld, boolean unchargeSS)
	{
		if (isAlikeDead())
		{
			sendActionFailed();
			return;
		}
		
		if (target.isDead() || !isInRange(target, 2000))
		{
			sendActionFailed();
			return;
		}
		
		if (isPlayable() && target.isPlayable() && isInZoneBattle() != target.isInZoneBattle())
		{
			Player player = getPlayer();
			if (player != null)
			{
				player.sendPacket(SystemMsg.INVALID_TARGET);
				player.sendActionFailed();
			}
			return;
		}
		
		target.getListeners().onAttackHit(this);
		
		// if hitted by a cursed weapon, Cp is reduced to 0, if a cursed weapon is hitted by a Hero, Cp is reduced to 0
		if (!miss && target.isPlayer() && (isCursedWeaponEquipped() || getActiveWeaponInstance() != null && getActiveWeaponInstance().isHeroWeapon() && target.isCursedWeaponEquipped()))
			target.setCurrentCp(0);
		
		if (target.isStunned() && Formulas.calcStunBreak(crit))
			target.getEffectList().stopEffects(EffectType.Stun);
		
		displayGiveDamageMessage(target, damage, crit, miss, shld, false);
		
		ThreadPoolManager.getInstance().execute(new NotifyAITask(target, CtrlEvent.EVT_ATTACKED, this, damage));
		
		boolean checkPvP = checkPvP(target, null);
		// Reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary
		if (!miss && damage > 0)
		{
			target.reduceCurrentHp(damage, this, null, true, true, false, true, false, false, true);
			
			// Skills cast by at physical attack
			if (!target.isDead())
			{
				if (crit)
					useTriggers(target, TriggerType.CRIT, null, null, damage);
				
				useTriggers(target, TriggerType.ATTACK, null, null, damage);
				
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (Formulas.calcCastBreak(target, crit))
					target.abortCast(false, true);
			}
			
			if (soulshot && unchargeSS)
				unChargeShots(false);
		}
		
		if (miss)
			target.useTriggers(this, TriggerType.UNDER_MISSED_ATTACK, null, null, damage);
		
		startAttackStanceTask();
		
		if (checkPvP)
			startPvPFlag(target);
	}
	
	@SuppressWarnings("incomplete-switch")
	public void onMagicUseTimer(Creature aimingTarget, Skill skill, boolean forceUse)
	{
		_castInterruptTime = 0;
		
		if (skill.isUsingWhileCasting())
		{
			aimingTarget.getEffectList().stopEffect(skill.getId());
			onCastEndTime();
			return;
		}
		
		if (!skill.isOffensive() && getAggressionTarget() != null)
			forceUse = true;
		
		if (!skill.checkCondition(this, aimingTarget, forceUse, false, false))
		{
			if (skill.getSkillType() == SkillType.PET_SUMMON && isPlayer())
				getPlayer().setPetControlItem(null);
			onCastEndTime();
			return;
		}
		
		if ((_skillGeoCheckTask != null) && !GeoEngine.canSeeTarget(this, aimingTarget, isFlying()))
		{
			sendPacket(SystemMsg.CANNOT_SEE_TARGET);
			broadcastPacket(new MagicSkillCanceled(getObjectId()));
			onCastEndTime();
			return;
		}
		
		List<Creature> targets = skill.getTargets(this, aimingTarget, forceUse);
		
		int vitalityConsume = skill.getVitConsume();
		if ((vitalityConsume > 0) && (isPlayer()))
		{
			Player p = (Player) this;
			p.setVitality(Math.max(0.0D, p.getVitality() - vitalityConsume));
		}
		
		int hpConsume = skill.getHpConsume();
		if (hpConsume > 0)
			setCurrentHp(Math.max(0, _currentHp - hpConsume), false);
		
		double mpConsume2 = skill.getMpConsume2();
		if (mpConsume2 > 0)
		{
			if (skill.isMusic())
			{
				double inc = mpConsume2 / 2;
				double add = 0;
				for (Effect e : getEffectList().getAllEffects())
					if (e.getSkill().getId() != skill.getId() && e.getSkill().isMusic() && e.getTimeLeft() > 30)
						add += inc;
				mpConsume2 += add;
				mpConsume2 = calcStat(Stats.MP_DANCE_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
			}
			else if (skill.isMagic())
				mpConsume2 = calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
			else
				mpConsume2 = calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
			
			if (_currentMp < mpConsume2 && isPlayable())
			{
				sendPacket(SystemMsg.NOT_ENOUGH_MP);
				onCastEndTime();
				return;
			}
			reduceCurrentMp(mpConsume2, null);
		}
		
		callSkill(skill, targets, true);
		
		if (skill.getNumCharges() > 0)
			setIncreasedForce(getIncreasedForce() - skill.getNumCharges());
		
		if (skill.isSoulBoost())
			setConsumedSouls(getConsumedSouls() - Math.min(getConsumedSouls(), 5), null);
		else if (skill.getSoulsConsume() > 0)
			setConsumedSouls(getConsumedSouls() - skill.getSoulsConsume(), null);
		
		switch (skill.getFlyType())
		{
			case THROW_UP:
			case THROW_HORIZONTAL:
				Location flyLoc;
				for (Creature target : targets)
				{
					// target.setHeading(this, false); //TODO [VISTALL] set heading of target ? Oo
					flyLoc = getFlyLocation(null, skill);
					target.setLoc(flyLoc);
					broadcastPacket(new FlyToLocation(target, flyLoc, skill.getFlyType()));
				}
				break;
		}
		
		if (_scheduledCastCount > 0)
		{
			_scheduledCastCount--;
			_skillLaunchedTask = ThreadPoolManager.getInstance().schedule(new MagicLaunchedTask(this, forceUse), _scheduledCastInterval);
			_skillTask = ThreadPoolManager.getInstance().schedule(new MagicUseTask(this, forceUse), _scheduledCastInterval);
			return;
		}
		
		int skillCoolTime = Formulas.calcMAtkSpd(this, skill, skill.getCoolTime());
		if (skillCoolTime > 0)
			ThreadPoolManager.getInstance().schedule(new CastEndTimeTask(this), skillCoolTime);
		else
			onCastEndTime();
	}
	
	public void onCastEndTime()
	{
		finishFly();
		clearCastVars();
		getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING, null, null);
	}
	
	public void clearCastVars()
	{
		_animationEndTime = 0;
		_castInterruptTime = 0;
		_scheduledCastCount = 0;
		_castingSkill = null;
		_skillTask = null;
		_skillLaunchedTask = null;
		_skillGeoCheckTask = null;
		_flyLoc = null;
	}
	
	private void finishFly()
	{
		Location flyLoc = _flyLoc;
		_flyLoc = null;
		if (flyLoc != null)
		{
			setLoc(flyLoc);
			validateLocation(1);
		}
	}
	
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		if (attacker == null || isDead() || (attacker.isDead() && !isDot))
			return;
		
		if (isDamageBlocked() && transferDamage)
			return;
		
		if (isDamageBlocked() && attacker != this)
		{
			if (sendMessage)
				attacker.sendPacket(SystemMsg.THE_ATTACK_HAS_BEEN_BLOCKED);
			return;
		}
		
		if (isSummon())
		{
            damage *= BalancerConfig.ALT_SUMMONS_DAMAGE;
        }
		if (canReflect)
		{
			if (attacker.absorbAndReflect(this, skill, damage))
				return;
			
			damage = absorbToEffector(attacker, damage);
			damage = absorbToMp(attacker, damage);
			damage = absorbToSummon(attacker, damage);
		}
		
		getListeners().onCurrentHpDamage(damage, attacker, skill);
		
		if (attacker != this)
		{
			if (sendMessage)
				displayReceiveDamageMessage(attacker, (int) damage);
			
			if (!isDot)
				useTriggers(attacker, TriggerType.RECEIVE_DAMAGE, null, null, damage);
		}
		
		onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}
	
	protected void onReduceCurrentHp(final double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp)
	{
		// Alexander - Player is in Fight Club, then set damage done
		if (attacker != null && attacker.isPlayable() && attacker.getPlayer().isInFightClub())
			attacker.getPlayer().getFightClubEvent().onDamage(attacker, this, damage);
		
		if (awake && isSleeping())
			getEffectList().stopEffects(EffectType.Sleep);
		
		if (attacker != this || (skill != null && skill.isOffensive()))
		{
			if (isMeditated())
			{
				Effect effect = getEffectList().getEffectByType(EffectType.Meditation);
				if (effect != null)
					getEffectList().stopEffect(effect.getSkill());
			}
			
			startAttackStanceTask();
			checkAndRemoveInvisible();
			
			if (getCurrentHp() - damage < 0.5)
				useTriggers(attacker, TriggerType.DIE, null, null, damage);
		}
		
		// GM undying mode
		if (isPlayer() && getPlayer().isGM() && getPlayer().isUndying() && damage + 0.5 >= getCurrentHp())
			return;
		
		setCurrentHp(Math.max(getCurrentHp() - damage, 0), false);
		
		if (getCurrentHp() < 0.5)
		{
			if (!isPlayer() || !(getPlayer().isInOlympiadMode() && getPlayer().getOlympiadGame().getType() != CompType.TEAM))
				doDie(attacker);
		}
	}
	
	public void reduceCurrentMp(double i, Creature attacker)
	{
		if (attacker != null && attacker != this)
		{
			if (isSleeping())
				getEffectList().stopEffects(EffectType.Sleep);
			
			if (isMeditated())
			{
				Effect effect = getEffectList().getEffectByType(EffectType.Meditation);
				if (effect != null)
					getEffectList().stopEffect(effect.getSkill());
			}
		}
		
		if (isDamageBlocked() && attacker != null && attacker != this)
		{
			attacker.sendPacket(SystemMsg.THE_ATTACK_HAS_BEEN_BLOCKED);
			return;
		}
		
		// 5182 = Blessing of protection, СЂР°Р±РѕС‚Р°РµС‚ РµСЃР»Рё СЂР°Р·РЅРёС†Р° СѓСЂРѕРІРЅРµР№ Р±РѕР»СЊС€Рµ 10 Рё РЅРµ РІ Р·РѕРЅРµ РѕСЃР°РґС‹
		if (attacker != null && attacker.isPlayer() && Math.abs(attacker.getLevel() - getLevel()) > 10)
		{
			// РџРљ РЅРµ РјРѕР¶РµС‚ РЅР°РЅРµСЃС‚Рё СѓСЂРѕРЅ С‡Р°СЂСѓ СЃ Р±Р»РµСЃСЃРёРЅРіРѕРј
			if (attacker.getKarma() > 0 && getEffectList().getEffectsBySkillId(5182) != null && !isInZone(ZoneType.SIEGE))
				return;
			// С‡Р°СЂ СЃ Р±Р»РµСЃСЃРёРЅРіРѕРј РЅРµ РјРѕР¶РµС‚ РЅР°РЅРµСЃС‚Рё СѓСЂРѕРЅ РџРљ
			if (getKarma() > 0 && attacker.getEffectList().getEffectsBySkillId(5182) != null && !attacker.isInZone(ZoneType.SIEGE))
				return;
		}
		
		i = _currentMp - i;
		
		if (i < 0)
			i = 0;
		
		setCurrentMp(i);
		
		if (attacker != null && attacker != this)
			startAttackStanceTask();
	}
	
	public double relativeSpeed(GameObject target)
	{
		return getMoveSpeed() - target.getMoveSpeed() * Math.cos(headingToRadians(getHeading()) - headingToRadians(target.getHeading()));
	}
	
	public void removeAllSkills()
	{
		for (Skill s : getAllSkillsArray())
			removeSkill(s);
	}
	
	public void removeBlockStats(List<Stats> stats)
	{
		if (_blockedStats != null)
		{
			_blockedStats.removeAll(stats);
			if (_blockedStats.isEmpty())
				_blockedStats = null;
		}
	}
	
	public Skill removeSkill(Skill skill)
	{
		if (skill == null)
			return null;
		return removeSkillById(skill.getId());
	}
	
	public Skill removeSkillById(Integer id)
	{
		// Remove the skill from the L2Character _skills
		Skill oldSkill = _skills.remove(id);
		
		// Remove all its Func objects from the L2Character calculator set
		if (oldSkill != null)
		{
			removeTriggers(oldSkill);
			removeStatsOwner(oldSkill);
			if (Config.ALT_DELETE_SA_BUFFS && (oldSkill.isItemSkill() || oldSkill.isHandler() || oldSkill.getName().startsWith("Item Skill")))
			{
				// Р—Р°РІРµСЂС€Р°РµРј РІСЃРµ СЌС„С„РµРєС‚С‹, РїСЂРёРЅР°РґР»РµР¶Р°С‰РёРµ СЃС‚Р°СЂРѕРјСѓ СЃРєРёР»Р»Сѓ
				List<Effect> effects = getEffectList().getEffectsBySkill(oldSkill);
				if (effects != null)
					for (Effect effect : effects)
						effect.exit();
				// Р� СЃ РїРµС‚РѕРІ С‚РѕР¶Рµ
				Summon pet = getPet();
				if (pet != null)
				{
					effects = pet.getEffectList().getEffectsBySkill(oldSkill);
					if (effects != null)
						for (Effect effect : effects)
							effect.exit();
				}
			}
		}
		
		return oldSkill;
	}
	
	public void addTriggers(StatTemplate f)
	{
		if (f.getTriggerList().isEmpty())
			return;
		
		for (TriggerInfo t : f.getTriggerList())
		{
			addTrigger(t);
		}
	}
	
	public void addTrigger(TriggerInfo t)
	{
		if (_triggers == null)
			_triggers = new ConcurrentHashMap<TriggerType, Set<TriggerInfo>>();
		
		Set<TriggerInfo> hs = _triggers.get(t.getType());
		if (hs == null)
		{
			hs = new CopyOnWriteArraySet<TriggerInfo>();
			_triggers.put(t.getType(), hs);
		}
		
		hs.add(t);
		
		if (t.getType() == TriggerType.ADD)
			useTriggerSkill(this, null, t, null, 0);
	}
	
	public void removeTriggers(StatTemplate f)
	{
		if (_triggers == null || f.getTriggerList().isEmpty())
			return;
		
		for (TriggerInfo t : f.getTriggerList())
			removeTrigger(t);
	}
	
	public void removeTrigger(TriggerInfo t)
	{
		if (_triggers == null)
			return;
		Set<TriggerInfo> hs = _triggers.get(t.getType());
		if (hs == null)
			return;
		hs.remove(t);
	}
	
	public void sendActionFailed()
	{
		sendPacket(ActionFail.STATIC);
	}
	
	public boolean hasAI()
	{
		return _ai != null;
	}
	
	public CharacterAI getAI()
	{
		if (_ai == null)
			synchronized (this)
			{
				if (_ai == null)
					_ai = new CharacterAI(this);
			}
		
		return _ai;
	}
	
	public void setAI(CharacterAI newAI)
	{
		if (newAI == null)
			return;
		
		CharacterAI oldAI = _ai;
		
		synchronized (this)
		{
			_ai = newAI;
		}
		
		if (oldAI != null)
		{
			if (oldAI.isActive())
			{
				oldAI.stopAITask();
				newAI.startAITask();
				newAI.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			}
		}
	}
	
	public final void setCurrentHp(double newHp, boolean canRessurect, boolean sendInfo)
	{
		int maxHp = getMaxHp();
		
		newHp = Math.min(maxHp, Math.max(0, newHp));
		
		if (_currentHp == newHp)
			return;
		
		if (newHp >= 0.5 && isDead() && !canRessurect)
			return;
		
		double hpStart = _currentHp;
		
		_currentHp = newHp;
		
		if (isDead.compareAndSet(true, false))
			onRevive();
		
		checkHpMessages(hpStart, _currentHp);
		
		if (sendInfo)
		{
			broadcastStatusUpdate();
			sendChanges();
		}
		
		if (_currentHp < maxHp)
			startRegeneration();
	}
	
	public final void setCurrentHp(double newHp, boolean canRessurect)
	{
		setCurrentHp(newHp, canRessurect, true);
	}
	
	public final void setCurrentMp(double newMp, boolean sendInfo)
	{
		int maxMp = getMaxMp();
		
		newMp = Math.min(maxMp, Math.max(0, newMp));
		
		if (_currentMp == newMp)
			return;
		
		if (newMp >= 0.5 && isDead())
			return;
		
		_currentMp = newMp;
		
		if (sendInfo)
		{
			broadcastStatusUpdate();
			sendChanges();
		}
		
		if (_currentMp < maxMp)
			startRegeneration();
	}
	
	public final void setCurrentMp(double newMp)
	{
		setCurrentMp(newMp, true);
	}
	
	public final void setCurrentCp(double newCp, boolean sendInfo)
	{
		if (!isPlayer())
			return;
		
		int maxCp = getMaxCp();
		newCp = Math.min(maxCp, Math.max(0, newCp));
		
		if (_currentCp == newCp)
			return;
		
		if (newCp >= 0.5 && isDead())
			return;
		
		_currentCp = newCp;
		
		if (sendInfo)
		{
			broadcastStatusUpdate();
			sendChanges();
		}
		
		if (_currentCp < maxCp)
			startRegeneration();
	}
	
	public final void setCurrentCp(double newCp)
	{
		setCurrentCp(newCp, true);
	}
	
	public void setCurrentHpMp(double newHp, double newMp, boolean canRessurect)
	{
		int maxHp = getMaxHp();
		int maxMp = getMaxMp();
		
		newHp = Math.min(maxHp, Math.max(0, newHp));
		newMp = Math.min(maxMp, Math.max(0, newMp));
		
		if (_currentHp == newHp && _currentMp == newMp)
			return;
		
		if (newHp >= 0.5 && isDead() && !canRessurect)
			return;
		
		double hpStart = _currentHp;
		
		_currentHp = newHp;
		_currentMp = newMp;
		
		if (isDead.compareAndSet(true, false))
			onRevive();
		
		checkHpMessages(hpStart, _currentHp);
		
		broadcastStatusUpdate();
		sendChanges();
		
		if (_currentHp < maxHp || _currentMp < maxMp)
			startRegeneration();
	}
	
	public void setCurrentHpMp(double newHp, double newMp)
	{
		setCurrentHpMp(newHp, newMp, false);
	}
	
	public final void setFlying(boolean mode)
	{
		_flying = mode;
	}
	
	@Override
	public final int getHeading()
	{
		return _heading;
	}
	
	public void setHeading(int heading)
	{
		_heading = heading;
	}
	
	public final void setIsTeleporting(boolean value)
	{
		isTeleporting.compareAndSet(!value, value);
	}
	
	public final void setName(String name)
	{
		_name = name;
	}
	
	public Creature getCastingTarget()
	{
		return castingTarget.get();
	}
	
	public void setCastingTarget(Creature target)
	{
		if (target == null)
			castingTarget = HardReferences.emptyRef();
		else
			castingTarget = target.getRef();
	}
	
	public final void setRunning()
	{
		if (!_running)
		{
			_running = true;
			broadcastPacket(new ChangeMoveType(this));
		}
	}
	
	public void setSkillMastery(Integer skill, int mastery)
	{
		if (_skillMastery == null)
			_skillMastery = new HashMap<Integer, Integer>();
		_skillMastery.put(skill, mastery);
	}
	
	public void setAggressionTarget(Creature target)
	{
		if (target == null)
			_aggressionTarget = HardReferences.emptyRef();
		else
			_aggressionTarget = target.getRef();
	}
	
	public Creature getAggressionTarget()
	{
		return _aggressionTarget.get();
	}
	
	public void setTarget(GameObject object)
	{
		if (object != null && !object.isVisible())
			object = null;
		
		/*
		 * DS: РЅР° РѕС„С„Рµ СЃР±СЂРѕСЃ С‚РµРєСѓС‰РµР№ С†РµР»Рё РЅРµ РѕС‚РјРµРЅСЏРµС‚ Р°С‚Р°РєСѓ РёР»Рё РєР°СЃС‚. if (object == null) { if (isAttackingNow() && getAI().getAttackTarget() == getTarget()) abortAttack(false, true); if (isCastingNow() && getAI().getAttackTarget() == getTarget()) abortCast(false, true); }
		 */
		
		if (object == null)
			target = HardReferences.emptyRef();
		else
			target = object.getRef();
	}
	
	public void setTitle(String title)
	{
		_title = title;
	}
	
	public void setWalking()
	{
		if (_running)
		{
			_running = false;
			broadcastPacket(new ChangeMoveType(this));
		}
	}
	
	public void startAbnormalEffect(AbnormalEffect ae)
	{
		if (ae == AbnormalEffect.NULL)
		{
			_abnormalEffects = AbnormalEffect.NULL.getMask();
			_abnormalEffects2 = AbnormalEffect.NULL.getMask();
			_abnormalEffects3 = AbnormalEffect.NULL.getMask();
		}
		else if (ae.isSpecial())
			_abnormalEffects2 |= ae.getMask();
		else if (ae.isEvent())
			_abnormalEffects3 |= ae.getMask();
		else
			_abnormalEffects |= ae.getMask();
		sendChanges();
	}
	
	public void startAttackStanceTask()
	{
		startAttackStanceTask0();
	}
	
	/**
	 * Р—Р°РїСѓСЃРєР°РµРј Р·Р°РґР°С‡Сѓ Р°РЅРёРјР°С†РёРё Р±РѕРµРІРѕР№ РїРѕР·С‹. Р•СЃР»Рё Р·Р°РґР°С‡Р° СѓР¶Рµ Р·Р°РїСѓС‰РµРЅР°, СѓРІРµР»РёС‡РёРІР°РµРј РІСЂРµРјСЏ, РєРѕС‚РѕСЂРѕРµ РїРµСЂСЃРѕРЅР°Р¶ Р±СѓРґРµС‚ РІ Р±РѕРµРІРѕР№ РїРѕР·Рµ РЅР° 15СЃ
	 */
	protected void startAttackStanceTask0()
	{
		// РїСЂРµРґС‹РґСѓС‰Р°СЏ Р·Р°РґР°С‡Р° РµС‰Рµ РЅРµ Р·Р°РєРѕРЅС‡РµРЅР°, СѓРІРµР»РёС‡РёРІР°РµРј РІСЂРµРјСЏ
		if (isInCombat())
		{
			_stanceEndTime = System.currentTimeMillis() + 15000L;
			return;
		}
		
		_stanceEndTime = System.currentTimeMillis() + 15000L;
		
		broadcastPacket(new AutoAttackStart(getObjectId()));
		
		// РѕС‚РјРµРЅСЏРµРј РїСЂРµРґС‹РґСѓС‰СѓСЋ
		final Future<?> task = _stanceTask;
		if (task != null)
			task.cancel(false);
		
		// Р”РѕР±Р°РІР»СЏРµРј Р·Р°РґР°С‡Сѓ, РєРѕС‚РѕСЂР°СЏ Р±СѓРґРµС‚ РїСЂРѕРІРµСЂСЏС‚СЊ, РµСЃР»Рё РёСЃС‚РµРєР»Рѕ РІСЂРµРјСЏ РЅР°С…РѕР¶РґРµРЅРёСЏ РїРµСЂСЃРѕРЅР°Р¶Р° РІ Р±РѕРµРІРѕР№ РїРѕР·Рµ,
		// РѕС‚РјРµРЅСЏРµС‚ Р·Р°РґР°С‡Сѓ Рё РѕСЃС‚Р°РЅР°Р»РёРІР°РµС‚ Р°РЅРёРјР°С†РёСЋ.
		_stanceTask = LazyPrecisionTaskManager.getInstance().scheduleAtFixedRate(_stanceTaskRunnable == null ? _stanceTaskRunnable = new AttackStanceTask() : _stanceTaskRunnable, 1000L, 1000L);
	}
	
	/**
	 * РћСЃС‚Р°РЅР°РІР»РёРІР°РµРј Р·Р°РґР°С‡Сѓ Р°РЅРёРјР°С†РёРё Р±РѕРµРІРѕР№ РїРѕР·С‹.
	 */
	public void stopAttackStanceTask()
	{
		_stanceEndTime = 0L;
		
		final Future<?> task = _stanceTask;
		if (task != null)
		{
			task.cancel(false);
			_stanceTask = null;
			
			broadcastPacket(new AutoAttackStop(getObjectId()));
		}
	}
	
	private class AttackStanceTask extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			if (!isInCombat())
				stopAttackStanceTask();
		}
	}
	
	/**
	 * РћСЃС‚Р°РЅРѕРІРёС‚СЊ СЂРµРіРµРЅРµСЂР°С†РёСЋ
	 */
	protected void stopRegeneration()
	{
		regenLock.lock();
		try
		{
			if (_isRegenerating)
			{
				_isRegenerating = false;
				
				if (_regenTask != null)
				{
					_regenTask.cancel(false);
					_regenTask = null;
				}
			}
		}
		finally
		{
			regenLock.unlock();
		}
	}
	
	/**
	 * Р—Р°РїСѓСЃС‚РёС‚СЊ СЂРµРіРµРЅРµСЂР°С†РёСЋ
	 */
	protected void startRegeneration()
	{
		if (!isVisible() || isDead() || getRegenTick() == 0L)
			return;
		
		if (_isRegenerating)
			return;
		
		regenLock.lock();
		try
		{
			if (!_isRegenerating)
			{
				_isRegenerating = true;
				_regenTask = RegenTaskManager.getInstance().scheduleAtFixedRate(_regenTaskRunnable == null ? _regenTaskRunnable = new RegenTask() : _regenTaskRunnable, 0, getRegenTick());
			}
		}
		finally
		{
			regenLock.unlock();
		}
	}
	
	public long getRegenTick()
	{
		return 3000L;
	}
	
	private class RegenTask implements Runnable
	{
		@Override
		public void run()
		{
			if (isAlikeDead() || getRegenTick() == 0L)
				return;
			
			double hpStart = _currentHp;
			
			int maxHp = getMaxHp();
			int maxMp = getMaxMp();
			int maxCp = isPlayer() ? getMaxCp() : 0;
			
			double addHp = 0.;
			double addMp = 0.;
			
			regenLock.lock();
			try
			{
				if (_currentHp < maxHp)
					addHp += Formulas.calcHpRegen(Creature.this);
				
				if (_currentMp < maxMp)
					addMp += Formulas.calcMpRegen(Creature.this);
				
				// Added regen bonus when character is sitting
				if (isPlayer() && Config.REGEN_SIT_WAIT)
				{
					Player pl = (Player) Creature.this;
					if (pl.isSitting())
					{
						pl.updateWaitSitTime();
						if (pl.getWaitSitTime() > 5)
						{
							addHp += pl.getWaitSitTime();
							addMp += pl.getWaitSitTime();
						}
					}
				}
				else if (isRaid())
				{
					addHp *= Config.RATE_RAID_REGEN;
					addMp *= Config.RATE_RAID_REGEN;
				}
				
				_currentHp += Math.max(0, Math.min(addHp, calcStat(Stats.HP_LIMIT, null, null) * maxHp / 100. - _currentHp));
				_currentMp += Math.max(0, Math.min(addMp, calcStat(Stats.MP_LIMIT, null, null) * maxMp / 100. - _currentMp));
				
				_currentHp = Math.min(maxHp, _currentHp);
				_currentMp = Math.min(maxMp, _currentMp);
				
				if (isPlayer())
				{
					_currentCp += Math.max(0, Math.min(Formulas.calcCpRegen(Creature.this), calcStat(Stats.CP_LIMIT, null, null) * maxCp / 100. - _currentCp));
					_currentCp = Math.min(maxCp, _currentCp);
				}
				
				// РѕС‚СЂРµРіРµРЅРёР»РёСЃСЊ, РѕСЃС‚Р°РЅР°РІР»РёРІР°РµРј Р·Р°РґР°С‡Сѓ
				if (_currentHp == maxHp && _currentMp == maxMp && _currentCp == maxCp)
					stopRegeneration();
			}
			finally
			{
				regenLock.unlock();
			}
			
			broadcastStatusUpdate();
			sendChanges();
			
			checkHpMessages(hpStart, _currentHp);
		}
	}
	
	public void stopAbnormalEffect(AbnormalEffect ae)
	{
		if (ae.isSpecial())
			_abnormalEffects2 &= ~ae.getMask();
		if (ae.isEvent())
			_abnormalEffects3 &= ~ae.getMask();
		else
			_abnormalEffects &= ~ae.getMask();
		sendChanges();
	}
	
	/**
	 * Р‘Р»РѕРєРёСЂСѓРµРј РїРµСЂСЃРѕРЅР°Р¶Р°
	 */
	public void block()
	{
		_blocked = true;
	}
	
	/**
	 * Р Р°Р·Р±Р»РѕРєРёСЂСѓРµРј РїРµСЂСЃРѕРЅР°Р¶Р°
	 */
	public void unblock()
	{
		_blocked = false;
	}
	
	/**
	 * @return РїСЂРµРґС‹РґСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean startConfused()
	{
		return _confused.getAndSet(true);
	}
	
	/**
	 * @return С‚РµРєСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean stopConfused()
	{
		return _confused.setAndGet(false);
	}
	
	/**
	 * @return РїСЂРµРґС‹РґСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean startFear()
	{
		return _afraid.getAndSet(true);
	}
	
	/**
	 * @return С‚РµРєСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean stopFear()
	{
		return _afraid.setAndGet(false);
	}
	
	/**
	 * @return РїСЂРµРґС‹РґСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean startMuted()
	{
		return _muted.getAndSet(true);
	}
	
	/**
	 * @return С‚РµРєСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean stopMuted()
	{
		return _muted.setAndGet(false);
	}
	
	/**
	 * @return РїСЂРµРґС‹РґСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean startPMuted()
	{
		return _pmuted.getAndSet(true);
	}
	
	/**
	 * @return С‚РµРєСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean stopPMuted()
	{
		return _pmuted.setAndGet(false);
	}
	
	/**
	 * @return РїСЂРµРґС‹РґСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean startAMuted()
	{
		return _amuted.getAndSet(true);
	}
	
	/**
	 * @return С‚РµРєСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean stopAMuted()
	{
		return _amuted.setAndGet(false);
	}
	
	/**
	 * @return РїСЂРµРґС‹РґСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean startRooted()
	{
		return _rooted.getAndSet(true);
	}
	
	/**
	 * @return С‚РµРєСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean stopRooted()
	{
		return _rooted.setAndGet(false);
	}
	
	/**
	 * @return РїСЂРµРґС‹РґСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean startSleeping()
	{
		return _sleeping.getAndSet(true);
	}
	
	/**
	 * @return С‚РµРєСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean stopSleeping()
	{
		return _sleeping.setAndGet(false);
	}
	
	/**
	 * @return РїСЂРµРґС‹РґСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean startStunning()
	{
		return _stunned.getAndSet(true);
	}
	
	/**
	 * @return С‚РµРєСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean stopStunning()
	{
		return _stunned.setAndGet(false);
	}
	
	/**
	 * @return РїСЂРµРґС‹РґСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean startParalyzed()
	{
		return _paralyzed.getAndSet(true);
	}
	
	/**
	 * @return С‚РµРєСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean stopParalyzed()
	{
		return _paralyzed.setAndGet(false);
	}
	
	/**
	 * @return РїСЂРµРґС‹РґСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean startImmobilized()
	{
		return _immobilized.getAndSet(true);
	}
	
	/**
	 * @return С‚РµРєСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean stopImmobilized()
	{
		return _immobilized.setAndGet(false);
	}
	
	/**
	 * @return РїСЂРµРґС‹РґСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean startHealBlocked()
	{
		return _healBlocked.getAndSet(true);
	}
	
	/**
	 * @return С‚РµРєСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean stopHealBlocked()
	{
		return _healBlocked.setAndGet(false);
	}
	
	/**
	 * @return РїСЂРµРґС‹РґСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean startDamageBlocked()
	{
		return _damageBlocked.getAndSet(true);
	}
	
	/**
	 * @return С‚РµРєСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean stopDamageBlocked()
	{
		return _damageBlocked.setAndGet(false);
	}
	
	/**
	 * @return РїСЂРµРґС‹РґСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean startBuffImmunity()
	{
		return _buffImmunity.getAndSet(true);
	}
	
	/**
	 * @return С‚РµРєСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean stopBuffImmunity()
	{
		return _buffImmunity.setAndGet(false);
	}
	
	/**
	 * @return РїСЂРµРґС‹РґСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean startDebuffImmunity()
	{
		return _debuffImmunity.getAndSet(true);
	}
	
	/**
	 * @return С‚РµРєСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean stopDebuffImmunity()
	{
		return _debuffImmunity.setAndGet(false);
	}
	
	/**
	 * @return РїСЂРµРґС‹РґСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean startEffectImmunity()
	{
		return _effectImmunity.getAndSet(true);
	}
	
	/**
	 * @return С‚РµРєСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean stopEffectImmunity()
	{
		return _effectImmunity.setAndGet(false);
	}
	
	/**
	 * @return С‚РµРєСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean startWeaponEquipBlocked()
	{
		return _weaponEquipBlocked.getAndSet(true);
	}
	
	/**
	 * @return С‚РµРєСѓС‰РµРµ СЃРѕСЃС‚РѕСЏРЅРёРµ
	 */
	public boolean stopWeaponEquipBlocked()
	{
		return _weaponEquipBlocked.getAndSet(false);
	}
	
	public boolean startFrozen()
	{
		return _frozen.getAndSet(true);
	}
	
	public boolean stopFrozen()
	{
		return _frozen.setAndGet(false);
	}
	
	public void setFakeDeath(boolean value)
	{
		_fakeDeath = value;
	}
	
	public void breakFakeDeath()
	{
		getEffectList().stopAllSkillEffects(EffectType.FakeDeath);
	}
	
	public void setMeditated(boolean value)
	{
		_meditated = value;
	}
	
	public final void setIsBlessedByNoblesse(boolean value)
	{
		_isBlessedByNoblesse = value;
	}
	
	public final void setIsSalvation(boolean value)
	{
		_isSalvation = value;
	}
	
	public void setIsInvul(boolean value)
	{
		_isInvul = value;
	}
	
	public void setLockedTarget(boolean value)
	{
		_lockedTarget = value;
	}
	
	public boolean isConfused()
	{
		return _confused.get();
	}
	
	public boolean isFakeDeath()
	{
		return _fakeDeath;
	}
	
	public boolean isAfraid()
	{
		return _afraid.get();
	}
	
	public boolean isBlocked()
	{
		return _blocked;
	}
	
	public boolean isMuted(Skill skill)
	{
		if (skill == null || skill.isNotAffectedByMute())
			return false;
		return isMMuted() && skill.isMagic() || isPMuted() && !skill.isMagic();
	}
	
	public boolean isPMuted()
	{
		return _pmuted.get();
	}
	
	public boolean isMMuted()
	{
		return _muted.get();
	}
	
	public boolean isAMuted()
	{
		return _amuted.get();
	}
	
	public boolean isRooted()
	{
		return _rooted.get();
	}
	
	public boolean isSleeping()
	{
		return _sleeping.get();
	}
	
	public boolean isStunned()
	{
		return _stunned.get();
	}
	
	public boolean isMeditated()
	{
		return _meditated;
	}
	
	public boolean isWeaponEquipBlocked()
	{
		return _weaponEquipBlocked.get();
	}
	
	public boolean isParalyzed()
	{
		return _paralyzed.get();
	}
	
	public boolean isFrozen()
	{
		return _frozen.get();
	}
	
	public boolean isImmobilized()
	{
		return _immobilized.get() || getRunSpeed() < 1;
	}
	
	public boolean isHealBlocked()
	{
		return isAlikeDead() || _healBlocked.get();
	}
	
	public boolean isDamageBlocked()
	{
		return isInvul() || _damageBlocked.get();
	}
	
	public boolean isCastingNow()
	{
		return _skillTask != null;
	}
	
	public boolean isLockedTarget()
	{
		return _lockedTarget;
	}
	
	public boolean isMovementDisabled()
	{
		return isBlocked() || isRooted() || isImmobilized() || isAlikeDead() || isStunned() || isSleeping() || isParalyzed() || isAttackingNow() || isCastingNow() || isFrozen();
	}
	
	public boolean isFlyingDisabled()
	{
		return isBlocked() || isRooted() || isImmobilized() || isAlikeDead() || isStunned() || isSleeping() || isParalyzed() || isFrozen();
	}
	
	public boolean isActionsDisabled()
	{
		return isBlocked() || isAlikeDead() || isStunned() || isSleeping() || isParalyzed() || isAttackingNow() || isCastingNow() || isFrozen();
	}
	
	public final boolean isAttackingDisabled()
	{
		return _attackReuseEndTime > System.currentTimeMillis();
	}
	
	public boolean isOutOfControl()
	{
		return isBlocked() || isConfused() || isAfraid();
	}
	
	public void teleToLocation(Location loc)
	{
		teleToLocation(loc.x, loc.y, loc.z, getReflection());
	}
	
	public void teleToLocation(Location loc, int refId)
	{
		teleToLocation(loc.x, loc.y, loc.z, refId);
	}
	
	public void teleToLocation(Location loc, Reflection r)
	{
		teleToLocation(loc.x, loc.y, loc.z, r);
	}
	
	public void teleToLocation(int x, int y, int z)
	{
		teleToLocation(x, y, z, getReflection());
	}
	
	public void checkAndRemoveInvisible()
	{
		InvisibleType invisibleType = getInvisibleType();
		if (invisibleType == InvisibleType.EFFECT)
			getEffectList().stopEffects(EffectType.Invisible);
	}
	
	public void teleToLocation(int x, int y, int z, int refId)
	{
		Reflection r = ReflectionManager.getInstance().get(refId);
		if (r == null)
			return;
		teleToLocation(x, y, z, r);
	}
	
	public void teleToLocation(int x, int y, int z, Reflection r)
	{
		if (!isTeleporting.compareAndSet(false, true))
			return;
		
		if (isFakeDeath())
			breakFakeDeath();
		
		abortCast(true, false);
		if (!isLockedTarget())
			setTarget(null);
		stopMove();
		
		if (!isBoat() && !isFlying() && !World.isWater(new Location(x, y, z), r))
			z = GeoEngine.getHeight(x, y, z, r.getGeoIndex());
		
		// TODO [G1ta0] СѓР±СЂР°С‚СЊ DimensionalRiftManager.teleToLocation
		if (isPlayer() && DimensionalRiftManager.getInstance().checkIfInRiftZone(getLoc(), true))
		{
			Player player = (Player) this;
			if (player.isInParty() && player.getParty().isInDimensionalRift())
			{
				Location newCoords = DimensionalRiftManager.getInstance().getRoom(0, 0).getTeleportCoords();
				x = newCoords.x;
				y = newCoords.y;
				z = newCoords.z;
				player.getParty().getDimensionalRift().usedTeleport(player);
			}
		}
		
		if (isPlayer())
		{
			Player player = (Player) this;
			
			player.getListeners().onTeleport(x, y, z, r);
			
			// Alexander - Send a teleport event to the tutorial for the player
			Quest q = QuestManager.getQuest(255);
			if (q != null)
			{
				player.processQuestEvent(q.getName(), "CE42", null);
			}
			
			decayMe();
			
			setXYZ(x, y, z);
			
			setReflection(r);
			
			// It is necessary at the teleport from a higher point to a lower, otherwise harmed by the "fall"
			player.setLastClientPosition(null);
			player.setLastServerPosition(null);
			
			player.sendPacket(new TeleportToLocation(player, x, y, z));
			
			if (player.getPet() != null)
			{
				player.getPet().teleportToOwner();
			}
		}
		else
		{
			setXYZ(x, y, z);
			
			setReflection(r);
			
			broadcastPacket(new TeleportToLocation(this, x, y, z));
			onTeleported();
		}
	}
	
	public boolean onTeleported()
	{
		return isTeleporting.compareAndSet(true, false);
	}
	
	public void sendMessage(CustomMessage message)
	{
		
	}
	
	public void sendChatMessage(int objectId, int messageType, String charName, String text)
	{
		
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + getObjectId() + "]";
	}
	
	@Override
	public double getColRadius()
	{
		return getTemplate().collisionRadius;
	}
	
	@Override
	public double getColHeight()
	{
		return getTemplate().collisionHeight;
	}
	
	public EffectList getEffectList()
	{
		if (_effectList == null)
			synchronized (this)
			{
				if (_effectList == null)
					_effectList = new EffectList(this);
			}
		
		return _effectList;
	}
	
	public boolean paralizeOnAttack(Creature attacker)
	{
		int max_attacker_level = 0xFFFF;
		
		MonsterInstance leader;
		if (isRaid() || isMinion() && (leader = ((MinionInstance) this).getLeader()) != null && leader.isRaid())
			max_attacker_level = getLevel() + Config.RAID_MAX_LEVEL_DIFF;
		else if (isNpc())
		{
			int max_level_diff = ((NpcInstance) this).getParameter("ParalizeOnAttack", -1000);
			if (max_level_diff != -1000)
				max_attacker_level = getLevel() + max_level_diff;
		}
		
		if (attacker.getLevel() > max_attacker_level)
			return true;
		
		return false;
	}
	
	@Override
	protected void onDelete()
	{
		GameObjectsStorage.remove(_storedId);
		
		getEffectList().stopAllEffects();
		
		super.onDelete();
	}
	
	// ---------------------------- Not Implemented -------------------------------
	
	public void addExpAndSp(long exp, long sp)
	{
	}
	
	public void broadcastCharInfo()
	{
	}
	
	public void checkHpMessages(double currentHp, double newHp)
	{
	}
	
	public boolean checkPvP(Creature target, Skill skill)
	{
		return false;
	}
	
	public boolean consumeItem(int itemConsumeId, long itemCount)
	{
		return true;
	}
	
	public boolean consumeItemMp(int itemId, int mp)
	{
		return true;
	}
	
	public boolean isFearImmune()
	{
		return false;
	}
	
	public boolean isLethalImmune()
	{
		return false;
	}
	
	public boolean getChargedSoulShot()
	{
		return false;
	}
	
	public int getChargedSpiritShot()
	{
		return 0;
	}
	
	public int getIncreasedForce()
	{
		return 0;
	}
	
	public int getConsumedSouls()
	{
		return 0;
	}
	
	public int getAgathionEnergy()
	{
		return 0;
	}
	
	public void setAgathionEnergy(int val)
	{
		//
	}
	
	public int getKarma()
	{
		return 0;
	}
	
	public double getLevelMod()
	{
		return 1;
	}
	
	public int getNpcId()
	{
		return 0;
	}
	
	public Summon getPet()
	{
		return null;
	}
	
	public int getPvpFlag()
	{
		return 0;
	}
	
	public void setTeam(TeamType t)
	{
		_team = t;
		sendChanges();
	}
	
	public TeamType getTeam()
	{
		return _team;
	}
	
	public boolean isUndead()
	{
		return false;
	}
	
	public boolean isParalyzeImmune()
	{
		return false;
	}
	
	public void reduceArrowCount()
	{
	}
	
	public void sendChanges()
	{
		getStatsRecorder().sendChanges();
	}
	
	public void sendMessage(String message)
	{
	}
	
	public void sendPacket(IStaticPacket mov)
	{
	}
	
	public void sendPacket(IStaticPacket... mov)
	{
	}
	
	public void sendPacket(List<? extends IStaticPacket> mov)
	{
	}
	
	public void setIncreasedForce(int i)
	{
	}
	
	public void setConsumedSouls(int i, NpcInstance monster)
	{
	}
	
	public void startPvPFlag(Creature target)
	{
	}
	
	public boolean unChargeShots(boolean spirit)
	{
		return false;
	}
	
	public void updateEffectIcons()
	{
	}
	
	/**
	 * Р’С‹СЃС‚Р°РІРёС‚СЊ РїСЂРµРґРµР»СЊРЅС‹Рµ Р·РЅР°С‡РµРЅРёСЏ HP/MP/CP Рё Р·Р°РїСѓСЃС‚РёС‚СЊ СЂРµРіРµРЅРµСЂР°С†РёСЋ, РµСЃР»Рё РІ СЌС‚РѕРј РµСЃС‚СЊ РЅРµРѕР±С…РѕРґРёРјРѕСЃС‚СЊ
	 */
	protected void refreshHpMpCp()
	{
		final int maxHp = getMaxHp();
		final int maxMp = getMaxMp();
		final int maxCp = isPlayer() ? getMaxCp() : 0;
		
		if (_currentHp > maxHp)
			setCurrentHp(maxHp, false);
		if (_currentMp > maxMp)
			setCurrentMp(maxMp, false);
		if (_currentCp > maxCp)
			setCurrentCp(maxCp, false);
		
		if (_currentHp < maxHp || _currentMp < maxMp || _currentCp < maxCp)
			startRegeneration();
	}
	
	public void updateStats()
	{
		refreshHpMpCp();
		sendChanges();
	}
	
	public void setOverhitAttacker(Creature attacker)
	{
	}
	
	public void setOverhitDamage(double damage)
	{
	}
	
	public boolean isCursedWeaponEquipped()
	{
		return false;
	}
	
	public boolean isHero()
	{
		return false;
	}
	
	public int getAccessLevel()
	{
		return 0;
	}
	
	public Clan getClan()
	{
		return null;
	}
	
	public double getRateAdena()
	{
		return 1.;
	}
	
	public double getRateItems()
	{
		return 1.;
	}
	
	public double getRateExp()
	{
		return 1.;
	}
	
	public double getRateSp()
	{
		return 1.;
	}
	
	public double getRateSpoil()
	{
		return 1.;
	}
	
	public int getFormId()
	{
		return 0;
	}
	
	public boolean isNameAbove()
	{
		return true;
	}
	
	@Override
	public void setLoc(Location loc)
	{
		setXYZ(loc.x, loc.y, loc.z);
	}
	
	public void setLoc(Location loc, boolean MoveTask)
	{
		setXYZ(loc.x, loc.y, loc.z, MoveTask);
	}
	
	@Override
	public void setXYZ(int x, int y, int z)
	{
		setXYZ(x, y, z, false);
	}
	
	public void setXYZ(int x, int y, int z, boolean MoveTask)
	{
		if (!MoveTask)
			stopMove();
		
		moveLock.lock();
		try
		{
			super.setXYZ(x, y, z);
		}
		finally
		{
			moveLock.unlock();
		}
		
		updateZones();
	}
	
	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		
		updateStats();
		updateZones();
	}
	
	@Override
	public void spawnMe(Location loc)
	{
		if (loc.h > 0)
			setHeading(loc.h);
		super.spawnMe(loc);
	}
	
	@Override
	protected void onDespawn()
	{
		if (!isLockedTarget())
			setTarget(null);
		stopMove();
		stopAttackStanceTask();
		stopRegeneration();
		
		updateZones();
		clearStatusListeners();
		
		super.onDespawn();
	}
	
	public final void doDecay()
	{
		if (!isDead())
			return;
		
		onDecay();
	}
	
	protected void onDecay()
	{
		decayMe();
	}
	
	public void validateLocation(int broadcast)
	{
		L2GameServerPacket sp = new ValidateLocation(this);
		if (broadcast == 0)
			sendPacket(sp);
		else if (broadcast == 1)
			broadcastPacket(sp);
		else
			broadcastPacketToOthers(sp);
	}
	
	// Р¤СѓРЅРєС†РёСЏ РґР»СЏ РґРёР·Р°РєС‚РёРІР°С†РёРё СѓРјРµРЅРёР№ РїРµСЂСЃРѕРЅР°Р¶Р° (РµСЃР»Рё СѓРјРµРЅРёРµ РЅРµ Р°РєС‚РёРІРЅРѕ, С‚Рѕ РѕРЅ РЅРµ РґР°РµС‚ СЃС‚Р°С‚С‚РѕРІ Рё РёРјРµРµС‚ СЃРµСЂСѓСЋ РёРєРѕРЅРєСѓ).
	private final TIntHashSet _unActiveSkills = new TIntHashSet();
	
	public void addUnActiveSkill(Skill skill)
	{
		if (skill == null || isUnActiveSkill(skill.getId()))
			return;
		
		removeStatsOwner(skill);
		removeTriggers(skill);
		
		_unActiveSkills.add(skill.getId());
	}
	
	public void removeUnActiveSkill(Skill skill)
	{
		if (skill == null || !isUnActiveSkill(skill.getId()))
			return;
		
		addStatFuncs(skill.getStatFuncs());
		addTriggers(skill);
		
		_unActiveSkills.remove(skill.getId());
	}
	
	public boolean isUnActiveSkill(int id)
	{
		return _unActiveSkills.contains(id);
	}
	
	public void removeInvisibleEffect()
	{
		if (getEffectList() == null)
			return;
		
		Effect effect = getEffectList().getEffectByType(EffectType.Invisible);
		if (effect != null)
			effect.exit();
	}
	
	public abstract int getLevel();
	
	public abstract ItemInstance getActiveWeaponInstance();
	
	public abstract WeaponTemplate getActiveWeaponItem();
	
	public abstract ItemInstance getSecondaryWeaponInstance();
	
	public abstract WeaponTemplate getSecondaryWeaponItem();
	
	public CharListenerList getListeners()
	{
		if (listeners == null)
			synchronized (this)
			{
				if (listeners == null)
					listeners = new CharListenerList(this);
			}
		return listeners;
	}
	
	public <T extends Listener<Creature>> boolean addListener(T listener)
	{
		return getListeners().add(listener);
	}
	
	public <T extends Listener<Creature>> boolean removeListener(T listener)
	{
		return getListeners().remove(listener);
	}
	
	public CharStatsChangeRecorder<? extends Creature> getStatsRecorder()
	{
		if (_statsRecorder == null)
			synchronized (this)
			{
				if (_statsRecorder == null)
					_statsRecorder = new CharStatsChangeRecorder<Creature>(this);
			}
		
		return _statsRecorder;
	}
	
	@Override
	public boolean isCreature()
	{
		return true;
	}
	
	public void displayGiveDamageMessage(Creature target, int damage, boolean crit, boolean miss, boolean shld, boolean magic)
	{
		if (miss && target.isPlayer() && !target.isDamageBlocked())
			target.sendPacket(new SystemMessage2(SystemMsg.C1_HAS_EVADED_C2S_ATTACK).addName(target).addName(this));
	}
	
	public void displayReceiveDamageMessage(Creature attacker, int damage)
	{
		//
	}
	
	public Collection<TimeStamp> getSkillReuses()
	{
		return _skillReuses.values();
	}
	
	public TimeStamp getSkillReuse(Skill skill)
	{
		return _skillReuses.get(skill.hashCode());
	}
	
	public String getVisibleName()
	{
		return getName();
	}
}