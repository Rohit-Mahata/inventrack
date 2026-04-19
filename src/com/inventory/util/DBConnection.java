package com.inventory.util;

import java.sql.*;

public class DBConnection {

    private static final String DB_URL = "jdbc:sqlite:inventory.db";
    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(DB_URL);
                System.out.println("Database connected!");
            }
        } catch (Exception e) {
            System.out.println("Connection error: " + e.getMessage());
        }
        return connection;
    }

    public static void initializeDatabase() {
        String usersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id       INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                role     TEXT NOT NULL DEFAULT 'staff',
                sync_status TEXT DEFAULT 'pending'
            );
        """;

        String productsTable = """
            CREATE TABLE IF NOT EXISTS products (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                name            TEXT NOT NULL,
                category        TEXT,
                quantity        INTEGER NOT NULL DEFAULT 0,
                price           REAL NOT NULL DEFAULT 0.0,
                low_stock_limit INTEGER DEFAULT 10,
                sync_status     TEXT DEFAULT 'pending'
            );
        """;

        String salesTable = """
            CREATE TABLE IF NOT EXISTS sales (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                product_id  INTEGER NOT NULL,
                quantity    INTEGER NOT NULL,
                total       REAL NOT NULL,
                sale_date   TEXT NOT NULL,
                sync_status TEXT DEFAULT 'pending',
                FOREIGN KEY (product_id) REFERENCES products(id)
            );
        """;

        String stockTable = """
            CREATE TABLE IF NOT EXISTS stock_movements (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                product_id  INTEGER NOT NULL,
                type        TEXT NOT NULL,
                quantity    INTEGER NOT NULL,
                note        TEXT,
                move_date   TEXT NOT NULL,
                sync_status TEXT DEFAULT 'pending',
                FOREIGN KEY (product_id) REFERENCES products(id)
            );
        """;

        String insertAdmin = """
            INSERT OR IGNORE INTO users (username, password, role)
            VALUES ('admin', 'admin123', 'admin');
        """;

        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(usersTable);
            stmt.execute(productsTable);
            stmt.execute(salesTable);
            stmt.execute(stockTable);
            stmt.execute(insertAdmin);
            System.out.println("Tables created successfully!");
        } catch (SQLException e) {
            System.out.println("Table creation error: " + e.getMessage());
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database closed!");
            }
        } catch (SQLException e) {
            System.out.println("Close error: " + e.getMessage());
        }
    }
}