package server;

import java.util.HashMap;
import message.Message;

public class ServerInfo {
    private String ipAddress;
    private int serverId;

    public ServerInfo(String ipAddress, int serverId) {
        this.ipAddress = ipAddress;
        this.serverId = serverId;
    }

    public String getIpAddress() {
        return ipAddress;
    }
    public int getServerId() {
        return serverId;
    }

    public static ServerInfo toMessage(String str) {
        HashMap<String, String> hm = Message.parseMessage(str);
        String ipAddress = hm.get("ipAddress");
        int serverId = Integer.parseInt(hm.get("serverId"));
        return new ServerInfo(ipAddress, serverId);
    }

    @Override
    public String toString() {
        return String.format("ipAddress:[%s]serverId:[%d]", ipAddress, serverId);
    }
}
