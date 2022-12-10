package criticalSection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import criticalSection.file.FileContent;
import criticalSection.file.FileInfo;
import criticalSection.file.FileRequest;
import criticalSection.file.FileSemaphore;
import criticalSection.file.FileWriteInfo;
import message.CriticalSectionRequest;
import server.ServerInfo;

public class CentralizedLockManager {
    private static final Logger logger = LoggerFactory.getLogger(CentralizedLockManager.class);
    private HashMap<String, BaseSemaphore> semaphoreMap = new HashMap<>();
    private Queue<CriticalSectionRequest> requestQueue = new LinkedList<>();
    private HashMap<ServerInfo, FileInfo> fileInfos = new HashMap<>();
    private File homeDir = null;

    public CentralizedLockManager(File homeDir) {
        this.homeDir = homeDir;
    }

    public void addCriticalSectionRequest(CriticalSectionRequest csRequest) {
        logger.debug("Received CS Request: " + csRequest.getCritSectType());
        requestQueue.add(csRequest);
    }

    public void handleRequest() {
        if (requestQueue.isEmpty()) return;

        CriticalSectionRequest csRequest = requestQueue.poll();

        CriticalSectionType csRequestType = csRequest.getCritSectType();

        if (csRequestType==CriticalSectionType.FILE) {
            if (!fileInfos.containsKey(csRequest.getServerInfo())) {
                fileInfos.put(csRequest.getServerInfo(), new FileInfo());
            }
            handleFileRequest((FileRequest)csRequest.getCritSect(), fileInfos.get(csRequest.getServerInfo()));
        } else {
            logger.warn("Request type not found: " + csRequestType);
        }
    }

    private FileWriteInfo handleFileRequest(FileRequest fileRequest, FileInfo filePointer) {
        FileInfo fileInfo = fileRequest.getFileInfo();
        RequestType requestType = fileRequest.getRequestType();

        logger.debug("Handle CS File Request: " + requestType);
        
        String filePath = homeDir.getPath() + filePointer.getFilePath() + "/" + fileInfo.getFilePath();
        fileInfo.setFilePath(filePath);
        
        if (!semaphoreMap.containsKey(filePath)) {
            logger.debug("File not found in semaphore map: " + filePath);
            semaphoreMap.put(filePath, new FileSemaphore(fileInfo));    
        } 
        
        BaseSemaphore semaphoreVal = semaphoreMap.get(filePath);
        

        if (semaphoreVal instanceof FileSemaphore) {
            FileSemaphore fileSemaphore = (FileSemaphore)semaphoreVal;
            switch (requestType) {
                case READ:
                    return new FileWriteInfo(filePath, fileSemaphore.read());
                case REQUEST_WRITE:   
                    fileSemaphore.requestWrite();
                case WRITE:    
                    FileContent content = fileInfo.getFileContent();
                    // content.setFilePath(filePath);
                    fileSemaphore.write(content);
                default:
                    break;
            }
        } else {
            logger.error("Semaphore requested is not a file");
        }

        return null;
    }
    
}
