package criticalSection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import criticalSection.file.FileInfo;
import criticalSection.file.FileRequest;
import criticalSection.file.FileSemaphore;
import criticalSection.file.FileWriteInfo;
import message.CriticalSectionRequest;

public class CentralizedLockManager {
    private static final Logger logger = LoggerFactory.getLogger(CentralizedLockManager.class);
    Queue<CriticalSectionRequest> requestQueue = new LinkedList<>();
    HashMap<String, BaseSemaphore> semaphoreMap = new HashMap<>();

    public void addCriticalSectionRequest(CriticalSectionRequest csRequest) {
        requestQueue.add(csRequest);
    }

    public void handleRequest() {
        if (requestQueue.isEmpty()) return;

        CriticalSectionRequest csRequest = requestQueue.poll();

        CriticalSectionType csRequestType = csRequest.getCritSectType();

        if (csRequestType==CriticalSectionType.FILE) {
            handleFileRequest((FileRequest)csRequest.getCritSect());
        } else {
            logger.warn("Request type not found: " + csRequestType);
        }
    }

    private FileWriteInfo handleFileRequest(FileRequest fileRequest) {
        FileInfo fileInfo = fileRequest.getFileInfo();
        RequestType requestType = fileRequest.getRequestType();

        if (!semaphoreMap.containsKey(fileInfo.getFilePath())) {
            semaphoreMap.put(fileInfo.getFilePath(), new FileSemaphore(fileInfo));    
        } 

        BaseSemaphore semaphoreVal = semaphoreMap.get(fileInfo.getFilePath());

        if (semaphoreVal instanceof FileSemaphore) {
            FileSemaphore fileSemaphore = (FileSemaphore)semaphoreVal;
            switch (requestType) {
                case READ:
                    return new FileWriteInfo(fileInfo.getFilePath(), fileSemaphore.read());
                case REQUEST_WRITE:   
                    fileSemaphore.requestWrite();
                case WRITE:    
                    fileSemaphore.write(fileInfo.getFileContent());
                default:
                    break;
            }
        } else {
            logger.error("Semaphore requested is not a file");
        }

        return null;
    }
    
}
