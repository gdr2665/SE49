package com.softwareengineering.finsage.utils;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.softwareengineering.finsage.model.Transaction;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class InsightGenerator {
    private static final OpenAIClient client = OpenAIOkHttpClient.builder()
            .baseUrl("https://www.dmxapi.com/v1")
            .apiKey("sk-Pu6K6Vre4B8Mi8nmJQAEHwlOVvqmBNgwAeaBzVi15TIC4GFT")
            .build();

    public static String generateFinancialInsight(ObservableList<Transaction> transactions,
                                                  BigDecimal incomeSum,
                                                  BigDecimal expenseSum,
                                                  BigDecimal totalSum) {
        // Format the transactions data
        StringBuilder transactionsData = new StringBuilder();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        transactionsData.append("Transaction History:\n");
        for (Transaction t : transactions) {
            transactionsData.append(String.format("- %s: %s (%s)\n",
                    t.getDate().format(dateFormatter),
                    currencyFormat.format(t.getAmount()),
                    t.getNote()));
        }

        // Prepare the prompt
        String systemPrompt = "You are a financial analyst. Analyze the following transaction data and provide " +
                "a concise yet insightful report (3-5 paragraphs) that includes:\n" +
                "1. Summary of income vs expenses\n" +
                "2. Notable spending patterns\n" +
                "3. Potential areas for improvement\n" +
                "4. General financial health assessment\n" +
                "Use professional but accessible language and provide actionable recommendations.";

        String userPrompt = String.format("Financial Summary:\n" +
                        "Total Income: %s\n" +
                        "Total Expenses: %s\n" +
                        "Net Balance: %s\n\n" +
                        "%s",
                currencyFormat.format(incomeSum),
                currencyFormat.format(expenseSum),
                currencyFormat.format(totalSum),
                transactionsData.toString());

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
                    .orElse("Failed to generate insight. Please try again.");
        } catch (Exception e) {
            return "Error generating insight: " + e.getMessage();
        }
    }
}
