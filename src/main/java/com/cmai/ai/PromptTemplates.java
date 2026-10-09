package com.cmai.ai;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

/**
 * Loads src/main/resources/prompts/*.txt and fills {{placeholders}} in a single pass,
 * so text inserted for one placeholder is never re-interpreted as another.
 */
@Component
public class PromptTemplates {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{(\\w+)}}");

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public String render(String name, Map<String, String> variables) {
        String template = cache.computeIfAbsent(name, this::load);
        Matcher m = PLACEHOLDER.matcher(template);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            String value = variables.getOrDefault(m.group(1), "");
            m.appendReplacement(out, Matcher.quoteReplacement(value));
        }
        m.appendTail(out);
        return out.toString();
    }

    public String raw(String name) {
        return cache.computeIfAbsent(name, this::load);
    }

    private String load(String name) {
        try (InputStream in = new ClassPathResource("prompts/" + name + ".txt").getInputStream()) {
            return StreamUtils.copyToString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Missing prompt template: " + name, e);
        }
    }
}
