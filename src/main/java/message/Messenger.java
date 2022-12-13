package message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.ServerInfo;
import process.QueueManager;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Messenger{
    private static final Logger logger = LoggerFactory.getLogger(Messenger.class);
    private Socket messengerSocket = null;
    private PrintWriter out = null;
    private ServerInfo sourceServerInfo = null;
    private ServerInfo destServerInfo = null;
    private int sentCount = 0;
    private static int allSentCount = 0;
    private ReceiveProcess receiveProcess = null;

    public Messenger(QueueManager queue, Socket messengerSocket, ServerInfo sourceServerInfo, ServerInfo destServerInfo) {
        this.messengerSocket = messengerSocket;
        this.sourceServerInfo = sourceServerInfo;
        this.destServerInfo = destServerInfo;

        try {
            this.out = new PrintWriter(messengerSocket.getOutputStream());
        } catch (IOException e) {
            logger.error("Failed to connect to Messenger Server Socket", e);
        }

        try {
            receiveProcess = new ReceiveProcess(queue, messengerSocket.getInputStream(), this);
            receiveProcess.start();
        } catch (IOException e) {
            logger.error("Failed to get InputStream", e);
        }
    }

    public ServerInfo getSourceServerInfo() {
        return sourceServerInfo;
    }

    public ServerInfo getDestServerInfo() {
        return destServerInfo;
    }

    public void sendSocket(ServerMessage msg) {
        sentCount++;
        allSentCount++;

        String msgStr = msg.toString().replaceAll("\n", "\\\\n");
        logger.debug("Sent " + msg.toLog());
        logger.trace("Sent: " + msgStr);
        
        out.println(msgStr);
        out.flush();
    }

    public void sendSocket(MessageContent data) {
        sendSocket(new ServerMessage(sourceServerInfo, destServerInfo, data));
    }

    public boolean isAlive() {
        return receiveProcess.isAlive();
    }

    public void close() {
        receiveProcess.interrupt();
        try {
            logger.warn("message count (self): " + sentCount + " total messages: " + allSentCount);
            out.close();
            messengerSocket.close();
        } catch (Exception e) {
            logger.error("Messenger socket failed to close.", e);
        }
    }

}
