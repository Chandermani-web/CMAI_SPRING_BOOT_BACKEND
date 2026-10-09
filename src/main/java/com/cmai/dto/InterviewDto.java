package com.cmai.dto;

import com.cmai.entity.Interview;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InterviewDto(
        Long id,
        Long userId,
        String type,
        String role,
        boolean useResume,
        int currentQuestion,
        int totalQuestions,
        List<QuestionDto> questions,
        int overallScore,
        List<String> strengths,
        List<String> weaknesses,
        List<String> recommendations,
        String summary,
        String status,
        Instant createdAt,
        Instant updatedAt) {


    /** Compatibility with the Node API, whose documents used Mongo's "_id". */
    @JsonProperty("_id")
    public Long legacyId() {
        return id;
    }
    /** Must be called inside a transaction (questions are lazy). */
    public static InterviewDto from(Interview i) {
        return new InterviewDto(
                i.getId(),
                i.getUserId(),
                i.getType(),
                i.getTargetRole(),
                i.isUseResume(),
                i.getCurrentQuestion(),
                i.getTotalQuestions(),
                i.getQuestions().stream().map(QuestionDto::from).toList(),
                i.getOverallScore(),
                new ArrayList<>(i.getStrengths()),
                new ArrayList<>(i.getWeaknesses()),
                new ArrayList<>(i.getRecommendations()),
                i.getSummary() == null ? "" : i.getSummary(),
                i.getStatus(),
                i.getCreatedAt(),
                i.getUpdatedAt());
    }
}
