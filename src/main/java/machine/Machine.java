package machine;

import message.Message;
import message.MessageContent;
import message.MessageType;
import message.Messenger;
import message.CentralServerHandshake;
import message.ClientHandshake;
import message.CriticalSectionRequest;
import message.CriticalSectionResponse;
import message.ServerMessage;
import process.Entity;
import server.ServerInfo;
import settings.Settings;
import userInput.Editor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import criticalSection.CriticalSectionProgress;
import criticalSection.CriticalSectionType;
import criticalSection.RequestType;
import criticalSection.file.FileInfo;
import criticalSection.file.FileRequest;
import criticalSection.file.FileContent;
import criticalSection.file.FileContentInfo;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import message.ServerConnection;

public class Machine extends Entity{
    private static final Logger logger = LoggerFactory.getLogger(Machine.class);
    private Messenger centralClient = createActiveClient(getCentralServerInfo());
    private ArrayList<Messenger> clients = new ArrayList<>();
    private HashMap<String, CriticalSectionProgress> pendingCSReqs = new HashMap<>();
    private HashMap<String, Queue<ServerMessage>> otherCSReqs = new HashMap<>();
    private Editor editor = null;
    private long readTimer = 0;
    private long writeTimer = 0;
    private int machines = 0;

    public Machine() {
        
        setWaitingForServer(true);
    }

    public boolean handleServerMessage(ServerMessage msg) {
        if (super.handleServerMessage(msg)) return true;
        switch (msg.getMessageType()) {
            case CENTRAL_SERVER_HANDSHAKE:
                CentralServerHandshake sh = (CentralServerHandshake)msg.getData();
                
                setServerId(sh.getServerId());
                logger.info("Assigned server id: " + sh.getServerId());

                logger.info("Received central server handshake from Server " + msg.getSource().getServerId());

                ArrayList<ServerInfo> activeClients = sh.getActiveClients();
                setActiveClients(activeClients);
                logger.info("Current number of active clients: " + clients.size());
                
                msg.reply(new MessageContent(MessageType.CENTRAL_CLIENT_HANDSHAKE));
                logger.info("Sent central client handshake");

                startServerProcess();

                break;
            case SERVER_HANDSHAKE:
                int msgServerId = msg.getSource().getServerId();
                logger.info("Received server handshake from Server " + msgServerId);

                logger.debug("Sending client handshake from Server: " + getServerInfo().getServerId());
                
                ClientHandshake ch = new ClientHandshake(getServerInfo().getServerId());
                logger.debug("Server Destination Messenger: " + msg.getMessenger().getDestServerInfo().getServerId());
                msg.reply(ch);
                // logger.info("Sent client handshake to client: " + msg.getMessenger());
                break;
            case CLIENT_HANDSHAKE:
                ClientHandshake clientHandshake = (ClientHandshake)msg.getData();
                int sourceServerId = clientHandshake.getServerId();

                // clients.add();
                ServerInfo serverInfo = msg.getDest();
                
                serverInfo.setServerId(sourceServerId);
                logger.debug("Assigned client to id: " + sourceServerId);
                
                addActiveClient(serverInfo);
                logger.debug("From client handshake, added client: " + sourceServerId);
                break;
            case CS_REQUEST:
                CriticalSectionRequest request = (CriticalSectionRequest)msg.getData();
                logger.info("Received critical section with type: " + request.getCritSectType());

                if (request.getCritSectType()==CriticalSectionType.FILE) {
                    if (distributedMutualExclusion(request)) {
                        msg.reply(
                            new CriticalSectionResponse(
                                MessageType.CS_RESPONSE, 
                                ((FileRequest)request.getCritSect()).getFileInfo()
                            )
                        );
                    } else {
                        FileInfo fileInfo = ((FileRequest)request.getCritSect()).getFileInfo();
                        if (!otherCSReqs.containsKey(fileInfo.getFilePath())) {
                            otherCSReqs.put(fileInfo.getFilePath(), new LinkedList<ServerMessage>());
                        } 
                        otherCSReqs.get(fileInfo.getFilePath()).add(msg);
                    }
                }

                getClock().receiveUpdate(request.getClock());
                break;
            case CS_RESPONSE:
                logger.info("Received critical section response");
                CriticalSectionResponse response = (CriticalSectionResponse)msg.getData();
                CriticalSectionProgress progress = pendingCSReqs.get(response.getFileInfo().getFilePath());

                if (progress.addResponse()) {
                    
                    requestApproved(progress);
                    
                }
                break;
            case CS_EXIT:
                logger.warn("Received critical section response from server notifying exit and unlocking file");
                CriticalSectionResponse exitResp = (CriticalSectionResponse)msg.getData();
                FileInfo fileInfo = exitResp.getFileInfo();

                logger.debug("pendingCSReqs contains: " + pendingCSReqs.containsKey(fileInfo.getFilePath()));

                CriticalSectionProgress exitProgress = pendingCSReqs.get(fileInfo.getFilePath());
                logger.debug("Terminating critical section progress for: '" + fileInfo.getFilePath() + "'");

                if (exitProgress == null) {
                    logger.warn("Progress is null");
                    break;
                }

                RequestType requestType = exitProgress.getRequestType();

                logger.debug("Request type: " + requestType);

                if (requestType == RequestType.READ) {
                    logger.debug("Adding to readTimer");
                    readTimer += System.nanoTime();
                } else {
                    logger.debug("Adding to writeTimer");
                    writeTimer += System.nanoTime();
                }

                if (requestType!=RequestType.REQUEST_WRITE) {
                    pendingCSReqs.remove(fileInfo.getFilePath());
                    logger.debug("Removing progress from pendingCSReqs HashMap");

                    if (otherCSReqs.containsKey(fileInfo.getFilePath())) {
                        Queue<ServerMessage> q = otherCSReqs.get(fileInfo.getFilePath());

                        logger.info("Sent out " + q.size() + " 'OK' messages to critical section request(s)");
                        
                        while (!q.isEmpty()) {
                            ServerMessage serverMessage = q.poll();

                            logger.debug("Sent 'OK' to: " + serverMessage.getSource().getServerId());
                            serverMessage.reply(new CriticalSectionResponse(MessageType.CS_RESPONSE, exitResp.getFileInfo()));
                        }
                    }

                    setWaitingForServer(false); // writing done resume user input
                }
                
                if (requestType!=RequestType.WRITE) { // storing file locally
                    FileContentInfo fcInfo = ((FileContentInfo)fileInfo);
                    FileContent fc = fcInfo.getFileContent();

                    if (!fc.isFileExists()) {
                        System.out.println("File does not exist");
                    } else {
                        File file = new File(getHomeDir(), fcInfo.getFilePath());
                        file.getParentFile().mkdirs();

                        if (requestType==RequestType.READ) {
                            System.out.println(fc.getContent());
                        }
                        
                        try {
                            PrintWriter pw = new PrintWriter(file);
                            pw.write(fc.getContent());
                            pw.close();

                            logger.info("Writing to local file to read: " + file.getPath() + " with size: " + file.length() + " bytes");
                        } catch (FileNotFoundException e) {
                            logger.error("Could not create file", e);
                        }
                    }
                    
                    if (requestType==RequestType.REQUEST_WRITE) {
                        activateEditor(fcInfo.getFilePath());
                    }
                } 

                break;
            default:
                break;
        }

        return false;
    }

