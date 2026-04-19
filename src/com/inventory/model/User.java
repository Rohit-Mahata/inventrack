package com.inventory.model;

public class User {

    private int id;
    private String username;
    private String password;
    private String role;
    private String syncStatus;

    public User() {}

    public User(int id, String username, String password, String role, String syncStatus) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.syncStatus = syncStatus;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }

    @Override
    public String toString() {
        return "User{id=" + id + ", username=" + username + ", role=" + role + "}";
    }
}