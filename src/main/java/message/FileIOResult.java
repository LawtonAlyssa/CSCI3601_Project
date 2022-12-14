package message;

public class FileIOResult extends MessageContent{
    private ServerMessage serverMessage = null;

    public FileIOResult(ServerMessage ServerMessage) {
        super(MessageType.FILE_IO_RESULT);

        this.serverMessage = ServerMessage;
    }

    public ServerMessage getServerMessage() {
        return serverMessage;
    }    
}
