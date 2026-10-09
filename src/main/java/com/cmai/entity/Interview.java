package com.cmai.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "interviews", indexes = @Index(name = "idx_interviews_user", columnList = "user_id"))
public class Interview extends BaseEntity {

    public static final String STATUS_IN_PROGRESS = "in-progress";
    public static final String STATUS_COMPLETED = "completed";

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** "technical" or "hr". */
    @Column(nullable = false, length = 20)
    private String type;

    @Column(name = "target_role", nullable = false)
    private String targetRole;

    @Column(name = "use_resume", nullable = false)
    private boolean useResume;

    @Column(name = "current_question", nullable = false)
    private int currentQuestion;

    @Column(name = "total_questions", nullable = false)
    private int totalQuestions;

    @Column(name = "overall_score", nullable = false)
    private int overallScore;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> strengths = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> weaknesses = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> recommendations = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String summary = "";

    @Column(nullable = false, length = 20)
    private String status = STATUS_IN_PROGRESS;

    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<InterviewQuestion> questions = new ArrayList<>();

    public void addQuestion(InterviewQuestion question) {
        question.setInterview(this);
        question.setOrderIndex(questions.size());
        questions.add(question);
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }
    public boolean isUseResume() { return useResume; }
    public void setUseResume(boolean useResume) { this.useResume = useResume; }
    public int getCurrentQuestion() { return currentQuestion; }
    public void setCurrentQuestion(int currentQuestion) { this.currentQuestion = currentQuestion; }
    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    public int getOverallScore() { return overallScore; }
    public void setOverallScore(int overallScore) { this.overallScore = overallScore; }
    public List<String> getStrengths() { return strengths; }
    public void setStrengths(List<String> strengths) { this.strengths = strengths; }
    public List<String> getWeaknesses() { return weaknesses; }
    public void setWeaknesses(List<String> weaknesses) { this.weaknesses = weaknesses; }
    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<InterviewQuestion> getQuestions() { return questions; }
}
