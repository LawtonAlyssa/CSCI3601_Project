package criticalSection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import criticalSection.file.FileInfo;
import criticalSection.file.FileRequest;
import criticalSection.file.FileSemaphore;
import message.CriticalSectionRequest;
import message.ServerMessage;
import process.QueueManager;
import server.ServerInfo;

public class CentralizedLockManager {
    private static final Logger logger = LoggerFactory.getLogger(CentralizedLockManager.class);
    private HashMap<String, BaseSemaphore> semaphoreMap = new HashMap<>();
    private Queue<ServerMessage> requestQueue = new LinkedBlockingQueue<>();
    private HashMap<ServerInfo, FileInfo> fileInfos = new HashMap<>();
    private File homeDir = null;
    private QueueManager queue = null;

    public CentralizedLockManager(File homeDir, QueueManager queue) {
        this.homeDir = homeDir;
        this.queue = queue;
    }

    public void addCriticalSectionRequest(ServerMessage msg) {
        requestQueue.add(msg);
    }

    public void handleRequest() {
        if (requestQueue.isEmpty()) return;

        ServerMessage msg = requestQueue.poll();

        CriticalSectionRequest csRequest = (CriticalSectionRequest)msg.getData();
        logger.debug("Received CS Request: " + csRequest.getCritSectType());
        
        CriticalSectionType csRequestType = csRequest.getCritSectType();

        if (csRequestType==CriticalSectionType.FILE) {
            if (!fileInfos.containsKey(csRequest.getServerInfo())) {
                fileInfos.put(csRequest.getServerInfo(), new FileInfo());
            }
            FileRequest fileRequest = (FileRequest)csRequest.getCritSect();
            // proccess
            handleFileRequest(fileRequest, fileInfos.get(csRequest.getServerInfo()), msg);

        } else {
            logger.warn("Request type not found: " + csRequestType);
        }

        logger.info("Completed handling request");
    }

    private void handleFileRequest(FileRequest fileRequest, FileInfo filePointer, ServerMessage serverMessage) {
        FileInfo fileInfo = fileRequest.getFileInfo();
        
        RequestType requestType = fileRequest.getRequestType();

        logger.debug("Handle CS File Request: " + requestType);
        
        String filePath = homeDir.getPath() + filePointer.getFilePath() + "/" + fileInfo.getFilePath();
        // fileInfo.setFilePath(filePath);
        
        if (!semaphoreMap.containsKey(filePath)) {
            logger.debug("First access in semaphore map: " + filePath);
            semaphoreMap.put(filePath, new FileSemaphore(filePath));    
        } 
        
        BaseSemaphore semaphoreVal = semaphoreMap.get(filePath);
        // fileInfo.setFilePath(originalFilePath);

        if (semaphoreVal instanceof FileSemaphore) {
            FileSemaphore fileSemaphore = (FileSemaphore)semaphoreVal;
            
            logger.info("Created HandleFileIOProcess");
            HandleFileIOProcess hfiop = new HandleFileIOProcess(queue, fileSemaphore, requestType, fileInfo, serverMessage);
            hfiop.start();
        } else {
            logger.error("Semaphore requested is not a file");
        }
    }

    public void close() {
        logger.warn("I/O Time | Read time = " + (FileSemaphore.readTimer/1000) + "[us] | Write time = " + (FileSemaphore.writeTimer/1000) + "[us]");
    }
    
}
