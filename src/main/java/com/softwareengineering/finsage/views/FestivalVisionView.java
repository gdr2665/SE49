// File: com.softwareengineering.finsage.views.FestivalVisionView.java
package com.softwareengineering.finsage.views;

import com.softwareengineering.finsage.controllers.FestivalVisionController;
import com.softwareengineering.finsage.model.Category;
import com.softwareengineering.finsage.model.Holiday;
import com.softwareengineering.finsage.model.Transaction;
import com.softwareengineering.finsage.utils.InsightGenerator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class FestivalVisionView extends BorderPane {
    private FestivalVisionController controller;
    private TableView<Transaction> transactionTable;
    private ComboBox<Category> categoryFilter;
    private ComboBox<Holiday> holidayFilter;
    private ObservableList<Transaction> transactions;
    private Label incomeSumLabel;
    private Label expenseSumLabel;
    private Label totalSumLabel;
    private PieChart categoryPieChart;

    public FestivalVisionView(FestivalVisionController controller) {
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
        filterGrid.add(categoryBox, 0, 0);

        // Holiday filter
        VBox holidayBox = new VBox(5);
        Label holidayLabel = new Label("Holiday:");
        holidayFilter = new ComboBox<>();
        holidayFilter.setPromptText("All Holidays");
        holidayFilter.setConverter(new StringConverter<Holiday>() {
            @Override
            public String toString(Holiday holiday) {
                return holiday == null ? "" : holiday.getName();
            }

            @Override
            public Holiday fromString(String string) {
                return null;
            }
        });

        // Add listener to holiday filter
        holidayFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        Button manageHolidaysBtn = new Button("Manage");
        manageHolidaysBtn.setOnAction(e -> {
            HolidayDialog dialog = new HolidayDialog((Stage) getScene().getWindow());
            dialog.show();
            refreshHolidays();
        });

        HBox holidayControlBox = new HBox(5);
        holidayControlBox.getChildren().addAll(holidayFilter, manageHolidaysBtn);
        holidayBox.getChildren().addAll(holidayLabel, holidayControlBox);
        filterGrid.add(holidayBox, 1, 0);

        // Create summary labels
        incomeSumLabel = new Label("Income: 0.00");
        expenseSumLabel = new Label("Expense: 0.00");
        totalSumLabel = new Label("Total: 0.00");

        VBox summaryBox = new VBox(5);
        summaryBox.getChildren().addAll(incomeSumLabel, expenseSumLabel, totalSumLabel);
        filterGrid.add(summaryBox, 2, 0);

        // Create transaction table
        transactionTable = new TableView<>();
        transactionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Date column
        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        // Amount column
        TableColumn<Transaction, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

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

        transactionTable.getColumns().addAll(dateCol, amountCol, categoryCol, noteCol);
        transactionTable.setItems(transactions);

        // Add listener to transactions to update summary and pie chart
        transactions.addListener((javafx.collections.ListChangeListener.Change<? extends Transaction> c) -> {
            updateSummaryLabels();
            updatePieChart();
        });

        // Create pie chart
        categoryPieChart = new PieChart();
        categoryPieChart.setTitle("Category Distribution");
        updatePieChart();

        // Split pane for table and chart
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(transactionTable, categoryPieChart);
        splitPane.setDividerPositions(0.7);

        // Add button for new transactions
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10));

        Button addItemBtn = new Button("Add Item");
        addItemBtn.setOnAction(e -> {
            TransactionDialog dialog = new TransactionDialog((Stage) getScene().getWindow());
            dialog.showAndWait();
            loadTransactions();
        });

        Button generateInsightBtn = new Button("Generate Insight");
        generateInsightBtn.setOnAction(e -> generateAndShowInsight());

        buttonBox.getChildren().addAll(addItemBtn, generateInsightBtn);

        // Set layout
        setTop(filterGrid);
        setCenter(splitPane);
        setBottom(buttonBox);

        // Initialize filters
        refreshCategories();
        refreshHolidays();
    }

    private void updatePieChart() {
        categoryPieChart.getData().clear();

        // Group transactions by category and sum amounts
        transactions.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        t -> {
                            Category category = controller.getCategoryById(t.getCategoryId());
                            return category != null ? category.getName() : "Unknown";
                        },
                        java.util.stream.Collectors.reducing(
                                BigDecimal.ZERO,
                                Transaction::getAmount,
                                BigDecimal::add
                        )
                ))
                .forEach((categoryName, totalAmount) -> {
                    PieChart.Data slice = new PieChart.Data(
                            categoryName,
                            totalAmount.doubleValue()
                    );
                    categoryPieChart.getData().add(slice);
                });
    }

    private void updateSummaryLabels() {
        BigDecimal incomeSum = BigDecimal.ZERO;
        BigDecimal expenseSum = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if (t.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                incomeSum = incomeSum.add(t.getAmount());
            } else {
                expenseSum = expenseSum.add(t.getAmount().abs());
            }
        }

        BigDecimal totalSum = incomeSum.subtract(expenseSum);

        incomeSumLabel.setText(String.format("Income: %,.2f", incomeSum));
        expenseSumLabel.setText(String.format("Expense: %,.2f", expenseSum));
        totalSumLabel.setText(String.format("Total: %,.2f", totalSum));
    }

    private void loadTransactions() {
        transactions.setAll(controller.getTransactions());
        updateSummaryLabels();
        updatePieChart();
    }

    private void refreshCategories() {
        categoryFilter.setItems(FXCollections.observableArrayList(controller.getCategories()));
    }

    private void refreshHolidays() {
        holidayFilter.setItems(FXCollections.observableArrayList(controller.getHolidays()));
    }

    private void applyFilters() {
        Category selectedCategory = categoryFilter.getValue();
        Holiday selectedHoliday = holidayFilter.getValue();

        List<Transaction> filtered = controller.getFilteredTransactions(
                selectedCategory != null ? selectedCategory.getId() : null,
                selectedHoliday
        );

        transactions.setAll(filtered);
        updateSummaryLabels();
        updatePieChart();
    }

    // 修改 generateAndShowInsight() 方法:
    private void generateAndShowInsight() {
        // Calculate current sums
        BigDecimal incomeSum = BigDecimal.ZERO;
        BigDecimal expenseSum = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if (t.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                incomeSum = incomeSum.add(t.getAmount());
            } else {
                expenseSum = expenseSum.add(t.getAmount().abs());
            }
        }
        BigDecimal totalSum = incomeSum.subtract(expenseSum);

        try {
            String insight = InsightGenerator.generateFinancialInsight(
                    transactions, incomeSum, expenseSum, totalSum);
            showInsightDialog(insight);
        } catch (Exception e) {
            showErrorDialog("Failed to generate insight: " + e.getMessage());
        }
    }

    private void showInsightDialog(String insight) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Financial Insight");
        alert.setHeaderText("Your Financial Analysis");

        TextArea textArea = new TextArea(insight);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefSize(600, 400);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Failed to generate insight");
        alert.setContentText(message);
        alert.showAndWait();
    }
}