package l2f.gameserver.handler.admincommands.impl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import l2f.gameserver.Announcements;
//import l2f.gameserver.ThreadPoolManager;
import l2f.gameserver.handler.admincommands.IAdminCommandHandler;
import l2f.gameserver.model.GameObjectsStorage;
import l2f.gameserver.model.Player;
import l2f.gameserver.skills.AbnormalEffect;
import l2f.gameserver.network.serverpackets.ExShowScreenMessage;
import l2f.gameserver.network.serverpackets.NpcHtmlMessage;
import l2f.gameserver.network.serverpackets.PlaySound;
import l2f.gameserver.network.serverpackets.PlaySound.Type;
import l2f.gameserver.network.serverpackets.components.ChatType;

/**
 *
 * @author  NeverMore
 */
public class AdminDanceSystem implements IAdminCommandHandler
{
	boolean _temp = false;
	
	private static enum Commands 
	{ 
	  admin_dance, 
	  admin_gangnam, 
	  admin_sexi,
	  admin_christmas,
	  admin_macarena,
	  admin_stopdance,
	  admin_mytype,
	  admin_beyonce
	}
	
    @SuppressWarnings({ "rawtypes", "unused" })
    @Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
    {
	 Commands command = (Commands) comm;
	 
		if (fullString.startsWith("admin_dance"))
		{
			activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/dancesystem.htm"));
		}
		    
		if (fullString.startsWith("admin_stopdance"))
		{
	    		for (Player player : GameObjectsStorage.getAllPlayers())
	    		{
	    			player.sendMessage("Dance event terminated.");
			        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	      			scheduler.schedule(new MyTask2(), 10, TimeUnit.SECONDS);
	 			 }
		}	
	    		
	    if (fullString.startsWith("admin_gangnam"))
	    {
			
			if (_temp == true)
			{
				String msg = "There is already a dancing event running! Try later!";
				activeChar.sendPacket(new ExShowScreenMessage(msg, 20000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false));
				activeChar.sendMessage(msg);
				return false;
			}
			_temp = true;
				String msg = "Lets have some fun ! In 30 sec's dance event begins !";
				activeChar.sendPacket(new ExShowScreenMessage(msg, 20000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false));
				activeChar.sendMessage(msg);
				Announcements.getInstance().announceToAll("Let's dance boys and girls, turn up the volume! In 30 sec's dance event begins!", ChatType.BATTLEFIELD);
		        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
				scheduler.schedule(new Runnable()
				{
      			@Override
      			public void run()
      			{
      				try
      		        {
      					for(Player player : GameObjectsStorage.getAllPlayers())
      					{
      				        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
      						PlaySound _song = new PlaySound(Type.MUSIC, "Gangnam", 0, 0, 0, 0, 0);
      						player.sendPacket(_song);
     						String msg = "Show me what you got , lets shake it baby !";
     						player.sendPacket(new ExShowScreenMessage(msg, 20000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false));
      						player.sendMessage(msg);
      						player.startParalyzed();
      						player.setIsInvul(true);
      		 			    player.broadcastSkillOrSocialAnimation(10, 0, 0, 0);
      						player.startAbnormalEffect(AbnormalEffect.MAGIC_CIRCLE);
      						scheduler.schedule(new MyTask(), 4, TimeUnit.SECONDS);
//      						scheduler.schedule(new MyTask2(), 44, TimeUnit.SECONDS);
//      						scheduler.shutdown();
      					}
      		        }
      				catch (Exception e)
      				{
      				}
      			}
      		}, 30, TimeUnit.SECONDS);
//				scheduler.shutdown();
	    }  
	    
	    if (fullString.startsWith("admin_mytype"))
		{
	
	if (_temp == true)
	{
		String msg4 = "There is already a dancing event running! Try later!";
		activeChar.sendPacket(new ExShowScreenMessage(msg4, 20000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false));
		activeChar.sendMessage(msg4);
		return false;
	}
	_temp = true;
		String msg4 = "Lets have some fun ! In 30 sec's dance event begins !";
		activeChar.sendPacket(new ExShowScreenMessage(msg4, 20000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false));
		activeChar.sendMessage(msg4);
		Announcements.getInstance().announceToAll("Let's dance boys and girls, turn up the volume! In 30 sec's dance event begins!", ChatType.BATTLEFIELD);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.schedule(new Runnable()
		{
			@Override
			public void run()
			{
				try
		        {
					for(Player player : GameObjectsStorage.getAllPlayers())
					{
				        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
						PlaySound _song = new PlaySound(Type.MUSIC, "mytype", 0, 0, 0, 0, 0);
						player.sendPacket(_song);
						String msg = "Show me what you got , lets shake it baby !";
						player.sendPacket(new ExShowScreenMessage(msg, 20000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false));
						player.sendMessage(msg);
						player.startParalyzed();
						player.setIsInvul(true);
		 			    player.broadcastSkillOrSocialAnimation(10, 0, 0, 0);
						player.startAbnormalEffect(AbnormalEffect.MAGIC_CIRCLE);
						scheduler.schedule(new MyTask(), 4, TimeUnit.SECONDS);
//						scheduler.schedule(new MyTask2(), 44, TimeUnit.SECONDS);
//						scheduler.shutdown();
					}
		        }
				catch (Exception e)
				{
				}
			}
		}, 30, TimeUnit.SECONDS);
//		scheduler.shutdown();
		}    

	    if (fullString.startsWith("admin_beyonce"))
		{
	
	if (_temp == true)
	{
		String msg4 = "There is already a dancing event running! Try later!";
		activeChar.sendPacket(new ExShowScreenMessage(msg4, 20000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false));
		activeChar.sendMessage(msg4);
		return false;
	}
	_temp = true;
		String msg4 = "Lets have some fun ! In 30 sec's dance event begins !";
		activeChar.sendPacket(new ExShowScreenMessage(msg4, 20000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false));
		activeChar.sendMessage(msg4);
		Announcements.getInstance().announceToAll("Let's dance boys and girls, turn up the volume! In 30 sec's dance event begins!", ChatType.BATTLEFIELD);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.schedule(new Runnable()
		{
			@Override
			public void run()
			{
				try
		        {
					for(Player player : GameObjectsStorage.getAllPlayers())
					{
				        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
						PlaySound _song = new PlaySound(Type.MUSIC, "beyonce", 0, 0, 0, 0, 0);
						player.sendPacket(_song);
						String msg = "Show me what you got , lets shake it baby !";
						player.sendPacket(new ExShowScreenMessage(msg, 20000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false));
						player.sendMessage(msg);
						player.startParalyzed();
						player.setIsInvul(true);
		 			    player.broadcastSkillOrSocialAnimation(10, 0, 0, 0);
						player.startAbnormalEffect(AbnormalEffect.MAGIC_CIRCLE);
						scheduler.schedule(new MyTask(), 4, TimeUnit.SECONDS);
//						scheduler.schedule(new MyTask2(), 44, TimeUnit.SECONDS);
//						scheduler.shutdown();
					}
		        }
				catch (Exception e)
				{
				}
			}
		}, 30, TimeUnit.SECONDS);
//		scheduler.shutdown();
		} 
	    
	if (fullString.startsWith("admin_jingle"))	
			{
				
			if (_temp == true)
			{
				String msg3 = "There is already a dancing event running! Try later!";
				activeChar.sendPacket(new ExShowScreenMessage(msg3, 20000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false));
				activeChar.sendMessage(msg3);
				return false;
			}
			_temp = true;
				String msg3 = "Lets have some fun ! In 30 sec's dance event begins! TURN UP THE VOLUME";
				activeChar.sendPacket(new ExShowScreenMessage(msg3, 20000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false));
				activeChar.sendMessage(msg3);
				Announcements.getInstance().announceToAll("Let's dance boys and girls, turn up the volume! In 30 sec's dance event begins!", ChatType.BATTLEFIELD);
		        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
				scheduler.schedule(new Runnable()
				{
      			@Override
      			public void run()
      			{
      				try
      		        {
      					for(Player player : GameObjectsStorage.getAllPlayers())
      					{
      				        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
      						PlaySound _song = new PlaySound(Type.MUSIC, "jingle", 0, 0, 0, 0, 0);
      						player.sendPacket(_song);
     						String msg = "Show me what you got , lets shake it baby !";
     						player.sendPacket(new ExShowScreenMessage(msg, 20000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false));
      						player.sendMessage(msg);
      						player.startParalyzed();
      						player.setIsInvul(true);
      		 			    player.broadcastSkillOrSocialAnimation(10, 0, 0, 0);
      						player.startAbnormalEffect(AbnormalEffect.MAGIC_CIRCLE);
      						scheduler.schedule(new MyTask(), 4, TimeUnit.SECONDS);
//      						scheduler.schedule(new MyTask2(), 44, TimeUnit.SECONDS);
//      						scheduler.shutdown();
      					}
      		        }
      				catch (Exception e)
      				{
      				}
      			}
      		}, 30, TimeUnit.SECONDS);
//				scheduler.shutdown();
			}
  
					
				if (fullString.startsWith("admin_macarena"))
				{
			if (_temp == true)
			{
				String msg4 = "There is already a dancing event running! Try later!";
				activeChar.sendPacket(new ExShowScreenMessage(msg4, 20000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false));
				activeChar.sendMessage(msg4);
				return false;
			}
			_temp = true;
				String msg4 = "Lets have some fun ! In 30 sec's dance event begins! TURN UP THE VOLUME";
				activeChar.sendPacket(new ExShowScreenMessage(msg4, 20000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false));
				activeChar.sendMessage(msg4);
				Announcements.getInstance().announceToAll("Let's dance boys and girls, turn up the volume! In 30 sec's dance event begins!", ChatType.BATTLEFIELD);
		        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
				scheduler.schedule(new Runnable()
				{
      			@Override
      			public void run()
      			{
      				try
      		        {
      					for(Player player : GameObjectsStorage.getAllPlayers())
      					{
      				        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
      						PlaySound _song = new PlaySound(Type.MUSIC, "macarena", 0, 0, 0, 0, 0);
      						player.sendPacket(_song);
     						String msg = "Show me what you got , lets shake it baby !";
     						player.sendPacket(new ExShowScreenMessage(msg, 20000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false));
      						player.sendMessage(msg);
      						player.startParalyzed();
      						player.setIsInvul(true);
      		 			    player.broadcastSkillOrSocialAnimation(10, 0, 0, 0);
      						player.startAbnormalEffect(AbnormalEffect.MAGIC_CIRCLE);
      						scheduler.schedule(new MyTask(), 4, TimeUnit.SECONDS);
//      						scheduler.schedule(new MyTask2(), 44, TimeUnit.SECONDS);
//      						scheduler.shutdown();
      					}
      		        }
      				catch (Exception e)
      				{
      				}
      			}
      		}, 30, TimeUnit.SECONDS);
//				scheduler.shutdown();

				}    

				if (fullString.startsWith("admin_sexi"))
				{
			
			if (_temp == true)
			{
					String msg2 = "There is already a dancing event running! Try later!";
					activeChar.sendPacket(new ExShowScreenMessage(msg2, 20000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false));
					activeChar.sendMessage(msg2);
				return false;
			}
          	_temp = true;
			String msg2 = "Lets have some fun ! In 30 sec's dance event begins !";
			activeChar.sendPacket(new ExShowScreenMessage(msg2, 20000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false));
			activeChar.sendMessage(msg2);
			Announcements.getInstance().announceToAll("Let's dance boys and girls, turn up the volume! In 30 sec's dance event begins!", ChatType.BATTLEFIELD);
	        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
			scheduler.schedule(new Runnable()
			{
  			@Override
  			public void run()
  			{
  				try
  		        {
  					for(Player player : GameObjectsStorage.getAllPlayers())
  					{
  				        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  						PlaySound _song = new PlaySound(Type.MUSIC, "sexi", 0, 0, 0, 0, 0);
  						player.sendPacket(_song);
 						String msg = "Show me what you got , lets shake it baby !";
 						player.sendPacket(new ExShowScreenMessage(msg, 20000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false));
  						player.sendMessage(msg);
  						player.startParalyzed();
  						player.setIsInvul(true);
  		 			    player.broadcastSkillOrSocialAnimation(10, 0, 0, 0);
  						player.startAbnormalEffect(AbnormalEffect.MAGIC_CIRCLE);
  						scheduler.schedule(new MyTask(), 4, TimeUnit.SECONDS);
//  						scheduler.schedule(new MyTask2(), 44, TimeUnit.SECONDS);
//  						scheduler.shutdown();
  					}
  		        }
  				catch (Exception e)
  				{
  				}
  			}
  		}, 30, TimeUnit.SECONDS);
//			scheduler.shutdown();

		}
	return true;
    }

