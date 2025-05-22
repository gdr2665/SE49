package com.softwareengineering.finsage.views;

import com.softwareengineering.finsage.controllers.NormalVisionController;
import com.softwareengineering.finsage.dao.TransactionDao;
import com.softwareengineering.finsage.model.Category;
import com.softwareengineering.finsage.model.Transaction;
import com.softwareengineering.finsage.utils.TransactionImporter;
import com.softwareengineering.finsage.utils.UserLoginState;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class NormalVisionView extends BorderPane {
    private NormalVisionController controller;
    private TableView<Transaction> transactionTable;
    private ComboBox<Category> categoryFilter;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private TextField minAmountField;
    private TextField maxAmountField;
    private ComboBox<String> currencyComboBox;
    private ToggleGroup transactionTypeGroup;
    private ObservableList<Transaction> transactions;
    private Label incomeSumLabel;
    private Label expenseSumLabel;
    private Label totalSumLabel;

    public NormalVisionView(NormalVisionController controller) {
        this.controller = controller;
        this.transactions = FXCollections.observableArrayList();

        initUI();
        loadTransactions();
    }

    private void initUI() {
        // Create filter controls at the top
        GridPane filterGrid = new GridPane();
        filterGrid.setPadding(new Insets(10));
        filterGrid.setHgap(10);
        filterGrid.setVgap(10);

        // Transaction type radio buttons
        VBox typeBox = new VBox(5);
        Label typeLabel = new Label("Transaction Type:");
        transactionTypeGroup = new ToggleGroup();

        RadioButton allRadio = new RadioButton("All");
        allRadio.setToggleGroup(transactionTypeGroup);
        allRadio.setSelected(true);
        allRadio.setUserData("ALL");

        RadioButton incomeRadio = new RadioButton("Income");
        incomeRadio.setToggleGroup(transactionTypeGroup);
        incomeRadio.setUserData("INCOME");

        RadioButton expenseRadio = new RadioButton("Expense");
        expenseRadio.setToggleGroup(transactionTypeGroup);
        expenseRadio.setUserData("EXPENSE");

        typeBox.getChildren().addAll(typeLabel, allRadio, incomeRadio, expenseRadio);
        filterGrid.add(typeBox, 0, 0);

        // Add listeners to radio buttons to apply filters automatically
        allRadio.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        incomeRadio.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        expenseRadio.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Category filter
        VBox categoryBox = new VBox(5);
        Label categoryLabel = new Label("Category:");
        categoryFilter = new ComboBox<>();
        categoryFilter.setPromptText("All Categories");
        categoryFilter.setConverter(new StringConverter<Category>() {
            @Override
            public String toString(Category category) {
                return category == null ? "" : category.getName();
            }

            @Override
            public Category fromString(String string) {
                return null;
            }
        });

        // Add listener to category filter
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        Button manageCategoriesBtn = new Button("Manage");
        manageCategoriesBtn.setOnAction(e -> {
            CategoryDialog dialog = new CategoryDialog((Stage) getScene().getWindow());
            dialog.show();
            refreshCategories();
        });

        HBox categoryControlBox = new HBox(5);
        categoryControlBox.getChildren().addAll(categoryFilter, manageCategoriesBtn);
        categoryBox.getChildren().addAll(categoryLabel, categoryControlBox);
        filterGrid.add(categoryBox, 1, 0);

        // Date range filter
        VBox dateBox = new VBox(5);
        Label dateLabel = new Label("Date Range:");
        startDatePicker = new DatePicker(LocalDate.now().minusMonths(1));
        endDatePicker = new DatePicker(LocalDate.now());

        // Add listeners to date pickers
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        HBox dateRangeBox = new HBox(5);
        dateRangeBox.getChildren().addAll(startDatePicker, new Label("to"), endDatePicker);
        dateBox.getChildren().addAll(dateLabel, dateRangeBox);
        filterGrid.add(dateBox, 0, 1);

        // Amount range filter
        VBox amountBox = new VBox(5);
        Label amountLabel = new Label("Amount Range:");
        minAmountField = new TextField();
        minAmountField.setPromptText("Min");
        maxAmountField = new TextField();
        maxAmountField.setPromptText("Max");

        // Add listeners to amount fields
        minAmountField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        maxAmountField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        HBox amountRangeBox = new HBox(5);
        amountRangeBox.getChildren().addAll(minAmountField, new Label("to"), maxAmountField);
        amountBox.getChildren().addAll(amountLabel, amountRangeBox);
        filterGrid.add(amountBox, 1, 1);

        // Currency converter and summary labels
        VBox currencyBox = new VBox(5);
        Label currencyLabel = new Label("Currency:");
        currencyComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "RMB", "USD", "EUR", "HKD"));
        currencyComboBox.getSelectionModel().selectFirst();

        // Add listener to currency combo box
        currencyComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            applyFilters();
            updateSummaryLabels();
        });

        // Create summary labels
        incomeSumLabel = new Label("Income: 0.00");
        expenseSumLabel = new Label("Expense: 0.00");
        totalSumLabel = new Label("Total: 0.00");

        VBox summaryBox = new VBox(5);
        summaryBox.getChildren().addAll(incomeSumLabel, expenseSumLabel, totalSumLabel);

        HBox currencySummaryBox = new HBox(10);
        currencySummaryBox.getChildren().addAll(currencyComboBox, summaryBox);

        currencyBox.getChildren().addAll(currencyLabel, currencySummaryBox);
        filterGrid.add(currencyBox, 0, 2);

        // Create transaction table
        transactionTable = new TableView<>();
        transactionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Date column
        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        // Amount column (with currency conversion)
        TableColumn<Transaction, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cell -> {
            BigDecimal amount = cell.getValue().getAmount();
            String currency = currencyComboBox.getValue();
            BigDecimal convertedAmount = convertCurrency(amount, currency);
            return javafx.beans.binding.Bindings.createStringBinding(
                    () -> String.format("%s %,.2f", currency, convertedAmount));
        });

        // Category column
        TableColumn<Transaction, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(cell -> {
            String categoryId = cell.getValue().getCategoryId();
            Category category = controller.getCategoryById(categoryId);
            return javafx.beans.binding.Bindings.createStringBinding(
                    () -> category != null ? category.getName() : "Unknown");
        });

        // Note column
        TableColumn<Transaction, String> noteCol = new TableColumn<>("Note");
        noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));

        // Action column
        TableColumn<Transaction, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Transaction, Void> call(TableColumn<Transaction, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");

                    {
                        editBtn.setOnAction(e -> {
                            Transaction transaction = getTableView().getItems().get(getIndex());
                            editTransaction(transaction);
                        });

                        deleteBtn.setOnAction(e -> {
                            Transaction transaction = getTableView().getItems().get(getIndex());
                            deleteTransaction(transaction);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox buttons = new HBox(5, editBtn, deleteBtn);
                            setGraphic(buttons);
                        }
                    }
                };
            }
        });

        transactionTable.getColumns().addAll(dateCol, amountCol, categoryCol, noteCol, actionCol);
        transactionTable.setItems(transactions);

        // Add listener to transactions to update summary when data changes
        transactions.addListener((javafx.collections.ListChangeListener.Change<? extends Transaction> c) -> {
            updateSummaryLabels();
        });

        // Add button for new transactions
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10));

        Button addItemBtn = new Button("Add Item");
        addItemBtn.setOnAction(e -> {
            TransactionDialog dialog = new TransactionDialog((Stage) getScene().getWindow());
            dialog.showAndWait(); // Wait for the dialog to close
            loadTransactions(); // Refresh the table after adding
        });

        // Add import button
        Button importBtn = new Button("Import CSV");
        importBtn.setOnAction(e -> handleImport());

        buttonBox.getChildren().addAll(addItemBtn, importBtn);

        // Set layout
        setTop(filterGrid);
        setCenter(transactionTable);
        setBottom(buttonBox);

        // Initialize filters
        refreshCategories();
    }

    private void updateSummaryLabels() {
        String currency = currencyComboBox.getValue();
        BigDecimal incomeSum = BigDecimal.ZERO;
        BigDecimal expenseSum = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            BigDecimal convertedAmount = convertCurrency(t.getAmount(), currency);
            if (t.getCategoryId() != null && t.getCategoryId().startsWith("INC_")) {
                incomeSum = incomeSum.add(convertedAmount);
            } else {
                expenseSum = expenseSum.add(convertedAmount);
            }
        }

        BigDecimal totalSum = incomeSum.subtract(expenseSum);

        incomeSumLabel.setText(String.format("Income: %s %,.2f", currency, incomeSum));
        expenseSumLabel.setText(String.format("Expense: %s %,.2f", currency, expenseSum));
        totalSumLabel.setText(String.format("Total: %s %,.2f", currency, totalSum));
    }


    private BigDecimal convertCurrency(BigDecimal amount, String currency) {
        // Simple conversion rates (for demo purposes)
        switch (currency) {
            case "USD":
                return amount.multiply(new BigDecimal("0.15")); // 1 RMB = 0.15 USD
            case "EUR":
                return amount.multiply(new BigDecimal("0.13")); // 1 RMB = 0.13 EUR
            case "HKD":
                return amount.multiply(new BigDecimal("1.17")); // 1 RMB = 1.17 HKD
            default: // RMB
                return amount;
        }
    }

    private void loadTransactions() {
        transactions.setAll(controller.getTransactions());
        updateSummaryLabels();
    }

    private void refreshCategories() {
        categoryFilter.setItems(FXCollections.observableArrayList(controller.getCategories()));
    }

    private void applyFilters() {
        String type = (String) transactionTypeGroup.getSelectedToggle().getUserData();
        Category selectedCategory = categoryFilter.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        BigDecimal minAmount = null;
        BigDecimal maxAmount = null;

        try {
            if (!minAmountField.getText().isEmpty()) {
                minAmount = new BigDecimal(minAmountField.getText());
            }
            if (!maxAmountField.getText().isEmpty()) {
                maxAmount = new BigDecimal(maxAmountField.getText());
            }
        } catch (NumberFormatException e) {
            // Ignore invalid inputs
        }

        List<Transaction> filtered = controller.getFilteredTransactions(
                type,
                selectedCategory != null ? selectedCategory.getId() : null,
                startDate,
                endDate,
                minAmount,
                maxAmount
        );

        transactions.setAll(filtered);
        updateSummaryLabels();
    }

    private void editTransaction(Transaction transaction) {
        TransactionEditDialog dialog = new TransactionEditDialog((Stage) getScene().getWindow(), transaction);
        dialog.showAndWait();
        loadTransactions(); // Refresh the table after editing
    }

    private void handleImport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File to Import");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(getScene().getWindow());
        if (selectedFile != null) {
            try {
                TransactionImporter importer = new TransactionImporter(
                        new TransactionDao(),
                        UserLoginState.getCurrentUserId()
                );

                List<Transaction> imported = importer.importFromCsv(selectedFile.toPath());

                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Import Successful");
                alert.setHeaderText(null);
                alert.setContentText("Successfully imported " + imported.size() + " transactions.");
                alert.showAndWait();

                // Refresh the table
                loadTransactions();
            } catch (IOException e) {
                // Show error message
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Import Error");
                alert.setHeaderText("Error importing transactions");
                alert.setContentText("Could not import transactions: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void deleteTransaction(Transaction transaction) {
        // Add confirmation dialog before deleting
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Transaction");
        alert.setContentText("Are you sure you want to delete this transaction?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (controller.deleteTransaction(transaction.getId())) {
                loadTransactions(); // Refresh the table after deleting
            } else {
                // Show error message if deletion fails
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Delete Failed");
                errorAlert.setContentText("Could not delete the transaction.");
                errorAlert.showAndWait();
            }
        }
    }

}
