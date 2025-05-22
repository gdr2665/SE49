package com.softwareengineering.finsage.views;

import com.softwareengineering.finsage.controllers.AISuggestionController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AISuggestionView extends BorderPane {
    private final AISuggestionController controller;
    private TextArea questionArea;
    private TextArea answerArea;
    private Button submitButton;
    private ComboBox<String> suggestedQuestions;

    public AISuggestionView(AISuggestionController controller) {
        this.controller = controller;
        initUI();
    }

    private void initUI() {
        setPadding(new Insets(20));

        // Main container with consistent spacing
        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(10));

        // Title with better emphasis
        Label titleLabel = new Label("AI Financial Advisor");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 0 0 5 0;");

        // Suggested questions section with better grouping
        VBox suggestionBox = new VBox(5);
        Label suggestionLabel = new Label("Suggested Questions:");
        suggestionLabel.setStyle("-fx-font-weight: bold;");

        suggestedQuestions = new ComboBox<>();
        suggestedQuestions.getItems().addAll(controller.getSuggestedQuestions());
        suggestedQuestions.setPromptText("Select a question or type your own");
        suggestedQuestions.setMaxWidth(Double.MAX_VALUE);
        suggestedQuestions.setOnAction(e -> {
            String selected = suggestedQuestions.getSelectionModel().getSelectedItem();
            if (selected != null) {
                questionArea.setText(selected);
            }
        });
        suggestionBox.getChildren().addAll(suggestionLabel, suggestedQuestions);

        // Question input with better spacing
        VBox questionBox = new VBox(5);
        Label questionLabel = new Label("Your Question:");
        questionLabel.setStyle("-fx-font-weight: bold;");

        questionArea = new TextArea();
        questionArea.setPromptText("Ask me anything about your finances...");
        questionArea.setWrapText(true);
        questionArea.setPrefRowCount(3);
        VBox.setVgrow(questionArea, Priority.NEVER);
        questionBox.getChildren().addAll(questionLabel, questionArea);

        // Submit button with better alignment
        HBox buttonBox = new HBox();
        submitButton = new Button("Get Advice");
        submitButton.setMaxWidth(Double.MAX_VALUE);
        submitButton.setOnAction(e -> getAdvice());
        buttonBox.getChildren().add(submitButton);
        HBox.setHgrow(submitButton, Priority.ALWAYS);

        // Answer display with better visual hierarchy
        VBox answerBox = new VBox(5);
        Label answerLabel = new Label("AI Advice:");
        answerLabel.setStyle("-fx-font-weight: bold;");

        answerArea = new TextArea();
        answerArea.setWrapText(true);
        answerArea.setEditable(false);
        answerArea.setPrefRowCount(12);
        VBox.setVgrow(answerArea, Priority.ALWAYS);
        answerBox.getChildren().addAll(answerLabel, answerArea);

        // Assemble all components
        mainContainer.getChildren().addAll(
                titleLabel,
                suggestionBox,
                questionBox,
                buttonBox,
                answerBox
        );

        VBox.setVgrow(answerBox, Priority.ALWAYS);
        setCenter(mainContainer);
    }

    private void getAdvice() {
        String question = questionArea.getText().trim();
        if (question.isEmpty()) {
            showAlert("No Question", "Please enter a question or select one from the suggestions.");
            return;
        }

        // Show loading indicator
        submitButton.setDisable(true);
        answerArea.setText("Generating advice...");
        answerArea.setStyle("-fx-font-style: italic;");

        // Run in background thread to keep UI responsive
        new Thread(() -> {
            try {
                String advice = controller.getFinancialAdvice(question);

                // Update UI on JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    answerArea.setText(advice);
                    answerArea.setStyle("-fx-font-style: normal;");
                    submitButton.setDisable(false);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    answerArea.setText("Error getting advice: " + e.getMessage());
                    answerArea.setStyle("-fx-font-style: normal;");
                    submitButton.setDisable(false);
                });
            }
        }).start();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
