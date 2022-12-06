package machine;
import server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Machine {
    private static final Logger logger = LoggerFactory.getLogger(Machine.class);
    private Server server = Server.startLocalServer();

    public Machine(){        
        MachineProcess mp = new MachineProcess(server.getServerInfo());
        mp.start();
    }
    
    public static void main(String[] args) {
        logger.info("Hello World");
        logger.debug("Debug mode is on");
        logger.trace("Trace mode is on");
        new Machine();
    }
}
