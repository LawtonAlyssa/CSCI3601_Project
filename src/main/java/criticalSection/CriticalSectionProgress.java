package criticalSection;

import criticalSection.file.FileRequest;
import message.CriticalSectionRequest;

public class CriticalSectionProgress {
    private int respReceived = 0;
    private int totalRespNeed = 0;
    private CriticalSectionRequest request = null;
    private boolean isReadLocked = false;
    private boolean isWriteLocked = false;

    public CriticalSectionProgress(int totalRespNeed, CriticalSectionRequest request) {
        this.totalRespNeed = totalRespNeed;
        this.request = request;
    }
    
    public void setCritSectType(CriticalSectionType type) {
        getRequest().setCritSectType(type);
    }

    public boolean addResponse() {
        return ((++respReceived)==totalRespNeed);
    }

    public CriticalSectionRequest getRequest() {
        return request;
    }

    public RequestType getRequestType() {
        if (request.getCritSectType()!= CriticalSectionType.FILE) return null;
        FileRequest fileRequest = (FileRequest)request.getCritSect();
        return fileRequest.getRequestType();
    }

    public boolean isReadLocked() {
        return isReadLocked;
    }

    public void setReadLocked(boolean isReadLocked) {
        this.isReadLocked = isReadLocked;
        this.isWriteLocked = isReadLocked;
    }

    public boolean isWriteLocked() {
        return isWriteLocked;
    }

    public void setWriteLocked(boolean isWriteLocked) {
        this.isWriteLocked = isWriteLocked;
    }
    
}