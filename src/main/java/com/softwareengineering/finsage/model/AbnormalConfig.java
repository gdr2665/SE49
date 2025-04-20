package com.softwareengineering.finsage.model;

import java.math.BigDecimal;

public class AbnormalConfig {
    private String id;
    private String itemName;
    private BigDecimal threshold;
    private String userId;

    // Constructors, getters and setters
    public AbnormalConfig() {}

    public AbnormalConfig(String id, String itemName, BigDecimal threshold, String userId) {
        this.id = id;
        this.itemName = itemName;
        this.threshold = threshold;
        this.userId = userId;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public BigDecimal getThreshold() { return threshold; }
    public void setThreshold(BigDecimal threshold) { this.threshold = threshold; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
