package message;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageContent {
    private static final Logger logger = LoggerFactory.getLogger(MessageContent.class);
    private MessageType type;
    
    public MessageContent(MessageType type) {
        this.type = type;
    }
    
    public static MessageContent toMessage(String msgStr) {
        HashMap<String, String> hm = Message.parseMessage(msgStr);
        String typeStr = hm.get("type");
        logger.debug("Parsing Message Type: " + typeStr);
        MessageType type = MessageType.valueOf(typeStr);
        if (type==MessageType.CS_REQUEST) {
            return CriticalSectionRequest.toMessage(msgStr);
        } 
        // logger.error("Could not find message type: " + type);
        return new MessageContent(type);
    }

    @Override
    public String toString() {
        return String.format("type:[%s]", type);
    }

    public MessageType getType() {
        return type;
    }
}
