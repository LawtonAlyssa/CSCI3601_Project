package criticalSection.file;

import java.util.HashMap;
import message.Message;

public class FileContent{
    private String content;
    private boolean fileExists = false;

    public FileContent(String content, boolean fileExists) {
        this.content = content;
        this.fileExists = fileExists;
    }

    public String getContent() {
        return content;
    }

    public static FileContent toMessage(String msgStr) {
        HashMap<String, String> hm = Message.parseMessage(msgStr);
        String content = hm.get("content");
        boolean fileExists = Boolean.parseBoolean(hm.get("fileExists"));
        return new FileContent(content, fileExists);
    }

    @Override
    public String toString() {
        return String.format("content:[%s]fileExists:[%b]", content, fileExists);
    }

    public boolean isFileExists() {
        return fileExists;
    }
    
}
