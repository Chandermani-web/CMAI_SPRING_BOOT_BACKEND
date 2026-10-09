package com.cmai.web;

import com.cmai.config.AppProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Cross-site request forgery guard for the cookie session: state-changing requests that carry an
 * Origin header must come from one of the configured frontend origins.
 */
@Component
public class OriginCheckInterceptor implements HandlerInterceptor {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    private final AppProperties props;
    private final ObjectMapper mapper;

    public OriginCheckInterceptor(AppProperties props, ObjectMapper mapper) {
        this.props = props;
        this.mapper = mapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (CorsUtils.isPreFlightRequest(request) || SAFE_METHODS.contains(request.getMethod())) {
            return true;
        }
        String origin = request.getHeader("Origin");
        if (origin != null && !props.cors().allowedOrigins().contains(origin)) {
            HttpErrors.write(response, mapper, 403, "Request origin is not allowed");
            return false;
        }
        return true;
    }
}
