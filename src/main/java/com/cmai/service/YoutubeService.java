package com.cmai.service;

import com.cmai.config.AppProperties;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/** Finds a full course for a topic: a complete playlist first, otherwise a long-form video. */
@Service
public class YoutubeService {

    private static final Logger log = LoggerFactory.getLogger(YoutubeService.class);

    private final RestClient http = RestClient.create();
    private final String baseUrl;
    private final String apiKey;

    public YoutubeService(AppProperties props) {
        this.baseUrl = props.youtube().baseUrl();
        this.apiKey = props.youtube().apiKey();
    }

    /** @return a YouTube URL, or an empty string when nothing was found / the API is unavailable. */
    public String findLearningResource(String query) {
        if (apiKey == null || apiKey.isBlank() || query == null || query.isBlank()) {
            return "";
        }
        try {
            String playlist = searchPlaylist(query);
            if (playlist != null) {
                return playlist;
            }
            String video = searchLongVideo(query);
            return video == null ? "" : video;
        } catch (RuntimeException e) {
            // Deliberately not logging the exception message: it can contain the request URL with the API key.
            log.warn("YouTube search failed for '{}' ({})", query, e.getClass().getSimpleName());
            return "";
        }
    }

    private String searchPlaylist(String query) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("part", "snippet");
        params.put("q", query + " complete tutorial");
        params.put("type", "playlist");
        params.put("order", "relevance");
        params.put("maxResults", "5");

        JsonNode first = firstItem(search(params));
        String id = first == null ? "" : first.path("id").path("playlistId").asText("");
        return id.isBlank() ? null : "https://www.youtube.com/playlist?list=" + id;
    }

    private String searchLongVideo(String query) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("part", "snippet");
        params.put("q", query + " full course");
        params.put("type", "video");
        params.put("videoDuration", "long");
        params.put("order", "viewCount");
        params.put("maxResults", "5");

        JsonNode first = firstItem(search(params));
        String id = first == null ? "" : first.path("id").path("videoId").asText("");
        return id.isBlank() ? null : "https://www.youtube.com/watch?v=" + id;
    }

    private JsonNode search(Map<String, String> params) {
        StringBuilder url = new StringBuilder(baseUrl).append("?key={key}");
        Map<String, Object> vars = new HashMap<>();
        vars.put("key", apiKey);
        for (Map.Entry<String, String> e : params.entrySet()) {
            url.append('&').append(e.getKey()).append("={").append(e.getKey()).append('}');
            vars.put(e.getKey(), e.getValue());
        }
        return http.get().uri(url.toString(), vars).retrieve().body(JsonNode.class);
    }

    private static JsonNode firstItem(JsonNode body) {
        if (body == null) {
            return null;
        }
        JsonNode items = body.get("items");
        return items != null && items.isArray() && !items.isEmpty() ? items.get(0) : null;
    }
}
