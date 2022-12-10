package process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import message.Message;
import message.MessageContent;

public abstract class Process extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(Process.class);
    private ProcessInfo processInfo = new ProcessInfo();
    private QueueManager queue = null;

    public Process(QueueManager queue) {
        this.queue = queue;
    }

    public ProcessInfo getProcessInfo() {
        return processInfo;
    }

    public void send(MessageContent data) {
        queue.send(data);
    }

    public void send(Message data) {
        queue.send(data);
    }

}
