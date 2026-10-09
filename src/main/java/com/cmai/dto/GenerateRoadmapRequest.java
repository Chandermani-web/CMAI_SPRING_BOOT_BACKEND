package com.cmai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GenerateRoadmapRequest(
        @NotBlank(message = "role is required") @Size(max = 120) String role,
        @NotBlank(message = "targetPackage is required") @Size(max = 60) String targetPackage,
        Boolean useResume) {}
