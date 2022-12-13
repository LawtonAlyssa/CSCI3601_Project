package process;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import clock.Clock;
import clock.ClockType;
import clock.Lamport;
import message.Message;
import message.Messenger;
import message.ServerMessage;
import message.UserMessage;
import server.ServerInfo;
import server.ServerProcess;
import settings.Settings;
import userInput.UserInputProcess;

public class Entity {
    private static final Logger logger = LoggerFactory.getLogger(Entity.class);
    private ServerInfo serverInfo = null;
    private ServerInfo centralServerInfo = null;
    private QueueManager queue = new QueueManager();
    private Clock clock = null;
    private ArrayList<Messenger> servers = new ArrayList<>();
    private File parentHomeDir = new File(Settings.PARENT_HOME_DIR);
    private File homeDir = null;
    private long userInputDelay = 0;
    private Queue<String> userInputQueue = new LinkedList<>();
    private boolean waitingForServer = true;

    public Entity() {
        createParentHomeDir();

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

        startUserInputProcess();
    }

    public void startServerProcess() {
        ServerProcess sp = new ServerProcess(queue);
        sp.start();
        logger.trace("Server is listening");
        System.out.print("                                          \r>");
    }

    public void startUserInputProcess() {
        UserInputProcess uip = new UserInputProcess(queue);
        uip.start();
        logger.trace("Keyboard is listening");
    }

    public void setWaitingForServer(boolean waitingForEditorToStart) {
        this.waitingForServer = waitingForEditorToStart;
    }

    public void setServerId(int serverId) {
        serverInfo.setServerId(serverId);

        createHomeDir();
    }

    public void createHomeDir() {
        setHomeDir(new File(getParentHomeDir(), String.format("home_%d/aos", getServerInfo().getServerId())));
    }

    public long getUserInputDelay() {
        return userInputDelay;
    }

    public void setUserInputDelay(long userInputDelay) {
        this.userInputDelay = userInputDelay;
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

    public File getParentHomeDir() {
        return parentHomeDir;
    }

    public Clock getClock() {
        return clock;
    }

    public File getHomeDir() {
        return homeDir;
    }

    public void setHomeDir(File homeDir) {
        this.homeDir = homeDir;
    }

    public void createParentHomeDir() {
        if (!parentHomeDir.exists()) {
            parentHomeDir.mkdirs();
            logger.info("Created parent home directory");
        }
    }

    public boolean handleServerMessage(ServerMessage msg) {
        switch (msg.getMessageType()) {
            // case :
            //     break;
            default:
                break;
        }

        return false;
    }

    public boolean handleProcessMessage(Message msg) {
        switch (msg.getMessageType()) {
            case USER_INPUT:
                String msgStr = ((UserMessage)msg.getData()).getUserInput();
                logger.debug("Adding user input to queue:" + msgStr);
                userInputQueue.add(msgStr);
            default:
                break;
        }

        return false;
    }

    public boolean handleUserInputQueue() {
        if (userInputQueue.isEmpty()) {
            return false;
        }
        if (System.currentTimeMillis() < userInputDelay || waitingForServer) {
            return false;
        }
        
        String msgStr = userInputQueue.poll();
        logger.debug("Handling User Input from Queue: " + msgStr);
        String[] input = msgStr.split(" ");
        input[0] = input[0].toLowerCase();

        return handleUserInput(input);
    }

    public boolean handleUserInput(String[] tokenStr) {
        switch (tokenStr[0]) {
            case "exit":
                logger.info("User terminated Server");
                return true;
            case "delay":
                long dt = Long.parseLong(tokenStr[1]);
                logger.info("delay starts: " + dt);
                setUserInputDelay(System.currentTimeMillis() + dt);
                break;
            default:
                break;
        }

        return false;
    }

    public ArrayList<ServerInfo> getServers() {
        ArrayList<ServerInfo> serverList = new ArrayList<>();

        for (Messenger server : servers) {
            serverList.add(server.getDestServerInfo());
        }
        return serverList;
    }

    public void addServer(Messenger messenger) {
        servers.add(messenger);
    }

    public boolean receive() {
        Message msg = getQueue().receive();

        if (msg==null) return false;

        logger.debug("Queue Received " + msg.toLog());
        
        if (msg.isServerMessage()) return handleServerMessage((ServerMessage)msg);
        // boolean result = handleProcessMessage(msg);
        // logger.debug("receive() result=" + result);
        // return result;
        return handleProcessMessage(msg);
    }

    public boolean update() {
        for (Messenger server : servers) {
            if (!server.isAlive()) {
                servers.remove(server);
                break;
            }
        }
        
        return false;
    }

    public void run() {
        while (true) {
            if (receive()) break;
            if (handleUserInputQueue()) break;
            if (update()) break;
        }
        close();
    }

    public void close() {
        logger.info("Terminating...");

        for (Messenger server : servers) {
            server.close();
        }
    }
    
}
