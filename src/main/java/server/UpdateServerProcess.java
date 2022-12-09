package server;

import process.Process;

public class UpdateServerProcess extends Process{
    private Server server = null;

    public UpdateServerProcess(int serverId) {
        super();

        this.server = new Server(getProcessInfo(), serverId);

        setServerInfo(server.getServerInfo());
    }
    
    @Override
    public void run() {
        while (true) {
            server.UpdateMachineConnections();
        }
    }
    
}
