//package services.community;
//
//import java.util.StringTokenizer;
//
//import l2f.gameserver.Config;
//import l2f.gameserver.data.htm.HtmCache;
//import l2f.gameserver.handler.bbs.CommunityBoardManager;
//import l2f.gameserver.handler.bbs.ICommunityBoardHandler;
//import l2f.gameserver.model.Player;
//import l2f.gameserver.network.serverpackets.ShowBoard;
//import l2f.gameserver.scripts.ScriptFile;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class CommunityAuctionShow implements ScriptFile, ICommunityBoardHandler
//{
//	private static final Logger _log = LoggerFactory.getLogger(CommunityAuctionShow.class);
//
//
//	@Override
//	public void onLoad()
//	{
//		if (Config.COMMUNITYBOARD_ENABLED)
//		{
//			_log.info("CommunityBoard: Auction System Service Main Board loaded.");
//			CommunityBoardManager.getInstance().registerHandler(this);
//		}
//	}
//
//	@Override
//	public void onReload()
//	{
//		if (Config.COMMUNITYBOARD_ENABLED)
//			CommunityBoardManager.getInstance().removeHandler(this);
//	}
//
//	@Override
//	public void onShutdown()
//	{}
//
//	@Override
//	public String[] getBypassCommands()
//	{
//		return new String[] { "_maillistmain"};
//	}
//
//	@Override
//	public void onBypassCommand(Player player, String bypass)
//	{
//		StringTokenizer st = new StringTokenizer(bypass, "_");
//		String cmd = st.nextToken();
//		player.setSessionVar("add_fav", null);
//		String html = "";
//
//		if (!Config.ENABLE_AUCTION_SYSTEM)
//		{
//			String msg = "<html><body><br><br><br><center>Auction System is currently disabled!</center></body></html>";
//			player.sendPacket(new ShowBoard(msg, "101", player));
//		}
//
//		if ("maillistmain".equals(cmd))
//		{
//
//			html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "auctionlist.htm", player);
//
////			html = fillAuctionListPage(player, html, 1, new int[] {-1, -1}, "All", null, 1, 0, 0, 0);
//
//		}
//		//sending community board
//		ShowBoard.separateAndSend(html, player);
//	}
//
//	@Override
//	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
//	{}
//}
