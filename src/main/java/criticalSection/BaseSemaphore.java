package criticalSection;

import criticalSection.file.FileContent;

public abstract class BaseSemaphore {
    public abstract FileContent read();

    public abstract void requestWrite();

    public abstract void write(FileContent fc);
}
