package com.cmai.controller;

import com.cmai.dto.ApiResponse;
import com.cmai.dto.GenerateRoadmapRequest;
import com.cmai.dto.RoadmapDto;
import com.cmai.dto.SessionUser;
import com.cmai.dto.Sourced;
import com.cmai.service.RoadmapService;
import com.cmai.web.AuthInterceptor;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roadmap")
public class RoadmapController {

    private final RoadmapService roadmapService;

    public RoadmapController(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoadmapDto>> generate(
            @RequestAttribute(AuthInterceptor.ATTR_USER) SessionUser user,
            @Valid @RequestBody GenerateRoadmapRequest request) {
        RoadmapDto dto = roadmapService.generate(user.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Roadmap generated successfully.", dto));
    }

    @GetMapping("/all")
    public ApiResponse<List<RoadmapDto>> all(@RequestAttribute(AuthInterceptor.ATTR_USER) SessionUser user) {
        Sourced<List<RoadmapDto>> result = roadmapService.list(user.userId());
        return ApiResponse.ok("Roadmaps retrieved successfully.", result.value(), result.source());
    }

    @GetMapping("/{id}")
    public ApiResponse<RoadmapDto> one(
            @RequestAttribute(AuthInterceptor.ATTR_USER) SessionUser user, @PathVariable Long id) {
        Sourced<RoadmapDto> result = roadmapService.get(user.userId(), id);
        return ApiResponse.ok("Roadmap retrieved successfully.", result.value(), result.source());
    }
}
