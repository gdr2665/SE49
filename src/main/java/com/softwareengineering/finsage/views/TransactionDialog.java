// File: com.softwareengineering.finsage.views.TransactionDialog.java
package com.softwareengineering.finsage.views;

import com.softwareengineering.finsage.controllers.TransactionController;
import com.softwareengineering.finsage.model.Category;
import com.softwareengineering.finsage.model.Transaction;
import com.softwareengineering.finsage.utils.CategoryInferer;
import com.softwareengineering.finsage.utils.UserLoginState;
import com.softwareengineering.finsage.validator.Validator;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TransactionDialog {
    private Stage stage;
    private TransactionController controller;
    private ComboBox<Category> categoryComboBox;
    private TextField amountField;
    private DatePicker datePicker;
    private TextArea noteArea;

    public TransactionDialog(Stage owner) {
        this.stage = new Stage();
        this.controller = new TransactionController();

        initUI(owner);
    }

    private void initUI(Stage owner) {
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Add Transaction");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        // Amount field
        Label amountLabel = new Label("Amount:");
        amountField = new TextField();
        amountField.setPromptText("Positive for income, negative for expense");

        // Date picker
        Label dateLabel = new Label("Date:");
        datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());

        // Category combo box with auto-detect button
        Label categoryLabel = new Label("Category:");
        categoryComboBox = new ComboBox<>();
        categoryComboBox.setItems(FXCollections.observableArrayList(controller.getCategories()));
        categoryComboBox.setPromptText("Select category");

        // Auto-detect button
        Button autoDetectButton = new Button("Auto Detect");
        autoDetectButton.setOnAction(e -> autoDetectCategory());

        // Set up the StringConverter to display category names
        categoryComboBox.setConverter(new StringConverter<Category>() {
            @Override
            public String toString(Category category) {
                return category == null ? "" : category.getName();
            }

            @Override
            public Category fromString(String string) {
                return null; // Not needed for display purposes
            }
        });

        // Note area
        Label noteLabel = new Label("Note:");
        noteArea = new TextArea();
        noteArea.setPrefRowCount(3);
        noteArea.setWrapText(true);

        // Buttons
        Button addButton = new Button("Add");
        addButton.setOnAction(e -> addTransaction());

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> stage.close());

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(addButton, cancelButton);

        // Add components to grid
        grid.add(amountLabel, 0, 0);
        grid.add(amountField, 1, 0);
        grid.add(dateLabel, 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(categoryLabel, 0, 2);

        // Create HBox for category combo and auto-detect button
        HBox categoryBox = new HBox(10);
        categoryBox.getChildren().addAll(categoryComboBox, autoDetectButton);
        grid.add(categoryBox, 1, 2);

        grid.add(noteLabel, 0, 3);
        grid.add(noteArea, 1, 3);
        grid.add(buttonBox, 1, 4);

        Scene scene = new Scene(grid);
        stage.setScene(scene);
    }

    private void autoDetectCategory() {
        String note = noteArea.getText().trim();
        if (note.isEmpty()) {
            showAlert("No Note", "Please enter a note to auto-detect category.");
            return;
        }

        // Show loading indicator
        Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
        loadingAlert.setTitle("Detecting Category");
        loadingAlert.setHeaderText(null);
        loadingAlert.setContentText("Analyzing transaction note...");
        loadingAlert.show();

        // Run in background thread to keep UI responsive
        new Thread(() -> {
            try {
                String detectedCategoryName = CategoryInferer.inferCategoryFromNote(note);

                // Update UI on JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    loadingAlert.close();

                    if (detectedCategoryName == null) {
                        showAlert("Detection Failed", "Could not determine category from note.");
                        return;
                    }

                    // Find the matching category in user's categories
                    List<Category> userCategories = controller.getCategories();
                    Optional<Category> matchedCategory = userCategories.stream()
                            .filter(c -> c.getName().equalsIgnoreCase(detectedCategoryName))
                            .findFirst();

                    if (matchedCategory.isPresent()) {
                        categoryComboBox.getSelectionModel().select(matchedCategory.get());
                        showAlert("Category Detected",
                                "Detected category: " + matchedCategory.get().getName());
                    } else {
                        showAlert("Unknown Category",
                                "Detected category '" + detectedCategoryName + "' doesn't match your categories.");
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    loadingAlert.close();
                    showAlert("Error", "Failed to detect category: " + e.getMessage());
                });
            }
        }).start();
    }

    private void addTransaction() {
        // Validate amount
        String amountStr = amountField.getText().trim();
        if (!Validator.validateAmount(amountStr)) {
            showAlert("Invalid Amount", "Please enter a valid amount.");
            return;
        }

        BigDecimal amount = new BigDecimal(amountStr);
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            showAlert("Invalid Amount", "Amount cannot be zero.");
            return;
        }

        // Validate category
        Category selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            showAlert("No Category Selected", "Please select a category.");
            return;
        }

        // Validate note
        String note = noteArea.getText().trim();
        if (!Validator.validateNote(note)) {
            showAlert("Invalid Note", "Note cannot exceed 200 characters.");
            return;
        }

        // Create and save transaction
        Transaction transaction = new Transaction();
        transaction.setId(java.util.UUID.randomUUID().toString());
        transaction.setAmount(amount);
        transaction.setDate(datePicker.getValue());
        transaction.setCategoryId(selectedCategory.getId());
        transaction.setNote(note);
        transaction.setUserId(UserLoginState.getCurrentUserId());

        if (controller.addTransaction(transaction)) {
            stage.close();
        } else {
            showAlert("Error", "Failed to add transaction.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showAndWait() {
        categoryComboBox.setItems(FXCollections.observableArrayList(controller.getCategories()));
        stage.showAndWait();
    }
}
