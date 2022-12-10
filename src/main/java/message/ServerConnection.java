package message;

import java.net.Socket;

public class ServerConnection extends MessageContent{
    private Socket connectionSocket = null;

    public ServerConnection(Socket connectionSocket) {
        super(MessageType.SERVER_CONN);
        this.connectionSocket = connectionSocket;
    }

    public Socket getConnectionSocket() {
        return connectionSocket;
    }
    
}
