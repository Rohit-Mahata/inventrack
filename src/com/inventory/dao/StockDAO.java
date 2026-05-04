package com.inventory.dao;

import com.inventory.model.StockMovement;
import com.inventory.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockDAO {

    // Add stock movement (in or out)
    public boolean addMovement(StockMovement movement) {
        String sql = "INSERT INTO stock_movements (product_id, type, quantity, note, move_date, sync_status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, movement.getProductId());
            stmt.setString(2, movement.getType());
            stmt.setInt(3, movement.getQuantity());
            stmt.setString(4, movement.getNote());
            stmt.setString(5, movement.getMoveDate());
            stmt.setString(6, "pending");
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Add movement error: " + e.getMessage());
            return false;
        }
    }

    public StockMovement getMovementById(int id) {
        String sql = "SELECT sm.*, p.name as product_name FROM stock_movements sm JOIN products p ON sm.product_id = p.id WHERE sm.id=?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapMovement(rs);
            }
        } catch (SQLException e) {
            System.out.println("Get movement error: " + e.getMessage());
        }
        return null;
    }

    // Get all stock movements with product name
    public List<StockMovement> getAllMovements() {
        List<StockMovement> movements = new ArrayList<>();
        String sql = """
            SELECT sm.*, p.name as product_name
            FROM stock_movements sm
            JOIN products p ON sm.product_id = p.id
            ORDER BY sm.id DESC
        """;
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                movements.add(mapMovement(rs));
            }
        } catch (SQLException e) {
            System.out.println("Get movements error: " + e.getMessage());
        }
        return movements;
    }

    // Get movements by product id
    public List<StockMovement> getMovementsByProduct(int productId) {
        List<StockMovement> movements = new ArrayList<>();
        String sql = """
            SELECT sm.*, p.name as product_name
            FROM stock_movements sm
            JOIN products p ON sm.product_id = p.id
            WHERE sm.product_id = ?
            ORDER BY sm.id DESC
        """;
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                movements.add(mapMovement(rs));
            }
        } catch (SQLException e) {
            System.out.println("Get movements by product error: " + e.getMessage());
        }
        return movements;
    }

    // Get movements by type (IN or OUT)
    public List<StockMovement> getMovementsByType(String type) {
        List<StockMovement> movements = new ArrayList<>();
        String sql = """
            SELECT sm.*, p.name as product_name
            FROM stock_movements sm
            JOIN products p ON sm.product_id = p.id
            WHERE sm.type = ?
            ORDER BY sm.id DESC
        """;
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, type);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                movements.add(mapMovement(rs));
            }
        } catch (SQLException e) {
            System.out.println("Get movements by type error: " + e.getMessage());
        }
        return movements;
    }

    // Get recent movements limited by count
    public List<StockMovement> getRecentMovements(int limit) {
        List<StockMovement> movements = new ArrayList<>();
        String sql = """
            SELECT sm.*, p.name as product_name
            FROM stock_movements sm
            JOIN products p ON sm.product_id = p.id
            ORDER BY sm.id DESC
            LIMIT ?
        """;
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                movements.add(mapMovement(rs));
            }
        } catch (SQLException e) {
            System.out.println("Recent movements error: " + e.getMessage());
        }
        return movements;
    }

    // Map ResultSet to StockMovement object
    private StockMovement mapMovement(ResultSet rs) throws SQLException {
        return new StockMovement(
            rs.getInt("id"),
            rs.getInt("product_id"),
            rs.getString("product_name"),
            rs.getString("type"),
            rs.getInt("quantity"),
            rs.getString("note"),
            rs.getString("move_date"),
            rs.getString("sync_status")
        );
    }
}