package com.cmai.ai;

import com.cmai.entity.Resume;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Turns a stored resume into the text/JSON snippets the prompts need. */
public final class ResumeContext {

    private ResumeContext() {}

    /** Block inserted into the interview prompts. */
    public static String interviewBlock(Resume r) {
        return "Resume\n\n"
                + "Summary:\n" + nz(r.getSummary()) + "\n\n"
                + "Skills:\n" + join(r.getSkills()) + "\n\n"
                + "Projects:\n" + describe(r.getProjects()) + "\n\n"
                + "Experience:\n" + describe(r.getExperience()) + "\n\n"
                + "Strengths:\n" + join(r.getStrengths()) + "\n\n"
                + "Weaknesses:\n" + join(r.getWeaknesses()) + "\n\n"
                + "Missing Skills:\n" + join(r.getMissingSkills()) + "\n\n"
                + "Recommendations:\n" + join(r.getRecommendations()) + "\n";
    }

    /** Compact snapshot given to the roadmap generator and stored with the roadmap. */
    public static Map<String, Object> snapshot(Resume r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("skills", r.getSkills());
        m.put("experience", r.getExperience());
        m.put("projects", r.getProjects());
        m.put("missingSkills", r.getMissingSkills());
        m.put("score", r.getScore());
        m.put("suggestedRole", nz(r.getSuggestedRole()));
        m.put("recommendations", r.getRecommendations());
        return m;
    }

    private static String describe(List<Object> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        return items.stream().map(ResumeContext::oneLine).collect(Collectors.joining("; "));
    }

    private static String oneLine(Object item) {
        if (item instanceof Map<?, ?> map) {
            for (String key : new String[] {"name", "title", "projectName", "project", "role", "company"}) {
                Object v = map.get(key);
                if (v != null && !String.valueOf(v).isBlank()) {
                    return String.valueOf(v);
                }
            }
            return map.values().stream().map(String::valueOf).collect(Collectors.joining(" - "));
        }
        return String.valueOf(item);
    }

    private static String join(List<String> items) {
        return items == null ? "" : String.join(", ", items);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
