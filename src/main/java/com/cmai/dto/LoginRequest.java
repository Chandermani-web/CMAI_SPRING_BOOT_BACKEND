package com.cmai.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank(message = "Firebase token is required") String token) {}
