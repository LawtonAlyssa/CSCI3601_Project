package server;

import process.Process;
import process.ProcessCommType;
import process.QueueManager;
import settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerConnectProcess extends Process{
    private static final Logger logger = LoggerFactory.getLogger(ServerConnectProcess.class);
    private ServerSocket serverSocket = null;
    private int machineCounter = 0;
    private QueueManager queueManager = null;

    public ServerConnectProcess(ServerInfo serverInfo, QueueManager queueManager) {
        super(serverInfo);
        
        this.queueManager = queueManager;

        createServerSocket();
    }

    @Override
    public void run() {
        logger.info("Running ServerConnect...");
        try {
            while (true) {
                connect();
            }
        } finally {
            close();
        }
    }

    public void connect() {
        try {
            Socket connectionSocket = serverSocket.accept();

            machineCounter++;
            HandleClientProcess hcp = new HandleClientProcess(getServerInfo(), connectionSocket, machineCounter, queueManager);
            hcp.addDestProcess(ProcessCommType.DEFAULT, getProcessInfo());
            hcp.start();
            logger.trace("Server successfully connected to client");
        } catch (IOException e) {
            logger.error("Accept failed", e);
        }
    }

    public void createServerSocket() {
        for (int i = 0; i < Settings.MAX_CLIENTS; i++) {
            try {
                this.serverSocket = new ServerSocket(Settings.LOCAL_PORT_NUM+i);
                logger.trace("Found port number for server socket");
                return;
            } catch (IOException e) {
                // make new attempt at Socket
            }
        }

        logger.error("Maximum number of clients reached");
    }

    public void close() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            logger.error("Server socket failed to close.", e);
        }
    }
    
}
