package com.inventory.dao;

import com.inventory.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SecurityQuestionDAO {

    // Predefined security questions
    public static final String[] QUESTIONS = {
        "What is your mother's name?",
        "What was the name of your first pet?",
        "What city were you born in?",
        "What is your favorite movie?"
    };

    // Save security questions for a user (replaces existing ones)
    public boolean saveQuestions(int userId, Map<String, String> questionsAndAnswers) {
        // First delete existing questions for this user
        deleteByUserId(userId);

        String sql = "INSERT INTO security_questions (user_id, question, answer) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            for (Map.Entry<String, String> entry : questionsAndAnswers.entrySet()) {
                stmt.setInt(1, userId);
                stmt.setString(2, entry.getKey());
                stmt.setString(3, entry.getValue().toLowerCase().trim());
                stmt.addBatch();
            }
            stmt.executeBatch();
            return true;
        } catch (SQLException e) {
            System.out.println("Save security questions error: " + e.getMessage());
            return false;
        }
    }

    // Get security questions for a user (returns question -> answer map)
    public Map<String, String> getQuestionsByUserId(int userId) {
        Map<String, String> qa = new LinkedHashMap<>();
        String sql = "SELECT question, answer FROM security_questions WHERE user_id = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                qa.put(rs.getString("question"), rs.getString("answer"));
            }
        } catch (SQLException e) {
            System.out.println("Get security questions error: " + e.getMessage());
        }
        return qa;
    }

    // Check if user has security questions set up
    public boolean hasQuestions(int userId) {
        String sql = "SELECT COUNT(*) FROM security_questions WHERE user_id = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.out.println("Check security questions error: " + e.getMessage());
        }
        return false;
    }

    // Delete all security questions for a user
    public void deleteByUserId(int userId) {
        String sql = "DELETE FROM security_questions WHERE user_id = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Delete security questions error: " + e.getMessage());
        }
    }
}
