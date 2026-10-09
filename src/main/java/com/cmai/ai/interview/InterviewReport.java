package com.cmai.ai.interview;

import java.util.List;

public record InterviewReport(
        int overallScore,
        String summary,
        List<String> strengths,
        List<String> weaknesses,
        List<String> recommendations) {}
