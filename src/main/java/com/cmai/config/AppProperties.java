package com.cmai.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Typed view of the {@code app.*} section of application.yml. */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String env,
        Cors cors,
        Session session,
        Cookie cookie,
        RateLimit rateLimit,
        Coins coins,
        Ai ai,
        Firebase firebase,
        Razorpay razorpay,
        Youtube youtube) {

    public record Cors(List<String> allowedOrigins) {}

    public record Session(String cookieName, long ttlSeconds) {}

    public record Cookie(boolean secure, String sameSite) {}

    public record RateLimit(long windowSeconds, long maxRequests) {}

    public record Coins(int signupBonus, int interviewCost) {}

    public record Ai(
            String apiKey,
            String baseUrl,
            String model,
            double temperature,
            int maxTokens,
            int maxRetries,
            int timeoutSeconds) {}

    public record Firebase(String projectId, String serviceAccountPath, String serviceAccountJson) {}

    public record Razorpay(String keyId, String keySecret) {}

    public record Youtube(String baseUrl, String apiKey) {}

    public boolean isProduction() {
        return "production".equalsIgnoreCase(env);
    }
}
