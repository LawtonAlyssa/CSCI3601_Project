package process;

import java.util.HashMap;
import java.util.HashSet;
import message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessInfo {
    private static final Logger logger = LoggerFactory.getLogger(ProcessInfo.class);
    private int processId;
    private static HashSet<Integer> processIds = new HashSet<>();

    public ProcessInfo(int processId) {
        if (processIds.contains(processId)) {
            logger.error("Process id " + processId + " already exists ");
        }
        this.processId = processId;
        processIds.add(processId);
    }

    public ProcessInfo() {
        generateProcessId();
    }

    private void generateProcessId() {
        int maxCount = 1000;
        do {
            processId = (int)(Math.random() * 10000) + 1000;
            maxCount--;
        } while (maxCount > 0 && processIds.contains(processId));
        if (maxCount <= 0) {
            logger.error("Could not generate unique process id");
        } else {
            processIds.add(processId);
        }
    }

    public int getProcessId() {
        return processId;
    }

    public static ProcessInfo toMessage(String msgStr) {
        HashMap<String, String> hm = Message.parseMessage(msgStr);
        int processId = Integer.parseInt(hm.get("processId"));
        return new ProcessInfo(processId);
    }

    @Override
    public String toString() {
        return String.format("processId:[%d]", processId);
    }

}
