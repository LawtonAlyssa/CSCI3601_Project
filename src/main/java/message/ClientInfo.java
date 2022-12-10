package message;

import server.ServerInfo;

public class ClientInfo extends MessageContent{
    private ServerInfo clientInfo = null;

    public ClientInfo(ServerInfo clientInfo) {
        super(MessageType.CLIENT_INFO);
        this.clientInfo = clientInfo;
    }

    public static CentralServerHandshake toMessage(String msgStr) {
        return null;
    }

    @Override
    public String toString() {
        return null;
    }

    public ServerInfo getClientInfo() {
        return clientInfo;
    }
}
