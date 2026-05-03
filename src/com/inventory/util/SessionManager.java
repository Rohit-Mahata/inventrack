package com.inventory.util;

import com.inventory.model.User;
import java.util.UUID;

public class SessionManager {

    private static User currentUser = null;
    private static String sessionId = null;

    public static void setCurrentUser(User user) {
        currentUser = user;
        sessionId = UUID.randomUUID().toString();
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static String getSessionId() {
        return sessionId;
    }

    public static String getRole() {
        return currentUser != null ? currentUser.getRole() : "staff";
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.getRole().equals("admin");
    }

    public static void logout() {
        currentUser = null;
        sessionId = null;
    }
}