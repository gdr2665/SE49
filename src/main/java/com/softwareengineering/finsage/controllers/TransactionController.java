// File: com.softwareengineering.finsage.controllers.TransactionController.java
package com.softwareengineering.finsage.controllers;

import com.softwareengineering.finsage.dao.CategoryDao;
import com.softwareengineering.finsage.dao.TransactionDao;
import com.softwareengineering.finsage.model.Category;
import com.softwareengineering.finsage.model.Transaction;
import com.softwareengineering.finsage.utils.UserLoginState;

import java.math.BigDecimal;
import java.util.List;

public class TransactionController {
    private TransactionDao transactionDao;
    private CategoryDao categoryDao;

    public TransactionController() {
        this.transactionDao = new TransactionDao();
        this.categoryDao = new CategoryDao();
    }

    public List<Transaction> getTransactions() {
        return transactionDao.getByUserId(UserLoginState.getCurrentUserId());
    }

    public List<Transaction> getTransactionsByType(boolean isIncome) {
        List<Transaction> transactions = getTransactions();
        transactions.removeIf(t -> (isIncome && t.getAmount().compareTo(BigDecimal.ZERO) < 0) ||
                (!isIncome && t.getAmount().compareTo(BigDecimal.ZERO) > 0));
        return transactions;
    }

    public List<Category> getCategories() {
        return categoryDao.getByUserId(UserLoginState.getCurrentUserId());
    }

    public boolean addTransaction(Transaction transaction) {
        transactionDao.save(transaction);
        return true;
    }

    public boolean updateTransaction(Transaction transaction) {
        return transactionDao.update(transaction);
    }


    public boolean deleteTransaction(String transactionId) {
        String userId = UserLoginState.getCurrentUserId();
        return transactionDao.delete(transactionId);
    }


    public Category getCategoryById(String categoryId) {
        if (categoryId == null) return null;
        return categoryDao.getById(categoryId);
    }

}
