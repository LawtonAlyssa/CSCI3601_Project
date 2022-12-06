package server;

import process.Process;
import settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.ServerSocket;
// import java.util.ArrayList;

public class ServerConnectProcess extends Process{
    private static final Logger logger = LoggerFactory.getLogger(ServerConnectProcess.class);
    private ServerSocket serverSocket = null;
    // private ArrayList<ServerToClientConnection> clientConnections = new ArrayList<>();
    
    public ServerConnectProcess(ServerInfo serverInfo) {
        super(serverInfo);
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
            HandleClientProcess hcp = new HandleClientProcess(getServerInfo(), serverSocket.accept());
            hcp.start();
            // logger.info("Server successfully connected to client");
        } catch (IOException e) {
            logger.error("Accept failed", e);
        }
    }

    public void createServerSocket() {
        for (int i = 0; i < Settings.MAX_CLIENTS; i++) {
            try {
                this.serverSocket = new ServerSocket(Settings.LOCAL_PORT_NUM+i);
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
