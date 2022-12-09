package server;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import process.Process;
import process.ProcessInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import message.ClientInfo;
import message.Message;

public class HandleClientProcess extends Process{
    private static final Logger logger = LoggerFactory.getLogger(HandleClientProcess.class);
    private ServerToClientConnection s2cc;

    public HandleClientProcess(ProcessInfo parentProcessInfo, Socket connectionSocket, int connectedServerId, BlockingQueue<Message> msgQueue) {
        super(parentProcessInfo, msgQueue);

        this.s2cc = new ServerToClientConnection(connectionSocket, getProcessInfo(), connectedServerId);
        logger.debug("s2cc created");
    }

    @Override
    public void run() {
        logger.debug("Running HandleClientProcess...");
        s2cc.startHandshake();
        while (!s2cc.confirmHandshake()) {}
        sendToParentProcess(new ClientInfo(s2cc.getClientServerInfo()));
    }
}
