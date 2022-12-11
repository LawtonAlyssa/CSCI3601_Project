package criticalSection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.LinkedList;

public class Semaphore {
    private static final Logger logger = LoggerFactory.getLogger(Semaphore.class);
    private int value = 0;
    private LinkedList<Thread> waitingQueue;
    
    public Semaphore(int value) {
        this.value = value;
    }

    public Semaphore() {
        
    }

    public void waitSemaphore() {
        value--;

        if (value < 0) {
            waitingQueue.add(Thread.currentThread());
            block();
        }
    }

    private void block() {
        try {
            wait();
        } catch (Exception e) {
            logger.error("Could not block semaphore", e);
        }
    }

    public void signal() {
        value++;

        if (value <= 0) {
            Thread p = waitingQueue.removeFirst();
            wakeUp(p);
        }
    }

    private void wakeUp(Thread p) {
        p.notify();
    }
}
