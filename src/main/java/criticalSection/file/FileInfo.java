package criticalSection.file;

import java.util.HashMap;
import message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileInfo {
    private static final Logger logger = LoggerFactory.getLogger(FileInfo.class);
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
        if (hasContent)  return FileContentInfo.toMessage(msgStr);
        String filePath = hm.get("filePath");
        return new FileInfo(filePath, hasContent);
    }

    @Override
    public String toString() {
        return String.format("filePath:[%s]hasContent:[%b]", filePath, hasContent);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
        logger.debug("FileInfo hash: " + result);

        return result;
    }
    
}
