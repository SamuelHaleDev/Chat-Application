
/*

* Client.java
*
* This file is the main file for the client side of the chat application.
*
* 5-18-2024
*
* Copyright (c) 2024 by Samuel Hale
* */
import javax.swing.*;
import javax.websocket.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Client extends JFrame {
    private ChatClientEndpoint chatClientEndpoint;
    private static final Logger logger = LogManager.getLogger(Client.class);
    private static JTextField usernameField;
    private static JButton connectButton;
    private static String username;

    public Client() {
        setTitle("WhatsChat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);
        setLocationRelativeTo(null);

        // Initialize the username field and connect button
        usernameField = new JTextField(20);
        usernameField.setText("Enter your username");
        connectButton = new JButton("Connect");

        // Add an action listener to the connect button
        connectButton.addActionListener(e -> connect());

        // Create a panel and add the username field and connect
            // button to it
        JPanel panel = new JPanel();
        panel.add(usernameField);
        panel.add(connectButton);

        // Add the panel to the frame
        add(panel);

        setVisible(true);
    }

    public void connect() {
        // Get the username from the text field
        username = usernameField.getText();

        // Create a WebSocket container
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        // Define the endpoint URI
        URI uri = URI.create("ws://localhost:8080/ws/chat");

        // Create a new instance of the Client.ChatClientEndpoint and
            // connect to the server
        try {
            container.connectToServer(ChatClientEndpoint.class, uri);
            chatClientEndpoint = ChatClientEndpoint.getInstance();

            // After connecting, hide the username field and connect button
            usernameField.setVisible(false);
            connectButton.setVisible(false);

            // Call the displayChatRoom method to display the chat room
            displayChatRoom();
        } catch (DeploymentException | IOException e) {
            logger.error("Error connecting to server: {}", e.getMessage());
        }

        // After connecting, hide the username field and connect
            // button
        usernameField.setVisible(false);
        connectButton.setVisible(false);
    }

    public void displayChatRoom() {
        setSize(500, 500);
        setLocationRelativeTo(null);

        // Header panel with back button and header label
        JButton backButton = new JButton("\u2190");
        backButton.addActionListener(e -> {
            // Show the username field and connect button
            usernameField.setVisible(true);
            connectButton.setVisible(true);
        });
        JLabel header = new JLabel("Chat Room", SwingConstants.CENTER);
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(header, BorderLayout.CENTER);

        // Chat Room Panel
        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        // Footer Panel with text field and send button
        JTextField messageField = new JTextField(20);
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> {
            String message = messageField.getText();
            chatClientEndpoint.sendMessage(username, message);
            messageField.setText("");
        });
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.add(messageField, BorderLayout.CENTER);
        footerPanel.add(sendButton, BorderLayout.EAST);

        // Main panel
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(chatScrollPane, BorderLayout.CENTER);
        panel.add(footerPanel, BorderLayout.SOUTH);

        // Remove all components from the frame
        getContentPane().removeAll();

        add(panel);

        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        new Client();
    }

}
