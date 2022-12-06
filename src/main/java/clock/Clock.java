package clock;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import message.Message;

public abstract class Clock {
    private static final Logger logger = LoggerFactory.getLogger(Clock.class);
    private ClockType clockType = null;
    
    public Clock(ClockType clockType) {
        this.clockType = clockType;
    }

    public abstract void newEventUpdate();

    public abstract void receiveUpdate(Clock a);

    public static Clock toMessage(String msgStr) {
        HashMap<String, String> hm = Message.parseMessage(msgStr);
        ClockType clockType = ClockType.valueOf(hm.get("clockType"));
        if (clockType==ClockType.LAMPORT) {
            return Lamport.toMessage(msgStr);
        } 
        logger.error("Could not find clock type: " + clockType);
        return null;
    }

    @Override
    public String toString() {
        return String.format("type:[%s]", clockType);
    }
}
