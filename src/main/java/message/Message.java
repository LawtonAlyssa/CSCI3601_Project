package message;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Message {
    private static final Logger logger = LoggerFactory.getLogger(Message.class);
    private MessageContent data;
    private boolean isServerMessage = false;
    
    public Message(MessageContent data) {
        this.data = data;
    }

    protected Message(MessageContent data, boolean isServerMessage) {
        this.data = data;
        this.isServerMessage = isServerMessage;
    }

    public boolean isServerMessage() {
        return isServerMessage;
    }

    public MessageContent getData() {
        return data;
    }

    public MessageType getMessageType() {
        return data.getType();
    }

    public static Message toMessage(String msgStr) {
        HashMap<String, String> hm = Message.parseMessage(msgStr);
        MessageContent data = MessageContent.toMessage(hm.get("data"));
        
        return new Message(data);
    }

    public static HashMap<String, String> parseMessage(String msgStr) {
        HashMap<String, String> hm = new HashMap<>();
        int idx = 0;

        while(idx < msgStr.length()) {
            int endIdx = Message.getLabelEndIdx(msgStr, idx);
            if (endIdx <= idx) {
                logger.trace("Parsing msgStr: " + msgStr + " idx: " + idx + " endIdx: " + endIdx);
            }
            String label = msgStr.substring(idx, endIdx);
            idx = endIdx + 1;
            endIdx = Message.getBodyEndIdx(msgStr, idx);
            String body = msgStr.substring(idx + 1, endIdx);
            logger.trace("Parsed - label: " + label + " body: " + body);
            idx = endIdx + 1;
            hm.put(label, body);
        }

        return hm;
    }

    public static int getLabelEndIdx(String msgStr, int startIdx) {
        return msgStr.indexOf(":", startIdx);
    }

    public static int getBodyEndIdx(String msgStr, int startIdx) {
        int openBrackets = 0;
        for (int i = startIdx; i < msgStr.length(); i++) {
            switch (msgStr.charAt(i)) {
                case '[':
                    openBrackets++;
                    break;
                case ']':
                    openBrackets--;
                    if (openBrackets==0) {
                        return i;
                    }
                default:
                    break;
            }
        }
        return msgStr.length();
    }

    @Override
    public String toString() {
        return String.format("data:[%s]", data);
    }

    public String toLog() {
        return String.format("Message | Type: %s | %s", getMessageType(), (isServerMessage()) ? "Server" : "Process");
    }

}