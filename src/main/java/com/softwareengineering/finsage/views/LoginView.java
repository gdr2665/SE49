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

public class LoginView {
    private Stage stage;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Button registerButton;
    private Button thirdPartyLoginButton;
    private ToggleGroup loginMethodGroup;
    private RadioButton usernameRadio;
    private RadioButton emailRadio;
    private RadioButton phoneRadio;

    public LoginView(Stage stage) {
        this.stage = stage;
        createUI();
    }

    private void createUI() {
        stage.setTitle("FinSage - Login");

        // Title
        Text title = new Text("FinSage");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));

        // Login method selection
        loginMethodGroup = new ToggleGroup();
        usernameRadio = new RadioButton("Username");
        usernameRadio.setToggleGroup(loginMethodGroup);
        usernameRadio.setSelected(true);

        emailRadio = new RadioButton("Email");
        emailRadio.setToggleGroup(loginMethodGroup);

        phoneRadio = new RadioButton("Phone");
        phoneRadio.setToggleGroup(loginMethodGroup);

        HBox loginMethodBox = new HBox(10);
        loginMethodBox.setAlignment(Pos.CENTER);
        loginMethodBox.getChildren().addAll(usernameRadio, emailRadio, phoneRadio);

        // Form fields
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        usernameField = new TextField();
        passwordField = new PasswordField();

        grid.add(new Label("Username/Email/Phone:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);

        // Buttons
        loginButton = new Button("Login");
        registerButton = new Button("Register");
        thirdPartyLoginButton = new Button("Third-Party Login");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(registerButton, loginButton);

        // Main layout
        VBox mainLayout = new VBox(20);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.getChildren().addAll(title, loginMethodBox, grid, buttonBox, thirdPartyLoginButton);

        Scene scene = new Scene(mainLayout, 400, 400);
        stage.setScene(scene);
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

    public Button getRegisterButton() {
        return registerButton;
    }

    public Button getThirdPartyLoginButton() {
        return thirdPartyLoginButton;
    }

    public ToggleGroup getLoginMethodGroup() {
        return loginMethodGroup;
    }

    public RadioButton getUsernameRadio() {
        return usernameRadio;
    }

    public RadioButton getEmailRadio() {
        return emailRadio;
    }

    public RadioButton getPhoneRadio() {
        return phoneRadio;
    }
}
