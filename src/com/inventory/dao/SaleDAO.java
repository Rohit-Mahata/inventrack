package com.inventory.dao;

import com.inventory.model.Sale;
import com.inventory.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {

    // Add new sale
    public boolean addSale(Sale sale) {
        String sql = "INSERT INTO sales (product_id, quantity, total, sale_date, sync_status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, sale.getProductId());
            stmt.setInt(2, sale.getQuantity());
            stmt.setDouble(3, sale.getTotal());
            stmt.setString(4, sale.getSaleDate());
            stmt.setString(5, "pending");
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Add sale error: " + e.getMessage());
            return false;
        }
    }

    public Sale getSaleById(int id) {
        String sql = "SELECT s.*, p.name as product_name FROM sales s JOIN products p ON s.product_id = p.id WHERE s.id=?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapSale(rs);
            }
        } catch (SQLException e) {
            System.out.println("Get sale error: " + e.getMessage());
        }
        return null;
    }

    // Get all sales with product name
    public List<Sale> getAllSales() {
        List<Sale> sales = new ArrayList<>();
        String sql = """
            SELECT s.*, p.name as product_name
            FROM sales s
            JOIN products p ON s.product_id = p.id
            ORDER BY s.sale_date DESC
        """;
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                sales.add(mapSale(rs));
            }
        } catch (SQLException e) {
            System.out.println("Get all sales error: " + e.getMessage());
        }
        return sales;
    }

    // Get sales by date
    public List<Sale> getSalesByDate(String date) {
        List<Sale> sales = new ArrayList<>();
        String sql = """
            SELECT s.*, p.name as product_name
            FROM sales s
            JOIN products p ON s.product_id = p.id
            WHERE s.sale_date = ?
            ORDER BY s.sale_date DESC
        """;
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, date);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                sales.add(mapSale(rs));
            }
        } catch (SQLException e) {
            System.out.println("Get sales by date error: " + e.getMessage());
        }
        return sales;
    }

    // Get today's total sales amount
    public double getTodaySalesTotal() {
        String today = java.time.LocalDate.now().toString();
        String sql = "SELECT SUM(total) FROM sales WHERE sale_date = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, today);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.out.println("Today sales error: " + e.getMessage());
        }
        return 0.0;
    }

    // Get monthly total sales amount
    public double getMonthlySalesTotal() {
        String month = java.time.LocalDate.now().toString().substring(0, 7);
        String sql = "SELECT SUM(total) FROM sales WHERE sale_date LIKE ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, month + "%");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.out.println("Monthly sales error: " + e.getMessage());
        }
        return 0.0;
    }

    // Get recent sales limited by count
    public List<Sale> getRecentSales(int limit) {
        List<Sale> sales = new ArrayList<>();
        String sql = """
            SELECT s.*, p.name as product_name
            FROM sales s
            JOIN products p ON s.product_id = p.id
            ORDER BY s.id DESC
            LIMIT ?
        """;
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                sales.add(mapSale(rs));
            }
        } catch (SQLException e) {
            System.out.println("Recent sales error: " + e.getMessage());
        }
        return sales;
    }

    // Delete sale
    public boolean deleteSale(int id) {
        String sql = "DELETE FROM sales WHERE id=?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            boolean result = stmt.executeUpdate() > 0;
            if (result && com.inventory.util.FirebaseConfig.isConnected()) {
                com.inventory.util.FirebaseConfig.getDB().collection("sales").document(String.valueOf(id)).delete();
            }
            return result;
        } catch (SQLException e) {
            System.out.println("Delete sale error: " + e.getMessage());
            return false;
        }
    }

    // Map ResultSet to Sale object
    private Sale mapSale(ResultSet rs) throws SQLException {
        return new Sale(
            rs.getInt("id"),
            rs.getInt("product_id"),
            rs.getString("product_name"),
            rs.getInt("quantity"),
            rs.getDouble("total"),
            rs.getString("sale_date"),
            rs.getString("sync_status")
        );
    }
}