package server;

import java.net.Socket;
import process.Process;
import process.ProcessCommType;
import process.QueueManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import message.ClientInfo;

public class HandleClientProcess extends Process{
    private static final Logger logger = LoggerFactory.getLogger(HandleClientProcess.class);
    private ServerToClientConnection s2cc;

    public HandleClientProcess(ServerInfo serverInfo, Socket connectionSocket, int connectedServerId, QueueManager queueManager) {
        super(serverInfo, queueManager);

        this.s2cc = new ServerToClientConnection(connectionSocket, getProcessInfo(), connectedServerId, queueManager);
        logger.debug("s2cc created");
    }

    @Override
    public void run() {
        logger.debug("Running HandleClientProcess...");
        s2cc.startHandshake();
        while (!s2cc.confirmHandshake()) {}
        send(ProcessCommType.DEFAULT, new ClientInfo(s2cc.getClientServerInfo()));
        // sendToParentProcess(new ClientInfo(s2cc.getClientServerInfo()));
    }
}
