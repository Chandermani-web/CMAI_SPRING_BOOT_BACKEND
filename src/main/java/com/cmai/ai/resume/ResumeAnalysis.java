package com.cmai.ai.resume;

import java.util.List;

public record ResumeAnalysis(
        String name,
        String email,
        String phone,
        String summary,
        List<String> skills,
        List<Object> projects,
        List<Object> education,
        List<Object> experience,
        List<String> strengths,
        List<String> weaknesses,
        List<String> missingSkills,
        String suggestedRole,
        int score,
        List<String> recommendations) {}
