
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
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.websocket.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Client extends JFrame {
    private ChatClientEndpoint chatClientEndpoint;
    private static final Logger logger = LogManager.getLogger(Client.class);
    private static JTextField usernameField;
    private JTextPane chatArea;
    private static String username;
    private JList<String> chatRoomList;
    private JTabbedPane tabbedPane;
    private JList<String> discoveryChatRooms;
    private String currentChatRoom;

    public Client() {
        try {
            // Set the look and feel to the system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Change the default colors
            UIManager.put("Panel.background", Color.DARK_GRAY);
            UIManager.put("Button.background", Color.LIGHT_GRAY);
            UIManager.put("Button.foreground", Color.BLACK);
            UIManager.put("Label.foreground", Color.WHITE);
            UIManager.put("TextField.background", Color.LIGHT_GRAY);
            UIManager.put("TextField.foreground", Color.BLACK);
            UIManager.put("List.background", Color.LIGHT_GRAY);
            UIManager.put("List.foreground", Color.BLACK);
            UIManager.put("TabbedPane.background", Color.DARK_GRAY);
            UIManager.put("TabbedPane.foreground", Color.WHITE);
            UIManager.put("Table.background", Color.LIGHT_GRAY);
            UIManager.put("Table.foreground", Color.BLACK);
            UIManager.put("OptionPane.background", Color.DARK_GRAY);
            UIManager.put("OptionPane.foreground", Color.WHITE);

            // Update the UI of all components in the frame
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("WhatsChat");
        setIconImage(new ImageIcon("images/WhatsChat_Logo.png").getImage());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 500);
        setLocationRelativeTo(null);

        DefaultListModel<String> model = new DefaultListModel<>();
        chatRoomList = new JList<>(model);
        discoveryChatRooms = new JList<>(new DefaultListModel<>());

        // Initialize the username field and connect button
        usernameField = new JTextField(20);
        usernameField.addActionListener(e -> connect());

        JButton connectButton = new JButton("Connect");

        // Add an action listener to the connect button
        connectButton.addActionListener(e -> connect());

        // Create a panel and add the username field and connect
            // button to it
        JPanel panel = new JPanel(new FlowLayout());

        JLabel label = new JLabel("Username", SwingConstants.CENTER);

        // Set the font, foreground color, and alignment of the JLabel
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        label.setHorizontalAlignment(JLabel.LEFT);

        panel.add(Box.createVerticalBox());
        panel.add(label);
        panel.add(Box.createVerticalBox());
        panel.add(usernameField);
        panel.add(Box.createVerticalBox());
        panel.add(connectButton);

        // Add the panel to the frame
        add(panel);

        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        setVisible(true);

        connectButton.requestFocusInWindow();
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

        JPanel chatroomsPanel = new JPanel();
        chatroomsPanel.setLayout(new BorderLayout());
        displaySubscribedChatRooms(chatroomsPanel);
        tabbedPane.addTab("Chat Rooms", chatroomsPanel);

        JPanel discoverPanel = new JPanel();
        discoverPanel.setLayout(new BorderLayout());
        displayDiscoveryPage(discoverPanel);
        tabbedPane.addTab("Discover", discoverPanel);

        // Add a change listener to the tabbed pane
        tabbedPane.addChangeListener(e -> {
            int index = tabbedPane.getSelectedIndex();
            if (index == 0) {
                displaySubscribedChatRooms(chatroomsPanel);
            } else {
                displayDiscoveryPage(discoverPanel);
            }
        });

        tabbedPane.setSelectedIndex(0);

        getContentPane().removeAll();
        add(tabbedPane);
        revalidate();
        repaint();
    }

    public void displayDiscoveryPage(JPanel panel) {
        setSize(500, 500);

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
        ActionListener refreshAction = e -> {
            // Add code here to process communication for chatroom discovery
            if (chatClientEndpoint == null) {
                System.out.println("Chat client endpoint is null");
            } else {
                chatClientEndpoint.getChatRooms(() -> {
                    // Refresh the table
                    updateTable(tableModel);
                });
            }
        };
        refreshButton.addActionListener(refreshAction);

        Timer timer = new Timer(5000, refreshAction);
        timer.start();

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

                SwingUtilities.invokeLater(() -> displaySubscribedChatRooms(panel));
            });

            chatRoomNameField.addActionListener(e1 -> createButton.doClick());

            JPanel formPanel = new JPanel();
            formPanel.add(chatRoomNameField);
            formPanel.add(createButton);

            JDialog dialog = new JDialog();
            dialog.setModal(true);
            dialog.setTitle("Create a new chat room");
            dialog.setContentPane(formPanel);
            dialog.pack();
            dialog.setLocationRelativeTo(this);
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
        chatRoomTable.getColumn("Chat Room").setCellRenderer(new ButtonRenderer());
        chatRoomTable.getColumn("Chat Room").setCellEditor(new ButtonEditor(new JCheckBox(), chatRoomList, this.chatClientEndpoint));
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

    public void displayChatRoom(String chatRoomName) {
        setSize(500, 500);

        this.currentChatRoom = chatRoomName;

        // Header panel with back button and header label
        JButton backButton = new JButton("\u2190");
        backButton.addActionListener(e -> initializeNavigationPanel());
        JLabel header = new JLabel(chatRoomName, SwingConstants.CENTER);
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(header, BorderLayout.CENTER);

        // Chat Room Panel
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setContentType("text/html");
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        // Footer Panel with text field and send button
        JTextField messageField = new JTextField(20);
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> {
            String message = messageField.getText();
            chatClientEndpoint.sendMessage(chatRoomName, username, message);
            messageField.setText("");
        });

        messageField.addActionListener(e1 -> sendButton.doClick());
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.add(messageField, BorderLayout.CENTER);
        footerPanel.add(sendButton, BorderLayout.EAST);

        // Main panel
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(chatScrollPane, BorderLayout.CENTER);
        panel.add(footerPanel, BorderLayout.SOUTH);

        // Get chatroom history
        chatClientEndpoint.getMessageHistory(chatRoomName, username, (String[] messages) -> {
            for (String message : messages) {
                if (!message.isEmpty()) {
                    displayMessage(message);
                }
            }
        });

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
        // Extract the time and time zone from message that is in the format "[timestamp timezone]: username: message
        String[] parts = message.split("[\\[\\]]", 3);
        String tzandts = parts[1];
        String[] parts2 = parts[2].split(":", 4);
        String roomName = parts2[1].replace(" ", "");

        if (!roomName.equals(this.currentChatRoom)) {
            return;
        }

        String username = parts2[2].replace(" ", "");
        if (username.equals(this.username)) {
            username = "You";
        }
        String content = parts2[3].trim();

        String[] timeParts = tzandts.split(" ", 3);
        String timestamp = timeParts[0] + " " + timeParts[1];
        String timezone = timeParts[2];

        // Get clients time zone in GMT-00:00 format
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        String clientTimeZone = "GMT" + zdt.getOffset().toString();

        // Server and client timezone are both in "GMT-00:00" format convert the timestamp to clients time zone
        String clientTime = convertServerTimeToClientTime(timezone, clientTimeZone, timestamp);

        // If the clientTime has a leading 0 remove it e.g. "01:46" -> "1:46"
        if (clientTime.startsWith("0")) {
            clientTime = clientTime.substring(1);
        }

        String alignment = (username.equals("You") || username.equals(this.username)) ? "right" : "left";
        String htmlText = "<div style=\"text-align: " + alignment + "\">" + clientTime + " " + username + ": " + content + "</div>";

        HTMLEditorKit kit = (HTMLEditorKit) chatArea.getEditorKit();
        HTMLDocument doc = (HTMLDocument) chatArea.getDocument();

        try {
            kit.insertHTML(doc, doc.getLength(), htmlText, 0, 0, null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    /*
    *
    * Purpose: Convert the server time to the client time zone
    *
    * serverTz: The server time zone in "GMT-00:00" format
    * clientTz: The client time zone in "GMT-00:00" format
    * serverTime: The server time in "hh:mm a" format
    * */
    String convertServerTimeToClientTime(String serverTz, String clientTz, String serverTime) {
        // Parse the server time zone and client time zone
        ZoneId serverZoneId = ZoneId.of(serverTz.replace("GMT", "UTC"));
        ZoneId clientZoneId = ZoneId.of(clientTz.replace("GMT", "UTC"));

        // Parse the server time
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        LocalTime localServerTime = LocalTime.parse(serverTime, timeFormatter);

        // Create a ZonedDateTime object for the server time
        ZonedDateTime serverZonedDateTime = ZonedDateTime.of(
                1970, 1, 1, localServerTime.getHour(), localServerTime.getMinute(), 0, 0, serverZoneId
        );

        // Convert to the client time zone
        ZonedDateTime clientZonedDateTime = serverZonedDateTime.withZoneSameInstant(clientZoneId);

        // Format the client time
        String clientTime = clientZonedDateTime.format(timeFormatter);

        return clientTime;
    }

    public static void main(String[] args) {
        new Client();
    }

}