    @SuppressWarnings("rawtypes")
	@Override
	public Enum[] getAdminCommandEnum() {
		return Commands.values();
	}
	public class MyTask implements Runnable
	{
	    @Override
		public void run()
	    {
	    	if(_temp == true)
	    	{
	    		for(Player player : GameObjectsStorage.getAllPlayers())
	    		{
	  			   player.broadcastSkillOrSocialAnimation(18, 0, 0, 0);
	    		}
		        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	    		scheduler.schedule(new MyTask(), 18, TimeUnit.SECONDS);
	    		scheduler.schedule(new MyTask2(), 36, TimeUnit.SECONDS);
	    		scheduler.shutdown();
	    	}
	    }
	}
	class MyTask2 implements Runnable
	{

	    @Override
		public void run()
	    {
    		for(Player player : GameObjectsStorage.getAllPlayers())
    		{
 			   _temp = false;
			   player.stopParalyzed();
			   player.setIsInvul(false);
 			   player.broadcastSkillOrSocialAnimation(10, 0, 0, 0);
 			   player.broadcastSkillOrSocialAnimation(11, 0, 0, 0);
 			   player.broadcastSkillOrSocialAnimation(22133, 1, 0, 0);
 			   player.stopAbnormalEffect(AbnormalEffect.MAGIC_CIRCLE);
 			   player.getInventory().addItem(6673, 5, "Dance Reward");
 			   player.sendMessage("Gongratulations! Check your inventory for rewards! Damn dude that ass is deadly!");
 			   }
	    }
	}
}