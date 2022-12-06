package server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import clock.Clock;
import clock.ClockType;
import clock.Lamport;
import settings.Settings;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private ServerInfo serverInfo;
    private Clock clock;
    private static final int INITIAL_SERVER_ID = -1;
    
    public Server(ServerInfo serverInfo, Clock clock) {
        this.serverInfo = serverInfo;
        this.clock = clock;
        
        ServerConnectProcess sp = new ServerConnectProcess(serverInfo);
        sp.start();
    }

    public static Server startLocalServer() {
        try {
            ServerInfo serverInfo = new ServerInfo(InetAddress.getLocalHost().getHostAddress(), INITIAL_SERVER_ID);
            logger.info("Local IP Address: " + serverInfo.getIpAddress());
            return new Server(serverInfo, (Settings.CLOCK_TYPE == ClockType.LAMPORT) ? new Lamport() : null);
        } catch (UnknownHostException e) {
            logger.error("Could not start local server", e);
        }
        return null;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public Clock getClock() {
        return clock;
    }   

}