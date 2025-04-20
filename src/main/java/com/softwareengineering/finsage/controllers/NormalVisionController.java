// File: com.softwareengineering.finsage.controllers.NormalVisionController.java
package com.softwareengineering.finsage.controllers;

import com.softwareengineering.finsage.model.Category;
import com.softwareengineering.finsage.model.Transaction;
import com.softwareengineering.finsage.utils.UserLoginState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class NormalVisionController {
    private TransactionController transactionController;
    private CategoryController categoryController;

    public NormalVisionController() {
        this.transactionController = new TransactionController();
        this.categoryController = new CategoryController();
    }

    public List<Transaction> getTransactions() {
        return transactionController.getTransactions();
    }

    public List<Transaction> getFilteredTransactions(
            String type,
            String categoryId,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount) {

        List<Transaction> transactions = transactionController.getTransactions();

        return transactions.stream()
                .filter(t -> {
                    // Filter by type
                    if ("INCOME".equals(type) && t.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                        return false;
                    }
                    if ("EXPENSE".equals(type) && t.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                        return false;
                    }
                    return true;
                })
                .filter(t -> categoryId == null || t.getCategoryId().equals(categoryId))
                .filter(t -> startDate == null || !t.getDate().isBefore(startDate))
                .filter(t -> endDate == null || !t.getDate().isAfter(endDate))
                .filter(t -> minAmount == null || t.getAmount().compareTo(minAmount) >= 0)
                .filter(t -> maxAmount == null || t.getAmount().compareTo(maxAmount) <= 0)
                .collect(Collectors.toList());
    }

    public List<Category> getCategories() {
        return categoryController.getCategories();
    }

    public Category getCategoryById(String categoryId) {
        return getCategories().stream()
                .filter(c -> c.getId().equals(categoryId))
                .findFirst()
                .orElse(null);
    }

    public boolean deleteTransaction(String transactionId) {
        return transactionController.deleteTransaction(transactionId);
    }
}
