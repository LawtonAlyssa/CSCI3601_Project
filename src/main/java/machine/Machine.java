package machine;

import message.Message;
import message.MessageContent;
import message.MessageType;
import message.Messenger;
import message.CentralServerHandshake;
import message.ClientHandshake;
import message.ServerMessage;
import process.Entity;
import server.ServerInfo;
import settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import message.ServerConnection;

public class Machine extends Entity{
    private static final Logger logger = LoggerFactory.getLogger(Machine.class);
    private Messenger centralClient = createActiveClient(getCentralServerInfo());
    private ArrayList<Messenger> clients = new ArrayList<>();

    public void handleServerMessage(ServerMessage msg) {
        super.handleServerMessage(msg);
        switch (msg.getMessageType()) {
            case CENTRAL_SERVER_HANDSHAKE:
                CentralServerHandshake sh = (CentralServerHandshake)msg.getData();
                
                setServerId(sh.getServerId());
                logger.info("Assigned server id: " + sh.getServerId());

                logger.info("Received central server handshake from Server " + msg.getSource().getServerId());

                ArrayList<ServerInfo> activeClients = sh.getActiveClients();
                setActiveClients(activeClients);
                logger.info("Current number of active clients: " + clients.size());
                
                sendCentralClientHandshake();

                break;
            case SERVER_HANDSHAKE:
                int msgServerId = msg.getSource().getServerId();
                logger.info("Received server handshake from Server " + msgServerId);
                
                ClientHandshake ch = new ClientHandshake(getServerInfo().getServerId());
                logger.debug("Server Destination Messenger: " + msg.getMessenger().getDestServerInfo().getServerId());
                msg.getMessenger().sendSocket(ch);
                // logger.info("Sent client handshake to client: " + msg.getMessenger());
                break;
            case CLIENT_HANDSHAKE:
                ClientHandshake clientHandshake = (ClientHandshake)msg.getData();
                Messenger messenger = msg.getMessenger();
                int sourceServerId = clientHandshake.getServerId();

                messenger.getDestServerInfo().setServerId(sourceServerId);
                logger.debug("Assigned client to id: " + sourceServerId);
                break;
            default:
                break;
        }
    }

    public void sendCentralClientHandshake() {
        MessageContent msg = new MessageContent(MessageType.CENTRAL_CLIENT_HANDSHAKE);
        centralClient.sendSocket(msg);
        logger.info("Sent central client handshake");
    }

    public void handleProcessMessage(Message msg) {
        super.handleProcessMessage(msg);
        
        switch (msg.getMessageType()) {
            case SERVER_CONN:
                Socket connectionSocket = ((ServerConnection)msg.getData()).getConnectionSocket();

                ServerInfo connServerInfo = new ServerInfo(
                    connectionSocket.getInetAddress().getHostAddress().toString()
                );

                Messenger msgr = new Messenger(
                    getQueue(), 
                    connectionSocket, 
                    getServerInfo(), 
                    connServerInfo
                );
                
                addServer(msgr); // store server connection
                logger.debug("Server connected to a new Socket w/ id: " + connServerInfo.getServerId());

                msgr.sendSocket(new MessageContent(MessageType.SERVER_HANDSHAKE));
                break;
            default:
                break;
        }
    }

    public Messenger getActiveClient(ServerInfo serverInfo) {
        for (Messenger messenger : clients) {
            if (messenger.getDestServerInfo().equals(serverInfo)) {
                return messenger;
            }
        }
        return null;
    }

    public void addActiveClient(ServerInfo clientServerInfo) {
        if (getActiveClient(clientServerInfo)!=null) return;
        if (clientServerInfo.equals(getServerInfo())) return;

        logger.debug("Added client w/ id: " + clientServerInfo.getServerId());
        clients.add(createActiveClient(clientServerInfo));
    }

    public Messenger createActiveClient(ServerInfo clientServerInfo) {

        Socket clientSocket;

        String clientIpAddress = clientServerInfo.getIpAddress();

        logger.debug("Connecting to server: " + clientServerInfo.getServerId());

        try {
            clientSocket = new Socket(clientIpAddress, Settings.LOCAL_PORT_NUM+clientServerInfo.getServerId());

            return new Messenger(
                getQueue(), 
                clientSocket, 
                getServerInfo(), 
                clientServerInfo
            );
        } catch (UnknownHostException e) {
            logger.error("Cannot find host: " + clientIpAddress, e);
        } catch (IOException e) {
            logger.error("Couldn't get I/O for the connection to: " + clientIpAddress, e);
        }

        return null;
    }

    public void setActiveClients(ArrayList<ServerInfo> clientServerInfos) {
        // servers.clear();

        for (ServerInfo serverInfo : clientServerInfos) {
            addActiveClient(serverInfo);
        }
    }

    @Override
    public void createHomeDir() {
        super.createHomeDir();
        File homeDir = getHomeDir();

        if (homeDir.exists()) {
            homeDir.delete();
            logger.info("Deleted home directory: " + homeDir.getPath());
        }
        homeDir.mkdirs();
    }
    
}
