package com.softwareengineering.finsage.model;

public class Category {
    private String id;
    private String name;
    private String userId;

    // Constructors, getters and setters
    public Category() {}

    public Category(String id, String name, String userId) {
        this.id = id;
        this.name = name;
        this.userId = userId;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
