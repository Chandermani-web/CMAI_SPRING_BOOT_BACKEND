package com.cmai.ai.roadmap;

import com.cmai.ai.JsonResponseParser;
import com.cmai.ai.PromptTemplates;
import com.cmai.ai.SpringAiChat;
import com.cmai.exception.ApiException;
import com.cmai.util.NodeReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Spring AI agent: plans the learning modules for a target role and salary. */
@Component
public class RoadmapAgent {

    private static final int MAX_MODULES = 15;

    private final SpringAiChat chat;
    private final PromptTemplates templates;
    private final JsonResponseParser parser;
    private final ObjectMapper mapper;

    public RoadmapAgent(
            SpringAiChat chat, PromptTemplates templates, JsonResponseParser parser, ObjectMapper mapper) {
        this.chat = chat;
        this.templates = templates;
        this.parser = parser;
        this.mapper = mapper;
    }

    public Map<String, Object> generate(
            String role, String targetPackage, boolean useResume, Map<String, Object> resumeSnapshot) {

        String resumeJson;
        try {
            resumeJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resumeSnapshot);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialise resume snapshot", e);
        }

        String user = "Target Role:\n" + role
                + "\n\nTarget Package:\n" + targetPackage
                + "\n\nUse Resume:\n" + useResume
                + "\n\nResume:\n" + resumeJson;

        JsonNode node = parser.parse(chat.complete(templates.raw("roadmap-system"), user));
        if (!node.isObject()) {
            throw new ApiException(502, "The AI returned an invalid roadmap. Please try again.");
        }

        List<Map<String, Object>> modules = new ArrayList<>();
        JsonNode rawModules = node.get("modules");
        if (rawModules != null && rawModules.isArray()) {
            for (JsonNode m : rawModules) {
                if (modules.size() >= MAX_MODULES) {
                    break;
                }
                String title = NodeReader.text(m, "title");
                if (title.isBlank()) {
                    continue;
                }
                Map<String, Object> module = new LinkedHashMap<>();
                module.put("title", title);
                module.put("description", NodeReader.text(m, "description"));
                module.put("difficulty", NodeReader.capitalize(NodeReader.text(m, "difficulty")));
                module.put("topics", NodeReader.strings(m, "topics"));
                module.put("duration", NodeReader.text(m, "duration"));
                modules.add(module);
            }
        }
        if (modules.isEmpty()) {
            throw new ApiException(502, "The AI did not return any learning modules. Please try again.");
        }

        String title = NodeReader.text(node, "title");
        String pkg = NodeReader.text(node, "targetPackage");

        Map<String, Object> roadmap = new LinkedHashMap<>();
        roadmap.put("title", title.isBlank() ? role + " roadmap" : title);
        roadmap.put("targetPackage", pkg.isBlank() ? targetPackage : pkg);
        roadmap.put("duration", NodeReader.text(node, "duration"));
        roadmap.put("level", NodeReader.capitalize(NodeReader.text(node, "level")));
        roadmap.put("modules", modules);
        return roadmap;
    }
}
