package message;

import java.util.HashMap;

public class ClientHandshake extends MessageContent{
    private int serverId;

    public ClientHandshake(int serverId) {
        super(MessageType.CLIENT_HANDSHAKE);

        this.serverId = serverId;
    }

    public int getServerId() {
        return serverId;
    }

    public static ClientHandshake toMessage(String msgStr) {
        HashMap<String, String> hm = Message.parseMessage(msgStr);
        int serverId = Integer.parseInt(hm.get("serverId"));
        return new ClientHandshake(serverId);
    }

    @Override
    public String toString() {
        return String.format("%sserverId:[%d]", super.toString(), serverId);
    }
}
