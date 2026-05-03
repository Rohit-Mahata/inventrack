package com.inventory.dao;

import com.inventory.model.Product;
import com.inventory.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // Add new product
    public boolean addProduct(Product product) {
        String sql = "INSERT INTO products (name, category, quantity, price, low_stock_limit, sync_status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getCategory());
            stmt.setInt(3, product.getQuantity());
            stmt.setDouble(4, product.getPrice());
            stmt.setInt(5, product.getLowStockLimit());
            stmt.setString(6, "pending");
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Add product error: " + e.getMessage());
            return false;
        }
    }

    // Update existing product
    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET name=?, category=?, quantity=?, price=?, low_stock_limit=?, sync_status=? WHERE id=?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getCategory());
            stmt.setInt(3, product.getQuantity());
            stmt.setDouble(4, product.getPrice());
            stmt.setInt(5, product.getLowStockLimit());
            stmt.setString(6, "pending");
            stmt.setInt(7, product.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Update product error: " + e.getMessage());
            return false;
        }
    }

    // Delete product
    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id=?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            boolean result = stmt.executeUpdate() > 0;
            if (result && com.inventory.util.FirebaseConfig.isConnected()) {
                com.inventory.util.FirebaseConfig.getDB().collection("products").document(String.valueOf(id)).delete();
            }
            return result;
        } catch (SQLException e) {
            System.out.println("Delete product error: " + e.getMessage());
            return false;
        }
    }

    // Get all products
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY name ASC";
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        } catch (SQLException e) {
            System.out.println("Get all products error: " + e.getMessage());
        }
        return products;
    }

    // Search products by name
    public List<Product> searchProducts(String keyword) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE name LIKE ? ORDER BY name ASC";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        } catch (SQLException e) {
            System.out.println("Search products error: " + e.getMessage());
        }
        return products;
    }

    // Get low stock products
    public List<Product> getLowStockProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE quantity <= low_stock_limit ORDER BY quantity ASC";
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        } catch (SQLException e) {
            System.out.println("Low stock error: " + e.getMessage());
        }
        return products;
    }

    // Get total product count
    public int getTotalProducts() {
        String sql = "SELECT COUNT(*) FROM products";
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Count error: " + e.getMessage());
        }
        return 0;
    }

    // Get product by id
    public Product getProductById(int id) {
        String sql = "SELECT * FROM products WHERE id=?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapProduct(rs);
        } catch (SQLException e) {
            System.out.println("Get product error: " + e.getMessage());
        }
        return null;
    }

    // Get product by name (case-insensitive)
    public Product getProductByName(String name) {
        String sql = "SELECT * FROM products WHERE LOWER(name) = LOWER(?)";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapProduct(rs);
        } catch (SQLException e) {
            System.out.println("Get product by name error: " + e.getMessage());
        }
        return null;
    }

    // Upsert: update existing product by name or insert new
    public boolean upsertProduct(Product product) {
        Product existing = getProductByName(product.getName());
        if (existing != null) {
            product.setId(existing.getId());
            return updateProduct(product);
        } else {
            return addProduct(product);
        }
    }

    // Find exact match by name, price, and lowStockLimit
    public Product findExactMatch(String name, double price, int lowStockLimit) {
        String sql = "SELECT * FROM products WHERE LOWER(name) = LOWER(?) AND price = ? AND low_stock_limit = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setDouble(2, price);
            stmt.setInt(3, lowStockLimit);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapProduct(rs);
        } catch (SQLException e) {
            System.out.println("Find exact match error: " + e.getMessage());
        }
        return null;
    }

    // Add quantity to existing product
    public boolean addQuantityToExisting(int productId, int additionalQuantity) {
        String sql = "UPDATE products SET quantity = quantity + ?, sync_status = 'pending' WHERE id = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, additionalQuantity);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Add quantity error: " + e.getMessage());
            return false;
        }
    }

    // Map ResultSet to Product object
    private Product mapProduct(ResultSet rs) throws SQLException {
        return new Product(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("category"),
            rs.getInt("quantity"),
            rs.getDouble("price"),
            rs.getInt("low_stock_limit"),
            rs.getString("sync_status")
        );
    }
}