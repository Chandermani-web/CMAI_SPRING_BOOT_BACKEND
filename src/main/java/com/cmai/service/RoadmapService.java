package com.cmai.service;

import com.cmai.ai.ResumeContext;
import com.cmai.ai.roadmap.ResourceAgent;
import com.cmai.ai.roadmap.RoadmapAgent;
import com.cmai.dto.GenerateRoadmapRequest;
import com.cmai.dto.RoadmapDto;
import com.cmai.dto.Sourced;
import com.cmai.entity.Resume;
import com.cmai.entity.Roadmap;
import com.cmai.exception.ApiException;
import com.cmai.repository.ResumeRepository;
import com.cmai.repository.RoadmapRepository;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class RoadmapService {

    private static final Duration CACHE_TTL = Duration.ofHours(1);

    private final RoadmapRepository roadmaps;
    private final ResumeRepository resumes;
    private final RoadmapAgent roadmapAgent;
    private final ResourceAgent resourceAgent;
    private final CacheService cache;
    private final TransactionTemplate tx;

    public RoadmapService(
            RoadmapRepository roadmaps,
            ResumeRepository resumes,
            RoadmapAgent roadmapAgent,
            ResourceAgent resourceAgent,
            CacheService cache,
            TransactionTemplate tx) {
        this.roadmaps = roadmaps;
        this.resumes = resumes;
        this.roadmapAgent = roadmapAgent;
        this.resourceAgent = resourceAgent;
        this.cache = cache;
        this.tx = tx;
    }

    /** Two-step pipeline, like the old graph: plan the modules, then attach learning resources. */
    @SuppressWarnings("unchecked")
    public RoadmapDto generate(Long userId, GenerateRoadmapRequest req) {
        String role = req.role().trim();
        String targetPackage = req.targetPackage().trim();
        boolean useResume = Boolean.TRUE.equals(req.useResume());

        Map<String, Object> resumeSnapshot = null;
        if (useResume) {
            Resume resume = resumes.findByUserId(userId)
                    .orElseThrow(() -> new ApiException(
                            400, "Upload your resume first to personalise the roadmap"));
            resumeSnapshot = ResumeContext.snapshot(resume);
        }

        Map<String, Object> plan = roadmapAgent.generate(role, targetPackage, useResume, resumeSnapshot);
        List<Map<String, Object>> modules = (List<Map<String, Object>>) plan.get("modules");

        Map<String, Object> finalRoadmap = new LinkedHashMap<>(plan);
        finalRoadmap.put("modules", resourceAgent.attach(modules));

        Map<String, Object> snapshot = resumeSnapshot;
        RoadmapDto dto = Objects.requireNonNull(tx.execute(status -> {
            Roadmap entity = new Roadmap();
            entity.setUserId(userId);
            entity.setTargetRole(role);
            entity.setTargetPackage(targetPackage);
            entity.setUseResume(useResume);
            entity.setResume(snapshot);
            entity.setRoadmapData(finalRoadmap);
            return RoadmapDto.from(roadmaps.save(entity));
        }));

        cache.evict(listKey(userId));
        cache.put(itemKey(dto.id(), userId), dto, CACHE_TTL);
        return dto;
    }

    public Sourced<List<RoadmapDto>> list(Long userId) {
        Optional<List<RoadmapDto>> cached = cache.getList(listKey(userId), RoadmapDto.class);
        if (cached.isPresent()) {
            return new Sourced<>(cached.get(), "redis");
        }
        List<RoadmapDto> all = roadmaps.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(RoadmapDto::from)
                .toList();
        cache.put(listKey(userId), all, CACHE_TTL);
        return new Sourced<>(all, "database");
    }

    public Sourced<RoadmapDto> get(Long userId, Long id) {
        Optional<RoadmapDto> cached = cache.get(itemKey(id, userId), RoadmapDto.class);
        if (cached.isPresent()) {
            return new Sourced<>(cached.get(), "redis");
        }
        RoadmapDto dto = roadmaps.findByIdAndUserId(id, userId)
                .map(RoadmapDto::from)
                .orElseThrow(() -> new ApiException(404, "Roadmap not found."));
        cache.put(itemKey(id, userId), dto, CACHE_TTL);
        return new Sourced<>(dto, "database");
    }

    private static String listKey(Long userId) {
        return "roadmaps:" + userId;
    }

    private static String itemKey(Long id, Long userId) {
        return "roadmap:" + id + ":" + userId;
    }
}
