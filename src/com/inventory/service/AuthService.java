package com.inventory.service;

import com.inventory.dao.UserDAO;
import com.inventory.model.User;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public User authenticate(String username, String password) {
        if (username == null || password == null) return null;
        String cleanUsername = username.trim();
        String cleanPassword = password.trim();
        if (cleanUsername.isEmpty() || cleanPassword.isEmpty()) return null;
        return userDAO.login(cleanUsername, cleanPassword);
    }
}
