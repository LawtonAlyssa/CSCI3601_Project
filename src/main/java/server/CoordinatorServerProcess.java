package server;

import process.Process;
import process.QueueManager;
import settings.Settings;

public class CoordinatorServerProcess extends Process{
    private Server server = null;
    private QueueManager queueManager = null;

    public CoordinatorServerProcess(int serverId) {
        super(Settings.SERVER_PROCESS_ID);

        this.server = new Server(getProcessInfo(), serverId);
        setServerInfo(server.getServerInfo());
        
        this.queueManager = new QueueManager(getServerInfo());
        
        server.startServerConnectProcess(queueManager);
    }
    
    @Override
    public void run() {
        while (true) {
            server.UpdateMachineConnections();
        }
    }
    
}