    public void setHomeDir(File homeDir) {
        super.setHomeDir(homeDir);
        editor = new Editor(homeDir);
    }

    public void requestApproved(CriticalSectionProgress progress) {
        if (progress.getRequestType()==RequestType.READ) {
            progress.setWriteLocked(true);
        } else {
            progress.setReadLocked(true);
        }
        logger.warn("Critical section request access approved and locking file --> Request type: " +
            progress.getRequestType() + ", reading is " + 
            ((progress.isReadLocked()) ? "locked" : "allowed") + ", writing is " +
            ((progress.isWriteLocked()) ? "locked" : "allowed"));

        // READ
        logger.warn("Sending read request to server");
        centralClient.sendSocket(progress.getRequest());

        // if (progress.getRequestType()==RequestType.WRITE) {
        //     activateEditor();
        // } 
    }

    public void sendFileContentToServer(FileContentInfo fcInfo) {
        logger.debug("Starting second writeTimer");
        writeTimer -= System.nanoTime();

        setWaitingForServer(true);
        
        FileRequest fileRequest = new FileRequest(fcInfo, RequestType.WRITE);
        getClock().newEventUpdate();

        CriticalSectionRequest csRequest = pendingCSReqs.get(fcInfo.getFilePath()).getRequest();
        csRequest.setCritSect(fileRequest);

        logger.warn("Sent write request to Server");
        centralClient.sendSocket(csRequest);
    }

