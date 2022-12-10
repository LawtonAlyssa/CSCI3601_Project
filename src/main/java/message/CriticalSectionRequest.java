package message;

import java.util.HashMap;
import clock.Clock;
import criticalSection.CriticalSectionInfo;
import criticalSection.CriticalSectionType;
import server.ServerInfo;

public class CriticalSectionRequest extends MessageContent{
    private ServerInfo serverInfo = null;
    private CriticalSectionInfo critSect = null;
    private Clock clock = null;

    public CriticalSectionRequest(ServerInfo serverInfo, CriticalSectionInfo critSect, Clock clock) {
        super(MessageType.CS_REQUEST);
        this.serverInfo = serverInfo;
        this.critSect = critSect;
        this.clock = clock;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public CriticalSectionInfo getCritSect() {
        return critSect;
    }

    public CriticalSectionType getCritSectType() {
        return critSect.getCritSectType();
    }

    public Clock getClock() {
        return clock;
    }

    public static CriticalSectionRequest toMessage(String msgStr) {
        HashMap<String, String> hm = Message.parseMessage(msgStr);
        ServerInfo computerId = ServerInfo.toMessage(hm.get("computerId"));
        CriticalSectionInfo critSect = CriticalSectionInfo.toMessage(hm.get("critSect"));
        Clock clock = Clock.toMessage(hm.get("clock"));
        return new CriticalSectionRequest(computerId, critSect, clock);
    }

    @Override
    public String toString() {
        return String.format("%scomputerId:[%s]critSect:[%s]clock:[%s]", super.toString(), serverInfo, critSect, clock);
    }
}
