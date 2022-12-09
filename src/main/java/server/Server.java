package server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import clock.Clock;
import clock.ClockType;
import clock.Lamport;
import message.ClientInfo;
import message.Message;
import message.MessageType;
import process.Entity;
import process.ProcessInfo;
import settings.Settings;

public class Server extends Entity{
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private ServerInfo serverInfo;
    private Clock clock;
    private static final int INITIAL_SERVER_ID = -1;
    private ArrayList<ServerInfo> machineConnections = new ArrayList<>();
    private BlockingQueue<Message> msgQueue = new LinkedBlockingQueue<>();
    
    public Server(ProcessInfo processInfo, ServerInfo serverInfo, Clock clock) {
        super(processInfo);
        this.serverInfo = serverInfo;
        this.clock = clock;
        startServerConnectProcess();
    }

    protected Server(ProcessInfo processInfo, int serverId) {
        super(processInfo);

        try {
            this.serverInfo = new ServerInfo(InetAddress.getLocalHost().getHostAddress(), serverId);
            logger.info("Local IP Address: " + serverInfo.getIpAddress());
            this.clock = (Settings.CLOCK_TYPE == ClockType.LAMPORT) ? new Lamport() : null;
            startServerConnectProcess();
        } catch (UnknownHostException e) {
            logger.error("Could not start local server", e);
        } catch (Exception e) {
            logger.error("Unknown error occurred", e);
        }
    }

    public void startServerConnectProcess() {
        ServerConnectProcess sp = new ServerConnectProcess(serverInfo, msgQueue);
        sp.start();
        logger.trace("ServerConnectProcess started");
    }

    public void UpdateMachineConnections() {
        if (!msgQueue.isEmpty()) {
            Message polledMsg = msgQueue.poll();
            if (polledMsg.getMessageType()==MessageType.CLIENT_INFO) {
                ServerInfo clientInfo = ((ClientInfo)polledMsg.getData()).getClientInfo();
                machineConnections.add(clientInfo);
                logger.debug("Added machine " + clientInfo.getServerId());
            }
        }
    } 

    public static Server startLocalServer(ProcessInfo processInfo) {
        return new Server(processInfo, INITIAL_SERVER_ID);
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public Clock getClock() {
        return clock;
    }   

}