    public boolean distributedMutualExclusion(CriticalSectionRequest request) {
        FileRequest fileRequest = (FileRequest)request.getCritSect();

        logger.info("Received CS File Request type: " + fileRequest.getRequestType());

        FileInfo fileInfo = fileRequest.getFileInfo();

        logger.info("Requested to access file: " + fileInfo.getFilePath());

        // Case 1
        if (!pendingCSReqs.containsKey(fileInfo.getFilePath())) {
            logger.warn("Case 1: Receiving computer doesn't want to access CS, sends 'OK'");
            return true;
        }

        CriticalSectionProgress currProgress = pendingCSReqs.get(fileInfo.getFilePath());
        RequestType currRequestType = currProgress.getRequestType();
        
        CriticalSectionProgress reqProgress = pendingCSReqs.get(fileInfo.getFilePath());
        RequestType requestType = fileRequest.getRequestType();

        if (currRequestType==RequestType.READ && requestType==RequestType.READ) {
            logger.warn("Case 1a: Receiving computer wants to read too, sends 'OK'");
            return true;
        } 

        // Case 2
        logger.warn("--> Request type: " + requestType + ", reading is " + 
            ((reqProgress.isReadLocked()) ? "locked" : "allowed") + ", writing is " +
            ((reqProgress.isWriteLocked()) ? "locked" : "allowed"));

        

        if ((requestType==RequestType.READ && reqProgress.isReadLocked()) || 
            (requestType!=RequestType.READ && reqProgress.isWriteLocked())) {
            logger.warn("Case 2: Receiving computer is currently accessing CS, queues request");
            return false;
        }
        
        // Case 3
        if (getClock().isLessThan(request.getClock())) {
            logger.warn("Case 3: Receiving computer wants to also access CS and has smaller clock value, queue request");
            return false;
        }
        
        if (getClock().isEqualTo(request.getClock())) {
            if (getServerInfo().getServerId() > request.getServerInfo().getServerId()) {
                logger.warn("Case 3a: Receiving computer wants to also access CS, has equal clock value, and greater ID, sends 'OK'");
                return true;
            }
            logger.warn("Case 3a: Receiving computer wants to also access CS, has equal clock value, and smaller ID, queues request");
            return false;
        } 

        logger.warn("Case 3: Receiving computer wants to also access CS and has greater clock value, sends 'OK'");
        return true;    
    }

    public boolean handleProcessMessage(Message msg) {
        if (super.handleProcessMessage(msg)) return true;
        
        switch (msg.getMessageType()) {
            case SERVER_CONN:
                Socket connectionSocket = ((ServerConnection)msg.getData()).getConnectionSocket();

                ServerInfo connServerInfo = new ServerInfo(
                    connectionSocket.getInetAddress().getHostAddress().toString()
                );

                Messenger msgr = new Messenger(
                    getQueue(), 
                    connectionSocket, 
                    getServerInfo(), 
                    connServerInfo
                );
                
                addServer(msgr); // store server connection
                logger.debug("Server connected to a new Socket w/ id: " + connServerInfo.getServerId());

                msgr.sendSocket(new MessageContent(MessageType.SERVER_HANDSHAKE));
                break;
            default:
                break;
        }

        return false;
    }

