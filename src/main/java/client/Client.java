package client;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import message.Messenger;
import process.ProcessInfo;

public class Client extends Messenger{
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    
    public Client(Socket clientSocket, ProcessInfo processInfo) {
        super(clientSocket, processInfo);
    }
}