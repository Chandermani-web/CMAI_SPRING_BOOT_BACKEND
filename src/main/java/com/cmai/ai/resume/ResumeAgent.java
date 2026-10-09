package com.cmai.ai.resume;

import com.cmai.ai.JsonResponseParser;
import com.cmai.ai.PromptTemplates;
import com.cmai.ai.SpringAiChat;
import com.cmai.exception.ApiException;
import com.cmai.util.NodeReader;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

/** Spring AI agent: turns raw resume text into a structured analysis and an ATS-style score. */
@Component
public class ResumeAgent {

    private static final int MAX_CHARS = 40_000;

    private final SpringAiChat chat;
    private final PromptTemplates templates;
    private final JsonResponseParser parser;

    public ResumeAgent(SpringAiChat chat, PromptTemplates templates, JsonResponseParser parser) {
        this.chat = chat;
        this.templates = templates;
        this.parser = parser;
    }

    public ResumeAnalysis analyze(String resumeText) {
        String text = resumeText.length() > MAX_CHARS ? resumeText.substring(0, MAX_CHARS) : resumeText;
        String reply = chat.complete(templates.raw("resume-system"), text);

        JsonNode node = parser.parse(reply);
        if (!node.isObject()) {
            throw new ApiException(502, "The AI returned an unexpected resume analysis. Please try again.");
        }

        String suggestedRole = NodeReader.text(node, "suggestedRole");
        return new ResumeAnalysis(
                NodeReader.text(node, "name"),
                NodeReader.text(node, "email"),
                NodeReader.text(node, "phone"),
                NodeReader.text(node, "summary"),
                NodeReader.strings(node, "skills"),
                NodeReader.objects(node, "projects"),
                NodeReader.objects(node, "education"),
                NodeReader.objects(node, "experience"),
                NodeReader.strings(node, "strengths"),
                NodeReader.strings(node, "weaknesses"),
                NodeReader.strings(node, "missingSkills"),
                suggestedRole.isBlank() ? "Not specified" : suggestedRole,
                NodeReader.intIn(node, "score", 0, 100, 0),
                NodeReader.strings(node, "recommendations"));
    }
}
