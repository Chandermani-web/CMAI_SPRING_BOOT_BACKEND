package com.cmai.dto;

public record StartInterviewResponse(
        boolean success,
        Long interviewId,
        int currentQuestion,
        int totalQuestions,
        QuestionDto question,
        int interviewCoin) {}
