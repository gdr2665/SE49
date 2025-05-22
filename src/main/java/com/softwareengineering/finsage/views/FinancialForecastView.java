package com.softwareengineering.finsage.views;

import com.softwareengineering.finsage.controllers.FinancialForecastController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class FinancialForecastView extends BorderPane {
    private FinancialForecastController controller;
    private ComboBox<YearMonth> monthComboBox;
    private PieChart categoryPieChart;
    private Label totalAmountLabel;
    private Label predictionTitleLabel;
    private Button predictButton;
    private ProgressIndicator progressIndicator;
    private TableView<CategoryPrediction> predictionTable;

    public FinancialForecastView(FinancialForecastController controller) {
        this.controller = controller;
        initUI();
    }

    private void initUI() {
        // Create month selection combo box
        monthComboBox = new ComboBox<>();
        monthComboBox.setConverter(new StringConverter<YearMonth>() {
            @Override
            public String toString(YearMonth yearMonth) {
                return yearMonth.format(DateTimeFormatter.ofPattern("MMM yyyy"));
            }

            @Override
            public YearMonth fromString(String string) {
                return YearMonth.parse(string, DateTimeFormatter.ofPattern("MMM yyyy"));
            }
        });
        monthComboBox.setItems(controller.getFutureMonths());
        monthComboBox.getSelectionModel().selectFirst();

        // Create predict button
        predictButton = new Button("Predict");
        predictButton.setOnAction(e -> makePrediction());

        // Create progress indicator
        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);

        // Create labels
        predictionTitleLabel = new Label();
        predictionTitleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        totalAmountLabel = new Label();
        totalAmountLabel.setStyle("-fx-font-size: 14px;");

        // Create pie chart
        categoryPieChart = new PieChart();
        categoryPieChart.setTitle("Predicted Expense by Category");
        categoryPieChart.setLegendVisible(true);
        categoryPieChart.setLabelsVisible(true);

        // Create prediction table
        predictionTable = new TableView<>();
        predictionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Create columns for the table
        TableColumn<CategoryPrediction, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<CategoryPrediction, BigDecimal> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountColumn.setCellFactory(tc -> new TableCell<CategoryPrediction, BigDecimal>() {
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

        predictionTable.getColumns().addAll(categoryColumn, amountColumn);
        predictionTable.setPlaceholder(new Label("No prediction data available"));

        // Create controls container
        HBox controlsBox = new HBox(10,
                new Label("Select Month:"),
                monthComboBox,
                predictButton,
                progressIndicator);
        controlsBox.setPadding(new Insets(10));

        // Create labels container
        VBox labelsBox = new VBox(5, predictionTitleLabel, totalAmountLabel);
        labelsBox.setPadding(new Insets(0, 10, 10, 10));

        // Create main layout
        VBox mainBox = new VBox(20);
        mainBox.setPadding(new Insets(15));
        mainBox.getChildren().addAll(controlsBox, labelsBox, categoryPieChart, predictionTable);

        setCenter(mainBox);
    }

    private void makePrediction() {
        YearMonth selectedMonth = monthComboBox.getValue();
        if (selectedMonth == null) {
            return;
        }

        // Show loading indicator
        predictButton.setDisable(true);
        progressIndicator.setVisible(true);
        categoryPieChart.setData(FXCollections.emptyObservableList());
        predictionTable.setItems(FXCollections.emptyObservableList());

        // Run prediction in a background thread to keep UI responsive
        new Thread(() -> {
            Map<String, BigDecimal> categoryPredictions = controller.predictExpenseByCategory(selectedMonth);
            BigDecimal totalAmount = controller.calculateTotalPredictedExpense(categoryPredictions);
            ObservableList<PieChart.Data> pieChartData = controller.convertToPieChartData(categoryPredictions);

            // Convert predictions to table data
            ObservableList<CategoryPrediction> tableData = FXCollections.observableArrayList();
            categoryPredictions.forEach((category, amount) ->
                    tableData.add(new CategoryPrediction(category, amount))
            );

            // Update UI on JavaFX Application Thread
            javafx.application.Platform.runLater(() -> {
                updatePredictionDisplay(selectedMonth, totalAmount, pieChartData, tableData);
                predictButton.setDisable(false);
                progressIndicator.setVisible(false);
            });
        }).start();
    }

    private void updatePredictionDisplay(YearMonth month, BigDecimal totalAmount,
                                         ObservableList<PieChart.Data> pieChartData, ObservableList<CategoryPrediction> tableData) {
        // Update labels
        predictionTitleLabel.setText(String.format("Predicted Expenses for %s",
                month.format(DateTimeFormatter.ofPattern("MMM yyyy"))));

        totalAmountLabel.setText(String.format("Total Predicted: %,.2f", totalAmount));

        // Update pie chart
        categoryPieChart.setData(pieChartData);

        // Customize pie chart appearance
        for (PieChart.Data data : pieChartData) {
            data.getNode().setStyle(
                    "-fx-pie-color: " + getCategoryColor(data.getName()) + ";"
            );
        }

        // Update table
        predictionTable.setItems(tableData);
    }

    private String getCategoryColor(String categoryName) {
        // Simple color mapping based on category name hash
        int hash = categoryName.hashCode();
        String[] colors = {
                "#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF",
                "#FF9F40", "#8AC249", "#EA5F89", "#00BFFF", "#FFA07A"
        };
        return colors[Math.abs(hash) % colors.length];
    }

    // Model class for the table data
    public static class CategoryPrediction {
        private final String category;
        private final BigDecimal amount;

        public CategoryPrediction(String category, BigDecimal amount) {
            this.category = category;
            this.amount = amount;
        }

        public String getCategory() {
            return category;
        }

        public BigDecimal getAmount() {
            return amount;
        }
    }
}
