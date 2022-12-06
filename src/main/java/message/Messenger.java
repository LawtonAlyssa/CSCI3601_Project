package message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import process.ProcessInfo;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Messenger {
    private static final Logger logger = LoggerFactory.getLogger(Messenger.class);
    private Socket messengerSocket = null;
    private ProcessInfo processInfo = null;
    private BlockingQueue<Message> messageQueue = null;
    private PrintWriter out = null;

    public Messenger(Socket messengerSocket, ProcessInfo processInfo) {
        this.messengerSocket = messengerSocket;
        this.processInfo = processInfo;

        try {
            this.out = new PrintWriter(messengerSocket.getOutputStream());
        } catch (IOException e) {
            logger.error("Failed to connect to Messenger Server Socket", e);
        }

        messageQueue = new LinkedBlockingQueue<Message>();

        try {
            ReceiveProcess rp = new ReceiveProcess(this.processInfo.getServerInfo(), messengerSocket.getInputStream(), messageQueue);
            rp.start();
        } catch (IOException e) {
            logger.error("Failed to get InputStream", e);
        }
    }

    public void send(ProcessInfo source, ProcessInfo dest, MessageContent data) {
        out.println(new Message(source, dest, data).toString());
        out.flush();
    }

    public void send(ProcessInfo dest, MessageContent data) {
        send(processInfo, dest, data);
    }

    public Message receive() {
        return (messageQueue.isEmpty()) ? null : messageQueue.poll();
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
