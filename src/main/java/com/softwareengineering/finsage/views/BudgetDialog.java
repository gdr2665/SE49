package com.softwareengineering.finsage.views;

import com.softwareengineering.finsage.controllers.BudgetController;
import com.softwareengineering.finsage.model.Budget;
import com.softwareengineering.finsage.model.Category;
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
import java.time.YearMonth;

public class BudgetDialog {
    private Stage stage;
    private BudgetController controller;
    private ComboBox<Category> categoryComboBox;
    private TextField amountField;
    private YearMonth month;
    private Budget existingBudget;

    public BudgetDialog(Stage owner, YearMonth month, Budget existingBudget) {
        this.stage = new Stage();
        this.controller = new BudgetController();
        this.month = month;
        this.existingBudget = existingBudget;

        initUI(owner);
    }

    private void initUI(Stage owner) {
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        stage.setTitle(existingBudget == null ? "Add Budget" : "Edit Budget");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        // Month display
        Label monthLabel = new Label("Month:");
        Label monthValue = new Label(month.toString());

        // Category combo box (only for category budgets)
        Label categoryLabel = new Label("Category:");
        categoryComboBox = new ComboBox<>();
        categoryComboBox.setItems(FXCollections.observableArrayList(controller.getCategories()));
        categoryComboBox.setPromptText("Select category");

        categoryComboBox.setConverter(new StringConverter<Category>() {
            @Override
            public String toString(Category category) {
                return category == null ? "" : category.getName();
            }

            @Override
            public Category fromString(String string) {
                return null;
            }
        });

        // Amount field
        Label amountLabel = new Label("Amount:");
        amountField = new TextField();
        amountField.setPromptText("Enter budget amount");

        // Buttons
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> saveBudget());

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> stage.close());

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(saveButton, cancelButton);

        // Add components to grid
        grid.add(monthLabel, 0, 0);
        grid.add(monthValue, 1, 0);

        if (existingBudget == null || !existingBudget.isTotalBudget()) {
            grid.add(categoryLabel, 0, 1);
            grid.add(categoryComboBox, 1, 1);
            grid.add(amountLabel, 0, 2);
            grid.add(amountField, 1, 2);
            grid.add(buttonBox, 1, 3);
        } else {
            grid.add(amountLabel, 0, 1);
            grid.add(amountField, 1, 1);
            grid.add(buttonBox, 1, 2);
        }

        // If editing existing budget, populate fields
        if (existingBudget != null) {
            amountField.setText(existingBudget.getAmount().toString());

            if (!existingBudget.isTotalBudget()) {
                for (Category category : controller.getCategories()) {
                    if (category.getId().equals(existingBudget.getCategoryId())) {
                        categoryComboBox.getSelectionModel().select(category);
                        break;
                    }
                }
            }
        }

        Scene scene = new Scene(grid);
        stage.setScene(scene);
    }

    private void saveBudget() {
        try {
            BigDecimal amount = new BigDecimal(amountField.getText().trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert("Invalid Amount", "Budget amount must be positive.");
                return;
            }

            Budget budget;
            if (existingBudget != null) {
                budget = existingBudget;
                budget.setAmount(amount);
            } else {
                budget = new Budget();
                budget.setId(java.util.UUID.randomUUID().toString());
                budget.setMonth(month);
                budget.setAmount(amount);
                budget.setUserId(controller.getCurrentUserId());

                if (categoryComboBox.getSelectionModel().getSelectedItem() != null) {
                    budget.setCategoryId(categoryComboBox.getSelectionModel().getSelectedItem().getId());
                }
            }

            if (controller.saveBudget(budget)) {
                stage.close();
            } else {
                showAlert("Error", "Failed to save budget.");
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Amount", "Please enter a valid number for the budget amount.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showAndWait() {
        stage.showAndWait();
    }
}
