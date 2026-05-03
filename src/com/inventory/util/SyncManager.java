package com.inventory.util;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentReference;
import com.inventory.dao.ProductDAO;
import com.inventory.dao.SaleDAO;
import com.inventory.dao.StockDAO;
import com.inventory.dao.UserDAO;
import com.inventory.model.Product;
import com.inventory.model.Sale;
import com.inventory.model.StockMovement;
import com.inventory.model.User;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SyncManager {

    private static ScheduledExecutorService scheduler;

    public static void syncAll() {
        if (!FirebaseConfig.isConnected()) {
            System.out.println("Sync skipped: Firebase not connected");
            return;
        }
        if (!isInternetAvailable()) {
            System.out.println("Sync skipped: No internet connection");
            return;
        }
        if (!checkSession()) return;
        try {
            pullAll();
            syncProducts();
            syncSales();
            syncStock();
            syncUsers();
            System.out.println("Sync completed successfully");
            if (com.inventory.ui.ContentPanel.getInstance() != null) {
                javax.swing.SwingUtilities.invokeLater(() -> com.inventory.ui.ContentPanel.getInstance().refreshCurrent());
            }
        } catch (Exception e) {
            System.out.println("Sync error: " + e.getMessage());
        }
    }

    private static boolean checkSession() {
        if (SessionManager.getCurrentUser() == null || SessionManager.getSessionId() == null) return true;
        try {
            var db = FirebaseConfig.getDB();
            var docRef = db.collection("active_sessions").document(SessionManager.getCurrentUser().getUsername());
            var docSnapshot = docRef.get().get();
            if (docSnapshot.exists()) {
                String remoteSessionId = docSnapshot.getString("sessionId");
                Boolean active = docSnapshot.getBoolean("active");
                if (active != null && active && remoteSessionId != null && !remoteSessionId.equals(SessionManager.getSessionId())) {
                    System.out.println("Session overridden by another device. Logging out.");
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        javax.swing.JOptionPane.showMessageDialog(null, "You have been logged out because this account signed in on another device.", "Session Expired", javax.swing.JOptionPane.WARNING_MESSAGE);
                        System.exit(0);
                    });
                    return false;
                }
            }
        } catch (Exception e) {
            System.out.println("Session check error: " + e.getMessage());
        }
        return true;
    }

    public static void syncProducts() {
        Firestore db = FirebaseConfig.getDB();
        ProductDAO dao = new ProductDAO();
        List<Product> products = dao.getAllProducts();
        for (Product p : products) {
            if (!"pending".equals(p.getSyncStatus())) continue;
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("id", p.getId());
                data.put("name", p.getName());
                data.put("category", p.getCategory());
                data.put("quantity", p.getQuantity());
                data.put("price", p.getPrice());
                data.put("lowStockLimit", p.getLowStockLimit());
                data.put("syncStatus", "synced");

                DocumentReference docRef = db.collection("products").document(String.valueOf(p.getId()));
                docRef.set(data).get();
                markAsSynced("products", p.getId());
            } catch (Exception e) {
                System.out.println("Sync product " + p.getId() + " error: " + e.getMessage());
            }
        }
    }

    public static void syncSales() {
        Firestore db = FirebaseConfig.getDB();
        SaleDAO dao = new SaleDAO();
        List<Sale> sales = dao.getAllSales();
        for (Sale s : sales) {
            if (!"pending".equals(s.getSyncStatus())) continue;
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("id", s.getId());
                data.put("productId", s.getProductId());
                data.put("productName", s.getProductName());
                data.put("quantity", s.getQuantity());
                data.put("total", s.getTotal());
                data.put("saleDate", s.getSaleDate());
                data.put("syncStatus", "synced");

                DocumentReference docRef = db.collection("sales").document(String.valueOf(s.getId()));
                docRef.set(data).get();
                markAsSynced("sales", s.getId());
            } catch (Exception e) {
                System.out.println("Sync sale " + s.getId() + " error: " + e.getMessage());
            }
        }
    }

    public static void syncStock() {
        Firestore db = FirebaseConfig.getDB();
        StockDAO dao = new StockDAO();
        List<StockMovement> movements = dao.getAllMovements();
        for (StockMovement m : movements) {
            if (!"pending".equals(m.getSyncStatus())) continue;
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("id", m.getId());
                data.put("productId", m.getProductId());
                data.put("productName", m.getProductName());
                data.put("type", m.getType());
                data.put("quantity", m.getQuantity());
                data.put("note", m.getNote());
                data.put("moveDate", m.getMoveDate());
                data.put("syncStatus", "synced");

                DocumentReference docRef = db.collection("stock_movements").document(String.valueOf(m.getId()));
                docRef.set(data).get();
                markAsSynced("stock_movements", m.getId());
            } catch (Exception e) {
                System.out.println("Sync stock " + m.getId() + " error: " + e.getMessage());
            }
        }
    }

    public static void syncUsers() {
        Firestore db = FirebaseConfig.getDB();
        UserDAO dao = new UserDAO();
        List<User> users = dao.getAllUsers();
        for (User u : users) {
            if (!"pending".equals(u.getSyncStatus())) continue;
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("id", u.getId());
                data.put("username", u.getUsername());
                // Never sync password field for security
                data.put("role", u.getRole());
                data.put("syncStatus", "synced");

                DocumentReference docRef = db.collection("users").document(String.valueOf(u.getId()));
                docRef.set(data).get();
                markAsSynced("users", u.getId());
            } catch (Exception e) {
                System.out.println("Sync user " + u.getId() + " error: " + e.getMessage());
            }
        }
    }

    public static void markAsSynced(String table, int id) {
        String sql = "UPDATE " + table + " SET sync_status = 'synced' WHERE id = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Mark synced error (" + table + ", " + id + "): " + e.getMessage());
        }
    }

    public static boolean isInternetAvailable() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("8.8.8.8", 53), 3000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void startAutoSync() {
        if (scheduler != null && !scheduler.isShutdown()) return;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SyncManager-AutoSync");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            try {
                syncAll();
            } catch (Exception e) {
                System.out.println("Auto-sync error: " + e.getMessage());
            }
        }, 10, 30, TimeUnit.SECONDS);
        System.out.println("Auto-sync started (every 30 seconds)");
    }

    public static void stopAutoSync() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            System.out.println("Auto-sync stopped");
        }
    }

    public static void pullAll() {
        pullProducts();
        pullUsers();
        pullSales();
        pullStock();
    }

    public static void pullProducts() {
        Firestore db = FirebaseConfig.getDB();
        try {
            List<com.google.cloud.firestore.QueryDocumentSnapshot> docs = db.collection("products").get().get().getDocuments();
            java.util.Set<Integer> remoteIds = new java.util.HashSet<>();
            ProductDAO dao = new ProductDAO();
            
            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : docs) {
                int id = Integer.parseInt(doc.getId());
                remoteIds.add(id);
                Product local = dao.getProductById(id);
                if (local == null || !"pending".equals(local.getSyncStatus())) {
                    Product p = new Product();
                    p.setId(id);
                    p.setName(doc.getString("name"));
                    p.setCategory(doc.getString("category"));
                    p.setQuantity(doc.getLong("quantity") != null ? doc.getLong("quantity").intValue() : 0);
                    p.setPrice(doc.getDouble("price") != null ? doc.getDouble("price") : 0.0);
                    p.setLowStockLimit(doc.getLong("lowStockLimit") != null ? doc.getLong("lowStockLimit").intValue() : 0);
                    p.setSyncStatus("synced");

                    if (local == null) {
                        String sql = "INSERT INTO products(id, name, category, quantity, price, low_stock_limit, sync_status) VALUES(?,?,?,?,?,?,?)";
                        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
                            stmt.setInt(1, p.getId());
                            stmt.setString(2, p.getName());
                            stmt.setString(3, p.getCategory());
                            stmt.setInt(4, p.getQuantity());
                            stmt.setDouble(5, p.getPrice());
                            stmt.setInt(6, p.getLowStockLimit());
                            stmt.setString(7, p.getSyncStatus());
                            stmt.executeUpdate();
                        }
                    } else {
                        String sql = "UPDATE products SET name=?, category=?, quantity=?, price=?, low_stock_limit=?, sync_status=? WHERE id=?";
                        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
                            stmt.setString(1, p.getName());
                            stmt.setString(2, p.getCategory());
                            stmt.setInt(3, p.getQuantity());
                            stmt.setDouble(4, p.getPrice());
                            stmt.setInt(5, p.getLowStockLimit());
                            stmt.setString(6, p.getSyncStatus());
                            stmt.setInt(7, p.getId());
                            stmt.executeUpdate();
                        }
                    }
                }
            }
            // Handle deletes
            List<Product> locals = dao.getAllProducts();
            for (Product p : locals) {
                if ("synced".equals(p.getSyncStatus()) && !remoteIds.contains(p.getId())) {
                    try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement("DELETE FROM products WHERE id=?")) {
                        stmt.setInt(1, p.getId());
                        stmt.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Pull products error: " + e.getMessage());
        }
    }

    public static void pullUsers() {
        Firestore db = FirebaseConfig.getDB();
        try {
            List<com.google.cloud.firestore.QueryDocumentSnapshot> docs = db.collection("users").get().get().getDocuments();
            java.util.Set<String> remoteIds = new java.util.HashSet<>();
            UserDAO dao = new UserDAO();
            
            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : docs) {
                String username = doc.getId();
                remoteIds.add(username);
                User local = dao.getUserByUsername(username);
                
                if (local == null || !"pending".equals(local.getSyncStatus())) {
                    if (local == null) {
                        String sql = "INSERT INTO users(username, password, role, sync_status) VALUES(?,?,?,?)";
                        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
                            stmt.setString(1, username);
                            stmt.setString(2, doc.getString("password"));
                            stmt.setString(3, doc.getString("role"));
                            stmt.setString(4, "synced");
                            stmt.executeUpdate();
                        }
                    } else {
                        String sql = "UPDATE users SET password=?, role=?, sync_status=? WHERE username=?";
                        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
                            stmt.setString(1, doc.getString("password"));
                            stmt.setString(2, doc.getString("role"));
                            stmt.setString(3, "synced");
                            stmt.setString(4, username);
                            stmt.executeUpdate();
                        }
                    }
                }
            }
            // Handle deletes
            List<User> locals = dao.getAllUsers();
            for (User u : locals) {
                if ("synced".equals(u.getSyncStatus()) && !remoteIds.contains(u.getUsername())) {
                    try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement("DELETE FROM users WHERE username=?")) {
                        stmt.setString(1, u.getUsername());
                        stmt.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Pull users error: " + e.getMessage());
        }
    }

    public static void pullSales() {
        Firestore db = FirebaseConfig.getDB();
        try {
            List<com.google.cloud.firestore.QueryDocumentSnapshot> docs = db.collection("sales").get().get().getDocuments();
            java.util.Set<Integer> remoteIds = new java.util.HashSet<>();
            SaleDAO dao = new SaleDAO();
            
            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : docs) {
                int id = Integer.parseInt(doc.getId());
                remoteIds.add(id);
                Sale local = dao.getSaleById(id);
                
                if (local == null || !"pending".equals(local.getSyncStatus())) {
                    if (local == null) {
                        String sql = "INSERT INTO sales(id, product_id, product_name, quantity, total, sale_date, sync_status) VALUES(?,?,?,?,?,?,?)";
                        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
                            stmt.setInt(1, id);
                            stmt.setInt(2, doc.getLong("productId").intValue());
                            stmt.setString(3, doc.getString("productName"));
                            stmt.setInt(4, doc.getLong("quantity").intValue());
                            stmt.setDouble(5, doc.getDouble("total"));
                            stmt.setString(6, doc.getString("saleDate"));
                            stmt.setString(7, "synced");
                            stmt.executeUpdate();
                        }
                    } else {
                        String sql = "UPDATE sales SET product_id=?, product_name=?, quantity=?, total=?, sale_date=?, sync_status=? WHERE id=?";
                        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
                            stmt.setInt(1, doc.getLong("productId").intValue());
                            stmt.setString(2, doc.getString("productName"));
                            stmt.setInt(3, doc.getLong("quantity").intValue());
                            stmt.setDouble(4, doc.getDouble("total"));
                            stmt.setString(5, doc.getString("saleDate"));
                            stmt.setString(6, "synced");
                            stmt.setInt(7, id);
                            stmt.executeUpdate();
                        }
                    }
                }
            }
            // Handle deletes
            List<Sale> locals = dao.getAllSales();
            for (Sale s : locals) {
                if ("synced".equals(s.getSyncStatus()) && !remoteIds.contains(s.getId())) {
                    try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement("DELETE FROM sales WHERE id=?")) {
                        stmt.setInt(1, s.getId());
                        stmt.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Pull sales error: " + e.getMessage());
        }
    }

    public static void pullStock() {
        Firestore db = FirebaseConfig.getDB();
        try {
            List<com.google.cloud.firestore.QueryDocumentSnapshot> docs = db.collection("stock_movements").get().get().getDocuments();
            java.util.Set<Integer> remoteIds = new java.util.HashSet<>();
            StockDAO dao = new StockDAO();
            
            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : docs) {
                int id = Integer.parseInt(doc.getId());
                remoteIds.add(id);
                StockMovement local = dao.getMovementById(id);
                
                if (local == null || !"pending".equals(local.getSyncStatus())) {
                    if (local == null) {
                        String sql = "INSERT INTO stock_movements(id, product_id, product_name, type, quantity, note, move_date, sync_status) VALUES(?,?,?,?,?,?,?,?)";
                        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
                            stmt.setInt(1, id);
                            stmt.setInt(2, doc.getLong("productId").intValue());
                            stmt.setString(3, doc.getString("productName"));
                            stmt.setString(4, doc.getString("type"));
                            stmt.setInt(5, doc.getLong("quantity").intValue());
                            stmt.setString(6, doc.getString("note"));
                            stmt.setString(7, doc.getString("moveDate"));
                            stmt.setString(8, "synced");
                            stmt.executeUpdate();
                        }
                    } else {
                        String sql = "UPDATE stock_movements SET product_id=?, product_name=?, type=?, quantity=?, note=?, move_date=?, sync_status=? WHERE id=?";
                        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
                            stmt.setInt(1, doc.getLong("productId").intValue());
                            stmt.setString(2, doc.getString("productName"));
                            stmt.setString(3, doc.getString("type"));
                            stmt.setInt(4, doc.getLong("quantity").intValue());
                            stmt.setString(5, doc.getString("note"));
                            stmt.setString(6, doc.getString("moveDate"));
                            stmt.setString(7, "synced");
                            stmt.setInt(8, id);
                            stmt.executeUpdate();
                        }
                    }
                }
            }
            // Handle deletes
            List<StockMovement> locals = dao.getAllMovements();
            for (StockMovement m : locals) {
                if ("synced".equals(m.getSyncStatus()) && !remoteIds.contains(m.getId())) {
                    try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement("DELETE FROM stock_movements WHERE id=?")) {
                        stmt.setInt(1, m.getId());
                        stmt.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Pull stock error: " + e.getMessage());
        }
    }
}
