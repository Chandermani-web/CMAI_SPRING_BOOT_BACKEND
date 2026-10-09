package com.cmai.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

/** Defensive readers for LLM JSON: never trust shape or types, always return something usable. */
public final class NodeReader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private NodeReader() {}

    public static String text(JsonNode node, String field) {
        if (node == null) {
            return "";
        }
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) {
            return "";
        }
        return v.isValueNode() ? v.asText("").trim() : v.toString();
    }

    public static List<String> strings(JsonNode node, String field) {
        List<String> out = new ArrayList<>();
        JsonNode arr = node == null ? null : node.get(field);
        if (arr == null || !arr.isArray()) {
            return out;
        }
        for (JsonNode el : arr) {
            String s = el.isValueNode() ? el.asText("").trim() : el.toString();
            if (!s.isBlank()) {
                out.add(s);
            }
        }
        return out;
    }

    /** Array of free-form values (strings, objects...) kept as plain Java Maps/Lists/Strings. */
    public static List<Object> objects(JsonNode node, String field) {
        List<Object> out = new ArrayList<>();
        JsonNode arr = node == null ? null : node.get(field);
        if (arr == null || !arr.isArray()) {
            return out;
        }
        for (JsonNode el : arr) {
            out.add(MAPPER.convertValue(el, Object.class));
        }
        return out;
    }

    /** Reads a number (or numeric string), rounds it and clamps it into [min, max]. */
    public static int intIn(JsonNode node, String field, int min, int max, int fallback) {
        JsonNode v = node == null ? null : node.get(field);
        if (v == null || v.isNull()) {
            return fallback;
        }
        double d;
        if (v.isNumber()) {
            d = v.asDouble();
        } else {
            try {
                d = Double.parseDouble(v.asText("").trim());
            } catch (NumberFormatException e) {
                return fallback;
            }
        }
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            return fallback;
        }
        return (int) Math.max(min, Math.min(max, Math.round(d)));
    }

    public static String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String v = value.trim();
        return v.substring(0, 1).toUpperCase() + v.substring(1).toLowerCase();
    }
}
