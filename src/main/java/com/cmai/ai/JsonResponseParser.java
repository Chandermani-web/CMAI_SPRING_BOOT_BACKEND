package com.cmai.ai;

import com.cmai.exception.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Extracts JSON from an LLM reply even when it is wrapped in code fences or surrounded by prose. */
@Component
public class JsonResponseParser {

    private static final Logger log = LoggerFactory.getLogger(JsonResponseParser.class);

    private final ObjectMapper mapper;

    public JsonResponseParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public JsonNode parse(String content) {
        if (content == null || content.isBlank()) {
            throw new ApiException(502, "The AI returned an empty response. Please try again.");
        }

        String cleaned = content.trim()
                .replaceFirst("(?is)^```json\\s*", "")
                .replaceFirst("(?s)^```\\s*", "")
                .replaceFirst("(?s)\\s*```$", "")
                .trim();

        int firstObject = cleaned.indexOf('{');
        int firstArray = cleaned.indexOf('[');
        int start;
        if (firstObject == -1) {
            start = firstArray;
        } else if (firstArray == -1) {
            start = firstObject;
        } else {
            start = Math.min(firstObject, firstArray);
        }
        if (start > 0) {
            cleaned = cleaned.substring(start);
        }

        int end = Math.max(cleaned.lastIndexOf('}'), cleaned.lastIndexOf(']'));
        if (end != -1 && end < cleaned.length() - 1) {
            cleaned = cleaned.substring(0, end + 1);
        }

        try {
            return mapper.readTree(cleaned);
        } catch (JsonProcessingException e) {
            log.warn("AI returned invalid JSON ({} chars): {}", cleaned.length(), e.getOriginalMessage());
            throw new ApiException(502, "The AI returned an unreadable response. Please try again.");
        }
    }
}
