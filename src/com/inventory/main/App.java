package com.inventory.main;

import com.inventory.ui.LoginFrame;
import com.inventory.util.DBConnection;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class App {

    public static void main(String[] args) {
        // Initialize database first
        DBConnection.initializeDatabase();

        if (DBConnection.getConnection() == null) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                null,
                "Unable to start: SQLite JDBC driver is missing.\nAdd sqlite-jdbc to classpath and restart.",
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            ));
            return;
        }

        // Then launch UI
        SwingUtilities.invokeLater(() -> {
            new LoginFrame();
        });
    }
}