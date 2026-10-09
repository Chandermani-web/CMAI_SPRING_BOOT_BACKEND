package com.cmai.dto;

import com.cmai.entity.InterviewQuestion;
import java.util.ArrayList;
import java.util.List;

public record FeedbackDto(
        int score,
        int correctness,
        int clarity,
        int relevance,
        int detail,
        int efficiency,
        int problemSolving,
        int communication,
        int creativity,
        String feedback,
        List<String> improvement) {

    public static FeedbackDto empty() {
        return new FeedbackDto(0, 0, 0, 0, 0, 0, 0, 0, 0, "", List.of());
    }

    public static FeedbackDto from(InterviewQuestion q) {
        return new FeedbackDto(
                q.getScore(),
                q.getCorrectness(),
                q.getClarity(),
                q.getRelevance(),
                q.getDetail(),
                q.getEfficiency(),
                q.getProblemSolving(),
                q.getCommunication(),
                q.getCreativity(),
                q.getFeedbackText() == null ? "" : q.getFeedbackText(),
                q.getImprovement() == null ? List.of() : new ArrayList<>(q.getImprovement()));
    }

    /** Copies this feedback onto the entity. */
    public void applyTo(InterviewQuestion q) {
        q.setScore(score);
        q.setCorrectness(correctness);
        q.setClarity(clarity);
        q.setRelevance(relevance);
        q.setDetail(detail);
        q.setEfficiency(efficiency);
        q.setProblemSolving(problemSolving);
        q.setCommunication(communication);
        q.setCreativity(creativity);
        q.setFeedbackText(feedback);
        q.setImprovement(new ArrayList<>(improvement));
    }
}
