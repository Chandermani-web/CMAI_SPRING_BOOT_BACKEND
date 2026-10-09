package com.cmai.ai.interview;

import com.cmai.ai.AiCalls;
import com.cmai.ai.JsonResponseParser;
import com.cmai.ai.PromptTemplates;
import com.cmai.dto.QuestionDto;
import com.cmai.exception.ApiException;
import com.cmai.util.NodeReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SummaryAgent {

    private final SummaryAssistant assistant;
    private final PromptTemplates templates;
    private final JsonResponseParser parser;
    private final ObjectMapper mapper;

    public SummaryAgent(
            SummaryAssistant assistant, PromptTemplates templates, JsonResponseParser parser, ObjectMapper mapper) {
        this.assistant = assistant;
        this.templates = templates;
        this.parser = parser;
        this.mapper = mapper;
    }

    public InterviewReport summarize(String type, String role, List<QuestionDto> questions) {
        String questionsJson;
        try {
            questionsJson = mapper.writeValueAsString(questions);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialise interview questions", e);
        }

        String prompt = templates.render(
                "interview-summary", Map.of("type", type, "role", role, "questions", questionsJson));

        JsonNode node = parser.parse(AiCalls.guarded(() -> assistant.summarize(prompt)));
        if (!node.isObject()) {
            throw new ApiException(502, "The AI returned an unreadable report. Please try again.");
        }

        // If the model forgets the overall score, fall back to the average of the per-question scores.
        int average = (int) Math.round(
                questions.stream().mapToInt(q -> q.feedback().score()).average().orElse(0));

        return new InterviewReport(
                NodeReader.intIn(node, "overallScore", 0, 100, average),
                NodeReader.text(node, "summary"),
                NodeReader.strings(node, "strengths"),
                NodeReader.strings(node, "weaknesses"),
                NodeReader.strings(node, "recommendations"));
    }
}
