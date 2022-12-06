package process;

import java.util.HashMap;
import java.util.HashSet;
import message.Message;
import server.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessInfo {
    private static final Logger logger = LoggerFactory.getLogger(ProcessInfo.class);
    private ServerInfo serverInfo = null;
    private int processId;
    private static HashSet<Integer> processIds = new HashSet<>();

    public ProcessInfo(ServerInfo serverInfo, int processId) {
        this.serverInfo = serverInfo;
        if (processIds.contains(processId)) {
            logger.error("Process id " + processId + " already exists in server: " + serverInfo.getIpAddress());
        }
        this.processId = processId;
        processIds.add(processId);
    }

    public ProcessInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
        generateProcessId();
    }

    private void generateProcessId() {
        int maxCount = 1000;
        do {
            processId = (int)(Math.random() * 10000) + 1000;
            maxCount--;
        } while (maxCount > 0 && processIds.contains(processId));
        if (maxCount <= 0) {
            logger.error("Could not generate unique process id");
        } else {
            processIds.add(processId);
        }
    }

    public int getProcessId() {
        return processId;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public static ProcessInfo toMessage(String msgStr) {
        HashMap<String, String> hm = Message.parseMessage(msgStr);
        ServerInfo serverInfo = ServerInfo.toMessage(hm.get("serverInfo"));
        int processId = Integer.parseInt(hm.get("processId"));
        return new ProcessInfo(serverInfo, processId);
    }

    @Override
    public String toString() {
        return String.format("serverInfo:[%s]processId:[%d]", serverInfo, processId);
    }
}
