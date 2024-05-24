
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
    private JTextArea chatArea;
    private static String username;
    private JList<String> chatRoomList;
    private JTabbedPane tabbedPane;
    private JList<String> discoveryChatRooms;

    public Client() {
        setTitle("WhatsChat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);
        setLocationRelativeTo(null);

        DefaultListModel<String> model = new DefaultListModel<>();
        chatRoomList = new JList<>(model);
        discoveryChatRooms = new JList<>(new DefaultListModel<>());

        // Initialize the username field and connect button
        usernameField = new JTextField(20);
        usernameField.setText("Enter your username");
        JButton connectButton = new JButton("Connect");

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
            container.connectToServer(new ChatClientEndpoint(this), uri);
            chatClientEndpoint = ChatClientEndpoint.getInstance();

            // Initialize the navigation panel
            initializeNavigationPanel();

        } catch (DeploymentException | IOException e) {
            logger.error("Error connecting to server: {}", e.getMessage());
        }
    }

    public void initializeNavigationPanel() {
        tabbedPane = new JTabbedPane();

        JPanel discoverPanel = new JPanel();
        discoverPanel.setLayout(new BorderLayout());
        displayDiscoveryPage(discoverPanel);
        tabbedPane.addTab("Discover", discoverPanel);

        JPanel chatroomsPanel = new JPanel();
        chatroomsPanel.setLayout(new BorderLayout());
        displaySubscribedChatRooms(chatroomsPanel);
        tabbedPane.addTab("Chat Rooms", chatroomsPanel);

        getContentPane().removeAll();
        add(tabbedPane);
        revalidate();
        repaint();
    }

    public void displayDiscoveryPage(JPanel panel) {
        setSize(500, 500);
        setLocationRelativeTo(null);

        panel.removeAll();

        // Header panel with a header label and a "Refresh" button
        JLabel header = new JLabel("Discover Chat Rooms", SwingConstants.CENTER);
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            // Add code here to process communication for chatroom discovery
            chatClientEndpoint.getChatRooms();
        });

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(header, BorderLayout.CENTER);
        headerPanel.add(refreshButton, BorderLayout.EAST);

        // Chat Room List Panel
        JScrollPane chatRoomScrollPane = new JScrollPane(discoveryChatRooms);
        discoveryChatRooms.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedChatRoom = discoveryChatRooms.getSelectedValue();
                //displayChatRoom(selectedChatRoom);
            }
        });

        // Add components to the passed panel
        panel.setLayout(new BorderLayout());
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(chatRoomScrollPane, BorderLayout.CENTER);

        // Request the server for the list of chat rooms
        chatClientEndpoint.getChatRooms();

        revalidate();
        repaint();
    }

    public void displaySubscribedChatRooms(JPanel panel) {
        setSize(500, 500);
        setLocationRelativeTo(null);
        panel.removeAll();

        // Header panel with header label and a "+" button
        JLabel header = new JLabel("Subscribed Chat Rooms", SwingConstants.CENTER);
        JButton addButton = new JButton("+");
        addButton.addActionListener(e -> {
            JTextField chatRoomNameField = new JTextField(20);
            JButton createButton = new JButton("Create");
            createButton.addActionListener(e1 -> {
                String chatRoomName = chatRoomNameField.getText();
                // Add code here to process communication for chatroom creation
                chatClientEndpoint.createChatRoom(chatRoomName);

                // Show a confirmation dialog
                JOptionPane.showMessageDialog(null, "Chat room '" + chatRoomName + "' created successfully!");

                Window dialog = SwingUtilities.windowForComponent((Component) e1.getSource());
                if (dialog != null) {
                    dialog.dispose();
                }
            });

            JPanel formPanel = new JPanel();
            formPanel.add(chatRoomNameField);
            formPanel.add(createButton);

            JDialog dialog = new JDialog();
            dialog.setModal(true);
            dialog.setTitle("Create a new chat room");
            dialog.setContentPane(formPanel);
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        });

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(header, BorderLayout.CENTER);
        headerPanel.add(addButton, BorderLayout.EAST);

        // Chat Room List Panel
        JScrollPane chatRoomScrollPane = new JScrollPane(chatRoomList);
        chatRoomList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedChatRoom = chatRoomList.getSelectedValue();
                //displayChatRoom(selectedChatRoom);
            }
        });

        // Add components to the passed panel
        panel.setLayout(new BorderLayout());
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(chatRoomScrollPane, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    public void displayChatRoom() {
        setSize(500, 500);
        setLocationRelativeTo(null);

        // Header panel with back button and header label
        JButton backButton = new JButton("\u2190");
        JLabel header = new JLabel("Chat Room", SwingConstants.CENTER);
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(header, BorderLayout.CENTER);

        // Chat Room Panel
        chatArea = new JTextArea();
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

    public void addChatRoom(String chatRoomName) {
        DefaultListModel<String> model = (DefaultListModel<String>) chatRoomList.getModel();
        model.addElement(chatRoomName);
    }

    public JList<String> getDiscoveryChatRooms() {
        return discoveryChatRooms;
    }

    public void displayMessage(String message) {
        chatArea.append(message + "\n");
    }

    public static void main(String[] args) {
        new Client();
    }

}