    public boolean handleUserInput(String[] tokenStr) {
        if (editor != null && editor.isActive()) {
            FileContentInfo fcInfo = editor.handleUserInput(tokenStr);
            if (fcInfo != null) {
                sendFileContentToServer(fcInfo);
            }
            return false;
        }

        if (super.handleUserInput(tokenStr)) return true;
        
        RequestType requestType = null;

        switch (tokenStr[0]) {
            case "wait":
                if (tokenStr.length==2) {
                    try {
                        machines = Integer.parseInt(tokenStr[1]);
                        
                        if (clients.size()>=machines) {
                            logger.warn("All machines are already connected");
                        } else {
                            logger.warn("Waiting for " + machines  + " machines to connect to");
                        }
                        setWaitingForServer(clients.size()<machines); // Allow user input after handshake with central server
                    } catch (Exception e) {
                        logger.error("Second argument must be an integer value");
                    }
                } else {
                    logger.error("Invalid number of arguments for wait");
                }
                break;
            case "read":
                logger.debug("Starting readTimer");
                
                readTimer -= System.nanoTime();
                
                requestType = RequestType.READ;
                break;
            case "write":
                logger.debug("Starting writeTimer");
                writeTimer -= System.nanoTime();

                
                requestType = RequestType.REQUEST_WRITE;
                
                if (tokenStr.length == 2) {
                    logger.info("User requested to write to file: " + tokenStr[1]);
                    // editor.setFile(new File(tokenStr[1]));
                } else {
                    logger.error("Invalid number of arguments for write");
                }
                break;
            default:
                break;
            }
            
        if (requestType!=null) {
            setWaitingForServer(true); // pause user input until server is ready

            logger.trace("Handling command: " + tokenStr[0]);

            if (tokenStr.length < 2) {
                logger.warn("Missing file argument");
                return false;
            }
    
            FileInfo fileInfo = new FileInfo(tokenStr[1], false);
            CriticalSectionRequest critSectRequest = new CriticalSectionRequest(getServerInfo(), new FileRequest(fileInfo, requestType), getClock());
            
            CriticalSectionProgress critSectReqProgress = new CriticalSectionProgress(clients.size(), critSectRequest);
            logger.debug("Create critical section progress for: '" + fileInfo.getFilePath() + "'");

            pendingCSReqs.put(fileInfo.getFilePath(), critSectReqProgress);

            logger.debug("pendingCSReqs contains: " + pendingCSReqs.containsKey(fileInfo.getFilePath()));


            getClock().newEventUpdate();

            if (clients.size()==0) {
                logger.info("Instant approval since 0 clients");
                requestApproved(critSectReqProgress);
            }

            for (Messenger client : clients) {
                client.sendSocket(critSectRequest);
            }
        }

        return false;
    }

    public void activateEditor(String filePath) {
        logger.info("Activating file editor");
        editor.setFile(new File(filePath));
        editor.setActive(true);
        editor.dump();
        setWaitingForServer(false);
    }

    public Messenger getActiveClient(ServerInfo serverInfo) {
        for (Messenger messenger : clients) {
            if (messenger==null) continue;
            if (messenger.getDestServerInfo().equals(serverInfo)) {
                return messenger;
            }
        }
        return null;
    }

    public void addActiveClient(ServerInfo clientServerInfo) {
        if (getActiveClient(clientServerInfo)!=null) return;
        if (clientServerInfo.equals(getServerInfo())) return;

        logger.debug("Added client w/ id: " + clientServerInfo.getServerId());
        clients.add(createActiveClient(clientServerInfo));

        if (clients.size()==machines) {
            logger.warn("All machines have been connected");
        }
        setWaitingForServer(clients.size()<machines); // Allow user input if enough machines are connected
    }

    public Messenger createActiveClient(ServerInfo clientServerInfo) {
        Socket clientSocket;

        String clientIpAddress = clientServerInfo.getIpAddress();

        try {
            clientSocket = new Socket(clientIpAddress, Settings.LOCAL_PORT_NUM+clientServerInfo.getServerId());

            logger.info("Connecting to server: " + clientServerInfo.getServerId());

            return new Messenger(
                getQueue(), 
                clientSocket, 
                getServerInfo(), 
                clientServerInfo
            );

        } catch (UnknownHostException e) {
            logger.error("Cannot find host: " + clientIpAddress, e);
        } catch (IOException e) {
            logger.error("Couldn't get I/O for the connection to: " + clientIpAddress, e);
        }

        return null;
    }

    public void setActiveClients(ArrayList<ServerInfo> clientServerInfos) {
        // servers.clear();

        for (ServerInfo serverInfo : clientServerInfos) {
            addActiveClient(serverInfo);
        }
    }

    public boolean update() {
        if (centralClient!=null && !centralClient.isAlive()) {
            close();
            return true;
        }

        for (Messenger client : clients) {
            if (!client.isAlive()) {
                clients.remove(client);
                break;
            }
        }
        
        return false;
    }

    @Override
    public void createHomeDir() {
        super.createHomeDir();
        File homeDir = getHomeDir();

        if (homeDir.exists()) {
            deleteDirectory(homeDir);
            logger.info("Deleted home directory: " + homeDir.getPath());
        }
        homeDir.mkdirs();
    }

    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public void close() {
        logger.info("Terminating Machine...");

        logger.warn("Read time = " + (readTimer/1000) + "[us] | Write time = " + (writeTimer/1000) + "[us]");

        for (Messenger client : clients) {
            client.close();
        }
        if (centralClient != null) {
            centralClient.close();
        }
        
        super.close();
    }
    
}
