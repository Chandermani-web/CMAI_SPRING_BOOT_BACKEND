package com.cmai.dto;

public record SubmitAnswerResponse(
        boolean success,
        boolean completed,
        int currentQuestion,
        int totalQuestions,
        QuestionDto question,
        FeedbackDto feedback) {}
