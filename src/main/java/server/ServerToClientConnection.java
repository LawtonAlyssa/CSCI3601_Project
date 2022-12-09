package server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.Socket;
import message.Message;
import message.MessageType;
import message.Messenger;
import message.ServerHandshake;
import process.ProcessInfo;
import process.QueueManager;
import settings.Settings;

public class ServerToClientConnection extends Messenger{
    private static final Logger logger = LoggerFactory.getLogger(ServerToClientConnection.class);
    private ProcessInfo clientProcessInfo = null;

    public ServerToClientConnection(Socket connectionSocket, ProcessInfo processInfo, int serverId, QueueManager queueManager) {
       super(processInfo, connectionSocket, queueManager);
       this.clientProcessInfo = new ProcessInfo(ServerInfo.createServerInfoFromSocket(connectionSocket, serverId), Settings.SERVER_PROCESS_ID);
       logger.info("Connected to client:" + connectionSocket.getInetAddress());
    }

    public void startHandshake() {
        sendSocket(clientProcessInfo, new ServerHandshake(getClientServerInfo().getServerId(), null));
        logger.info("Sent handshake from server");
    }

    public ServerInfo getClientServerInfo() {
        return clientProcessInfo.getServerInfo();
    }

    public boolean confirmHandshake() {
        Message msg = receiveSocket();

        if (msg==null) return false;

        logger.debug("Received message with message type: " + msg.getMessageType());
        
        if (msg.getMessageType()==MessageType.CLIENT_HANDSHAKE) {
            logger.info("Received confirmation handshake from client");
            return true;
        }

        return false;
    }

}
