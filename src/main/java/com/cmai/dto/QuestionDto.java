package com.cmai.dto;

import com.cmai.entity.InterviewQuestion;

public record QuestionDto(
        String question, String userAnswer, String difficulty, int timer, FeedbackDto feedback) {

    public static QuestionDto from(InterviewQuestion q) {
        return new QuestionDto(
                q.getQuestion(),
                q.getUserAnswer() == null ? "" : q.getUserAnswer(),
                q.getDifficulty(),
                q.getTimer(),
                FeedbackDto.from(q));
    }
}
