package message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import process.Process;
import process.ProcessInfo;

public class ReceiveProcess extends Process{
    private static final Logger logger = LoggerFactory.getLogger(ReceiveProcess.class);
    private BufferedReader in;
    private BlockingQueue<Message> queue = null;

    public ReceiveProcess(ProcessInfo parentProcessInfo, InputStream inputStream, BlockingQueue<Message> queue) {
        super(parentProcessInfo, queue);
        this.in = new BufferedReader(new InputStreamReader(inputStream));
        this.queue = queue;
    }

    public void receive() {
        try {
            String line = in.readLine();
            // logger.debug("Received line: " + line);
            if (line==null || line.length()==0) return;
            Message msg = Message.toMessage(line);
            logger.debug("Received message type: " + msg.getMessageType());
            queue.add(msg);
        } catch (IOException e) {
            logger.error("Failed to receive message", e);
        }
    }

    @Override
    public void run() {
        try {
            logger.debug("Starting Receive Thread...");
            while (true) {
                receive();
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
