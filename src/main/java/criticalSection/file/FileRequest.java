package criticalSection.file;

import java.util.HashMap;
import criticalSection.CriticalSectionInfo;
import criticalSection.CriticalSectionType;
import criticalSection.RequestType;
import message.Message;

public class FileRequest extends CriticalSectionInfo{
    private FileInfo fileInfo;
    private RequestType requestType;

    public FileRequest(FileInfo fileInfo, RequestType requestType) {
        super(CriticalSectionType.FILE);
        this.fileInfo = fileInfo;
        this.requestType = requestType;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public RequestType getRequestType() {
        return requestType;
    }   

    public static FileRequest toMessage(String msgStr) {
        HashMap<String, String> hm = Message.parseMessage(msgStr);
        FileInfo fileInfo = FileInfo.toMessage(hm.get("fileInfo"));
        RequestType requestType = RequestType.valueOf(hm.get("requestType"));
        return new FileRequest(fileInfo, requestType);
    }

    @Override
    public String toString() {
        return String.format("%sfileInfo:[%s]requestType:[%s]", super.toString(), fileInfo, requestType);
    }
}
