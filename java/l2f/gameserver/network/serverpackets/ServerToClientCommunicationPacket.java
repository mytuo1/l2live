package l2f.gameserver.network.serverpackets;

public class ServerToClientCommunicationPacket extends L2GameServerPacket
{
	private static boolean IS_OLD_CLIENT = false; // If you use Interlude or older version, set on "true"
	private ServerRequest serverRequest;
	private String drawText;
	private WarnWindowType warnWindowType;

	public ServerToClientCommunicationPacket(final String drawText)
	{
		this.serverRequest = ServerRequest.SC_SERVER_REQUEST_SET_DRAW_TEXT;
		this.drawText = drawText;
	}

	public ServerToClientCommunicationPacket(final WarnWindowType warnWindowType, final String warnMessage)
	{
		this.serverRequest = ServerRequest.SC_SERVER_REQUEST_SHOW_CUSTOM_WARN_MESSAGE;
		this.warnWindowType = warnWindowType;
		this.drawText = warnMessage;
	}

	@Override
	protected final void writeImpl() {
		writeC(IS_OLD_CLIENT ? 0x70 : 0x7D);
		writeC(serverRequest.ordinal());
		switch(serverRequest) {
			case SC_SERVER_REQUEST_SET_DRAW_TEXT:
				writeS(drawText);
				break;
			case SC_SERVER_REQUEST_SHOW_CUSTOM_WARN_MESSAGE:
				writeC(warnWindowType.ordinal());
				writeS(drawText);
				break;
		}
	}

	public static enum ServerRequest
	{
		SC_SERVER_REQUEST_SET_DRAW_TEXT,
		SC_SERVER_REQUEST_SHOW_CUSTOM_WARN_MESSAGE,
	}

	public static enum WarnWindowType
	{
		UL2CW_DEFAULT,
		UL2CW_CLOSE_WINDOW,
	}
}