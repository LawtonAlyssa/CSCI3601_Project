package userInput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import message.UserMessage;
import process.Process;
import process.QueueManager;

public class UserInputProcess extends Process{
    private static final Logger logger = LoggerFactory.getLogger(UserInputProcess.class);
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    public UserInputProcess(QueueManager queue) {
        super(queue);
    }

    @Override
    public void run() {
        while (true) {
            getUserInput();
        }
    }

    public void getUserInput() {
        try {
            String input = br.readLine();

            if (input==null) return;

            logger.info("Input sent: " + input);

            send(new UserMessage(input));
            
            System.out.print("                                          \r>");
        } catch (IOException e) {
            logger.error("Could not read standard in", e);
        }
    }
    
}
