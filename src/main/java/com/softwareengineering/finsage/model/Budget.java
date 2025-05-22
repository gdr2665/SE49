package com.softwareengineering.finsage.model;

import java.math.BigDecimal;
import java.time.YearMonth;

public class Budget {
    private String id;
    private YearMonth month;
    private String categoryId; // null for total budget
    private BigDecimal amount;
    private String userId;

    public Budget() {}

    public Budget(String id, YearMonth month, String categoryId, BigDecimal amount, String userId) {
        this.id = id;
        this.month = month;
        this.categoryId = categoryId;
        this.amount = amount;
        this.userId = userId;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public YearMonth getMonth() { return month; }
    public void setMonth(YearMonth month) { this.month = month; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public boolean isTotalBudget() {
        return categoryId == null;
    }
}
