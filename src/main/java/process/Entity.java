package process;

import server.ServerInfo;

public class Entity {
    ProcessInfo processInfo = null;
    QueuePair queuePair = null;

    public Entity(ProcessInfo processInfo, QueueManager queueManager) {
        this.processInfo = processInfo;
        setQueuePair(queueManager);
    }

    public Entity(ProcessInfo processInfo) {
        this.processInfo = processInfo;
    }

    public void setQueuePair(QueueManager queueManager) {
        this.queuePair = queueManager.addProcess(processInfo);
    }

    public ProcessInfo getProcessInfo() {
        return processInfo;
    }
    
    public ServerInfo getServerInfo() {
        return processInfo.getServerInfo();
    }

    public QueuePair getQueuePair() {
        return queuePair;
    }
    
}
