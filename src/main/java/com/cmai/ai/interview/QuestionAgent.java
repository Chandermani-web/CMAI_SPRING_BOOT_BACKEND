package com.cmai.ai.interview;

import com.cmai.ai.AiCalls;
import com.cmai.ai.JsonResponseParser;
import com.cmai.ai.PromptTemplates;
import com.cmai.exception.ApiException;
import com.cmai.util.NodeReader;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class QuestionAgent {

    private static final Set<String> DIFFICULTIES = Set.of("easy", "medium", "hard");
    private static final int MAX_QUESTIONS = 10;

    private final QuestionAssistant assistant;
    private final PromptTemplates templates;
    private final JsonResponseParser parser;

    public QuestionAgent(QuestionAssistant assistant, PromptTemplates templates, JsonResponseParser parser) {
        this.assistant = assistant;
        this.templates = templates;
        this.parser = parser;
    }

    /**
     * @param type "technical" or "hr"
     * @param resumeBlock resume text for personalisation, or null when the resume is not used
     */
    public List<GeneratedQuestion> generate(String type, String role, String resumeBlock) {
        String template = "hr".equals(type) ? "interview-hr" : "interview-technical";
        String prompt = templates.render(
                template,
                Map.of(
                        "role", role,
                        "resumeAvailable", resumeBlock == null ? "NO" : "YES",
                        "resumeBlock", resumeBlock == null ? "" : resumeBlock));

        JsonNode node = parser.parse(AiCalls.guarded(() -> assistant.generate(prompt)));
        if (node.isObject() && node.get("questions") != null) {
            node = node.get("questions");
        }
        if (!node.isArray()) {
            throw new ApiException(502, "Failed to generate interview questions. Please try again.");
        }

        List<GeneratedQuestion> questions = new ArrayList<>();
        for (JsonNode item : node) {
            if (questions.size() >= MAX_QUESTIONS) {
                break;
            }
            String text = NodeReader.text(item, "question");
            if (text.isBlank()) {
                continue;
            }
            String difficulty = NodeReader.text(item, "difficulty").toLowerCase(Locale.ROOT);
            if (!DIFFICULTIES.contains(difficulty)) {
                difficulty = "easy";
            }
            int timer = NodeReader.intIn(item, "timer", 60, 3600, 60);
            questions.add(new GeneratedQuestion(text, difficulty, timer));
        }

        if (questions.isEmpty()) {
            throw new ApiException(502, "Failed to generate interview questions. Please try again.");
        }
        return questions;
    }
}
