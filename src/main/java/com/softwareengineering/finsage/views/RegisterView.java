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

public class RegisterView {
    private Stage stage;
    private TextField usernameField;
    private TextField emailField;
    private TextField phoneField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private Button registerButton;
    private Button backButton;

    public RegisterView(Stage stage) {
        this.stage = stage;
        createUI();
    }

    private void createUI() {
        stage.setTitle("FinSage - Register");

        // Title
        Text title = new Text("Register");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        // Form fields
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        usernameField = new TextField();
        emailField = new TextField();
        phoneField = new TextField();
        passwordField = new PasswordField();
        confirmPasswordField = new PasswordField();

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Password:"), 0, 3);
        grid.add(passwordField, 1, 3);
        grid.add(new Label("Confirm Password:"), 0, 4);
        grid.add(confirmPasswordField, 1, 4);

        // Buttons
        registerButton = new Button("Register");
        backButton = new Button("Back to Login");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(backButton, registerButton);

        // Main layout
        VBox mainLayout = new VBox(20);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.getChildren().addAll(title, grid, buttonBox);

        Scene scene = new Scene(mainLayout, 400, 450);
        stage.setScene(scene);
    }

    public TextField getUsernameField() {
        return usernameField;
    }

    public TextField getEmailField() {
        return emailField;
    }

    public TextField getPhoneField() {
        return phoneField;
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    public PasswordField getConfirmPasswordField() {
        return confirmPasswordField;
    }

    public Button getRegisterButton() {
        return registerButton;
    }

    public Button getBackButton() {
        return backButton;
    }
}
