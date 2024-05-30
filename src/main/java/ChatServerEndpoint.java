import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ServerEndpoint("/chat")
public class ChatServerEndpoint {
    private static final Logger logger = LogManager.getLogger(ChatServerEndpoint.class);
    private static Set<Session> sessions = new CopyOnWriteArraySet<>();
    private static Map<String, ChatRoom> chatRooms = new ConcurrentHashMap<>();

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
                processBChatMessage(content, session);

                System.out.println("S| Sending message to " + session.getId() + ": " + response);
                break;
            case "CREATE":
                response = processCreateChatRoom(content, session);
                break;
            case "GET_CHATROOMS":
                response = processGetChatRooms(session);
                break;
            case "SUBSCRIBE":
                response = processSubcribeChatRoom(content, session);
                break;
            case "UNSUBSCRIBE":
                response = processUnsubscribeChatRoom(content, session);
                break;
            case "GET_HISTORY":
                response = processGetChatHistory(content);
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

    private String processGetChatHistory(String content) {
        // Process the chat history retrieval...
        logger.info("S| Processing chat history retrieval: {}", content);

        // Split the content into two parts based on a ":" delimiter
        String[] parts = content.split(":", 2);
        String chatRoomName = parts[0];
        String username = parts[1];

        // Get the chat room
        ChatRoom chatRoom = chatRooms.get(chatRoomName);
        if (chatRoom != null) {
            // Get the chat history
            String chatHistory = chatRoom.getMessageHistory(username);
            System.out.println("S| Chat history: " + chatHistory);
            return "GET_HISTORY " + chatHistory;
        }

        // Return error message
        return "GET_HISTORY " + chatRoomName + " failed";
    }

    private String processUnsubscribeChatRoom(String chatRoomName, Session session) {
        // Process the chat room unsubscription...
        logger.info("S| Processing chat room unsubscription: {}", chatRoomName);

        // Get the chat room
        ChatRoom chatRoom = chatRooms.get(chatRoomName);
        // print the chatRoom object
        logger.info("S| Chat room: {}", chatRoom);
        if (chatRoom != null) {
            chatRoom.unsubscribe(session);
            // log subscribers
            chatRoom.getSubscribers().forEach(subscriber -> logger.info("S| Subscriber: {}", subscriber.getId()));
            return "UNSUBSCRIBE " + chatRoomName + " successful";
        }

        // Return error message
        return "UNSUBSCRIBE " + chatRoomName + " failed";
    }

    private String processSubcribeChatRoom(String chatRoomName, Session session) {
        // Process the chat room subscription...
        logger.info("S| Processing chat room subscription: {}", chatRoomName);

        // Get the chat room
        ChatRoom chatRoom = chatRooms.get(chatRoomName);
        if (chatRoom != null) {
            chatRoom.subscribe(session);
            return "SUBSCRIBE " + chatRoomName + " successful";
        }

        // Return error message
        return "SUBSCRIBE " + chatRoomName + " failed";
    }

    private String processGetChatRooms(Session session) {
        // Process the chat room retrieval...
        logger.info("S| Processing chat room retrieval");

        // Get the chat rooms the user is not subscribed to
        Set<String> chatRoomNames = chatRooms.entrySet().stream()
        .filter(entry -> !entry.getValue().getSubscribers().contains(session))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        // Return the list of chat rooms
        return "GET_CHATROOMS " + String.join(",", chatRoomNames);
    }

    private String processCreateChatRoom(String chatRoomName, Session session) throws IOException {
        // Process the chat room creation...
        logger.info("S| Processing chat room creation: {}", chatRoomName);

        // Create chatroom
        ChatRoom chatRoom = chatRooms.get(chatRoomName);
        if (chatRoom == null) {
            chatRoom = new ChatRoom(chatRoomName);
            chatRoom.subscribe(session);
            chatRooms.put(chatRoomName, chatRoom);
            session.getBasicRemote().sendText("SUBSCRIBE " + chatRoomName + " successful");
            return "CREATE " + chatRoomName + " successful";
        }

        // Return error message
        return "CREATE " + chatRoomName + " failed";
    }

    private String processChatMessage(String message) {
        // Process the chat message...
        logger.info("S| Processing chat message: {}", message);

        // Split the message into three parts based on a ":" delimiter
        String[] parts = message.split(":", 3);
        String sender = "You: ";
        String messageContent = parts[2];

        ZonedDateTime zdt = ZonedDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a ZZZZ");

        String formattedTime = "[" + formatter.format(zdt) + "]";

        String processedMessage = sender + messageContent;

        // Return the message
        return "MESSAGE " + formattedTime + ": " + processedMessage;
    }

    private String processBChatMessage(String message, Session session) throws IOException {
        // Process the chat message...
        logger.info("S| Processing chat message: {}", message);

        // Split the message into three parts based on a ":" delimiter
        String[] parts = message.split(":", 3);
        String chatRoomName = parts[0];
        String sender = parts[1];
        String messageContent = parts[2];

        ZonedDateTime zdt = ZonedDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a ZZZZ");

        String formattedTime = "[" + formatter.format(zdt) + "]";

        String processedMessage = "BMESSAGE " + formattedTime + ": " + sender + ": " + messageContent;

        // Get the chatRoom
        ChatRoom chatRoom = chatRooms.get(chatRoomName);

        // Publish the message to the chatRoom
        chatRoom.publish(processedMessage, session);



        // Return the message
        return "BMESSAGE " + System.currentTimeMillis() + ": " + message;
    }

    public static Map<String, ChatRoom> getChatRooms() {
        return chatRooms;
    }

    public static Set<Session> getSessions() {
        return sessions;
    }

    public void setChatRooms(Map<String, ChatRoom> chatRooms) {
        this.chatRooms = chatRooms;
    }

    public void setSessions(Set<Session> sessions) {
        this.sessions = sessions;
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("S| Connection closed: " + session.getId());
        sessions.remove(session);
    }

}
