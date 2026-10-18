package com.syos.domain.entity;

public class User {
    private String username;
    private String role; // "CASHIER" or "MANAGER"

    public User(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public String getUsername() { return username; }
    public String getRole() { return role; }

    public boolean isManager() { return "MANAGER".equalsIgnoreCase(role); }
}