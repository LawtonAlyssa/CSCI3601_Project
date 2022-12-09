package process;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import message.Message;
import message.MessageContent;
import server.ServerInfo;

public abstract class Process extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(Process.class);
    private ProcessInfo processInfo = null;
    private QueuePair queuePair = null;
    private HashMap<ProcessCommType, ProcessInfo> destProcessMap = new HashMap<>();

    public Process(ServerInfo serverInfo, QueueManager queueManager) {
        this.processInfo = new ProcessInfo(serverInfo);
        this.queuePair = queueManager.addProcess(processInfo);
    }

    public Process(ServerInfo serverInfo) {
        this.processInfo = new ProcessInfo(serverInfo);
    }

    public Process(int processId) {
        this.processInfo = new ProcessInfo(processId);
    }

    public void setServerInfo(ServerInfo serverInfo) {
        processInfo.setServerInfo(serverInfo);
    }

    public ProcessInfo getProcessInfo() {
        return processInfo;
    }

    public ServerInfo getServerInfo() {
        return processInfo.getServerInfo();
    }

    public void addDestProcess(ProcessCommType label, ProcessInfo processInfo) {
        destProcessMap.put(label, processInfo);
    }

    public void send(ProcessCommType dest, MessageContent data) {
        queuePair.sendToManager(getProcessInfo(), destProcessMap.get(dest), data);
    }

    public void send(Message data) {
        queuePair.sendToManager(data);
    }

    public Message receive() {
        return queuePair.receiveFrManager();
    }

    public void joinProcess() {
        try {
            join();
        } catch (InterruptedException e) {
            logger.error("Process was interrupted ", e);
        }
    }

}
