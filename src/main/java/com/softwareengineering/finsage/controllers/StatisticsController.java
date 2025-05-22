package com.softwareengineering.finsage.controllers;

import com.softwareengineering.finsage.dao.CategoryDao;
import com.softwareengineering.finsage.dao.TransactionDao;
import com.softwareengineering.finsage.model.Category;
import com.softwareengineering.finsage.model.Transaction;
import com.softwareengineering.finsage.utils.UserLoginState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsController {
    private TransactionDao transactionDao;
    private CategoryDao categoryDao;

    public StatisticsController() {
        this.transactionDao = new TransactionDao();
        this.categoryDao = new CategoryDao();
    }

    public List<Transaction> getTransactions(boolean isExpense, YearMonth month) {
        String userId = UserLoginState.getCurrentUserId();
        List<Transaction> transactions = transactionDao.getByUserId(userId);

        return transactions.stream()
                .filter(t -> isExpense ? t.getAmount().compareTo(BigDecimal.ZERO) < 0 : t.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .filter(t -> month == null || YearMonth.from(t.getDate()).equals(month))
                .collect(Collectors.toList());
    }

    public List<Transaction> getTop10Transactions(boolean isExpense, YearMonth month) {
        return getTransactions(isExpense, month).stream()
                .sorted(Comparator.comparing(t -> isExpense ? t.getAmount() : t.getAmount().negate(), Comparator.reverseOrder()))
                .limit(10)
                .collect(Collectors.toList());
    }

    public Map<String, BigDecimal> getCategorySummary(boolean isExpense, YearMonth month) {
        String userId = UserLoginState.getCurrentUserId();
        List<Transaction> transactions = getTransactions(isExpense, month);

        return transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getCategoryId,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                t -> isExpense ? t.getAmount().abs() : t.getAmount(),
                                BigDecimal::add
                        )
                ));
    }

    public Map<YearMonth, BigDecimal> getMonthlySummary(boolean isExpense) {
        String userId = UserLoginState.getCurrentUserId();
        List<Transaction> transactions = getTransactions(isExpense, null);

        return transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> YearMonth.from(t.getDate()),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                t -> isExpense ? t.getAmount().abs() : t.getAmount(),
                                BigDecimal::add
                        )
                ));
    }

    public String getCategoryName(String categoryId) {
        return categoryDao.findById(categoryId)
                .map(Category::getName)
                .orElse("Unknown");
    }

    public BigDecimal getTotalAmount(boolean isExpense, YearMonth month) {
        String userId = UserLoginState.getCurrentUserId();
        List<Transaction> transactions = getTransactions(isExpense, month);

        return transactions.stream()
                .map(t -> isExpense ? t.getAmount().abs() : t.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
