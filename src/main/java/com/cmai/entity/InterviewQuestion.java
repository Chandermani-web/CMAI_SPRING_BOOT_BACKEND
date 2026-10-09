package com.cmai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** One question of an interview, together with the candidate's answer and the AI feedback. */
@Entity
@Table(name = "interview_questions")
public class InterviewQuestion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer = "";

    @Column(nullable = false, length = 10)
    private String difficulty = "easy";

    /** Seconds the candidate has to answer. */
    @Column(nullable = false)
    private int timer = 60;

    // ---- feedback (all zero until the question is answered) ----
    @Column(name = "fb_score", nullable = false) private int score;
    @Column(name = "fb_correctness", nullable = false) private int correctness;
    @Column(name = "fb_clarity", nullable = false) private int clarity;
    @Column(name = "fb_relevance", nullable = false) private int relevance;
    @Column(name = "fb_detail", nullable = false) private int detail;
    @Column(name = "fb_efficiency", nullable = false) private int efficiency;
    @Column(name = "fb_problem_solving", nullable = false) private int problemSolving;
    @Column(name = "fb_communication", nullable = false) private int communication;
    @Column(name = "fb_creativity", nullable = false) private int creativity;

    @Column(name = "fb_text", columnDefinition = "TEXT")
    private String feedbackText = "";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "fb_improvement")
    private List<String> improvement = new ArrayList<>();

    public Interview getInterview() { return interview; }
    public void setInterview(Interview interview) { this.interview = interview; }
    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getUserAnswer() { return userAnswer; }
    public void setUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public int getTimer() { return timer; }
    public void setTimer(int timer) { this.timer = timer; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getCorrectness() { return correctness; }
    public void setCorrectness(int correctness) { this.correctness = correctness; }
    public int getClarity() { return clarity; }
    public void setClarity(int clarity) { this.clarity = clarity; }
    public int getRelevance() { return relevance; }
    public void setRelevance(int relevance) { this.relevance = relevance; }
    public int getDetail() { return detail; }
    public void setDetail(int detail) { this.detail = detail; }
    public int getEfficiency() { return efficiency; }
    public void setEfficiency(int efficiency) { this.efficiency = efficiency; }
    public int getProblemSolving() { return problemSolving; }
    public void setProblemSolving(int problemSolving) { this.problemSolving = problemSolving; }
    public int getCommunication() { return communication; }
    public void setCommunication(int communication) { this.communication = communication; }
    public int getCreativity() { return creativity; }
    public void setCreativity(int creativity) { this.creativity = creativity; }
    public String getFeedbackText() { return feedbackText; }
    public void setFeedbackText(String feedbackText) { this.feedbackText = feedbackText; }
    public List<String> getImprovement() { return improvement; }
    public void setImprovement(List<String> improvement) { this.improvement = improvement; }
}
