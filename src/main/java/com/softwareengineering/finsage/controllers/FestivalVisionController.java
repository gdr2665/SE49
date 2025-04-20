// File: com.softwareengineering.finsage.controllers.FestivalVisionController.java
package com.softwareengineering.finsage.controllers;

import com.softwareengineering.finsage.model.Category;
import com.softwareengineering.finsage.model.Holiday;
import com.softwareengineering.finsage.model.Transaction;
import com.softwareengineering.finsage.utils.UserLoginState;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class FestivalVisionController {
    private TransactionController transactionController;
    private CategoryController categoryController;
    private HolidayController holidayController;

    public FestivalVisionController() {
        this.transactionController = new TransactionController();
        this.categoryController = new CategoryController();
        this.holidayController = new HolidayController();
    }

    public List<Transaction> getTransactions() {
        return transactionController.getTransactions();
    }

    public List<Transaction> getFilteredTransactions(String categoryId, Holiday holiday) {
        List<Transaction> transactions = transactionController.getTransactions();

        return transactions.stream()
                .filter(t -> categoryId == null || t.getCategoryId().equals(categoryId))
                .filter(t -> {
                    if (holiday == null) return true;
                    LocalDate transactionDate = t.getDate(); // Assuming getDate() returns LocalDate
                    return !transactionDate.isBefore(holiday.getStartDate()) &&
                            !transactionDate.isAfter(holiday.getEndDate());
                })
                .collect(Collectors.toList());
    }

    public List<Category> getCategories() {
        return categoryController.getCategories();
    }

    public List<Holiday> getHolidays() {
        return holidayController.getHolidays();
    }

    public Category getCategoryById(String categoryId) {
        return getCategories().stream()
                .filter(c -> c.getId().equals(categoryId))
                .findFirst()
                .orElse(null);
    }
}
