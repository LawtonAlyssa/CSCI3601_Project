package machine;

import client.Client;
import message.Message;
import message.MessageContent;
import message.MessageType;
import message.ServerHandshake;
import process.Process;
import process.ProcessInfo;
import server.Server;
import server.ServerInfo;
import settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

 /**
 * handshake only
 */
public class MachineProcess extends Process{
    private static final Logger logger = LoggerFactory.getLogger(MachineProcess.class);
    private Server server = null;
    private Client coordClient = null;
    private ServerInfo coordServerInfo = null;

    public MachineProcess() {
        super();

        this.server = Server.startLocalServer(getProcessInfo());
        setServerInfo(this.server.getServerInfo());
        coordServerInfo = new ServerInfo(Settings.SERVER_COORD_IP_ADDR, Settings.SERVER_COORD_ID);
        
        try {
            coordClient = new Client(new Socket(coordServerInfo.getIpAddress(), Settings.SERVER_COORD_PORT_NUM), getProcessInfo());
        } catch (UnknownHostException e) {
            logger.error("Cannot find host: " + coordServerInfo.getIpAddress(), e);
        }
         catch (IOException e) {
            logger.error("Couldn't get I/O for the connection to: " + coordServerInfo.getIpAddress(), e);
        }
    }
   
    @Override
    public void run() {
        while (!receiveServerHandshake()) {}
        while (true) {
            server.UpdateMachineConnections();
        }
    }
    
    public boolean receiveServerHandshake() {
        Message msg = coordClient.receive();
        if (msg==null) return false;
        logger.debug("Received message with message type: " + msg.getMessageType());
        if (msg.getMessageType()==MessageType.SERVER_HANDSHAKE) {
            ServerHandshake sh = (ServerHandshake)msg.getData();
            logger.info("Received handshake from Server " + msg.getSource().getServerInfo().getServerId());
            logger.info("Assigned server id:" + sh.getServerId());
            sendClientHandshake();
            return true;
        }
        return false;
    }

    public void sendClientHandshake() {
        ProcessInfo dest = new ProcessInfo(coordServerInfo);
        MessageContent msg = new MessageContent(MessageType.CLIENT_HANDSHAKE);
        coordClient.send(dest, msg);
        logger.info("Sent client handshake");
    }
    
}
