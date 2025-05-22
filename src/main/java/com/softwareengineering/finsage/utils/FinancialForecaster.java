package com.softwareengineering.finsage.utils;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.softwareengineering.finsage.dao.CategoryDao;
import com.softwareengineering.finsage.dao.TransactionDao;
import com.softwareengineering.finsage.model.Category;
import com.softwareengineering.finsage.model.Transaction;
import com.softwareengineering.finsage.utils.UserLoginState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class FinancialForecaster {
    private static final TransactionDao transactionDao = new TransactionDao();
    private static final CategoryDao categoryDao = new CategoryDao();

    public static Map<String, BigDecimal> predictExpenseByCategory(YearMonth targetMonth) {
        String userId = UserLoginState.getCurrentUserId();
        if (userId == null) {
            return Collections.emptyMap();
        }

        // Get all categories for the user
        List<Category> categories = categoryDao.getByUserId(userId);
        if (categories.isEmpty()) {
            return Collections.emptyMap();
        }

        // Get historical data (last 6 months)
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.minusMonths(6).atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        List<Transaction> transactions = transactionDao.getByUserIdAndDateRange(userId, startDate, endDate);

        // Filter only expenses (negative amounts)
        List<Transaction> expenses = transactions.stream()
                .filter(t -> t.getAmount().compareTo(BigDecimal.ZERO) < 0)
                .collect(Collectors.toList());

        if (expenses.isEmpty()) {
            return Collections.emptyMap();
        }

        // Group by category and month to prepare historical data
        Map<String, Map<YearMonth, BigDecimal>> categoryMonthlyExpenses = new HashMap<>();

        for (Category category : categories) {
            Map<YearMonth, BigDecimal> monthlyData = expenses.stream()
                    .filter(t -> t.getCategoryId().equals(category.getId()))
                    .collect(Collectors.groupingBy(
                            t -> YearMonth.from(t.getDate()),
                            Collectors.reducing(
                                    BigDecimal.ZERO,
                                    t -> t.getAmount().abs(),
                                    BigDecimal::add
                            )
                    ));
            categoryMonthlyExpenses.put(category.getName(), monthlyData);
        }

        // Prepare data for the prompt
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Based on the following historical monthly expense data by category:\n");

        categoryMonthlyExpenses.forEach((categoryName, monthlyData) -> {
            promptBuilder.append("Category: ").append(categoryName).append("\n");
            monthlyData.forEach((month, amount) -> {
                promptBuilder.append(month.format(DateTimeFormatter.ofPattern("yyyy-MM")))
                        .append(": ")
                        .append(amount)
                        .append("\n");
            });
            promptBuilder.append("\n");
        });

        promptBuilder.append("Predict the expense distribution by category for ")
                .append(targetMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")))
                .append(". Consider seasonal trends, spending patterns, and category-specific trends. ")
                .append("Respond only with a JSON object where keys are category names and values are predicted amounts, ")
                .append("like this: {\"Food\": 500.00, \"Transportation\": 300.00, ...}")
                .append("\nImportant: Do not include any additional text or explanations, only the JSON object.");

        OpenAIClient client = OpenAIOkHttpClient.builder()
                .baseUrl("https://www.dmxapi.com/v1")
                .apiKey("sk-Pu6K6Vre4B8Mi8nmJQAEHwlOVvqmBNgwAeaBzVi15TIC4GFT")
                .build();

        ChatCompletionCreateParams createParams = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O)
                .temperature(0.3)
                .maxCompletionTokens(500)
                .addUserMessage(promptBuilder.toString())
                .build();

        try {
            String response = client.chat().completions().create(createParams)
                    .choices().stream()
                    .findFirst()
                    .flatMap(choice -> choice.message().content())
                    .map(String::trim)
                    .orElse("{}");

            // Clean the response and parse JSON
            return parsePredictionResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    private static Map<String, BigDecimal> parsePredictionResponse(String response) {
        Map<String, BigDecimal> result = new HashMap<>();

        try {
            // Clean the response string
            String cleanedResponse = response.trim();

            // Remove any markdown code block markers
            if (cleanedResponse.startsWith("```json")) {
                cleanedResponse = cleanedResponse.substring(7);
            }
            if (cleanedResponse.startsWith("```")) {
                cleanedResponse = cleanedResponse.substring(3);
            }
            if (cleanedResponse.endsWith("```")) {
                cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3);
            }

            // Remove all whitespace and newlines
            cleanedResponse = cleanedResponse.replaceAll("\\s+", "");

            // Extract the JSON content between curly braces
            int startIdx = cleanedResponse.indexOf('{');
            int endIdx = cleanedResponse.lastIndexOf('}');

            if (startIdx >= 0 && endIdx > startIdx) {
                String jsonContent = cleanedResponse.substring(startIdx + 1, endIdx);

                // Split into key-value pairs
                String[] pairs = jsonContent.split(",");

                for (String pair : pairs) {
                    String[] keyValue = pair.split(":");
                    if (keyValue.length == 2) {
                        try {
                            // Remove quotes from key and any whitespace
                            String key = keyValue[0].replaceAll("\"", "").trim();
                            String valueStr = keyValue[1].replaceAll("\"", "").trim();

                            // Handle numeric values with commas (like 8,200.00)
                            valueStr = valueStr.replace(",", "");

                            BigDecimal value = new BigDecimal(valueStr);
                            result.put(key, value);
                        } catch (Exception e) {
                            // Skip invalid entries
                            continue;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
