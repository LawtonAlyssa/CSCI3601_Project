package message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import process.Process;
import process.QueueManager;

public class ReceiveProcess extends Process{
    private static final Logger logger = LoggerFactory.getLogger(ReceiveProcess.class);
    private BufferedReader in;
    private Messenger messenger;

    public ReceiveProcess(QueueManager queue, InputStream inputStream, Messenger messenger) {
        super(queue);

        this.in = new BufferedReader(new InputStreamReader(inputStream));
        this.messenger = messenger;
    }

    private void receiveSocket() {
        try {
            String line = in.readLine();

            if (line==null || line.length()==0) return;

            logger.trace("Received: " + line);

            ServerMessage msg = ServerMessage.toMessage(line);
            msg.setMessenger(messenger);

            logger.debug("Received " + msg.toLog());

            send(msg);
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
