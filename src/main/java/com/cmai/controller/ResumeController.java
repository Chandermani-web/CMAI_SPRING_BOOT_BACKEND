package com.cmai.controller;

import com.cmai.dto.ApiResponse;
import com.cmai.dto.ResumeDto;
import com.cmai.dto.SessionUser;
import com.cmai.dto.Sourced;
import com.cmai.service.ResumeService;
import com.cmai.web.AuthInterceptor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ResumeDto> upload(
            @RequestAttribute(AuthInterceptor.ATTR_USER) SessionUser user,
            @RequestParam("resume") MultipartFile file) {
        return ApiResponse.ok("Resume analyzed successfully", resumeService.upload(user.userId(), file));
    }

    @GetMapping("/get-resume")
    public ApiResponse<ResumeDto> getResume(@RequestAttribute(AuthInterceptor.ATTR_USER) SessionUser user) {
        Sourced<ResumeDto> result = resumeService.get(user.userId());
        return ApiResponse.ok("Resume fetched successfully", result.value(), result.source(), 1);
    }
}
