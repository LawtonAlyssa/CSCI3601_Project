package message;

import java.util.ArrayList;
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

        logger.trace("Parsing Message Type: " + typeStr);
        
        MessageType type = MessageType.valueOf(typeStr);

        switch (type) {
            case CS_REQUEST:
                return CriticalSectionRequest.toMessage(msgStr);
            case CS_RESPONSE:
            case CS_EXIT:
                return CriticalSectionResponse.toMessage(msgStr);
            case CENTRAL_SERVER_HANDSHAKE:
                return CentralServerHandshake.toMessage(msgStr);
            case CLIENT_HANDSHAKE:
                return ClientHandshake.toMessage(msgStr);
            default:
                return new MessageContent(type);
        }
    }

    public static String arrayListToString(String label, ArrayList<?> arrList) {
        StringBuilder str = new StringBuilder();

        int len = (arrList==null)? 0 : arrList.size();
        str.append(String.format("%sCount:[%d]", label, len));

        for (int i = 0; i < len; i++) {
            str.append(String.format("%s%d:[%s]", label, i, arrList.get(i)));
        }
        
        return str.toString();
    }

    public static ArrayList<String> stringToArrayList(String label, String str) {
        HashMap<String, String> hm = Message.parseMessage(str);
        int len = Integer.parseInt(hm.get(label+"Count"));

        ArrayList<String> arrList = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            arrList.add(hm.get(label+i));
        }

        return arrList;
    }

    @Override
    public String toString() {
        return String.format("type:[%s]", type);
    }

    public MessageType getType() {
        return type;
    }

}
