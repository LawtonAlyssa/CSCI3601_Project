package criticalSection.file;

import java.util.HashMap;
import message.Message;

public class FileInfo {
    private String filePath = "/";
    private boolean hasContent = false;

    public FileInfo(String filePath, boolean hasContent) {
        this.filePath = filePath;
        this.hasContent = hasContent;
    }

    public FileInfo() {
        
    }

    public String getFilePath() {
        return filePath;
    }

    public FileContent getFileContent() {
        return null;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public static FileInfo toMessage(String msgStr) {
        HashMap<String, String> hm = Message.parseMessage(msgStr);
        boolean hasContent = Boolean.parseBoolean(hm.get("hasContent"));
        if (hasContent)  return FileWriteInfo.toMessage(msgStr);
        String filePath = hm.get("filePath");
        return new FileInfo(filePath, hasContent);
    }

    @Override
    public String toString() {
        return String.format("filePath:[%s]hasContent:[%b]", filePath, hasContent);
    }

}
