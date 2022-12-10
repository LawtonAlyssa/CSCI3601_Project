package server;

import java.io.File;
import java.net.Socket;
import java.util.HashMap;
import message.CentralServerHandshake;
import message.CriticalSectionRequest;
import message.Message;
import message.Messenger;
import message.ServerMessage;
import process.Entity;
import message.ServerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import criticalSection.CentralizedLockManager;
import criticalSection.file.FileInfo;

public class CentralServer extends Entity{
    private static final Logger logger = LoggerFactory.getLogger(CentralServer.class);
    private int machineCount = 0;
    private HashMap<ServerInfo, FileInfo> fileInfos = new HashMap<>();
    private CentralizedLockManager centLockManager = new CentralizedLockManager();

    public CentralServer() {
        setServerId(getCentralServerInfo().getServerId());
    }
    
    public void handleServerMessage(ServerMessage msg) {
        super.handleServerMessage(msg);
        switch (msg.getMessageType()) {
            case CENTRAL_CLIENT_HANDSHAKE:
                logger.info("Confirmed central handshake from Client " + msg.getSource().getServerId());
                break;
            case CS_REQUEST:
                CriticalSectionRequest critSectRequest = (CriticalSectionRequest)msg.getData();
                centLockManager.addCriticalSectionRequest(critSectRequest);
                break;
            default:
                break;
        }
    }

    public void handleProcessMessage(Message msg) {
        super.handleProcessMessage(msg);
        switch (msg.getMessageType()) {
            case SERVER_CONN:
                Socket connectionSocket = ((ServerConnection)msg.getData()).getConnectionSocket();

                machineCount++;

                ServerInfo connServerInfo = new ServerInfo(connectionSocket.getInetAddress().getHostAddress().toString(), machineCount);

                Messenger msgr = new Messenger(
                    getQueue(), 
                    connectionSocket, 
                    getServerInfo(), 
                    connServerInfo
                );

                addServer(msgr);
                logger.debug("Server connected to a new Socket w/ id: " + connServerInfo.getServerId());

                msgr.sendSocket(new CentralServerHandshake(machineCount, getServers()));
                break;
            default:
                break;
        }
    }

    @Override
    public void createHomeDir() {
        super.createHomeDir();
        File homeDir = getHomeDir();

        if (!homeDir.exists()) {
            homeDir.mkdirs();
        }
    }

}
