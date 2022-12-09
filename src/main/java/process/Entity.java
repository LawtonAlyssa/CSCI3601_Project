package process;

import server.ServerInfo;

public class Entity {
    ProcessInfo processInfo = null;

    public Entity(ProcessInfo processInfo) {
        this.processInfo = processInfo;
    }

    public ProcessInfo getProcessInfo() {
        return processInfo;
    }
    
    public ServerInfo getServerInfo() {
        return processInfo.getServerInfo();
    }
}
