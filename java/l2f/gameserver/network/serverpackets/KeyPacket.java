package l2f.gameserver.network.serverpackets;

import org.strixplatform.StrixPlatform;
import org.strixplatform.utils.StrixClientData;

public class KeyPacket extends L2GameServerPacket {
    private final byte[] key;

    //TODO[K] - Guard section start
    private final StrixClientData clientData;
    // TODO[K] - Strix section end

    public KeyPacket(byte data[]) {
        this.key = data;
        //TODO[K] - Guard section start
        this.clientData = null;
        // TODO[K] - Strix section end
    }

    //TODO[K] - Guard section start
    public KeyPacket(final byte[] key, final StrixClientData clientData) {
        this.key = key;
        this.clientData = clientData;
    }
    // TODO[K] - Strix section end
	
	@Override
	public void writeImpl() {
		writeC(0x2E);
		if(key == null || key.length == 0) {
            //TODO[K] - Guard section start
            if(StrixPlatform.getInstance().isBackNotificationEnabled() && clientData != null) {
                writeC(clientData.getServerResponse().ordinal());
            }
            else
            {
            	writeC(0x00);
            }
            // TODO[K] - Strix section end
			return;
		}
		writeC(0x01);
		writeB(key);
		writeD(0x01);
		writeD(0x00);
		writeC(0x00);
		writeD(0x00);
	}
}