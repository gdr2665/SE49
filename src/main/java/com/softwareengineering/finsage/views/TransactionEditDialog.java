package com.softwareengineering.finsage.views;

import com.softwareengineering.finsage.controllers.TransactionController;
import com.softwareengineering.finsage.model.Category;
import com.softwareengineering.finsage.model.Transaction;
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

public class TransactionEditDialog {
    private Stage stage;
    private TransactionController controller;
    private ComboBox<Category> categoryComboBox;
    private TextField amountField;
    private DatePicker datePicker;
    private TextArea noteArea;
    private Transaction transactionToEdit;

    public TransactionEditDialog(Stage owner, Transaction transaction) {
        this.stage = new Stage();
        this.controller = new TransactionController();
        this.transactionToEdit = transaction;

        initUI(owner);
        populateFields();
    }

    private void initUI(Stage owner) {
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Edit Transaction");

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

        // Category combo box
        Label categoryLabel = new Label("Category:");
        categoryComboBox = new ComboBox<>();
        categoryComboBox.setItems(FXCollections.observableArrayList(controller.getCategories()));
        categoryComboBox.setPromptText("Select category");

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
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> updateTransaction());

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> stage.close());

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(saveButton, cancelButton);

        // Add components to grid
        grid.add(amountLabel, 0, 0);
        grid.add(amountField, 1, 0);
        grid.add(dateLabel, 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(categoryLabel, 0, 2);
        grid.add(categoryComboBox, 1, 2);
        grid.add(noteLabel, 0, 3);
        grid.add(noteArea, 1, 3);
        grid.add(buttonBox, 1, 4);

        Scene scene = new Scene(grid);
        stage.setScene(scene);
    }

    private void populateFields() {
        if (transactionToEdit != null) {
            amountField.setText(transactionToEdit.getAmount().toString());
            datePicker.setValue(transactionToEdit.getDate());
            noteArea.setText(transactionToEdit.getNote());

            // Select the current category in the combo box
            for (Category category : controller.getCategories()) {
                if (category.getId().equals(transactionToEdit.getCategoryId())) {
                    categoryComboBox.getSelectionModel().select(category);
                    break;
                }
            }
        }
    }

    private void updateTransaction() {
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

        // Update the transaction
        transactionToEdit.setAmount(amount);
        transactionToEdit.setDate(datePicker.getValue());
        transactionToEdit.setCategoryId(selectedCategory.getId());
        transactionToEdit.setNote(note);

        if (controller.updateTransaction(transactionToEdit)) {
            stage.close();
        } else {
            showAlert("Error", "Failed to update transaction.");
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
        categoryComboBox.setItems(FXCollections.observableArrayList(controller.getCategories()));
        stage.showAndWait();
    }
}
