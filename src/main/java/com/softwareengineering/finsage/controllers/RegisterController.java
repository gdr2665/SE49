package com.softwareengineering.finsage.controllers;

import com.softwareengineering.finsage.model.User;
import com.softwareengineering.finsage.model.Category;
import com.softwareengineering.finsage.utils.DialogUtil;
import com.softwareengineering.finsage.utils.UserLoginState;
import com.softwareengineering.finsage.validator.Validator;
import com.softwareengineering.finsage.dao.UserDao;
import com.softwareengineering.finsage.dao.ThirdPartyDao;
import com.softwareengineering.finsage.dao.CategoryDao;
import com.softwareengineering.finsage.model.ThirdParty;
import com.softwareengineering.finsage.views.RegisterView;
import com.softwareengineering.finsage.views.ThirdPartyLoginView;
import javafx.stage.Stage;

import java.util.UUID;
import java.util.Arrays;
import java.util.List;

public class RegisterController {
    private RegisterView view;
    private UserDao userDao;
    private ThirdPartyDao thirdPartyDao;
    private CategoryDao categoryDao;

    // Default categories for new users
    private static final List<String> DEFAULT_CATEGORIES = Arrays.asList(
            "Food",
            "Housing",
            "Transportation",
            "Entertainment",
            "Shopping",
            "Healthcare",
            "Education",
            "Utilities",
            "Travel",
            "Investment",
            "Gifts",
            "Salary",
            "Savings",
            "Insurance"
    );

    public RegisterController(Stage stage) {
        this.view = new RegisterView(stage);
        this.userDao = new UserDao();
        this.thirdPartyDao = new ThirdPartyDao();
        this.categoryDao = new CategoryDao();
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        view.getRegisterButton().setOnAction(event -> handleRegister());
        view.getBackButton().setOnAction(event -> LoginController.showLoginView((Stage) view.getBackButton().getScene().getWindow()));
    }

    private void handleRegister() {
        String username = view.getUsernameField().getText().trim();
        String email = view.getEmailField().getText().trim();
        String phone = view.getPhoneField().getText().trim();
        String password = view.getPasswordField().getText();
        String confirmPassword = view.getConfirmPasswordField().getText();

        // Validate inputs
        if (!Validator.validateUsername(username)) {
            DialogUtil.showErrorDialog("Registration Error", "Invalid username. Must be 4-20 alphanumeric characters.");
            return;
        }

        if (!Validator.validateEmail(email)) {
            DialogUtil.showErrorDialog("Registration Error", "Invalid email format.");
            return;
        }

        if (!Validator.validatePhone(phone)) {
            DialogUtil.showErrorDialog("Registration Error", "Invalid phone number. Must be 11 digits starting with 1.");
            return;
        }

        if (!Validator.validatePassword(password)) {
            DialogUtil.showErrorDialog("Registration Error", "Invalid password. Must be 8-20 characters with letters, numbers and special symbols.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            DialogUtil.showErrorDialog("Registration Error", "Passwords do not match.");
            return;
        }

        // Check if username or email already exists
        if (userDao.getByUsername(username) != null) {
            DialogUtil.showErrorDialog("Registration Error", "Username already exists.");
            return;
        }

        if (userDao.getByEmail(email) != null) {
            DialogUtil.showErrorDialog("Registration Error", "Email already registered.");
            return;
        }

        // Create new user
        User newUser = new User(
                UUID.randomUUID().toString(),
                username,
                email,
                phone,
                password // In a real app, you should hash the password
        );

        try {
            userDao.save(newUser);

            // Add default categories for the new user
            addDefaultCategories(newUser.getId());

            DialogUtil.showInfoDialog("Registration Success", "Account created successfully with default categories!");

            // Offer to add third-party account
            boolean addThirdParty = DialogUtil.showConfirmationDialog("Third-Party Account",
                    "Would you like to link a third-party account (QQ/WeChat) now?");

            if (addThirdParty) {
                ThirdPartyLoginView thirdPartyView = new ThirdPartyLoginView(new Stage(), true);
                ThirdPartyLoginController thirdPartyController = new ThirdPartyLoginController(
                        thirdPartyView, newUser.getId(), true);

                // After adding third-party account, go to login
                thirdPartyView.getCloseButton().setOnAction(e -> {
                    thirdPartyView.getStage().close();
                    LoginController.showLoginView((Stage) view.getRegisterButton().getScene().getWindow());
                });
            } else {
                LoginController.showLoginView((Stage) view.getRegisterButton().getScene().getWindow());
            }
        } catch (Exception e) {
            DialogUtil.showErrorDialog("Registration Error", "Failed to create account: " + e.getMessage());
        }
    }

    private void addDefaultCategories(String userId) {
        for (String categoryName : DEFAULT_CATEGORIES) {
            Category category = new Category(
                    UUID.randomUUID().toString(),
                    categoryName,
                    userId
            );
            categoryDao.save(category);
        }
    }

    public void showView() {
        view.getUsernameField().getScene().getWindow().sizeToScene();
        ((Stage) view.getUsernameField().getScene().getWindow()).show();
    }
}
