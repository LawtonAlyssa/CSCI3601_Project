package process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.BlockingQueue;
import message.Message;
import message.MessageContent;
import server.ServerInfo;

public abstract class Process extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(Process.class);
    private ProcessInfo processInfo;
    private BlockingQueue<Message> msgQueue = null;
    private ProcessInfo parentProcessInfo = null;

    public Process(ProcessInfo parentProcessInfo, BlockingQueue<Message> msgQueue) {
        this.processInfo = new ProcessInfo(parentProcessInfo.getServerInfo());
        this.msgQueue = msgQueue;
        this.parentProcessInfo = parentProcessInfo;
    }

    public Process(ServerInfo serverInfo) {
        this.processInfo = new ProcessInfo(serverInfo);
    }

    public Process() {
        this.processInfo = new ProcessInfo();
    }

    public void setServerInfo(ServerInfo serverInfo) {
        processInfo.setServerInfo(serverInfo);
    }

    public void send(ProcessInfo source, ProcessInfo dest, MessageContent data) {
        try {
            msgQueue.put(new Message(source, dest, data));
        } catch (InterruptedException e) {
            logger.error("Could not send message", e);
        }
    }

    public void sendToParentProcess(MessageContent data) {
        send(processInfo, parentProcessInfo, data);
    }

    public ProcessInfo getProcessInfo() {
        return processInfo;
    }

    public ServerInfo getServerInfo() {
        return processInfo.getServerInfo();
    }

    public void joinProcess() {
        try {
            join();
        } catch (InterruptedException e) {
            logger.error("Process was interrupted ", e);
        }
    }
}
