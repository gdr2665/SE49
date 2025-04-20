// File: com.softwareengineering.finsage.Main.java
package com.softwareengineering.finsage;

import javafx.application.Application;
import javafx.stage.Stage;
import com.softwareengineering.finsage.controllers.LoginController;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        LoginController.showLoginView(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
