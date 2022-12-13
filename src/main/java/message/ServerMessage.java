package message;

import java.util.HashMap;
import server.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerMessage extends Message{
    private static final Logger logger = LoggerFactory.getLogger(ServerMessage.class);
    private ServerInfo source;
    private ServerInfo dest;
    private Messenger messenger;
    
    public ServerMessage(ServerInfo source, ServerInfo dest, MessageContent data) {
        super(data, true);
        this.source = source;
        this.dest = dest;
    }

    public void setMessenger(Messenger messenger) {
        this.messenger = messenger;
    }

    public Messenger getMessenger() {
        return messenger;
    }

    public ServerInfo getSource() {
        return source;
    }

    public ServerInfo getDest() {
        return dest;
    }

    public static ServerMessage toMessage(String msgStr) {
        HashMap<String, String> hm = Message.parseMessage(msgStr);
        ServerInfo source = ServerInfo.toMessage(hm.get("source"));
        ServerInfo dest = ServerInfo.toMessage(hm.get("dest"));
        MessageContent data = MessageContent.toMessage(hm.get("data"));
        
        return new ServerMessage(source, dest, data);
    }

    public void reply(MessageContent msg) {
        getMessenger().sendSocket(msg);
    }

    @Override
    public String toString() {
        return String.format("source:[%s]dest:[%s]%s", source, dest, super.toString());
    }

    public String toLog() {
        return String.format("Message | Source: %d | Dest: %d | Type: %s | Server", source.getServerId(), dest.getServerId(), getMessageType());
    }

}