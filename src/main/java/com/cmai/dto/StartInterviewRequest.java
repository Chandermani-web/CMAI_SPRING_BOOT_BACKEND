package com.cmai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StartInterviewRequest(
        @NotBlank(message = "type is required") String type,
        @NotBlank(message = "role is required") @Size(max = 120) String role,
        Boolean useResume) {}
