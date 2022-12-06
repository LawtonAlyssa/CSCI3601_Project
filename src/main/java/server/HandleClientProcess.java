package server;

import java.net.Socket;
import process.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandleClientProcess extends Process{
    private static final Logger logger = LoggerFactory.getLogger(HandleClientProcess.class);
    private Socket connectionSocket = null;

    public HandleClientProcess(ServerInfo serverInfo, Socket connectionSocket) {
        super(serverInfo);
        this.connectionSocket = connectionSocket;
    }

    @Override
    public void run() {
        logger.debug("Running HandleClientProcess...");
        ServerToClientConnection s2cc = new ServerToClientConnection(connectionSocket, getProcessInfo());
        logger.debug("s2cc created");
        s2cc.startHandshake();
        while (!s2cc.confirmHandshake()) {}
    }
}
