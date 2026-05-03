package com.inventory.main;

import com.inventory.ui.LoginFrame;
import com.inventory.util.DBConnection;
import com.inventory.util.FirebaseConfig;
import com.inventory.util.SyncManager;
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

        // Initialize Firebase before launching UI to ensure session checks work
        FirebaseConfig.initialize();
        if (FirebaseConfig.isConnected()) {
            SyncManager.startAutoSync();
        } else {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                null,
                "Failed to connect to cloud services (Firebase).\n" +
                "Cloud sync will be disabled. Check your internet connection or system clock.",
                "Cloud Sync Offline",
                JOptionPane.WARNING_MESSAGE
            ));
        }

        // Add shutdown hook to clear active session on app close
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (com.inventory.util.SessionManager.getCurrentUser() != null && FirebaseConfig.isConnected()) {
                    String username = com.inventory.util.SessionManager.getCurrentUser().getUsername();
                    var db = FirebaseConfig.getDB();
                    var docRef = db.collection("active_sessions").document(username);
                    java.util.Map<String, Object> update = new java.util.HashMap<>();
                    update.put("active", false);
                    docRef.update(update).get();
                }
            } catch (Exception e) {
                // Ignore errors during shutdown
            }
        }));

        // Then launch UI
        SwingUtilities.invokeLater(() -> {
            new LoginFrame();
        });
    }
}