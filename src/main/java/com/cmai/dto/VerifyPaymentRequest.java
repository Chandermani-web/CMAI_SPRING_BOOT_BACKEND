package com.cmai.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/** Accepts Razorpay Checkout's snake_case field names (camelCase also works). */
public record VerifyPaymentRequest(
        @JsonProperty("razorpay_order_id") @JsonAlias("razorpayOrderId") @NotBlank String razorpayOrderId,
        @JsonProperty("razorpay_payment_id") @JsonAlias("razorpayPaymentId") @NotBlank String razorpayPaymentId,
        @JsonProperty("razorpay_signature") @JsonAlias("razorpaySignature") @NotBlank String razorpaySignature) {}
