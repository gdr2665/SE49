// File: com.softwareengineering.finsage.controllers.LoginController.java
package com.softwareengineering.finsage.controllers;

import com.softwareengineering.finsage.model.User;
import com.softwareengineering.finsage.utils.DialogUtil;
import com.softwareengineering.finsage.utils.UserLoginState;
import com.softwareengineering.finsage.validator.Validator;
import com.softwareengineering.finsage.dao.UserDao;
import com.softwareengineering.finsage.views.LoginView;
import com.softwareengineering.finsage.views.ThirdPartyLoginView;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;

public class LoginController {
    private LoginView view;
    private UserDao userDao;

    public LoginController(Stage stage) {
        this.view = new LoginView(stage);
        this.userDao = new UserDao();
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        view.getLoginButton().setOnAction(event -> handleLogin());
        view.getRegisterButton().setOnAction(event -> showRegisterView());
        view.getThirdPartyLoginButton().setOnAction(event -> showThirdPartyLogin());
    }

    private void handleLogin() {
        String identifier = view.getUsernameField().getText().trim();
        String password = view.getPasswordField().getText();

        if (identifier.isEmpty() || password.isEmpty()) {
            DialogUtil.showErrorDialog("Login Error", "Please enter both username/email/phone and password.");
            return;
        }

        User user = null;
        RadioButton selectedRadio = (RadioButton) view.getLoginMethodGroup().getSelectedToggle();

        try {
            if (selectedRadio == view.getUsernameRadio()) {
                if (!Validator.validateUsername(identifier)) {
                    DialogUtil.showErrorDialog("Login Error", "Invalid username format.");
                    return;
                }
                user = userDao.getByUsername(identifier);
            } else if (selectedRadio == view.getEmailRadio()) {
                if (!Validator.validateEmail(identifier)) {
                    DialogUtil.showErrorDialog("Login Error", "Invalid email format.");
                    return;
                }
                user = userDao.getByEmail(identifier);
            } else if (selectedRadio == view.getPhoneRadio()) {
                if (!Validator.validatePhone(identifier)) {
                    DialogUtil.showErrorDialog("Login Error", "Invalid phone format.");
                    return;
                }
                // Assuming you have a getByPhone method in UserDao
                for (User u : userDao.getAll()) {
                    if (u.getPhone().equals(identifier)) {
                        user = u;
                        break;
                    }
                }
            }

            if (user == null) {
                DialogUtil.showErrorDialog("Login Error", "User not found.");
                return;
            }

            // In a real app, compare hashed passwords
            if (!user.getPassword().equals(password)) {
                DialogUtil.showErrorDialog("Login Error", "Incorrect password.");
                return;
            }

            // Login successful
            UserLoginState.setCurrentUser(user);
            DialogUtil.showInfoDialog("Login Success", "Welcome back, " + user.getUsername() + "!");

            // Close the login window
            ((Stage) view.getLoginButton().getScene().getWindow()).close();

            // Open the main view
            Stage mainStage = new Stage();
            MainController.showMainView(mainStage);

        } catch (Exception e) {
            DialogUtil.showErrorDialog("Login Error", "An error occurred: " + e.getMessage());
        }
    }


    private void showRegisterView() {
        Stage registerStage = new Stage();
        RegisterController registerController = new RegisterController(registerStage);
        registerController.showView();
        ((Stage) view.getRegisterButton().getScene().getWindow()).close();
    }

    private void showThirdPartyLogin() {
        ThirdPartyLoginView thirdPartyView = new ThirdPartyLoginView(new Stage(), false);
        ThirdPartyLoginController thirdPartyController = new ThirdPartyLoginController(
                thirdPartyView, null, false);
    }

    public static void showLoginView(Stage stage) {
        LoginController loginController = new LoginController(stage);
        stage.show();
    }
}
