package message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import process.Process;
import process.QueueManager;
import server.ServerInfo;

public class ReceiveProcess extends Process{
    private static final Logger logger = LoggerFactory.getLogger(ReceiveProcess.class);
    private BufferedReader in;

    public ReceiveProcess(ServerInfo serverInfo, InputStream inputStream, QueueManager queueManager) {
        super(serverInfo, queueManager);
        this.in = new BufferedReader(new InputStreamReader(inputStream));
    }

    private void receiveSocket() {
        try {
            String line = in.readLine();
            // logger.debug("Received line: " + line);
            if (line==null || line.length()==0) return;
            Message msg = Message.toMessage(line);
            logger.debug("Received message type: " + msg.getMessageType());
            send(msg);
            // queue.add(msg);
        } catch (IOException e) {
            logger.error("Failed to receive message", e);
        }
    }

    @Override
    public void run() {
        try {
            logger.debug("Starting ReceiveProcess...");
            while (true) {
                receiveSocket();
            } 
        } finally {
            close();
        }   
    }

    public void close() {
        try {
            in.close();
        } catch (IOException e) {
            logger.error("BufferedReader failed to close.", e);
        }
    }

}
