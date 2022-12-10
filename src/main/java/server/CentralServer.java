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
import settings.Settings;
import message.ServerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import criticalSection.CentralizedLockManager;
import criticalSection.RequestType;
import criticalSection.file.FileInfo;
import criticalSection.file.FileRequest;

public class CentralServer extends Entity{
    private static final Logger logger = LoggerFactory.getLogger(CentralServer.class);
    private int machineCount = 0;
    private CentralizedLockManager centLockManager = null;

    public CentralServer() {
        setServerId(getCentralServerInfo().getServerId());
        centLockManager = new CentralizedLockManager(getHomeDir());
    }
    
    public boolean handleServerMessage(ServerMessage msg) {
        if (super.handleServerMessage(msg)) return true;
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

        return false;
    }

    public boolean handleProcessMessage(Message msg) {
        if (super.handleProcessMessage(msg)) return true;
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

        return false;
    }

    public boolean handleUserInput(String[] tokenStr) {
        if (super.handleUserInput(tokenStr)) return true;

        if (!Settings.SERVER_COMMAND_ENABLED) return false;

        RequestType requestType = null;

        switch (tokenStr[0]) {
            case "touch":
                requestType = RequestType.WRITE;
                break;
            case "cat":
                requestType = RequestType.READ;
            default:
                break;
        }

        if (requestType!=null) {
            logger.trace("Handling command: " + tokenStr[0]);

            if (tokenStr.length < 2) {
                logger.warn("Missing file argument");
                return false;
            }
    
            FileInfo fileInfo = new FileInfo(tokenStr[1], false);
            CriticalSectionRequest critSectRequest = new CriticalSectionRequest(getServerInfo(), new FileRequest(fileInfo, requestType), getClock());
            centLockManager.addCriticalSectionRequest(critSectRequest);
        }

        return false;
    }

    public boolean update() {
        centLockManager.handleRequest();
        return false;
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
