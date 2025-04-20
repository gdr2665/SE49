// File: com.softwareengineering.finsage.controllers.ThirdPartyLoginController.java
package com.softwareengineering.finsage.controllers;

import com.softwareengineering.finsage.model.User;
import com.softwareengineering.finsage.model.ThirdParty;
import com.softwareengineering.finsage.dao.UserDao;
import com.softwareengineering.finsage.dao.ThirdPartyDao;
import com.softwareengineering.finsage.utils.DialogUtil;
import com.softwareengineering.finsage.utils.UserLoginState;
import com.softwareengineering.finsage.validator.Validator;
import com.softwareengineering.finsage.views.ThirdPartyLoginView;
import javafx.stage.Stage;

import java.util.UUID;

public class ThirdPartyLoginController {
    private ThirdPartyLoginView view;
    private ThirdPartyDao thirdPartyDao;
    private UserDao userDao;
    private String userId;
    private boolean isRegistration;

    public ThirdPartyLoginController(ThirdPartyLoginView view, String userId, boolean isRegistration) {
        this.view = view;
        this.thirdPartyDao = new ThirdPartyDao();
        this.userDao = new UserDao();
        this.userId = userId;
        this.isRegistration = isRegistration;
        setupEventHandlers();
        view.getStage().show();
    }

    private void setupEventHandlers() {
        view.getLoginButton().setOnAction(event -> handleThirdPartyLogin());
        view.getCloseButton().setOnAction(event -> view.getStage().close());
    }

    private void handleThirdPartyLogin() {
        String serviceName = view.getServiceComboBox().getValue();
        String serviceUsername = view.getUsernameField().getText().trim();
        String servicePassword = view.getPasswordField().getText();

        // Validate inputs
        if (!Validator.validateServiceName(serviceName)) {
            DialogUtil.showErrorDialog("Error", "Invalid service name. Only QQ or WeChat are allowed.");
            return;
        }

        if (serviceUsername.isEmpty()) {
            DialogUtil.showErrorDialog("Error", "Please enter your third-party account username.");
            return;
        }

        if (servicePassword.isEmpty()) {
            DialogUtil.showErrorDialog("Error", "Please enter your third-party account password.");
            return;
        }

        try {
            if (isRegistration) {
                // Linking a new third-party account during registration
                ThirdParty thirdParty = new ThirdParty(
                        UUID.randomUUID().toString(),
                        serviceName,
                        serviceUsername,
                        servicePassword,
                        userId
                );
                thirdPartyDao.save(thirdParty);
                DialogUtil.showInfoDialog("Success", "Third-party account linked successfully!");
                view.getStage().close();
            } else {
                // Third-party login
                final String finalServiceName = serviceName;
                final String finalServiceUsername = serviceUsername;
                final String finalServicePassword = servicePassword;

                ThirdParty thirdPartyAccount = thirdPartyDao.getAll().stream()
                        .filter(tp -> tp.getServiceName().equalsIgnoreCase(finalServiceName) &&
                                tp.getServiceUsername().equals(finalServiceUsername) &&
                                tp.getServicePassword().equals(finalServicePassword))
                        .findFirst()
                        .orElse(null);

                if (thirdPartyAccount == null) {
                    DialogUtil.showErrorDialog("Login Failed", "Invalid third-party account credentials.");
                    return;
                }

                final String thirdPartyUserId = thirdPartyAccount.getUserId();
                User user = userDao.getAll().stream()
                        .filter(u -> u.getId().equals(thirdPartyUserId))
                        .findFirst()
                        .orElse(null);

                if (user == null) {
                    DialogUtil.showErrorDialog("Login Failed", "No user account associated with this third-party account.");
                    return;
                }

                // Login successful
                UserLoginState.setCurrentUser(user);
                DialogUtil.showInfoDialog("Login Success", "Welcome back, " + user.getUsername() + "!");

                // Close the third-party login window
                view.getStage().close();

                // Close the main login window if it's still open
                Stage loginStage = (Stage) view.getLoginButton().getScene().getWindow();
                if (loginStage != null) {
                    loginStage.close();
                }

                // Open the main view
                Stage mainStage = new Stage();
                MainController.showMainView(mainStage);
            }
        } catch (Exception e) {
            DialogUtil.showErrorDialog("Error", "An error occurred: " + e.getMessage());
        }
    }

}
