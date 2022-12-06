package message;

import java.util.HashMap;
import process.ProcessInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Message {
    private static final Logger logger = LoggerFactory.getLogger(Message.class);
    private ProcessInfo source;
    private ProcessInfo dest;
    private MessageContent data;
    
    public Message(ProcessInfo source, ProcessInfo dest, MessageContent data) {
        this.source = source;
        this.dest = dest;
        this.data = data;
    }

    public ProcessInfo getSource() {
        return source;
    }

    public ProcessInfo getDest() {
        return dest;
    }

    public MessageContent getData() {
        return data;
    }

    public MessageType getMessageType() {
        return data.getType();
    }

    public static Message toMessage(String msgStr) {
        HashMap<String, String> hm = Message.parseMessage(msgStr);
        ProcessInfo source = ProcessInfo.toMessage(hm.get("source"));
        ProcessInfo dest = ProcessInfo.toMessage(hm.get("dest"));
        MessageContent data = MessageContent.toMessage(hm.get("data"));
        
        return new Message(source, dest, data);
    }

    public static HashMap<String, String> parseMessage(String msgStr) {
        HashMap<String, String> hm = new HashMap<>();
        int idx = 0;

        while(idx < msgStr.length()) {
            int endIdx = Message.getLabelEndIdx(msgStr, idx);
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
        return String.format("source:[%s]dest:[%s]data:[%s]", source, dest, data);
    }
}