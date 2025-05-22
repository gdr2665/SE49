package com.softwareengineering.finsage.dao;

import com.softwareengineering.finsage.model.Budget;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BudgetDao extends BaseDao<Budget> {
    private static final String[] HEADERS = {"id", "month", "categoryId", "amount", "userId"};
    private static final String CSV_FILE = "data/budgets.csv";
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public BudgetDao() {
        super(CSV_FILE, HEADERS);
    }

    @Override
    protected void printRecord(CSVPrinter printer, Budget budget) throws IOException {
        printer.printRecord(
                budget.getId(),
                budget.getMonth().format(MONTH_FORMATTER),
                budget.getCategoryId(),
                budget.getAmount(),
                budget.getUserId()
        );
    }

    @Override
    protected Budget parseRecord(CSVRecord record) {
        Budget budget = new Budget();
        budget.setId(record.get("id"));
        budget.setMonth(YearMonth.parse(record.get("month"), MONTH_FORMATTER));
        String categoryId = record.get("categoryId");
        budget.setCategoryId(categoryId.isEmpty() ? null : categoryId);
        budget.setAmount(new BigDecimal(record.get("amount")));
        budget.setUserId(record.get("userId"));
        return budget;
    }

    @Override
    protected String getId(Budget budget) {
        return budget.getId();
    }

    public List<Budget> getByUserId(String userId) {
        return findBy(b -> b.getUserId().equals(userId));
    }

    public List<Budget> getByMonthAndUserId(YearMonth month, String userId) {
        return findBy(b -> b.getMonth().equals(month) && b.getUserId().equals(userId));
    }

    public Optional<Budget> getTotalBudgetByMonthAndUserId(YearMonth month, String userId) {
        return findBy(b -> b.getMonth().equals(month) &&
                b.getUserId().equals(userId) &&
                b.isTotalBudget())
                .stream()
                .findFirst();
    }

    public Optional<Budget> getCategoryBudgetByMonthAndUserId(YearMonth month, String categoryId, String userId) {
        return findBy(b -> b.getMonth().equals(month) &&
                b.getCategoryId() != null &&
                b.getCategoryId().equals(categoryId) &&
                b.getUserId().equals(userId))
                .stream()
                .findFirst();
    }

    public boolean saveOrUpdateBudget(Budget budget) {
        // Check if budget already exists for this month, user and category
        Optional<Budget> existingBudget;
        if (budget.isTotalBudget()) {
            existingBudget = getTotalBudgetByMonthAndUserId(budget.getMonth(), budget.getUserId());
        } else {
            existingBudget = getCategoryBudgetByMonthAndUserId(budget.getMonth(), budget.getCategoryId(), budget.getUserId());
        }

        if (existingBudget.isPresent()) {
            // Update existing budget
            Budget existing = existingBudget.get();
            existing.setAmount(budget.getAmount());
            return update(existing);
        } else {
            // Save new budget
            return save(budget);
        }
    }
}
