package message;

public class UserMessage extends MessageContent{
    private String userInput = null;


    public UserMessage(String userInput) {
        super(MessageType.USER_INPUT);

        this.userInput = userInput;
    }

    public String getUserInput() {
        return userInput;
    }
    
}
