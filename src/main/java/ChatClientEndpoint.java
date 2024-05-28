import javax.swing.*;
import javax.websocket.*;

@ClientEndpoint
public class ChatClientEndpoint {
    private static ChatClientEndpoint instance;
    private Client client;
    private Session session;
    private Runnable getChatRoomsCallback;

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

    public void createChatRoom(String chatRoomName) {
        String formattedMessage = "CREATE " + chatRoomName;
        System.out.println("C| Sending message to server: " + formattedMessage);
        try {
            session.getBasicRemote().sendText(formattedMessage);
        } catch (Exception e) {
            System.out.println("C| Error sending message: " + e.getMessage());
        }
    }

    public void getChatRooms(Runnable callback) {
        String formattedMessage = "GET_CHATROOMS ";
        System.out.println("C| Sending message to server: " + formattedMessage);
        try {
            session.getBasicRemote().sendText(formattedMessage);
            this.getChatRoomsCallback = callback;
        } catch (Exception e) {
            System.out.println("C| Error sending message: " + e.getMessage());
        }
    }

    public void joinChatRoom(String chatRoomName) {
        String formattedMessage = "SUBSCRIBE " + chatRoomName;
        System.out.println("C| Sending message to server: " + formattedMessage);
        try {
            session.getBasicRemote().sendText(formattedMessage);
        } catch (Exception e) {
            System.out.println("C| Error sending message: " + e.getMessage());
        }
    }

    public void leaveChatRoom(String chatRoomName) {
        String formattedMessage = "UNSUBSCRIBE " + chatRoomName;
        System.out.println("C| Sending message to server: " + formattedMessage);
        try {
            session.getBasicRemote().sendText(formattedMessage);
        } catch (Exception e) {
            System.out.println("C| Error sending message: " + e.getMessage());
        }
    }

    public void displayChatRoom(String chatRoomName) {
        client.displayChatRoom(chatRoomName);
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
            case "BMESSAGE":
                client.displayMessage(content);
                break;
            case "SUBSCRIBE":
                // Extract the chat room name from content
                String chatRoomName = content.replaceFirst(" successful", "");
                client.addChatRoom(chatRoomName);
                break;
            case "UNSUBSCRIBE":
                // Extract the chat room name from content
                String chatRoomName2 = content.replaceFirst(" successful", "");
                client.removeChatRoom(chatRoomName2);
                break;
            case "CREATE":
                System.out.println("C| " + content);
                break;
            case "CONNECTION":
                System.out.println("C| " + content);
                break;
            case "GET_CHATROOMS":
                // Split the content by comma to get the chat room names
                String[] chatRoomNames = content.split(",");

                // Clear the discoveryChatRooms model
                DefaultListModel<String> model = (DefaultListModel<String>) client.getDiscoveryChatRooms().getModel();
                model.clear();

                // Add each chat room name to the discoveryChatRooms model
                for (String name: chatRoomNames) {
                    model.addElement(name);
                }

                if (this.getChatRoomsCallback == null) {
                    System.out.println("C| getChatRoomsCallback is null");
                } else {
                    this.getChatRoomsCallback.run();
                }
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
