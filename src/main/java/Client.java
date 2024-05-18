
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

public class Client extends JFrame {
    private static JTextField usernameField;
    private static JButton connectButton;

    public Client() {
        setTitle("WhatsChat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);
        setLocationRelativeTo(null);

        // Initialize the username field and connect button
        usernameField = new JTextField(20);
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

    public static void connect() {
        // Get the username from the text field
        String username = usernameField.getText();

        // Connection logic goes here...

        // After connecting, hide the username field and connect
            // button
        usernameField.setVisible(false);
        connectButton.setVisible(false);
    }

    public static void main(String[] args) {
        new Client();
    }
}
