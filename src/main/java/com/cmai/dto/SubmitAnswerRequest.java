package com.cmai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmitAnswerRequest(
        @NotNull(message = "interviewId is required") Long interviewId,
        @NotBlank(message = "answer is required") @Size(max = 20000, message = "answer is too long") String answer) {}
