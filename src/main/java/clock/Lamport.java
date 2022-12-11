package clock;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import message.Message;

public class Lamport extends Clock{
    private static final Logger logger = LoggerFactory.getLogger(Lamport.class);
    private int c = 0;

    public Lamport(int c) {
        super(ClockType.LAMPORT);
        this.c = c;
    }

    public Lamport() {
        super(ClockType.LAMPORT);
    }

    @Override
    public void newEventUpdate() {
        c++;
        logger.info("New event --> updated clock value to: " + c);
    }

    @Override
    public void receiveUpdate(Clock a) {
        if (a instanceof Lamport) {
            int prevC = c;
            c = Math.max(((Lamport)a).c, this.c) + 1;
            logger.info(String.format("Previous clock: %d Received clock: %d --> Current clock: %d", prevC, ((Lamport)a).c, c));
        } else {
            logger.error("Clock type not Lamport");
        }
    }

    public static Clock toMessage(String msgStr) {
        HashMap<String, String> hm = Message.parseMessage(msgStr);
        int c = Integer.parseInt(hm.get("c"));
        return new Lamport(c);
    }

    @Override
    public String toString() {
        return String.format("%sc:[%d]", super.toString(), c);
    }

    @Override
    public boolean isLessThan(Clock otherClock) {
        return this.c < ((Lamport)otherClock).c;
    }

    @Override
    public boolean isLessThanOrEqual(Clock otherClock) {
        return this.c <= ((Lamport)otherClock).c;

    }

    @Override
    public boolean isEqualTo(Clock otherClock) {
        return this.c == ((Lamport)otherClock).c;

    }

    @Override
    public boolean isConcurrent(Clock otherClock) {
        return false;

    }

}
