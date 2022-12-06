package criticalSection.file;

public class FileWriteInfo extends FileInfo{
    private FileContent fileContent;

    public FileWriteInfo(String filePath, FileContent fileContent) {
        super(filePath);
        this.fileContent = fileContent;
    }

    public FileContent getFileContent() {
        return fileContent;
    }
}
