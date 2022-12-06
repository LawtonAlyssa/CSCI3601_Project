package criticalSection.file;

public class FileInfo{
    private String filePath;

    public FileInfo(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public FileContent getFileContent() {
        return null;
    }
}
