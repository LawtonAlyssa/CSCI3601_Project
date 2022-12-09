package process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import message.Message;
import server.ServerInfo;

public class QueueManagerProcess extends Process{
    private static final Logger logger = LoggerFactory.getLogger(QueueManagerProcess.class);
    private BlockingQueue<Message> msgQueueToMgr = null;
    private ConcurrentHashMap<ProcessInfo, QueuePair> msgQueuesFrMgr = new ConcurrentHashMap<>();

    public QueueManagerProcess(ServerInfo serverInfo, BlockingQueue<Message> msgQueueToMgr, ConcurrentHashMap<ProcessInfo, QueuePair> msgQueuesFrMgr) {
        super(serverInfo);
        
        this.msgQueueToMgr = msgQueueToMgr;
        this.msgQueuesFrMgr = msgQueuesFrMgr;
    }

    @Override
    public void run() {
        while (true) {
            update();
        }
    }

    public void update() {
        if (msgQueueToMgr.isEmpty()) return;
        Message msg = msgQueueToMgr.poll();
        ProcessInfo dest = msg.getDest();

        QueuePair queuePair = msgQueuesFrMgr.get(dest);
        if (queuePair==null) {
            logger.warn("QueuePair for " + dest.getProcessId() + " is null");
            return;
        }
        queuePair.sendFrManager(msg);
    }
    
}
