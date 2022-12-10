package machine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MachineMain {
    private static final Logger logger = LoggerFactory.getLogger(MachineMain.class);
    
    public static void main(String[] args) {
        logger.info("Hello World");
        logger.debug("Debug mode is on");
        logger.trace("Trace mode is on");
                
        new Machine().run();
        System.exit(0);
        
    }
    
}
