// File: com.softwareengineering.finsage.views.ThirdPartyLoginView.java
package com.softwareengineering.finsage.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ThirdPartyLoginView {
    private Stage stage;
    private ComboBox<String> serviceComboBox;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Button closeButton;

    public ThirdPartyLoginView(Stage stage, boolean isRegistration) {
        this.stage = stage;
        createUI(isRegistration);
    }

    private void createUI(boolean isRegistration) {
        stage.setTitle(isRegistration ? "Link Third-Party Account" : "Third-Party Login");

        // Title
        Text title = new Text(isRegistration ? "Link Account" : "Third-Party Login");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        // Form fields
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        serviceComboBox = new ComboBox<>();
        serviceComboBox.getItems().addAll("QQ", "WeChat");
        serviceComboBox.setValue("QQ");

        usernameField = new TextField();
        passwordField = new PasswordField();

        grid.add(new Label("Service:"), 0, 0);
        grid.add(serviceComboBox, 1, 0);
        grid.add(new Label("Username:"), 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(passwordField, 1, 2);

        // Buttons
        loginButton = new Button(isRegistration ? "Link Account" : "Login");
        closeButton = new Button("Close");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(closeButton, loginButton);

        // Main layout
        VBox mainLayout = new VBox(20);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.getChildren().addAll(title, grid, buttonBox);

        Scene scene = new Scene(mainLayout, 350, 300);
        stage.setScene(scene);
    }

    public Stage getStage() {
        return stage;
    }

    public ComboBox<String> getServiceComboBox() {
        return serviceComboBox;
    }

    public TextField getUsernameField() {
        return usernameField;
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    public Button getLoginButton() {
        return loginButton;
    }

    public Button getCloseButton() {
        return closeButton;
    }
}
