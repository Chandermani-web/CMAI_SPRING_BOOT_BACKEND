package com.cmai.controller;

import com.cmai.dto.InterviewDto;
import com.cmai.dto.SessionUser;
import com.cmai.dto.StartInterviewRequest;
import com.cmai.dto.StartInterviewResponse;
import com.cmai.dto.SubmitAnswerRequest;
import com.cmai.dto.SubmitAnswerResponse;
import com.cmai.service.InterviewService;
import com.cmai.web.AuthInterceptor;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interview")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping("/start")
    public StartInterviewResponse start(
            @RequestAttribute(AuthInterceptor.ATTR_USER) SessionUser user,
            @Valid @RequestBody StartInterviewRequest request) {
        return interviewService.start(user.userId(), request);
    }

    @PostMapping("/answer")
    public SubmitAnswerResponse answer(
            @RequestAttribute(AuthInterceptor.ATTR_USER) SessionUser user,
            @Valid @RequestBody SubmitAnswerRequest request) {
        return interviewService.submitAnswer(user.userId(), request);
    }

    @GetMapping("/all-interview")
    public Map<String, Object> all(@RequestAttribute(AuthInterceptor.ATTR_USER) SessionUser user) {
        List<InterviewDto> interviews = interviewService.list(user.userId());
        return Map.of("success", true, "interviews", interviews);
    }

    @GetMapping("/{id}")
    public Map<String, Object> one(
            @RequestAttribute(AuthInterceptor.ATTR_USER) SessionUser user, @PathVariable Long id) {
        InterviewDto interview = interviewService.get(user.userId(), id);
        return Map.of("success", true, "interview", interview);
    }
}
