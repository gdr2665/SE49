// File: com.softwareengineering.finsage.controllers.CategoryController.java
package com.softwareengineering.finsage.controllers;

import com.softwareengineering.finsage.dao.CategoryDao;
import com.softwareengineering.finsage.dao.TransactionDao;
import com.softwareengineering.finsage.model.Category;
import com.softwareengineering.finsage.utils.UserLoginState;

import java.util.List;

public class CategoryController {
    private CategoryDao categoryDao;

    public CategoryController() {
        this.categoryDao = new CategoryDao();
    }

    public List<Category> getCategories() {
        return categoryDao.getByUserId(UserLoginState.getCurrentUserId());
    }

    public boolean addCategory(Category category) {
        if (categoryDao.getByUserId(UserLoginState.getCurrentUserId()).stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(category.getName()))) {
            return false;
        }

        category.setId(java.util.UUID.randomUUID().toString());
        categoryDao.save(category);
        return true;
    }

    public boolean deleteCategory(String categoryId) {
        // Check if category is used by any transactions
        TransactionDao transactionDao = new TransactionDao();
        boolean isUsed = transactionDao.getByUserId(UserLoginState.getCurrentUserId()).stream()
                .anyMatch(t -> t.getCategoryId().equals(categoryId));

        if (isUsed) {
            return false;
        }

        return true;
    }
}
