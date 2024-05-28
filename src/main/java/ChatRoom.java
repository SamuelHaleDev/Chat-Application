import javax.websocket.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ChatRoom {
    private final String name;
    private final Set<Session> subscribers = new CopyOnWriteArraySet<>();
    private final List<String> messageHistory = new ArrayList<>();

    public ChatRoom(String name) {
        this.name = name;
    }

    public void subscribe(Session session) {
        subscribers.add(session);
    }
    public void unsubscribe(Session session) {
        subscribers.remove(session);
    }

    public void publish(String message, Session senderSession) throws IOException {
        messageHistory.add(message.replaceFirst("BMESSAGE ", ""));
        // print message history
        System.out.println("Message history: " + messageHistory);
        for (Session subscriber : subscribers) {
            if (subscriber != senderSession) {
                subscriber.getBasicRemote().sendText(message);
            }
        }
    }

    public String getMessageHistory(String username) {
        // Create a temporary message history and initialize it to messageHistory
        List<String> tempMessageHistory = new ArrayList<>(this.messageHistory);
        // loop through message history
        for (String message : tempMessageHistory) {
            // Deassemble message
            String[] parts = message.split(":", 3);

            // Check if username == parts[1]
            if (parts[1].replace(" ", "").contains(username)) {
                parts[1] = " You";
            }

            // Reassemble message
            String tempMessage = parts[0] + ":" + parts[1] + ":" + parts[2];

            // Replace that message in messageHistory with new one
            tempMessageHistory.set(tempMessageHistory.indexOf(message), tempMessage);
        }

        return String.join(",", tempMessageHistory);
    }

    public String getName() {
        return name;
    }

    // getSubscribers
    public Set<Session> getSubscribers() {
        return subscribers;
    }
}
