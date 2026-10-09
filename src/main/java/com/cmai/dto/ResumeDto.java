package com.cmai.dto;

import com.cmai.entity.Resume;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ResumeDto(
        Long id,
        Long userId,
        String name,
        String email,
        String phone,
        String summary,
        int score,
        List<String> skills,
        List<Object> projects,
        List<Object> education,
        List<Object> experience,
        List<String> strengths,
        List<String> weaknesses,
        List<String> missingSkills,
        List<String> recommendations,
        String suggestedRole,
        Instant createdAt,
        Instant updatedAt) {


    /** Compatibility with the Node API, whose documents used Mongo's "_id". */
    @JsonProperty("_id")
    public Long legacyId() {
        return id;
    }
    public static ResumeDto from(Resume r) {
        return new ResumeDto(
                r.getId(),
                r.getUserId(),
                r.getName(),
                r.getEmail(),
                r.getPhone(),
                r.getSummary(),
                r.getScore(),
                new ArrayList<>(r.getSkills()),
                new ArrayList<>(r.getProjects()),
                new ArrayList<>(r.getEducation()),
                new ArrayList<>(r.getExperience()),
                new ArrayList<>(r.getStrengths()),
                new ArrayList<>(r.getWeaknesses()),
                new ArrayList<>(r.getMissingSkills()),
                new ArrayList<>(r.getRecommendations()),
                r.getSuggestedRole(),
                r.getCreatedAt(),
                r.getUpdatedAt());
    }
}
