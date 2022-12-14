package server;

import java.io.File;
import java.net.Socket;
import message.CentralServerHandshake;
import message.CriticalSectionRequest;
import message.CriticalSectionResponse;
import message.Message;
import message.MessageType;
import message.Messenger;
import message.ServerMessage;
import process.Entity;
import message.ServerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import criticalSection.CentralizedLockManager;
import criticalSection.CriticalSectionType;
import criticalSection.RequestType;
import criticalSection.file.FileInfo;
import criticalSection.file.FileRequest;

public class CentralServer extends Entity{
    private static final Logger logger = LoggerFactory.getLogger(CentralServer.class);
    private int machineCount = 0;
    private CentralizedLockManager centLockManager = null;
    private static long readTimer = 0;
    private static long writeTimer = 0;

    public CentralServer() {
        setServerId(getCentralServerInfo().getServerId());
        centLockManager = new CentralizedLockManager(getHomeDir());

        startServerProcess();
    }
    
    public boolean handleServerMessage(ServerMessage msg) {
        if (super.handleServerMessage(msg)) return true;
        switch (msg.getMessageType()) {
            case CENTRAL_CLIENT_HANDSHAKE:
                logger.info("Confirmed central handshake from Client " + msg.getSource().getServerId());
                break;
            case CS_REQUEST:
                CriticalSectionRequest csRequest = (CriticalSectionRequest)msg.getData();
                if (csRequest.getCritSectType()==CriticalSectionType.FILE) {
                    FileRequest fileRequest = (FileRequest)csRequest.getCritSect();
                    if (fileRequest.getRequestType()==RequestType.READ) {
                        readTimer-=System.nanoTime();
                    } else {
                        writeTimer-=System.nanoTime();
                    }
                }
                
                logger.info("Received crtical section request from Client: " + msg.getSource().getServerId());
                centLockManager.addCriticalSectionRequest(msg);
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

        switch (tokenStr[0]) {
            default:
                break;
        }

        return false;
    }

    public boolean update() {
        ServerMessage msg = centLockManager.handleRequest();
        if (msg != null) {
            CriticalSectionRequest csRequest = (CriticalSectionRequest)msg.getData();
            FileRequest fileRequest = (FileRequest)csRequest.getCritSect();
            FileInfo fileInfo = fileRequest.getFileInfo();

            if (fileRequest.getRequestType()==RequestType.READ) {
                readTimer+=System.nanoTime();
            } else {
                writeTimer+=System.nanoTime();
            }
            msg.reply(new CriticalSectionResponse(MessageType.CS_EXIT, fileInfo));
        }
        
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


    public void close() {
        logger.warn("Sync Time | Read time = " + (readTimer/1000) + "[us] | Write time = " + (writeTimer/1000) + "[us]");
        centLockManager.close();
    }
}
