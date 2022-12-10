package server;

import java.net.Socket;

import message.CentralServerHandshake;
import message.Message;
import message.Messenger;
import message.ServerMessage;
import process.Entity;
import message.ServerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CentralServer extends Entity{
    private static final Logger logger = LoggerFactory.getLogger(CentralServer.class);
    private int machineCount = 0;

    public CentralServer() {
        setServerId(getCentralServerInfo().getServerId());
    }
    
    public void handleServerMessage(ServerMessage msg) {
        super.handleServerMessage(msg);
        switch (msg.getMessageType()) {
            case CENTRAL_CLIENT_HANDSHAKE:
                logger.info("Confirmed central handshake from Client " + msg.getSource().getServerId());
                break;
            default:
                break;
        }
        // if (msg.getMessageType()==MessageType.SERVER_HANDSHAKE) {
        //     CentralServerHandshake sh = (CentralServerHandshake)msg.getData();
        //     logger.info("Received handshake from Server " + msg.getSource().getServerId());
        //     logger.info("Assigned server id:" + sh.getServerId());
        //     setServerId(sh.getServerId());

        //     ArrayList<ServerInfo> clients = sh.getActiveClients();
        //     setActiveClients(clients);
        //     logger.info("Current number of active clients: " + getActiveClients().size());
            
        //     sendClientHandshake();
        // }
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
}
