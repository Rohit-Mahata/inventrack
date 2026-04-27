package com.inventory.util;

import com.inventory.model.User;

public class SessionManager {

    private static User currentUser = null;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static String getRole() {
        return currentUser != null ? currentUser.getRole() : "staff";
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.getRole().equals("admin");
    }

    public static void logout() {
        currentUser = null;
    }
}