package message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import process.Entity;
import process.ProcessInfo;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Messenger extends Entity{
    private static final Logger logger = LoggerFactory.getLogger(Messenger.class);
    private Socket messengerSocket = null;
    private BlockingQueue<Message> msgQueue = null;
    private PrintWriter out = null;

    public Messenger(ProcessInfo processInfo, Socket messengerSocket) {
        super(processInfo);
        this.messengerSocket = messengerSocket;

        try {
            this.out = new PrintWriter(messengerSocket.getOutputStream());
        } catch (IOException e) {
            logger.error("Failed to connect to Messenger Server Socket", e);
        }

        msgQueue = new LinkedBlockingQueue<Message>();

        try {
            ReceiveProcess rp = new ReceiveProcess(getProcessInfo(), messengerSocket.getInputStream(), msgQueue);
            rp.start();
        } catch (IOException e) {
            logger.error("Failed to get InputStream", e);
        }
    }

    public void send(ProcessInfo source, ProcessInfo dest, MessageContent data) {
        String msgStr = new Message(source, dest, data).toString();
        logger.debug("Sent: " + msgStr);
        out.println(msgStr);
        out.flush();
    }

    public void send(ProcessInfo dest, MessageContent data) {
        send(getProcessInfo(), dest, data);
    }

    public Message receive() {
        return (msgQueue.isEmpty()) ? null : msgQueue.poll();
    }

    public void close() {
        try {
            out.close();
            messengerSocket.close();
        } catch (Exception e) {
            logger.error("Messenger socket failed to close.", e);
        }
    }
}
