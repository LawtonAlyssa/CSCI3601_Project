package process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import message.Message;
import message.MessageContent;

public class QueueManager {
    private static final Logger logger = LoggerFactory.getLogger(QueueManager.class);
    private BlockingQueue<Message> msgQueue = new LinkedBlockingQueue<>();

    public void send(Message msg) {
        try {
            msgQueue.put(msg);
        } catch (InterruptedException e) {
            logger.error("Could not send message", e);
        }
    }

    public void send(MessageContent data) {
        send(new Message(data));
    }

    public Message receive() {
        return (msgQueue.isEmpty())? null : msgQueue.poll(); 
    }

}
