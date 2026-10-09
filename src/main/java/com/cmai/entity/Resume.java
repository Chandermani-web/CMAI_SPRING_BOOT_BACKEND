package com.cmai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** One analysed resume per user. Free-form sections (education, projects...) are stored as JSON. */
@Entity
@Table(name = "resumes")
public class Resume extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "extract_text", columnDefinition = "LONGTEXT")
    private String extractText;

    @Column(nullable = false)
    private int score;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private String name;
    private String email;
    private String phone;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Object> education = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Object> experience = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Object> projects = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> skills = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> strengths = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> weaknesses = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "missing_skills")
    private List<String> missingSkills = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> recommendations = new ArrayList<>();

    @Column(name = "suggested_role")
    private String suggestedRole = "Not specified";

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getExtractText() { return extractText; }
    public void setExtractText(String extractText) { this.extractText = extractText; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public List<Object> getEducation() { return education; }
    public void setEducation(List<Object> education) { this.education = education; }
    public List<Object> getExperience() { return experience; }
    public void setExperience(List<Object> experience) { this.experience = experience; }
    public List<Object> getProjects() { return projects; }
    public void setProjects(List<Object> projects) { this.projects = projects; }
    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }
    public List<String> getStrengths() { return strengths; }
    public void setStrengths(List<String> strengths) { this.strengths = strengths; }
    public List<String> getWeaknesses() { return weaknesses; }
    public void setWeaknesses(List<String> weaknesses) { this.weaknesses = weaknesses; }
    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }
    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    public String getSuggestedRole() { return suggestedRole; }
    public void setSuggestedRole(String suggestedRole) { this.suggestedRole = suggestedRole; }
}
