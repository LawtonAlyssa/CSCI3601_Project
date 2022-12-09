package message;

import java.util.ArrayList;
import java.util.HashMap;
import server.ServerInfo;

public class ServerHandshake extends MessageContent{
    private int serverId;
    private ArrayList<ServerInfo> activeClients = null;

    public ServerHandshake(int serverId, ArrayList<ServerInfo> activeClients) {
        super(MessageType.SERVER_HANDSHAKE);
        this.serverId = serverId;
        this.activeClients = activeClients;
    }
    
    public static ServerHandshake toMessage(String msgStr) {
        HashMap<String, String> hm = Message.parseMessage(msgStr);
        int serverId = Integer.parseInt(hm.get("serverId"));
        ArrayList<ServerInfo> activeClients = new ArrayList<>();

        for (String serverInfo : MessageContent.stringToArrayList("activeClients", msgStr)) {
            activeClients.add(ServerInfo.toMessage(serverInfo));
        }

        return new ServerHandshake(serverId, activeClients);
    }

    @Override
    public String toString() {
        return String.format("%sserverId:[%s]%s", super.toString(), serverId, MessageContent.arrayListToString("activeClients", activeClients));
    }

    public int getServerId() {
        return serverId;
    }
    
}
