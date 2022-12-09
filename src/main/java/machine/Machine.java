package machine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Machine {
    private static final Logger logger = LoggerFactory.getLogger(Machine.class);
    
    public static void main(String[] args) {
        logger.info("Hello World");
        logger.debug("Debug mode is on");
        logger.trace("Trace mode is on");
        
        MachineProcess mp = new MachineProcess();
        
        mp.start();
        mp.joinProcess();
    }
    
}
