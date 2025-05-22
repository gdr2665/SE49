package com.softwareengineering.finsage.controllers;

import com.softwareengineering.finsage.dao.CategoryDao;
import com.softwareengineering.finsage.utils.FinancialForecaster;
import com.softwareengineering.finsage.utils.UserLoginState;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;

public class FinancialForecastController {
    private CategoryDao categoryDao;

    public FinancialForecastController() {
        this.categoryDao = new CategoryDao();
    }

    public ObservableList<YearMonth> getFutureMonths() {
        YearMonth currentMonth = YearMonth.now();
        List<YearMonth> futureMonths = new ArrayList<>();

        for (int i = 1; i <= 6; i++) {
            futureMonths.add(currentMonth.plusMonths(i));
        }

        return FXCollections.observableArrayList(futureMonths);
    }

    public Map<String, BigDecimal> predictExpenseByCategory(YearMonth month) {
        if (UserLoginState.getCurrentUserId() == null) {
            return Collections.emptyMap();
        }

        return FinancialForecaster.predictExpenseByCategory(month);
    }

    public ObservableList<PieChart.Data> convertToPieChartData(Map<String, BigDecimal> categoryPredictions) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        categoryPredictions.forEach((category, amount) -> {
            pieChartData.add(new PieChart.Data(
                    String.format("%s (%,.2f)", category, amount),
                    amount.doubleValue()
            ));
        });

        return pieChartData;
    }

    public BigDecimal calculateTotalPredictedExpense(Map<String, BigDecimal> categoryPredictions) {
        return categoryPredictions.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
