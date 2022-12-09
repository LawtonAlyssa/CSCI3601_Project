package server;

import process.Process;
import settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import message.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class ServerConnectProcess extends Process{
    private static final Logger logger = LoggerFactory.getLogger(ServerConnectProcess.class);
    private ServerSocket serverSocket = null;
    private int machineCounter = 0;
    private BlockingQueue<Message> serverInfosSend = null;

    public ServerConnectProcess(ServerInfo serverInfo, BlockingQueue<Message> serverInfosSend) {
        super(serverInfo);
        this.serverInfosSend = serverInfosSend;
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
            HandleClientProcess hcp = new HandleClientProcess(getProcessInfo(), connectionSocket, machineCounter, serverInfosSend);
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
