package com.softwareengineering.finsage.views;

import com.softwareengineering.finsage.controllers.AISuggestionController;
import com.softwareengineering.finsage.controllers.BudgetController;
import com.softwareengineering.finsage.controllers.FestivalVisionController;
import com.softwareengineering.finsage.controllers.FinancialForecastController;
import com.softwareengineering.finsage.controllers.NormalVisionController;
import com.softwareengineering.finsage.controllers.StatisticsController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import com.softwareengineering.finsage.controllers.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class MainView {
    private Stage stage;
    private BorderPane mainLayout;
    private NormalVisionView normalVisionView;
    private FestivalVisionView festivalVisionView;
    private StatisticsView statisticsView;
    private BudgetView budgetView;
    private AISuggestionView aiSuggestionView;
    private FinancialForecastView financialForecastView;

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

        // Add section titles
        Label visualizationTitle = createSectionTitle("Visualization");
        Label managementTitle = createSectionTitle("My Assistant");

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
        statisticsLabel.setOnMouseClicked(e -> {
            if (this.statisticsView == null) {
                this.statisticsView = new StatisticsView(new StatisticsController());
            }
            mainLayout.setCenter(statisticsView);
        });

        // Budget item
        Label budgetLabel = createMenuItem("Budget");
        budgetLabel.setOnMouseClicked(e -> {
            if (this.budgetView == null) {
                this.budgetView = new BudgetView(new BudgetController());
            }
            mainLayout.setCenter(budgetView);
        });

        // AI Advice item
        Label aiSuggestionLabel = createMenuItem("AI Advice");
        aiSuggestionLabel.setOnMouseClicked(e -> {
            if (this.aiSuggestionView == null) {
                this.aiSuggestionView = new AISuggestionView(new AISuggestionController());
            }
            mainLayout.setCenter(aiSuggestionView);
        });

        // Financial Forecast item
        Label forecastLabel = createMenuItem("Financial Forecast");
        forecastLabel.setOnMouseClicked(e -> {
            if (this.financialForecastView == null) {
                this.financialForecastView = new FinancialForecastView(new FinancialForecastController());
            }
            mainLayout.setCenter(financialForecastView);
        });

        leftMenu.getChildren().addAll(
                visualizationTitle,
                normalVisionLabel,
                festivalVisionLabel,
                statisticsLabel,
                managementTitle,
                budgetLabel,
                aiSuggestionLabel,
                forecastLabel
        );

        // Set the left menu in the layout
        mainLayout.setLeft(leftMenu);

        // Initialize with normal vision view
        this.normalVisionView = new NormalVisionView(new NormalVisionController());
        mainLayout.setCenter(normalVisionView);

        // Create and add quick search at the bottom
        HBox searchBox = new HBox(10);
        searchBox.setPadding(new Insets(10));
        searchBox.setAlignment(Pos.CENTER);
        searchBox.setStyle("-fx-background-color: #e0e0e0;");

        Label searchLabel = new Label("Quick Navigate:");
        searchLabel.setFont(Font.font(null, FontWeight.BOLD, 12));

        ComboBox<String> searchComboBox = new ComboBox<>();
        searchComboBox.getItems().addAll(
                "Normal Vision",
                "Festival Vision",
                "Statistics",
                "Budget",
                "AI Advice",
                "Financial Forecast"
        );
        searchComboBox.setPromptText("Select a feature...");
        searchComboBox.setPrefWidth(200);

        searchComboBox.setOnAction(e -> {
            String selected = searchComboBox.getValue();
            if (selected != null) {
                switch (selected) {
                    case "Normal Vision":
                        if (this.normalVisionView == null) {
                            this.normalVisionView = new NormalVisionView(new NormalVisionController());
                        }
                        mainLayout.setCenter(normalVisionView);
                        break;
                    case "Festival Vision":
                        if (this.festivalVisionView == null) {
                            this.festivalVisionView = new FestivalVisionView(new FestivalVisionController());
                        }
                        mainLayout.setCenter(festivalVisionView);
                        break;
                    case "Statistics":
                        if (this.statisticsView == null) {
                            this.statisticsView = new StatisticsView(new StatisticsController());
                        }
                        mainLayout.setCenter(statisticsView);
                        break;
                    case "Budget":
                        if (this.budgetView == null) {
                            this.budgetView = new BudgetView(new BudgetController());
                        }
                        mainLayout.setCenter(budgetView);
                        break;
                    case "AI Advice":
                        if (this.aiSuggestionView == null) {
                            this.aiSuggestionView = new AISuggestionView(new AISuggestionController());
                        }
                        mainLayout.setCenter(aiSuggestionView);
                        break;
                    case "Financial Forecast":
                        if (this.financialForecastView == null) {
                            this.financialForecastView = new FinancialForecastView(new FinancialForecastController());
                        }
                        mainLayout.setCenter(financialForecastView);
                        break;
                }
            }
        });

        searchBox.getChildren().addAll(searchLabel, searchComboBox);
        mainLayout.setBottom(searchBox);

        Scene scene = new Scene(mainLayout, 1200, 800);  // Slightly larger window for better display
        stage.setScene(scene);
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

    private Label createSectionTitle(String text) {
        Label label = new Label(text);
        label.setFont(Font.font(null, FontWeight.BOLD, 16));
        label.setPadding(new Insets(15, 5, 5, 10));
        label.setStyle("-fx-text-fill: #555555;");
        return label;
    }

    public Stage getStage() {
        return stage;
    }
}
