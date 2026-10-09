package com.cmai.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(@NotBlank(message = "planId is required") String planId) {}
