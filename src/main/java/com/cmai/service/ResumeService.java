package com.cmai.service;

import com.cmai.ai.resume.ResumeAgent;
import com.cmai.ai.resume.ResumeAnalysis;
import com.cmai.dto.ResumeDto;
import com.cmai.dto.Sourced;
import com.cmai.entity.Resume;
import com.cmai.exception.ApiException;
import com.cmai.repository.ResumeRepository;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResumeService {

    private static final Duration CACHE_TTL = Duration.ofHours(24);

    private final ResumeRepository resumes;
    private final DocumentTextExtractor extractor;
    private final ResumeAgent agent;
    private final CacheService cache;
    private final TransactionTemplate tx;

    public ResumeService(
            ResumeRepository resumes,
            DocumentTextExtractor extractor,
            ResumeAgent agent,
            CacheService cache,
            TransactionTemplate tx) {
        this.resumes = resumes;
        this.extractor = extractor;
        this.agent = agent;
        this.cache = cache;
        this.tx = tx;
    }

    /** Extract text, analyse with the LLM (no DB connection held meanwhile), then upsert the user's resume. */
    public ResumeDto upload(Long userId, MultipartFile file) {
        String text = extractor.extract(file);
        ResumeAnalysis analysis = agent.analyze(text);

        ResumeDto dto = Objects.requireNonNull(tx.execute(status -> {
            Resume resume = resumes.findByUserId(userId).orElseGet(Resume::new);
            resume.setUserId(userId);
            resume.setExtractText(text);
            resume.setName(analysis.name());
            resume.setEmail(analysis.email());
            resume.setPhone(analysis.phone());
            resume.setSummary(analysis.summary());
            resume.setScore(analysis.score());
            resume.setSkills(new ArrayList<>(analysis.skills()));
            resume.setProjects(new ArrayList<>(analysis.projects()));
            resume.setEducation(new ArrayList<>(analysis.education()));
            resume.setExperience(new ArrayList<>(analysis.experience()));
            resume.setStrengths(new ArrayList<>(analysis.strengths()));
            resume.setWeaknesses(new ArrayList<>(analysis.weaknesses()));
            resume.setMissingSkills(new ArrayList<>(analysis.missingSkills()));
            resume.setRecommendations(new ArrayList<>(analysis.recommendations()));
            resume.setSuggestedRole(analysis.suggestedRole());
            return ResumeDto.from(resumes.save(resume));
        }));

        cache.put(cacheKey(userId), dto, CACHE_TTL);
        return dto;
    }

    public Sourced<ResumeDto> get(Long userId) {
        Optional<ResumeDto> cached = cache.get(cacheKey(userId), ResumeDto.class);
        if (cached.isPresent()) {
            return new Sourced<>(cached.get(), "redis");
        }

        ResumeDto dto = resumes.findByUserId(userId)
                .map(ResumeDto::from)
                .orElseThrow(() -> new ApiException(404, "Resume not found"));
        cache.put(cacheKey(userId), dto, CACHE_TTL);
        return new Sourced<>(dto, "database");
    }

    private static String cacheKey(Long userId) {
        return "resume:" + userId;
    }
}
