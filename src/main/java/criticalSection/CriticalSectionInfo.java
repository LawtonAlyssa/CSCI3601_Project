package criticalSection;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import criticalSection.file.FileRequest;
import message.Message;

public class CriticalSectionInfo {
    private static final Logger logger = LoggerFactory.getLogger(CriticalSectionInfo.class);
    private CriticalSectionType critSectType = null;

    public CriticalSectionInfo(CriticalSectionType critSectType) {
        this.critSectType = critSectType;
    } 

    public CriticalSectionType getCritSectType() {
        return critSectType;
    }

    public void setCritSectType(CriticalSectionType critSectType) {
        this.critSectType = critSectType;
    }

    public static CriticalSectionInfo toMessage(String msgStr) {
        HashMap<String, String> hm = Message.parseMessage(msgStr);
        String typeStr = hm.get("critSectType");

        logger.trace("Parsing Critical Section Type: " + typeStr);
        
        CriticalSectionType type = CriticalSectionType.valueOf(typeStr);

        switch (type) {
            case FILE:
                return FileRequest.toMessage(msgStr);
            default:
                return new CriticalSectionInfo(type);
        }
    }

    @Override
    public String toString() {
        return String.format("critSectType:[%s]", critSectType);
    }
    
}
