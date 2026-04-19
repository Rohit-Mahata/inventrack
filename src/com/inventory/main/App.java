package com.inventory.main;

import com.inventory.ui.LoginFrame;
import com.inventory.util.DBConnection;
import javax.swing.SwingUtilities;

public class App {

    public static void main(String[] args) {
        // Initialize database first
        DBConnection.initializeDatabase();

        // Then launch UI
        SwingUtilities.invokeLater(() -> {
            new LoginFrame();
        });
    }
}