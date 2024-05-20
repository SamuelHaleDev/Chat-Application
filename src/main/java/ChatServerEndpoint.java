import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ServerEndpoint("/chat")
public class ChatServerEndpoint {
    private static final Logger logger = LogManager.getLogger(ChatServerEndpoint.class);

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("S| New connection: " + session.getId());
        try {
            session.getBasicRemote().sendText("Connection established!");
        } catch (IOException e) {
            logger.error("S| Error sending message to client: {}", e.getMessage());
        }
    }

    // Handle all incoming messages from Client here
    @OnMessage
    public void onMessage(String message, Session session) {
        String response = "";

        System.out.println("S| Message received from " + session.getId() + ": " + message);

        // Split the message into the command and the content
        String[] parts = message.split(" ", 2);
        String command = parts[0];
        String content = parts[1];

        // Handle the command
        switch (command) {
            case "MESSAGE":
                response = processChatMessage(content, session);

                System.out.println("S| Sending message to " + session.getId() + ": " + response);
                break;
            default:
                System.out.println("S| Unknown command: " + command);
        }
        try {
            session.getBasicRemote().sendText(response);
        } catch (IOException e) {
            logger.error("S| Error sending message to client: {}", e.getMessage());
        }
    }

    private String processChatMessage(String message, Session session) {
        // Process the chat message...
        logger.info("S| Processing chat message: {}", message);

        // Return the message
        return "MESSAGE " + System.currentTimeMillis() + ": " + message;
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("S| Connection closed: " + session.getId());
    }
}
