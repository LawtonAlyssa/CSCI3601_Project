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
import criticalSection.file.FileContentInfo;
import message.CriticalSectionRequest;
import message.ServerMessage;
import server.ServerInfo;

public class CentralizedLockManager {
    private static final Logger logger = LoggerFactory.getLogger(CentralizedLockManager.class);
    private HashMap<String, BaseSemaphore> semaphoreMap = new HashMap<>();
    private Queue<ServerMessage> requestQueue = new LinkedList<>();
    private HashMap<ServerInfo, FileInfo> fileInfos = new HashMap<>();
    private File homeDir = null;

    public CentralizedLockManager(File homeDir) {
        this.homeDir = homeDir;
    }

    public void addCriticalSectionRequest(ServerMessage msg) {
        requestQueue.add(msg);
    }

    public ServerMessage handleRequest() {
        if (requestQueue.isEmpty()) return null;

        ServerMessage msg = requestQueue.poll();

        CriticalSectionRequest csRequest = (CriticalSectionRequest)msg.getData();
        logger.debug("Received CS Request: " + csRequest.getCritSectType());
        
        CriticalSectionType csRequestType = csRequest.getCritSectType();

        if (csRequestType==CriticalSectionType.FILE) {
            if (!fileInfos.containsKey(csRequest.getServerInfo())) {
                fileInfos.put(csRequest.getServerInfo(), new FileInfo());
            }
            FileRequest fileRequest = (FileRequest)csRequest.getCritSect();
            FileContentInfo fcInfo = handleFileRequest(fileRequest, fileInfos.get(csRequest.getServerInfo()));

            if (fcInfo != null) {
                fileRequest.setFileInfo(fcInfo);
            }

        } else {
            logger.warn("Request type not found: " + csRequestType);
        }

        return msg;
    }

    private FileContentInfo handleFileRequest(FileRequest fileRequest, FileInfo filePointer) {
        FileInfo fileInfo = fileRequest.getFileInfo();
        String originalFilePath = fileInfo.getFilePath();
        
        RequestType requestType = fileRequest.getRequestType();

        logger.debug("Handle CS File Request: " + requestType);
        
        String filePath = homeDir.getPath() + filePointer.getFilePath() + "/" + fileInfo.getFilePath();
        // fileInfo.setFilePath(filePath);
        
        if (!semaphoreMap.containsKey(filePath)) {
            logger.debug("File not found in semaphore map: " + filePath);
            semaphoreMap.put(filePath, new FileSemaphore(filePath));    
        } 
        
        BaseSemaphore semaphoreVal = semaphoreMap.get(filePath);
        // fileInfo.setFilePath(originalFilePath);

        if (semaphoreVal instanceof FileSemaphore) {
            FileSemaphore fileSemaphore = (FileSemaphore)semaphoreVal;
            switch (requestType) {
                case REQUEST_WRITE:   
                //     fileSemaphore.requestWrite();
                case READ:
                    return new FileContentInfo(originalFilePath, fileSemaphore.read());
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

    public void close() {
        logger.info("I/O Time | Read time = " + (FileSemaphore.readTimer/1000) + "[us] | Write time = " + (FileSemaphore.writeTimer/1000) + "[us]");
    }
    
}
