package server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import settings.Settings;

public class CoordinatorServer{
    private static final Logger logger = LoggerFactory.getLogger(CoordinatorServer.class);

    public static void main(String[] args) {
        logger.info("Hello World");
        logger.debug("Debug mode is on");
        logger.trace("Trace mode is on");
         
        UpdateServerProcess usp = new UpdateServerProcess(Settings.SERVER_COORD_ID);

        usp.start();
        usp.joinProcess();
    }
}