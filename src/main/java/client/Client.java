package client;

import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import message.Messenger;
import process.ProcessInfo;
import process.QueueManager;

public class Client extends Messenger{
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    
    public Client(ProcessInfo processInfo, Socket clientSocket, QueueManager queueManager) {
        super(processInfo, clientSocket, queueManager);
        logger.trace("Created a client");
    }

}