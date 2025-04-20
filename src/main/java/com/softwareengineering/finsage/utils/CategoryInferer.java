package com.softwareengineering.finsage.utils;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.softwareengineering.finsage.dao.CategoryDao;
import com.softwareengineering.finsage.model.Category;

import java.util.List;
import java.util.stream.Collectors;

public class CategoryInferer {
    private static final CategoryDao categoryDao = new CategoryDao();

    public static String inferCategoryFromNote(String note) {
        if (note == null || note.trim().isEmpty()) {
            return null;
        }

        // Get current user's ID
        String userId = UserLoginState.getCurrentUserId();
        if (userId == null) {
            return null;
        }

        // Get all categories for the current user
        List<Category> userCategories = categoryDao.getByUserId(userId);
        if (userCategories.isEmpty()) {
            return null;
        }

        // Prepare category list for the prompt
        String categoryList = userCategories.stream()
                .map(Category::getName)
                .collect(Collectors.joining(", "));

        String systemPrompt = "Analyze the following transaction note and classify it into exactly one of these categories: " +
                categoryList + ". Only respond with the category name exactly as listed.";

        OpenAIClient client = OpenAIOkHttpClient.builder()
                .baseUrl("https://www.dmxapi.com/v1")
                .apiKey("sk-Pu6K6Vre4B8Mi8nmJQAEHwlOVvqmBNgwAeaBzVi15TIC4GFT")
                .build();

        ChatCompletionCreateParams createParams = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O)
                .temperature(0.2)
                .maxCompletionTokens(20)
                .addSystemMessage(systemPrompt)
                .addUserMessage(note)
                .build();

        try {
            return client.chat().completions().create(createParams)
                    .choices().stream()
                    .findFirst()
                    .flatMap(choice -> choice.message().content())
                    .map(String::trim)
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
