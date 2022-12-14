package criticalSection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import criticalSection.file.FileContent;
import criticalSection.file.FileContentInfo;
import criticalSection.file.FileInfo;
import criticalSection.file.FileRequest;
import criticalSection.file.FileSemaphore;
import message.CriticalSectionRequest;
import message.FileIOResult;
import message.ServerMessage;
import process.Process;
import process.QueueManager;

public class HandleFileIOProcess extends Process{
    private static final Logger logger = LoggerFactory.getLogger(HandleFileIOProcess.class);
    private FileSemaphore fileSemaphore = null;
    private RequestType requestType = null;
    private FileInfo fileInfo = null;
    private ServerMessage serverMessage = null;


    public HandleFileIOProcess(QueueManager queue, FileSemaphore fileSemaphore, RequestType requestType, FileInfo fileInfo, ServerMessage serverMessage) {
        super(queue);
        
        this.fileSemaphore = fileSemaphore;
        this.requestType = requestType;
        this.fileInfo = fileInfo;
        this.serverMessage = serverMessage;
    }

    public void run() {
        logger.info("start handling file io");
        FileContentInfo fileContentInfo = null;

        switch (requestType) {
            case REQUEST_WRITE:   
            case READ:
                fileContentInfo = new FileContentInfo(fileInfo.getFilePath(), fileSemaphore.read());
            case WRITE:    
                FileContent content = fileInfo.getFileContent();
                fileSemaphore.write(content);
            default:
                break;
        }

        CriticalSectionRequest csRequest = (CriticalSectionRequest)serverMessage.getData();
        FileRequest fileRequest = (FileRequest)csRequest.getCritSect();
        
        
        if (fileContentInfo != null) {
            fileRequest.setFileInfo(fileContentInfo);
        }
        
        send(new FileIOResult(serverMessage));
        logger.info("ends handle file io");
    }
    
}

