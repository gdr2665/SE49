package com.softwareengineering.finsage.model;

import java.time.LocalDateTime;

public class User {
    private String id;
    private String username;
    private String email;
    private String phone;
    private String password;
    private String resetCode;
    private LocalDateTime resetTime;

    // Constructors, getters and setters
    public User() {}

    public User(String id, String username, String email, String phone, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    // Getters and setters for all fields
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getResetCode() { return resetCode; }
    public void setResetCode(String resetCode) { this.resetCode = resetCode; }
    public LocalDateTime getResetTime() { return resetTime; }
    public void setResetTime(LocalDateTime resetTime) { this.resetTime = resetTime; }
}
