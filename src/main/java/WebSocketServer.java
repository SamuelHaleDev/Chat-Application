import org.glassfish.tyrus.server.Server;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebSocketServer {
    private static final Logger logger = LogManager.getLogger(WebSocketServer.class);

    public static void main(String[] args) {
        org.glassfish.tyrus.server.Server server = new org.glassfish.tyrus.server.Server("localhost", 8080, "/ws", ChatServerEndpoint.class);

        try {
            server.start();
            System.out.println("WebSocket server started on ws://localhost:8080/ws/chat");
            // Keep the server running
            int ignored = System.in.read();
        } catch (Exception e) {
            logger.error("Error starting server: {}", e.getMessage());
        } finally {
            server.stop();
        }
    }
}
