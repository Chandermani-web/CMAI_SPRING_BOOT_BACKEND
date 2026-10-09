package com.cmai.ai.roadmap;

import com.cmai.ai.JsonResponseParser;
import com.cmai.ai.PromptTemplates;
import com.cmai.ai.SpringAiChat;
import com.cmai.service.YoutubeService;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Attaches a documentation/article link (LLM) and a YouTube course (YouTube API) to each module. */
@Component
public class ResourceAgent {

    private static final Logger log = LoggerFactory.getLogger(ResourceAgent.class);

    private final SpringAiChat chat;
    private final PromptTemplates templates;
    private final JsonResponseParser parser;
    private final YoutubeService youtube;

    public ResourceAgent(
            SpringAiChat chat, PromptTemplates templates, JsonResponseParser parser, YoutubeService youtube) {
        this.chat = chat;
        this.templates = templates;
        this.parser = parser;
        this.youtube = youtube;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> attach(List<Map<String, Object>> modules) {
        Map<String, String> articles = fetchArticles(modules);

        List<String> titles = modules.stream().map(m -> String.valueOf(m.get("title"))).toList();
        List<String> videoUrls = new ArrayList<>();

        // One virtual thread per module, same idea as Promise.all in the original.
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<String>> futures = new ArrayList<>();
            for (String title : titles) {
                futures.add(executor.submit(() -> youtube.findLearningResource(title)));
            }
            for (Future<String> future : futures) {
                String url = "";
                try {
                    url = future.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    log.warn("YouTube lookup failed: {}", e.toString());
                }
                videoUrls.add(url == null ? "" : url);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < modules.size(); i++) {
            Map<String, Object> module = new LinkedHashMap<>(modules.get(i));
            module.put("youtube", videoUrls.get(i));
            module.put("article", articles.getOrDefault(key(titles.get(i)), ""));
            result.add(module);
        }
        return result;
    }

    private Map<String, String> fetchArticles(List<Map<String, Object>> modules) {
        Map<String, String> byTitle = new HashMap<>();
        String titles = String.join(
                "\n", modules.stream().map(m -> String.valueOf(m.get("title"))).toList());
        if (titles.isBlank()) {
            return byTitle;
        }
        try {
            String reply = chat.complete(templates.raw("roadmap-docs"), "Modules:\n\n" + titles);
            JsonNode node = parser.parse(reply);
            if (node.isArray()) {
                for (JsonNode item : node) {
                    String title = item.path("title").asText("");
                    String article = item.path("article").asText("").trim();
                    // Only plain web links: the value ends up in an <a href> on the frontend.
                    if (!title.isBlank() && article.matches("(?i)^https?://\\S+$")) {
                        byTitle.put(key(title), article);
                    }
                }
            }
        } catch (RuntimeException e) {
            log.warn("Could not fetch article links, continuing without them: {}", e.toString());
        }
        return byTitle;
    }

    private static String key(String title) {
        return title == null ? "" : title.toLowerCase(Locale.ROOT).trim();
    }
}
