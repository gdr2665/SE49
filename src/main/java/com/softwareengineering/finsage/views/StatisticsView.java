package com.softwareengineering.finsage.views;

import com.softwareengineering.finsage.controllers.StatisticsController;
import com.softwareengineering.finsage.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class StatisticsView extends BorderPane {
    private StatisticsController controller;
    private ToggleGroup toggleGroup;
    private ToggleButton expenseToggle;
    private ToggleButton incomeToggle;
    private PieChart categoryPieChart;
    private BarChart<String, Number> monthlyBarChart;
    private TableView<Transaction> top10Table;
    private TableView<Map.Entry<String, BigDecimal>> categoryTable;
    private Label totalAmountLabel;
    private ComboBox<YearMonth> monthFilterComboBox;

    public StatisticsView(StatisticsController controller) {
        this.controller = controller;
        initUI();
        updateChartsAndTable(true); // Default to show expenses
    }

    private void initUI() {
        // Create toggle buttons for expense/income
        toggleGroup = new ToggleGroup();
        expenseToggle = new ToggleButton("Expenses");
        incomeToggle = new ToggleButton("Income");

        expenseToggle.setToggleGroup(toggleGroup);
        incomeToggle.setToggleGroup(toggleGroup);
        expenseToggle.setSelected(true);

        toggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                boolean isExpense = newVal == expenseToggle;
                updateChartsAndTable(isExpense);
            }
        });

        // Create month filter combo box
        monthFilterComboBox = new ComboBox<>();
        monthFilterComboBox.setConverter(new StringConverter<YearMonth>() {
            @Override
            public String toString(YearMonth yearMonth) {
                return yearMonth.format(DateTimeFormatter.ofPattern("MMM yyyy"));
            }

            @Override
            public YearMonth fromString(String string) {
                return YearMonth.parse(string, DateTimeFormatter.ofPattern("MMM yyyy"));
            }
        });

        // Populate with months from current year
        YearMonth currentMonth = YearMonth.now();
        for (int i = 0; i < 12; i++) {
            monthFilterComboBox.getItems().add(currentMonth.minusMonths(i));
        }
        monthFilterComboBox.getSelectionModel().select(0);

        monthFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isExpense = toggleGroup.getSelectedToggle() == expenseToggle;
            updateChartsAndTable(isExpense);
        });

        HBox toggleBox = new HBox(10, expenseToggle, incomeToggle, new Label("Filter by Month:"), monthFilterComboBox);
        toggleBox.setPadding(new Insets(10));

        // Create total amount label
        totalAmountLabel = new Label();
        totalAmountLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        HBox headerBox = new HBox(20, toggleBox, totalAmountLabel);
        headerBox.setPadding(new Insets(10));

        // Create pie chart for categories
        categoryPieChart = new PieChart();
        categoryPieChart.setTitle("Category Distribution");

        // Create table for category amounts
        categoryTable = new TableView<>();
        categoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Map.Entry<String, BigDecimal>, String> categoryNameCol = new TableColumn<>("Category");
        categoryNameCol.setCellValueFactory(cell -> {
            String categoryId = cell.getValue().getKey();
            return javafx.beans.binding.Bindings.createStringBinding(
                    () -> controller.getCategoryName(categoryId));
        });

        TableColumn<Map.Entry<String, BigDecimal>, BigDecimal> categoryAmountCol = new TableColumn<>("Amount");
        categoryAmountCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createObjectBinding(() -> cell.getValue().getValue()));
        categoryAmountCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.2f", amount));
                }
            }
        });

        categoryTable.getColumns().addAll(categoryNameCol, categoryAmountCol);

        // Create bar chart for monthly data
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount");
        monthlyBarChart = new BarChart<>(xAxis, yAxis);
        monthlyBarChart.setTitle("Monthly Summary");
        monthlyBarChart.setLegendVisible(false);

        // Create table for top 10 transactions with header label
        VBox top10Container = new VBox(5);
        Label top10Label = new Label("Top 10 Expenses");
        top10Label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        top10Table = new TableView<>();
        top10Table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Date column - correctly handle LocalDate
        TableColumn<Transaction, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });

        // Amount column - correctly handle BigDecimal
        TableColumn<Transaction, BigDecimal> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.2f", amount.abs()));
                }
            }
        });

        // Category column
        TableColumn<Transaction, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(cell -> {
            String categoryId = cell.getValue().getCategoryId();
            return javafx.beans.binding.Bindings.createStringBinding(
                    () -> controller.getCategoryName(categoryId));
        });

        // Note column
        TableColumn<Transaction, String> noteCol = new TableColumn<>("Note");
        noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));

        top10Table.getColumns().addAll(dateCol, amountCol, categoryCol, noteCol);
        top10Container.getChildren().addAll(top10Label, top10Table);

        // Create main layout
        VBox mainBox = new VBox(20);
        mainBox.setPadding(new Insets(15));

        // Create a container for pie chart and category table
        VBox pieChartContainer = new VBox(10, categoryPieChart, categoryTable);

        // Create split pane with the new containers
        SplitPane chartPane = new SplitPane(pieChartContainer, monthlyBarChart);
        chartPane.setDividerPositions(0.5);

        mainBox.getChildren().addAll(headerBox, chartPane, top10Container);

        setCenter(mainBox);
    }

    private void updateChartsAndTable(boolean isExpense) {
        YearMonth selectedMonth = monthFilterComboBox.getValue();

        // Update total amount label
        BigDecimal totalAmount = controller.getTotalAmount(isExpense, selectedMonth);
        totalAmountLabel.setText(String.format("Total %s: %,.2f",
                isExpense ? "Expenses" : "Income",
                totalAmount));

        // Update pie chart
        updatePieChart(isExpense, selectedMonth);

        // Update category table
        updateCategoryTable(isExpense, selectedMonth);

        // Update bar chart
        updateBarChart(isExpense, selectedMonth);

        // Update top 10 table
        updateTop10Table(isExpense, selectedMonth);
    }

    private void updatePieChart(boolean isExpense, YearMonth month) {
        categoryPieChart.getData().clear();

        Map<String, BigDecimal> categorySummary = controller.getCategorySummary(isExpense, month);
        categorySummary.forEach((categoryId, amount) -> {
            String categoryName = controller.getCategoryName(categoryId);
            PieChart.Data slice = new PieChart.Data(
                    String.format("%s (%,.2f)", categoryName, amount),
                    amount.doubleValue()
            );
            categoryPieChart.getData().add(slice);
        });
    }

    private void updateCategoryTable(boolean isExpense, YearMonth month) {
        Map<String, BigDecimal> categorySummary = controller.getCategorySummary(isExpense, month);
        ObservableList<Map.Entry<String, BigDecimal>> items = FXCollections.observableArrayList(categorySummary.entrySet());
        categoryTable.setItems(items);
    }

    private void updateBarChart(boolean isExpense, YearMonth month) {
        monthlyBarChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<YearMonth, BigDecimal> monthlySummary = controller.getMonthlySummary(isExpense);
        monthlySummary.forEach((yearMonth, amount) -> {
            String monthStr = yearMonth.format(DateTimeFormatter.ofPattern("MMM yyyy"));
            series.getData().add(new XYChart.Data<>(monthStr, amount));
        });

        monthlyBarChart.getData().add(series);
    }

    private void updateTop10Table(boolean isExpense, YearMonth month) {
        ObservableList<Transaction> top10 = FXCollections.observableArrayList(
                controller.getTop10Transactions(isExpense, month)
        );
        top10Table.setItems(top10);

        // Update the top 10 label
        VBox parent = (VBox) top10Table.getParent();
        if (parent != null && parent.getChildren().size() > 0 && parent.getChildren().get(0) instanceof Label) {
            Label top10Label = (Label) parent.getChildren().get(0);
            top10Label.setText("Top 10 " + (isExpense ? "Expenses" : "Income"));
        }
    }
}
