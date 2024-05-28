import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ButtonRenderer extends JButton implements TableCellRenderer {
    public ButtonRenderer() {
        setOpaque(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setText((value == null) ? "" : value.toString());
        return this;
    }
}

class ButtonEditor extends DefaultCellEditor {
    protected JButton button;
    private String label;
    private boolean isPushed;
    private JList<String> discoveryChatRooms;
    private int row;
    private ChatClientEndpoint chatClientEndpoint;

    public ButtonEditor(JCheckBox checkBox, JList<String> discoveryChatRooms, ChatClientEndpoint chatClientEndpoint) {
        super(checkBox);
        this.discoveryChatRooms = discoveryChatRooms;
        this.chatClientEndpoint = chatClientEndpoint;
        button = new JButton();
        button.setOpaque(true);
        button.addActionListener(e -> fireEditingStopped());
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (isSelected) {
            button.setForeground(table.getSelectionForeground());
            button.setBackground(table.getSelectionBackground());
        } else {
            button.setForeground(table.getForeground());
            button.setBackground(table.getBackground());
        }
        label = (value == null) ? "" : value.toString();
        button.setText(label);
        isPushed = true;

        // Add an action listener to the button
        button.addActionListener(e -> {
            // Get the chat room name from the first column of the current row
            String chatRoomName = table.getModel().getValueAt(row, 0).toString();

            if (label.equals("Join")) {
                // Join the chat room
                chatClientEndpoint.joinChatRoom(chatRoomName);

                // Show a confirmation dialog
                JOptionPane.showMessageDialog(null, "Joined chat room '" + chatRoomName + "' successfully!");
            } else if (label.equals("Leave")) {
                // Leave the chat room
                chatClientEndpoint.leaveChatRoom(chatRoomName);

                // Show a confirmation dialog
                JOptionPane.showMessageDialog(null, "Left chat room '" + chatRoomName + "' successfully!");
            }
        });

        return button;
    }

    public Object getCellEditorValue() {
        if (isPushed) {
            // Check if the row index is within the bounds of the discoveryChatRooms list
            if (row >= 0 && row < discoveryChatRooms.getModel().getSize()) {
                // Get the chat room name from the discoveryChatRooms list
                String chatRoomName = discoveryChatRooms.getModel().getElementAt(row);
            }
            // Add code here to join the chat room
        }
        isPushed = false;
        return label;
    }

    public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing();
    }

    protected void fireEditingStopped() {
        super.fireEditingStopped();
    }
}
