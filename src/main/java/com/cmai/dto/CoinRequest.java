package com.cmai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CoinRequest(
        @NotNull(message = "coins is required") @Positive(message = "Invalid coin amount") @Max(10000) Integer coins,
        String action) {}
