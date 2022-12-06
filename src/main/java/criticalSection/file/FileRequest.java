package criticalSection.file;

import criticalSection.CriticalSectionInfo;
import criticalSection.RequestType;

public class FileRequest extends CriticalSectionInfo{
    private FileInfo fileInfo;
    private RequestType requestType;

    public FileRequest(FileInfo fileInfo, RequestType requestType) {
        this.fileInfo = fileInfo;
        this.requestType = requestType;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public RequestType getRequestType() {
        return requestType;
    }   
}
