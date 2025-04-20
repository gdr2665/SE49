// File: com.softwareengineering.finsage.views.MainView.java
package com.softwareengineering.finsage.views;

import com.softwareengineering.finsage.controllers.FestivalVisionController;
import com.softwareengineering.finsage.controllers.NormalVisionController;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class MainView {
    private Stage stage;
    private BorderPane mainLayout;
    private NormalVisionView normalVisionView;
    private FestivalVisionView festivalVisionView;

    public MainView(Stage stage) {
        this.stage = stage;
        createUI();
    }

    private void createUI() {
        stage.setTitle("FinSage - Main");

        // Create the main layout
        mainLayout = new BorderPane();

        // Create the left menu (simple VBox with clickable items)
        VBox leftMenu = new VBox(5);
        leftMenu.setPrefWidth(200);
        leftMenu.setPadding(new Insets(10));
        leftMenu.setStyle("-fx-background-color: #f4f4f4;");

        // Normal Vision item
        Label normalVisionLabel = createMenuItem("Normal Vision");
        normalVisionLabel.setOnMouseClicked(e -> {
            if (this.normalVisionView == null) {
                this.normalVisionView = new NormalVisionView(new NormalVisionController());
            }
            mainLayout.setCenter(normalVisionView);
        });

        // Festival Vision item
        Label festivalVisionLabel = createMenuItem("Festival Vision");
        festivalVisionLabel.setOnMouseClicked(e -> {
            if (this.festivalVisionView == null) {
                this.festivalVisionView = new FestivalVisionView(new FestivalVisionController());
            }
            mainLayout.setCenter(festivalVisionView);
        });

        // Statistics item
        Label statisticsLabel = createMenuItem("Statistics");
        statisticsLabel.setOnMouseClicked(e -> updateContent("Statistics Coming Soon!"));

        leftMenu.getChildren().addAll(normalVisionLabel, festivalVisionLabel, statisticsLabel);

        // Set the left menu in the layout
        mainLayout.setLeft(leftMenu);

        // Initialize with normal vision view
        this.normalVisionView = new NormalVisionView(new NormalVisionController());
        mainLayout.setCenter(normalVisionView);

        Scene scene = new Scene(mainLayout, 1200, 800);  // Slightly larger window for better display
        stage.setScene(scene);
    }

    private void updateContent(String text) {
        // For other views (statistics)
        Label contentLabel = new Label(text);
        contentLabel.setFont(Font.font(null, FontWeight.BOLD, 24));
        mainLayout.setCenter(contentLabel);
    }

    private Label createMenuItem(String text) {
        Label label = new Label(text);
        label.setFont(Font.font(null, FontWeight.BOLD, 14));
        label.setPadding(new Insets(15, 5, 15, 20));
        label.setPrefWidth(180);
        label.setStyle(
                "-fx-cursor: hand;" +
                        "-fx-background-color: #e0e0e0;" +
                        "-fx-background-radius: 5;" +
                        "-fx-border-radius: 5;" +
                        "-fx-effect: dropshadow(one-pass-box, rgba(0,0,0,0.1), 2, 0.0, 1, 1);"
        );

        // Hover effects
        label.setOnMouseEntered(e -> {
            label.setStyle(
                    "-fx-cursor: hand;" +
                            "-fx-background-color: #d0d0d0;" +
                            "-fx-background-radius: 5;" +
                            "-fx-border-radius: 5;" +
                            "-fx-effect: dropshadow(one-pass-box, rgba(0,0,0,0.15), 3, 0.0, 1, 1);"
            );
        });

        label.setOnMouseExited(e -> {
            label.setStyle(
                    "-fx-cursor: hand;" +
                            "-fx-background-color: #e0e0e0;" +
                            "-fx-background-radius: 5;" +
                            "-fx-border-radius: 5;" +
                            "-fx-effect: dropshadow(one-pass-box, rgba(0,0,0,0.1), 2, 0.0, 1, 1);"
            );
        });

        // Pressed effect
        label.setOnMousePressed(e -> {
            label.setStyle(
                    "-fx-cursor: hand;" +
                            "-fx-background-color: #c0c0c0;" +
                            "-fx-background-radius: 5;" +
                            "-fx-border-radius: 5;" +
                            "-fx-effect: dropshadow(one-pass-box, rgba(0,0,0,0.1), 1, 0.0, 1, 1);"
            );
        });

        label.setOnMouseReleased(e -> {
            label.setStyle(
                    "-fx-cursor: hand;" +
                            "-fx-background-color: #d0d0d0;" +
                            "-fx-background-radius: 5;" +
                            "-fx-border-radius: 5;" +
                            "-fx-effect: dropshadow(one-pass-box, rgba(0,0,0,0.15), 3, 0.0, 1, 1);"
            );
        });

        return label;
    }

    public Stage getStage() {
        return stage;
    }
}
