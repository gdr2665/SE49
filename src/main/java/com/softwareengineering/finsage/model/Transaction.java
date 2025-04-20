package com.softwareengineering.finsage.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaction {
    private String id;
    private BigDecimal amount;
    private LocalDate date;
    private String categoryId;
    private String note;
    private String userId;

    // Constructors, getters and setters
    public Transaction() {}

    public Transaction(String id, BigDecimal amount, LocalDate date, String categoryId, String note, String userId) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.categoryId = categoryId;
        this.note = note;
        this.userId = userId;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
