package criticalSection.file;

import java.util.HashMap;

import message.Message;

public class FileWriteInfo extends FileInfo{
    private FileContent fileContent;

    public FileWriteInfo(String filePath, FileContent fileContent) {
        super(filePath, true);
        this.fileContent = fileContent;
    }

    public FileContent getFileContent() {
        return fileContent;
    }

    public static FileWriteInfo toMessage(String msgStr) {
        HashMap<String, String> hm = Message.parseMessage(msgStr);
        String filePath = hm.get("filePath");
        FileContent fileContent = FileContent.toMessage(hm.get("fileContent"));
        return new FileWriteInfo(filePath, fileContent);
    }

    @Override
    public String toString() {
        return String.format("%sfileContent:[%s]", super.toString(), fileContent);
    }
    
}
