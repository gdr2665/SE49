package com.softwareengineering.finsage.views;

import com.softwareengineering.finsage.controllers.BudgetController;
import com.softwareengineering.finsage.model.Budget;
import com.softwareengineering.finsage.model.Category;
import com.softwareengineering.finsage.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public class BudgetView extends BorderPane {
    private BudgetController controller;
    private ComboBox<YearMonth> monthComboBox;
    private TableView<Budget> budgetTable;
    private ObservableList<Budget> budgets;
    private TextField totalBudgetField;
    private Label totalSpentLabel;
    private Label totalRemainingLabel;
    private ProgressBar totalProgressBar;

    public BudgetView(BudgetController controller) {
        this.controller = controller;
        this.budgets = FXCollections.observableArrayList();

        initUI();
        loadBudgets();
    }

    private void initUI() {
        // Create filter controls at the top
        GridPane filterGrid = new GridPane();
        filterGrid.setPadding(new Insets(10));
        filterGrid.setHgap(10);
        filterGrid.setVgap(10);

        // Month selection
        Label monthLabel = new Label("Month:");
        monthComboBox = new ComboBox<>();
        monthComboBox.setConverter(new StringConverter<YearMonth>() {
            @Override
            public String toString(YearMonth yearMonth) {
                return yearMonth == null ? "" : yearMonth.toString();
            }

            @Override
            public YearMonth fromString(String string) {
                return string == null || string.isEmpty() ? null : YearMonth.parse(string);
            }
        });

        // Populate with current and next 5 months
        YearMonth currentMonth = YearMonth.now();
        ObservableList<YearMonth> months = FXCollections.observableArrayList();
        for (int i = 0; i < 6; i++) {
            months.add(currentMonth.plusMonths(i));
        }
        monthComboBox.setItems(months);
        monthComboBox.setValue(currentMonth);

        // Add listener to month combo box
        monthComboBox.valueProperty().addListener((obs, oldVal, newVal) -> loadBudgets());

        // Total budget controls
        Label totalBudgetLabel = new Label("Total Budget:");
        totalBudgetField = new TextField();
        totalBudgetField.setPromptText("Enter total budget");

        Button saveTotalBudgetBtn = new Button("Save");
        saveTotalBudgetBtn.setOnAction(e -> saveTotalBudget());

        HBox totalBudgetBox = new HBox(10);
        totalBudgetBox.getChildren().addAll(totalBudgetField, saveTotalBudgetBtn);

        filterGrid.add(monthLabel, 0, 0);
        filterGrid.add(monthComboBox, 1, 0);
        filterGrid.add(totalBudgetLabel, 0, 1);
        filterGrid.add(totalBudgetBox, 1, 1);

        // Create summary panel for total budget
        VBox totalSummaryBox = new VBox(10);
        totalSummaryBox.setPadding(new Insets(10));

        totalSpentLabel = new Label("Spent: 0.00");
        totalRemainingLabel = new Label("Remaining: 0.00");
        totalProgressBar = new ProgressBar(0);
        totalProgressBar.setPrefWidth(200);

        totalSummaryBox.getChildren().addAll(
                new Label("Total Budget Summary:"),
                totalSpentLabel,
                totalRemainingLabel,
                totalProgressBar
        );

        // Create budget table
        budgetTable = new TableView<>();
        budgetTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Category column
        TableColumn<Budget, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(cell -> {
            String categoryId = cell.getValue().getCategoryId();
            if (categoryId == null) {
                return javafx.beans.binding.Bindings.createStringBinding(() -> "Total Budget");
            }
            Category category = controller.getCategoryById(categoryId);
            return javafx.beans.binding.Bindings.createStringBinding(
                    () -> category != null ? category.getName() : "Unknown Category");
        });

        // Budget amount column
        TableColumn<Budget, String> budgetCol = new TableColumn<>("Budget");
        budgetCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> String.format("%,.2f", cell.getValue().getAmount()))
        );

        // Spent amount column
        TableColumn<Budget, String> spentCol = new TableColumn<>("Spent");
        spentCol.setCellValueFactory(cell -> {
            YearMonth month = cell.getValue().getMonth();
            String categoryId = cell.getValue().getCategoryId();
            BigDecimal spent = controller.getSpentAmount(month, categoryId);
            return javafx.beans.binding.Bindings.createStringBinding(
                    () -> String.format("%,.2f", spent));
        });

        // Remaining amount column
        TableColumn<Budget, String> remainingCol = new TableColumn<>("Remaining");
        remainingCol.setCellValueFactory(cell -> {
            YearMonth month = cell.getValue().getMonth();
            String categoryId = cell.getValue().getCategoryId();
            BigDecimal spent = controller.getSpentAmount(month, categoryId);
            BigDecimal remaining = cell.getValue().getAmount().subtract(spent);
            return javafx.beans.binding.Bindings.createStringBinding(
                    () -> String.format("%,.2f", remaining));
        });

        // Progress column
        TableColumn<Budget, Void> progressCol = new TableColumn<>("Progress");
        progressCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Budget, Void> call(TableColumn<Budget, Void> param) {
                return new TableCell<>() {
                    private final ProgressBar progressBar = new ProgressBar();

                    {
                        progressBar.setPrefWidth(150);
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Budget budget = getTableView().getItems().get(getIndex());
                            YearMonth month = budget.getMonth();
                            String categoryId = budget.getCategoryId();
                            BigDecimal spent = controller.getSpentAmount(month, categoryId);
                            double progress = spent.divide(budget.getAmount(), 4, BigDecimal.ROUND_HALF_UP).doubleValue();
                            progressBar.setProgress(progress);

                            // Set color based on progress
                            if (progress > 0.9) {
                                progressBar.setStyle("-fx-accent: red;");
                            } else if (progress > 0.7) {
                                progressBar.setStyle("-fx-accent: orange;");
                            } else {
                                progressBar.setStyle("-fx-accent: green;");
                            }

                            setGraphic(progressBar);
                        }
                    }
                };
            }
        });

        // Action column
        TableColumn<Budget, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Budget, Void> call(TableColumn<Budget, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");

                    {
                        editBtn.setOnAction(e -> {
                            Budget budget = getTableView().getItems().get(getIndex());
                            editBudget(budget);
                        });

                        deleteBtn.setOnAction(e -> {
                            Budget budget = getTableView().getItems().get(getIndex());
                            deleteBudget(budget);
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

        budgetTable.getColumns().addAll(categoryCol, budgetCol, spentCol, remainingCol, progressCol, actionCol);
        budgetTable.setItems(budgets);

        // Add button for new category budgets
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10));

        Button addCategoryBudgetBtn = new Button("Add Category Budget");
        addCategoryBudgetBtn.setOnAction(e -> addCategoryBudget());

        buttonBox.getChildren().add(addCategoryBudgetBtn);

        // Set layout
        setTop(filterGrid);
        setCenter(budgetTable);
        setRight(totalSummaryBox);
        setBottom(buttonBox);
    }

    private void loadBudgets() {
        YearMonth month = monthComboBox.getValue();
        if (month == null) return;

        // Load budgets for selected month
        List<Budget> monthBudgets = controller.getBudgetsByMonth(month);
        budgets.setAll(monthBudgets);

        // Update total budget summary
        updateTotalBudgetSummary(month);
    }

    private void updateTotalBudgetSummary(YearMonth month) {
        Optional<Budget> totalBudget = controller.getTotalBudget(month);
        BigDecimal totalSpent = controller.getTotalSpentAmount(month);

        if (totalBudget.isPresent()) {
            totalBudgetField.setText(totalBudget.get().getAmount().toString());
            BigDecimal remaining = totalBudget.get().getAmount().subtract(totalSpent);

            totalSpentLabel.setText("Spent: " + String.format("%,.2f", totalSpent));
            totalRemainingLabel.setText("Remaining: " + String.format("%,.2f", remaining));

            // 防止除以零
            if (totalBudget.get().getAmount().compareTo(BigDecimal.ZERO) > 0) {
                double progress = totalSpent.divide(totalBudget.get().getAmount(), 4, BigDecimal.ROUND_HALF_UP).doubleValue();
                totalProgressBar.setProgress(progress);

                // 设置进度条颜色
                if (progress > 0.9) {
                    totalProgressBar.setStyle("-fx-accent: red;");
                } else if (progress > 0.7) {
                    totalProgressBar.setStyle("-fx-accent: orange;");
                } else {
                    totalProgressBar.setStyle("-fx-accent: green;");
                }
            } else {
                totalProgressBar.setProgress(0);
            }
        } else {
            totalBudgetField.clear();
            totalSpentLabel.setText("Spent: 0.00");
            totalRemainingLabel.setText("Remaining: 0.00");
            totalProgressBar.setProgress(0);
        }
    }


    private void saveTotalBudget() {
        YearMonth month = monthComboBox.getValue();
        if (month == null) return;

        try {
            BigDecimal amount = new BigDecimal(totalBudgetField.getText().trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert("Invalid Amount", "Budget amount must be positive.");
                return;
            }

            Budget budget = new Budget();
            budget.setId(java.util.UUID.randomUUID().toString());
            budget.setMonth(month);
            budget.setCategoryId(null); // null for total budget
            budget.setAmount(amount);
            budget.setUserId(controller.getCurrentUserId());

            if (controller.saveBudget(budget)) {
                loadBudgets(); // Refresh the view
            } else {
                showAlert("Error", "Failed to save total budget.");
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Amount", "Please enter a valid number for the budget amount.");
        }
    }

    private void addCategoryBudget() {
        YearMonth month = monthComboBox.getValue();
        if (month == null) return;

        BudgetDialog dialog = new BudgetDialog((Stage) getScene().getWindow(), month, null);
        dialog.showAndWait();
        loadBudgets(); // Refresh the view
    }

    private void editBudget(Budget budget) {
        BudgetDialog dialog = new BudgetDialog((Stage) getScene().getWindow(), budget.getMonth(), budget);
        dialog.showAndWait();
        loadBudgets(); // Refresh the view
    }

    private void deleteBudget(Budget budget) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Budget");
        alert.setContentText("Are you sure you want to delete this budget?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (controller.deleteBudget(budget.getId())) {
                loadBudgets(); // Refresh the view
            } else {
                showAlert("Error", "Failed to delete the budget.");
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
