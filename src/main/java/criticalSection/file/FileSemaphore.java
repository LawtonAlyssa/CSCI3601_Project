package criticalSection.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import criticalSection.BaseSemaphore;
import criticalSection.Semaphore;

public class FileSemaphore extends BaseSemaphore{
    private static final Logger logger = LoggerFactory.getLogger(FileSemaphore.class);
    private File file;
    private Semaphore rwMutex = new Semaphore();
    private Semaphore mutex = new Semaphore();
    private int readCount = 0;

    public FileSemaphore(FileInfo fileInfo) {
        this.file = new File(fileInfo.getFilePath());
    }

    @Override
    public FileContent read() {
        mutex.waitSemaphore();
        readCount++;
        if (readCount == 1) {
            rwMutex.waitSemaphore();
        }
        mutex.signal();

        FileContent fileContent = readingFile();

        mutex.waitSemaphore();
        readCount--;
        if (readCount == 0) {
            rwMutex.signal();
        }
        mutex.signal();

        return fileContent;
    }

    private FileContent readingFile() {
        BufferedReader fr = null;

        try {
            fr = new BufferedReader(new FileReader(file));
            StringBuilder fc = new StringBuilder();

            while (fr.ready()) {
                fc.append(fr.readLine());
            }

            return new FileContent(fc.toString());
        } catch (FileNotFoundException e) {
            logger.error("Could not find file: " + file.getName(), e);
        } catch (IOException e) {
            logger.error("Could not read file: " + file.getName(), e);
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    logger.error("Could not close file: " + file.getName(), e);
                }
            }
        }
        return new FileContent(null);
    }

    @Override
    public void requestWrite() {
        rwMutex.waitSemaphore();
    }

    @Override
    public void write(FileContent fc) {
        writingToFile(fc);
        rwMutex.signal();
    }

    public void writingToFile(FileContent fc) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(file);

            pw.println(fc.getContent());
        } catch (FileNotFoundException e) {
            logger.error("Could not find file: " + file.getName(), e);
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }
    
}
