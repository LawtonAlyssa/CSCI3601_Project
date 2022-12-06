package server;

public class ServerMessageInfo extends ServerInfo{
    private int portNum;

    public ServerMessageInfo(String ipAddress, int serverId, int portNum) {
        super(ipAddress, serverId);
        this.portNum = portNum;
    }

    public int getPortNum() {
        return portNum;
    }
}
