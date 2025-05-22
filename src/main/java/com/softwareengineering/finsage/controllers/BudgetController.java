package com.softwareengineering.finsage.controllers;

import com.softwareengineering.finsage.dao.BudgetDao;
import com.softwareengineering.finsage.dao.CategoryDao;
import com.softwareengineering.finsage.dao.TransactionDao;
import com.softwareengineering.finsage.model.Budget;
import com.softwareengineering.finsage.model.Category;
import com.softwareengineering.finsage.model.Transaction;
import com.softwareengineering.finsage.utils.UserLoginState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public class BudgetController {
    private BudgetDao budgetDao;
    private TransactionDao transactionDao;
    private CategoryDao categoryDao;

    public BudgetController() {
        this.budgetDao = new BudgetDao();
        this.transactionDao = new TransactionDao();
        this.categoryDao = new CategoryDao();
    }

    public List<Budget> getBudgetsByMonth(YearMonth month) {
        return budgetDao.getByMonthAndUserId(month, getCurrentUserId());
    }

    public Optional<Budget> getTotalBudget(YearMonth month) {
        return budgetDao.getTotalBudgetByMonthAndUserId(month, getCurrentUserId());
    }

    public Optional<Budget> getCategoryBudget(YearMonth month, String categoryId) {
        return budgetDao.getCategoryBudgetByMonthAndUserId(month, categoryId, getCurrentUserId());
    }

    public boolean saveBudget(Budget budget) {
        return budgetDao.saveOrUpdateBudget(budget);
    }

    public boolean deleteBudget(String budgetId) {
        return budgetDao.delete(budgetId);
    }

    public BigDecimal getTotalSpentAmount(YearMonth month) {
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();
        return transactionDao.getNetExpenseByDateRange(startDate, endDate, getCurrentUserId());
    }

    public BigDecimal getSpentAmount(YearMonth month, String categoryId) {
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        if (categoryId == null) {
            return getTotalSpentAmount(month);
        } else {
            return transactionDao.getByUserIdAndDateRange(getCurrentUserId(), startDate, endDate).stream()
                    .filter(t -> t.getCategoryId().equals(categoryId))
                    .map(t -> {
                        if (t.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                            return t.getAmount().abs();
                        }
                        return BigDecimal.ZERO;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    public List<Category> getCategories() {
        return categoryDao.getByUserId(getCurrentUserId());
    }

    public Category getCategoryById(String categoryId) {
        return categoryDao.getById(categoryId);
    }

    public String getCurrentUserId() {
        return UserLoginState.getCurrentUserId();
    }
}
