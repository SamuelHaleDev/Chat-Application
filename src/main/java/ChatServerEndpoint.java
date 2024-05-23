import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ServerEndpoint("/chat")
public class ChatServerEndpoint {
    private static final Logger logger = LogManager.getLogger(ChatServerEndpoint.class);
    private static final Set<Session> sessions = new CopyOnWriteArraySet<>();
    private static final Map<String, ChatRoom> chatRooms = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("S| New connection: " + session.getId());
        sessions.add(session);
        try {
            session.getBasicRemote().sendText("Connection established!");
        } catch (IOException e) {
            logger.error("S| Error sending message to client: {}", e.getMessage());
        }
    }

    // Handle all incoming messages from Client here
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        String response = "";

        System.out.println("S| Message received from " + session.getId() + ": " + message);

        // Split the message into the command and the content
        String[] parts = message.split(" ", 2);
        String command = parts[0];
        String content = parts.length > 1 ? parts[1] : "";

        // Handle the command
        switch (command) {
            case "MESSAGE":
                response = processChatMessage(content);

                // Broadcast the message to all connected clients
                broadcast(content, session);
                System.out.println("S| Sending message to " + session.getId() + ": " + response);
                break;
            case "CREATE":
                String chatRoomName = content;
                ChatRoom chatRoom = chatRooms.get(chatRoomName);
                if (chatRoom == null) {
                    chatRoom = new ChatRoom(chatRoomName);
                    chatRoom.subscribe(session);
                    chatRooms.put(chatRoomName, chatRoom);
                    session.getBasicRemote().sendText("CREATE " + chatRoomName + " successful");
                    session.getBasicRemote().sendText("SUBSCRIBE " + chatRoomName + " successful");
                } else {
                    session.getBasicRemote().sendText("CREATE " + chatRoomName + " failed: Chat room already exists");
                }
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

    private String processChatMessage(String message) {
        // Process the chat message...
        logger.info("S| Processing chat message: {}", message);

        String processedMessage = message.replaceFirst("^.*?: ", "You: ");

        // Return the message
        return "MESSAGE " + System.currentTimeMillis() + ": " + processedMessage;
    }

    private String processBChatMessage(String message) {
        // Process the chat message...
        logger.info("S| Processing chat message: {}", message);

        // Return the message
        return "BMESSAGE " + System.currentTimeMillis() + ": " + message;
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("S| Connection closed: " + session.getId());
        sessions.remove(session);
    }

    private void broadcast(String message, Session senderSession) {
        String bMessage = processBChatMessage(message);
        for (Session session : sessions) {
            if (session.equals(senderSession)) {
                continue;
            }
            try {
                session.getBasicRemote().sendText(bMessage);
            } catch (IOException e) {
                logger.error("S| Error sending message to client: {}", e.getMessage());
            }
        }
    }
}
