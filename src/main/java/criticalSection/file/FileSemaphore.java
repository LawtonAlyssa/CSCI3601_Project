package criticalSection.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import criticalSection.BaseSemaphore;
import criticalSection.Semaphore;
import settings.Settings;

public class FileSemaphore extends BaseSemaphore{
    private static final Logger logger = LoggerFactory.getLogger(FileSemaphore.class);
    private File file;
    private Semaphore rwMutex = new Semaphore(1);
    private Semaphore mutex = new Semaphore(1);
    private int readCount = 0;
    public static long readTimer = 0;
    public static long writeTimer = 0;

    public FileSemaphore(String filePath) {
        this.file = new File(filePath);
    }

    @Override
    public FileContent read() {

        logger.debug("Read: waiting for mutex");
        mutex.waitSemaphore();

        readCount++;
        logger.debug("Read count=" + readCount);

        if (readCount == 1) {
            logger.debug("Read: waiting for rwmutex");
            rwMutex.waitSemaphore();
        }
        logger.debug("Read: signal mutex");
        mutex.signal();

        logger.debug("Reading file");
        readTimer -= System.nanoTime();
        FileContent fileContent = readingFile();
        readTimer += System.nanoTime();

        logger.debug("Read: waiting for mutex");
        mutex.waitSemaphore();

        readCount--;
        logger.debug("Read count=" + readCount);

        if (readCount == 0) {
            logger.debug("Read: signal rwmutex");
            rwMutex.signal();
        }

        logger.debug("Read: signal mutex");
        mutex.signal();

        logger.debug("Done reading");
        return fileContent;
    }

    private void ioDelay() {
        try {
            logger.debug("Before I/O delay");
            TimeUnit.SECONDS.sleep(Settings.IO_DELAY);
            logger.debug("After I/O delay");
        } catch (InterruptedException e) {
            logger.error("Coult not delay I/O - interrupted", e);
        }
    }

    private FileContent readingFile() {
        ioDelay();
        
        BufferedReader fr = null;

        try {
            fr = new BufferedReader(new FileReader(file));
            StringBuilder fc = new StringBuilder();

            while (fr.ready()) {
                fc.append(fr.readLine()).append("\n");
            }

            return new FileContent(fc.toString(), true);
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
        return new FileContent("", false);
    }

    @Override
    public void requestWrite() {
        rwMutex.waitSemaphore();
    }

    @Override
    public void write(FileContent fc) {
        rwMutex.waitSemaphore();

        writeTimer -= System.nanoTime();
        writingToFile(fc);
        writeTimer += System.nanoTime();

        rwMutex.signal();
    }

    public void writingToFile(FileContent fc) {
        ioDelay();
        
        PrintWriter pw = null;

        file.getParentFile().mkdirs();
        String content = (fc==null) ? "" : fc.getContent();
        logger.info("Writing to file: " + file.getPath() + " with size: " + file.length() + " bytes");
        
        try {
            pw = new PrintWriter(file);

            pw.print(content);
        } catch (FileNotFoundException e) {
            logger.error("Could not find file: " + file.getName(), e);
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }
    
}
