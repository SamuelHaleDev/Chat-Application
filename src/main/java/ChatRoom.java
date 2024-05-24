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
        messageHistory.add(message);
        for (Session subscriber : subscribers) {
            if (subscriber != senderSession) {
                subscriber.getBasicRemote().sendText(message);
            }
        }
    }

    public String getMessageHistory() {
        return String.join("\n", messageHistory);
    }

    public String getName() {
        return name;
    }

    // getSubscribers
    public Set<Session> getSubscribers() {
        return subscribers;
    }
}
