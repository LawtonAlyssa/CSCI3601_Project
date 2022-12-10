package criticalSection.file;

import java.util.HashMap;
import message.Message;

public class FileContent{
    private String content;

    public FileContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public static FileContent toMessage(String msgStr) {
        HashMap<String, String> hm = Message.parseMessage(msgStr);
        String content = hm.get("fileContent");
        return new FileContent(content);
    }

    @Override
    public String toString() {
        return String.format("content:[%s]", content);
    }
    
}
