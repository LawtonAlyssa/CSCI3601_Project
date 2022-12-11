package criticalSection.file;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import message.Message;

public class FileContentInfo extends FileInfo{
    private static final Logger logger = LoggerFactory.getLogger(FileContentInfo.class);
    private FileContent fileContent;

    public FileContentInfo(String filePath, FileContent fileContent) {
        super(filePath, true);
        this.fileContent = fileContent;
    }

    public FileContent getFileContent() {
        return fileContent;
    }

    public static FileContentInfo toMessage(String msgStr) {
        HashMap<String, String> hm = Message.parseMessage(msgStr);
        String filePath = hm.get("filePath");
        FileContent fileContent = FileContent.toMessage(hm.get("fileContent"));
        return new FileContentInfo(filePath, fileContent);
    }

    @Override
    public String toString() {
        return String.format("%sfileContent:[%s]", super.toString(), fileContent);
    }

    @Override
    public int hashCode() {
        logger.debug("FileContentInfo hash: " + super.hashCode());
        return super.hashCode();
    }
    
}
