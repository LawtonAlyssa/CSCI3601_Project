package server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CentralServerMain{
    private static final Logger logger = LoggerFactory.getLogger(CentralServerMain.class);

    public static void main(String[] args) {
        logger.info("Hello World");
        logger.debug("Debug mode is on");
        logger.trace("Trace mode is on");
         
        new CentralServer().run();
    }

}