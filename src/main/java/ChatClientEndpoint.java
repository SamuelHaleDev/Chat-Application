import javax.websocket.*;

@ClientEndpoint
public class ChatClientEndpoint {
    private static ChatClientEndpoint instance;
    private Client client;
    private Session session;

    public ChatClientEndpoint(Client client) {
        this.client = client;
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        System.out.println("C| Connected to server");
        this.session = session;
        instance = this;
    }

    public void sendMessage(String username, String message) {
        String formattedMessage = "MESSAGE " + username + ": " + message;
        System.out.println("C| Sending message to server: " + formattedMessage);
        try {
            session.getBasicRemote().sendText(formattedMessage);
        } catch (Exception e) {
            System.out.println("C| Error sending message: " + e.getMessage());
        }
    }

    // Handle all incoming messages from Server here
    @OnMessage
    public void onMessage(String message) {
        System.out.println("C| Received message from server: " + message);

        // Split the message into the command and the content
        String[] parts = message.split(" ", 2);
        String command = parts[0];
        String content = parts[1];

        // Handle the command
        switch (command) {
            case "MESSAGE":
                client.displayMessage(content);
                break;
            default:
                System.out.println("C| Unknown command: " + command);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("C| Disconnected from server, Reason: " + closeReason);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("C| Error occurred: " + throwable.getMessage());
    }

    public static ChatClientEndpoint getInstance() {
        return instance;
    }
}
