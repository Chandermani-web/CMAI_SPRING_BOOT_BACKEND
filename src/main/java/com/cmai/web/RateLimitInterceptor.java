package com.cmai.web;

import com.cmai.config.AppProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/** Fixed-window limiter backed by Redis: 200 requests / 15 min / IP by default. Fails open if Redis is down. */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final StringRedisTemplate redis;
    private final AppProperties props;
    private final ObjectMapper mapper;

    public RateLimitInterceptor(StringRedisTemplate redis, AppProperties props, ObjectMapper mapper) {
        this.redis = redis;
        this.props = props;
        this.mapper = mapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }

        long windowSeconds = props.rateLimit().windowSeconds();
        long limit = props.rateLimit().maxRequests();
        long window = System.currentTimeMillis() / 1000 / windowSeconds;
        String key = "ratelimit:" + request.getRemoteAddr() + ":" + window;

        try {
            Long count = redis.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redis.expire(key, Duration.ofSeconds(windowSeconds));
            }
            long used = count == null ? 0 : count;
            response.setHeader("RateLimit-Limit", String.valueOf(limit));
            response.setHeader("RateLimit-Remaining", String.valueOf(Math.max(0, limit - used)));
            if (used > limit) {
                HttpErrors.write(response, mapper, 429, "Too many requests. Please try again later.");
                return false;
            }
        } catch (RuntimeException e) {
            log.warn("Rate limiter skipped (Redis unavailable): {}", e.getMessage());
        }
        return true;
    }
}
