package server;

import java.net.Socket;
import java.util.HashMap;
import message.Message;

public class ServerInfo {
    private String ipAddress;
    private int serverId = -1;

    public ServerInfo(String ipAddress, int serverId) {
        this.ipAddress = ipAddress;
        this.serverId = serverId;
    }

    public ServerInfo(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public static ServerInfo createServerInfoFromSocket(Socket socket, int serverId) {
        return new ServerInfo(
            socket.getInetAddress().getHostAddress().toString(), 
            serverId
        );
    }

    public void setServerId(int serverId) {
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + serverId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ServerInfo other = (ServerInfo) obj;
        if (serverId != other.serverId)
            return false;
        return true;
    }


    
}
