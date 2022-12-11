package message;

import java.util.HashMap;
import criticalSection.file.FileInfo;

public class CriticalSectionResponse extends MessageContent {
    private FileInfo fileInfo = null;

    public CriticalSectionResponse(MessageType type, FileInfo fileInfo) {
        super(type);

        this.fileInfo = fileInfo;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }
    
    public static CriticalSectionResponse toMessage(String msgStr) {
        HashMap<String, String> hm = Message.parseMessage(msgStr);
        MessageType type = MessageType.valueOf(hm.get("type"));
        FileInfo fileInfo = FileInfo.toMessage(hm.get("fileInfo"));
        return new CriticalSectionResponse(type, fileInfo);
    }

    @Override
    public String toString() {
        return String.format("%sfileInfo:[%s]", super.toString(), fileInfo);
    }
 
}
