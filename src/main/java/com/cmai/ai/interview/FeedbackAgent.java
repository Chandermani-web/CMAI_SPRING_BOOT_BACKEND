package com.cmai.ai.interview;

import com.cmai.ai.AiCalls;
import com.cmai.ai.JsonResponseParser;
import com.cmai.ai.PromptTemplates;
import com.cmai.dto.FeedbackDto;
import com.cmai.exception.ApiException;
import com.cmai.util.NodeReader;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class FeedbackAgent {

    private final FeedbackAssistant assistant;
    private final PromptTemplates templates;
    private final JsonResponseParser parser;

    public FeedbackAgent(FeedbackAssistant assistant, PromptTemplates templates, JsonResponseParser parser) {
        this.assistant = assistant;
        this.templates = templates;
        this.parser = parser;
    }

    public FeedbackDto evaluate(String question, String answer, String difficulty) {
        String prompt = templates.render(
                "interview-feedback",
                Map.of("question", question, "answer", answer, "difficulty", difficulty));

        JsonNode node = parser.parse(AiCalls.guarded(() -> assistant.evaluate(prompt)));
        if (!node.isObject()) {
            throw new ApiException(502, "The AI returned unreadable feedback. Please try again.");
        }

        // The model may answer with "improvements" (as prompted) or "improvement".
        List<String> improvement = NodeReader.strings(node, "improvements");
        if (improvement.isEmpty()) {
            improvement = NodeReader.strings(node, "improvement");
        }

        return new FeedbackDto(
                NodeReader.intIn(node, "score", 0, 100, 0),
                NodeReader.intIn(node, "correctness", 0, 100, 0),
                NodeReader.intIn(node, "clarity", 0, 100, 0),
                NodeReader.intIn(node, "relevance", 0, 100, 0),
                NodeReader.intIn(node, "detail", 0, 100, 0),
                NodeReader.intIn(node, "efficiency", 0, 100, 0),
                NodeReader.intIn(node, "problemSolving", 0, 100, 0),
                NodeReader.intIn(node, "communication", 0, 100, 0),
                NodeReader.intIn(node, "creativity", 0, 100, 0),
                NodeReader.text(node, "feedback"),
                improvement);
    }
}
