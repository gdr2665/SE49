// File: com.softwareengineering.finsage.controllers.MainController.java
package com.softwareengineering.finsage.controllers;

import javafx.stage.Stage;
import com.softwareengineering.finsage.views.MainView;

public class MainController {
    private MainView view;

    public MainController(Stage stage) {
        this.view = new MainView(stage);
    }

    public void showView() {
        view.getStage().show();
    }

    public static void showMainView(Stage stage) {
        MainController mainController = new MainController(stage);
        mainController.showView();
    }
}
