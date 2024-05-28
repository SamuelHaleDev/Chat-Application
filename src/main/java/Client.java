
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
import javax.swing.table.DefaultTableModel;
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

        // Add a change listener to the tabbed pane
        tabbedPane.addChangeListener(e -> {
            int index = tabbedPane.getSelectedIndex();
            if (index == 1) {
                displaySubscribedChatRooms(chatroomsPanel);
            } else {
                displayDiscoveryPage(discoverPanel);
            }
        });

        getContentPane().removeAll();
        add(tabbedPane);
        revalidate();
        repaint();
    }

    public void displayDiscoveryPage(JPanel panel) {
        setSize(500, 500);
        setLocationRelativeTo(null);

        panel.removeAll();

        // Chat Room Table Panel
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Chat Room");
        tableModel.addColumn("");

        DefaultListModel<String> model = (DefaultListModel<String>) discoveryChatRooms.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            tableModel.addRow(new Object[]{model.getElementAt(i), "Join"});
        }

        JTable chatRoomTable = new JTable(tableModel);
        chatRoomTable.getColumn("").setCellRenderer(new ButtonRenderer());
        chatRoomTable.getColumn("").setCellEditor(new ButtonEditor(new JCheckBox(), discoveryChatRooms, this.chatClientEndpoint));
        JScrollPane chatRoomScrollPane = new JScrollPane(chatRoomTable);

        // Header panel with a header label and a "Refresh" button
        JLabel header = new JLabel("Discover Chat Rooms", SwingConstants.CENTER);
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            // Add code here to process communication for chatroom discovery
            if (chatClientEndpoint == null) {
                System.out.println("Chat client endpoint is null");
            } else {
                chatClientEndpoint.getChatRooms(() -> {
                    // Refresh the table
                    updateTable(tableModel);
                });
            }
        });

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(header, BorderLayout.CENTER);
        headerPanel.add(refreshButton, BorderLayout.EAST);

        // Add components to the passed panel
        panel.setLayout(new BorderLayout());
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(chatRoomScrollPane, BorderLayout.CENTER);

        // Request the server for the list of chat rooms
        chatClientEndpoint.getChatRooms(() -> {
            // Refresh the table
            updateTable(tableModel);
        });


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

        // Chat Room Table Panel
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Chat Room");
        tableModel.addColumn("");

        DefaultListModel<String> model = (DefaultListModel<String>) chatRoomList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            tableModel.addRow(new Object[]{model.getElementAt(i), "Leave"});
        }

        JTable chatRoomTable = new JTable(tableModel);
        chatRoomTable.getColumn("").setCellRenderer(new ButtonRenderer());
        chatRoomTable.getColumn("").setCellEditor(new ButtonEditor(new JCheckBox(), chatRoomList, this.chatClientEndpoint));

        JScrollPane chatRoomScrollPane = new JScrollPane(chatRoomTable);

        // Add components to the passed panel
        panel.setLayout(new BorderLayout());
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(chatRoomScrollPane, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    public void updateTable(DefaultTableModel tableModel) {
        System.out.println("Updating table");

        // Clear the existing rows
        tableModel.setRowCount(0);

        // Get the updated chat room names
        DefaultListModel<String> model = (DefaultListModel<String>) discoveryChatRooms.getModel();

        // Add each chat room name to the table model
        for (int i = 0; i < model.getSize(); i++) {
            tableModel.addRow(new Object[]{model.getElementAt(i), "Join"});
        }
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

    public void removeChatRoom(String chatRoomName) {
        DefaultListModel<String> model = (DefaultListModel<String>) chatRoomList.getModel();
        model.removeElement(chatRoomName);
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
