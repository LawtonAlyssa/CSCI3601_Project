package process;

import server.ServerInfo;

public abstract class Process extends Thread {
    private ProcessInfo processInfo;

    public Process(ServerInfo serverInfo) {
        this.processInfo = new ProcessInfo(serverInfo);
    }

    public ProcessInfo getProcessInfo() {
        return processInfo;
    }

    public ServerInfo getServerInfo() {
        return processInfo.getServerInfo();
    }
}
