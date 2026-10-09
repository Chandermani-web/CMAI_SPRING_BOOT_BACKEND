package com.cmai.dto;

import com.cmai.entity.Roadmap;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RoadmapDto(
        Long id,
        Long userId,
        String role,
        String targetPackage,
        boolean useResume,
        Map<String, Object> resume,
        Map<String, Object> roadmap,
        Instant createdAt,
        Instant updatedAt) {


    /** Compatibility with the Node API, whose documents used Mongo's "_id". */
    @JsonProperty("_id")
    public Long legacyId() {
        return id;
    }
    public static RoadmapDto from(Roadmap r) {
        return new RoadmapDto(
                r.getId(),
                r.getUserId(),
                r.getTargetRole(),
                r.getTargetPackage(),
                r.isUseResume(),
                r.getResume(),
                r.getRoadmapData(),
                r.getCreatedAt(),
                r.getUpdatedAt());
    }
}
