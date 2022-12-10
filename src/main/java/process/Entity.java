package process;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import clock.Clock;
import clock.ClockType;
import clock.Lamport;
import message.Message;
import message.Messenger;
import message.ServerMessage;
import server.ServerInfo;
import server.ServerProcess;
import settings.Settings;

public class Entity {
    private static final Logger logger = LoggerFactory.getLogger(Entity.class);
    private ServerInfo serverInfo = null;
    private ServerInfo centralServerInfo = null;
    private QueueManager queue = new QueueManager();
    private Clock clock = null;
    private ArrayList<Messenger> servers = new ArrayList<>();
    private int machineCount = 0;

    public Entity() {
        try {
            this.serverInfo = new ServerInfo(InetAddress.getLocalHost().getHostAddress());
            logger.debug("Local IP Address: " + serverInfo.getIpAddress());
            this.centralServerInfo = new ServerInfo(Settings.CENTRAL_SERVER_IP_ADDR, Settings.CENTRAL_SERVER_ID);
        } catch (UnknownHostException e) {
            logger.error("Could not start local server", e);
        } catch (Exception e) {
            logger.error("Unknown error occurred", e);
        }
        
        this.clock = (Settings.CLOCK_TYPE == ClockType.LAMPORT) ? new Lamport() : null;

        startServerConnectProcess(queue);
    }

    public void startServerConnectProcess(QueueManager queue) {
        ServerProcess sp = new ServerProcess(queue);
        sp.start();
        logger.trace("Server is listening");
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public ServerInfo getCentralServerInfo() {
        return centralServerInfo;
    }

    public QueueManager getQueue() {
        return queue;
    }

    public void setServerId(int serverId) {
        serverInfo.setServerId(serverId);
    }

    public Clock getClock() {
        return clock;
    }

    public void handleServerMessage(ServerMessage msg) {
        switch (msg.getMessageType()) {
            // case :
            //     break;
            default:
                break;
        }
    }

    public void handleProcessMessage(Message msg) {
        switch (msg.getMessageType()) {
            default:
                break;
        }
    }

    public ArrayList<ServerInfo> getServers() {
        ArrayList<ServerInfo> serverList = new ArrayList<>();

        for (Messenger messenger : servers) {
            serverList.add(messenger.getDestServerInfo());
        }
        return serverList;
    }

    public void addServer(Messenger messenger) {
        servers.add(messenger);
    }

    public void receive() {
        Message msg = getQueue().receive();

        if (msg==null) return;

        logger.debug("Queue Received " + msg.toLog());
        
        if (msg.isServerMessage()) handleServerMessage((ServerMessage)msg);
        else handleProcessMessage(msg);
    }

    public void run() {
        while (true) {
            receive();
        }
    }
    
}
