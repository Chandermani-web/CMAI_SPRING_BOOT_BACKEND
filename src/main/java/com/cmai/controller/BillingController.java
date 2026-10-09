package com.cmai.controller;

import com.cmai.dto.CreateOrderRequest;
import com.cmai.dto.SessionUser;
import com.cmai.dto.VerifyPaymentRequest;
import com.cmai.exception.ApiException;
import com.cmai.service.BillingService;
import com.cmai.web.AuthInterceptor;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    /** Plans on sale and what one interview costs, so the UI never hard-codes prices. */
    @GetMapping("/plans")
    public Map<String, Object> plans() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("plans", billingService.plans());
        body.put("interviewCost", billingService.interviewCost());
        return body;
    }

    @PostMapping("/create-order")
    public Map<String, Object> createOrder(
            @RequestAttribute(AuthInterceptor.ATTR_USER) SessionUser user,
            @Valid @RequestBody CreateOrderRequest request) {
        Map<String, Object> result = billingService.createOrder(user.userId(), request.planId());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("message", "Order created successfully");
        body.putAll(result);
        return body;
    }

    @PostMapping("/verify-payment")
    public Map<String, Object> verify(
            @RequestAttribute(AuthInterceptor.ATTR_USER) SessionUser user,
            @Valid @RequestBody VerifyPaymentRequest request) {
        BillingService.VerifyOutcome outcome = billingService.verifyPayment(user.userId(), request);
        if (!outcome.valid()) {
            throw new ApiException(400, "Invalid signature");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("message", outcome.alreadyVerified() ? "Payment already verified" : "Payment verified successfully");
        body.put("interviewCoin", outcome.interviewCoin());
        return body;
    }
}
