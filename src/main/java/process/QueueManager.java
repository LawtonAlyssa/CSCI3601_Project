package process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import message.Message;
import server.ServerInfo;

public class QueueManager {
    private static final Logger logger = LoggerFactory.getLogger(QueueManager.class);
    private BlockingQueue<Message> msgQueueToMgr = null;
    private ConcurrentHashMap<ProcessInfo, QueuePair> msgQueuesFrMgr = new ConcurrentHashMap<>();

    public QueueManager(ServerInfo serverInfo) {
        this.msgQueueToMgr = new LinkedBlockingQueue<>();

        QueueManagerProcess qmp = new QueueManagerProcess(serverInfo, msgQueueToMgr, msgQueuesFrMgr);
        qmp.start();
    }

    public QueuePair addProcess(ProcessInfo process) {
        if (msgQueuesFrMgr.contains(process)) {
            return getQueuePair(process);
        }
        BlockingQueue<Message> msgQueueFrMgr = new LinkedBlockingQueue<Message>();
        QueuePair queuePair = new QueuePair(msgQueueToMgr, msgQueueFrMgr);
        msgQueuesFrMgr.put(process, queuePair);
        logger.debug("Added new process to QueueManager: " + process.getProcessId());
        return queuePair;
    }

    public QueuePair getQueuePair(ProcessInfo processInfo) {
        return msgQueuesFrMgr.getOrDefault(processInfo, null);
    }

}
