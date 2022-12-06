package server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.Socket;
import message.Message;
import message.MessageContent;
import message.MessageType;
import message.Messenger;
import process.ProcessInfo;

public class ServerToClientConnection extends Messenger{
    private static final Logger logger = LoggerFactory.getLogger(ServerToClientConnection.class);
    private ProcessInfo clientProcessInfo = null;

    public ServerToClientConnection(Socket connectionSocket, ProcessInfo processInfo) {
       super(connectionSocket, processInfo);
       this.clientProcessInfo = new ProcessInfo(new ServerInfo(connectionSocket.getInetAddress().toString(), 0));
       logger.info("Connected to client:" + connectionSocket.getInetAddress());
    }

    public void startHandshake() {
        send(clientProcessInfo, new MessageContent(MessageType.SERVER_HANDSHAKE));
        logger.info("Sent handshake from Server");
    }

    public boolean confirmHandshake() {
        Message msg = receive();
        if (msg==null) return false;
        logger.debug("Received message with message type: " + msg.getMessageType());
        if (msg.getMessageType()==MessageType.CLIENT_HANDSHAKE) {
            logger.info("Received confirmation handshake from Client");
            return true;
        }
        return false;
    }
}
