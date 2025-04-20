package com.softwareengineering.finsage.model;

import java.time.LocalDate;

public class Holiday {
    private String id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String userId;

    // Constructors, getters and setters
    public Holiday() {}

    public Holiday(String id, String name, LocalDate startDate, LocalDate endDate, String userId) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.userId = userId;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
