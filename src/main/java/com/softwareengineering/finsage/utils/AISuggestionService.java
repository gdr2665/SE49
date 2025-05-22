package com.softwareengineering.finsage.utils;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.softwareengineering.finsage.dao.BudgetDao;
import com.softwareengineering.finsage.dao.CategoryDao;
import com.softwareengineering.finsage.dao.TransactionDao;
import com.softwareengineering.finsage.model.Budget;
import com.softwareengineering.finsage.model.Category;
import com.softwareengineering.finsage.model.Transaction;
import com.softwareengineering.finsage.utils.UserLoginState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AISuggestionService {
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private final TransactionDao transactionDao;
    private final BudgetDao budgetDao;
    private final CategoryDao categoryDao;

    public AISuggestionService() {
        this.transactionDao = new TransactionDao();
        this.budgetDao = new BudgetDao();
        this.categoryDao = new CategoryDao();
    }

    public String getFinancialAdvice(String userQuestion) {
        String userId = UserLoginState.getCurrentUserId();
        if (userId == null) {
            return "Please login to get financial advice.";
        }

        // Get recent transactions (last 3 months)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(3);
        List<Transaction> transactions = transactionDao.getByUserIdAndDateRange(userId, startDate, endDate);

        // Get current month's budget
        YearMonth currentMonth = YearMonth.now();
        List<Budget> budgets = budgetDao.getByMonthAndUserId(currentMonth, userId);
        Budget totalBudget = budgetDao.getTotalBudgetByMonthAndUserId(currentMonth, userId).orElse(null);

        // Get all categories
        List<Category> categories = categoryDao.getByUserId(userId);

        // Prepare data for the prompt
        String transactionSummary = prepareTransactionSummary(transactions, categories);
        String budgetSummary = prepareBudgetSummary(budgets, totalBudget, categories);

        // Create the prompt
        String systemPrompt = "You are a financial advisor helping users manage their personal finances. " +
                "Analyze the user's financial data and provide personalized advice based on their transactions and budgets. " +
                "Be concise, practical, and focus on actionable recommendations. " +
                "When discussing specific categories, use their exact names from the user's data.";

        String userPrompt = "User's financial data:\n" +
                "=== Transactions (last 3 months) ===\n" +
                transactionSummary + "\n\n" +
                "=== Current Month Budget ===\n" +
                budgetSummary + "\n\n" +
                "User's question: " + userQuestion + "\n\n" +
                "Please provide detailed financial advice addressing the user's question.";

        // Call the AI model
        OpenAIClient client = OpenAIOkHttpClient.builder()
                .baseUrl("https://www.dmxapi.com/v1")
                .apiKey("sk-Pu6K6Vre4B8Mi8nmJQAEHwlOVvqmBNgwAeaBzVi15TIC4GFT")
                .build();

        ChatCompletionCreateParams createParams = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O)
                .temperature(0.7)
                .maxCompletionTokens(500)
                .addSystemMessage(systemPrompt)
                .addUserMessage(userPrompt)
                .build();

        try {
            return client.chat().completions().create(createParams)
                    .choices().stream()
                    .findFirst()
                    .flatMap(choice -> choice.message().content())
                    .map(String::trim)
                    .orElse("Sorry, I couldn't generate advice at this time. Please try again later.");
        } catch (Exception e) {
            return "Error connecting to AI service: " + e.getMessage();
        }
    }

    private String prepareTransactionSummary(List<Transaction> transactions, List<Category> categories) {
        if (transactions.isEmpty()) {
            return "No transactions in the last 3 months.";
        }

        // Group by category and calculate totals
        StringBuilder summary = new StringBuilder();
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Category category : categories) {
            BigDecimal categoryTotal = transactions.stream()
                    .filter(t -> t.getCategoryId().equals(category.getId()))
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (categoryTotal.compareTo(BigDecimal.ZERO) != 0) {
                String categoryType = category.getId().startsWith("INC_") ? "Income" : "Expense";
                if (categoryType.equals("Income")) {
                    totalIncome = totalIncome.add(categoryTotal);
                } else {
                    totalExpense = totalExpense.add(categoryTotal);
                }

                summary.append(String.format("- %s (%s): %,.2f\n",
                        category.getName(), categoryType, categoryTotal));
            }
        }

        // Add uncategorized transactions
        BigDecimal uncategorizedTotal = transactions.stream()
                .filter(t -> categories.stream().noneMatch(c -> c.getId().equals(t.getCategoryId())))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (uncategorizedTotal.compareTo(BigDecimal.ZERO) != 0) {
            summary.append(String.format("- Uncategorized: %,.2f\n", uncategorizedTotal));
            if (uncategorizedTotal.compareTo(BigDecimal.ZERO) > 0) {
                totalIncome = totalIncome.add(uncategorizedTotal);
            } else {
                totalExpense = totalExpense.add(uncategorizedTotal);
            }
        }

        summary.append(String.format("\nTotal Income: %,.2f\n", totalIncome));
        summary.append(String.format("Total Expense: %,.2f\n", totalExpense.abs()));
        summary.append(String.format("Net Savings: %,.2f", totalIncome.subtract(totalExpense.abs())));

        return summary.toString();
    }

    private String prepareBudgetSummary(List<Budget> budgets, Budget totalBudget, List<Category> categories) {
        if (budgets.isEmpty() && totalBudget == null) {
            return "No budget set for current month.";
        }

        StringBuilder summary = new StringBuilder();

        if (totalBudget != null) {
            summary.append(String.format("Total Budget: %,.2f\n", totalBudget.getAmount()));
        }

        for (Budget budget : budgets) {
            if (budget.getCategoryId() != null) {
                String categoryName = categories.stream()
                        .filter(c -> c.getId().equals(budget.getCategoryId()))
                        .findFirst()
                        .map(Category::getName)
                        .orElse("Unknown Category");

                summary.append(String.format("- %s: %,.2f\n", categoryName, budget.getAmount()));
            }
        }

        return summary.toString();
    }

    public List<String> getSuggestedQuestions() {
        return List.of(
                "How can I save more money?",
                "Am I overspending in any category?",
                "What's the best way to allocate my budget?",
                "How do my spending habits compare to last month?",
                "What areas should I focus on to improve my finances?"
        );
    }
}
