package process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.BlockingQueue;
import message.Message;
import message.MessageContent;

public class QueuePair {
    private static final Logger logger = LoggerFactory.getLogger(QueuePair.class);
    private BlockingQueue<Message> msgQueueToMgr = null;
    private BlockingQueue<Message> msgQueueFrMgr = null;

    public QueuePair(BlockingQueue<Message> msgQueueToMgr, BlockingQueue<Message> msgQueueFrMgr) {
        this.msgQueueToMgr = msgQueueToMgr;
        this.msgQueueFrMgr = msgQueueFrMgr;
    }

    public void sendToManager(Message msg) {
        try {
            msgQueueToMgr.put(msg);
        } catch (InterruptedException e) {
            logger.error("Could not send message", e);
        }
    }

    public void sendToManager(ProcessInfo source, ProcessInfo dest, MessageContent data) {
        sendToManager(new Message(source, dest, data));
    }

    public Message receiveFrManager() {
        return (msgQueueFrMgr.isEmpty())? null : msgQueueFrMgr.poll(); 
    }

    public void sendFrManager(Message msg) {
        try {
            msgQueueFrMgr.put(msg);
        } catch (InterruptedException e) {
            logger.error("Could not send message", e);
        }
    }

    public void sendFrManager(ProcessInfo source, ProcessInfo dest, MessageContent data) {
        sendFrManager(new Message(source, dest, data));
    }

}
