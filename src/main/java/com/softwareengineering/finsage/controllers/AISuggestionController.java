package com.softwareengineering.finsage.controllers;

import com.softwareengineering.finsage.utils.AISuggestionService;

import java.util.List;

public class AISuggestionController {
    private final AISuggestionService aiSuggestionService;

    public AISuggestionController() {
        this.aiSuggestionService = new AISuggestionService();
    }

    public String getFinancialAdvice(String question) {
        return aiSuggestionService.getFinancialAdvice(question);
    }

    public List<String> getSuggestedQuestions() {
        return aiSuggestionService.getSuggestedQuestions();
    }
}
