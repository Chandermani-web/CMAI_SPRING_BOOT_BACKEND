package com.cmai.web;

import com.cmai.config.AppProperties;
import com.cmai.dto.SessionUser;
import com.cmai.service.SessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/** Cookie + Redis session check. Protected controllers read the user via @RequestAttribute. */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String ATTR_USER = "cmai.sessionUser";
    public static final String ATTR_SESSION_ID = "cmai.sessionId";

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    private final SessionService sessions;
    private final AppProperties props;
    private final ObjectMapper mapper;

    public AuthInterceptor(SessionService sessions, AppProperties props, ObjectMapper mapper) {
        this.sessions = sessions;
        this.props = props;
        this.mapper = mapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }

        String sessionId = readCookie(request, props.session().cookieName());
        if (sessionId == null || sessionId.isBlank()) {
            HttpErrors.write(response, mapper, 401, "Unauthorized: No session found");
            return false;
        }

        Optional<SessionUser> user;
        try {
            user = sessions.find(sessionId);
        } catch (DataAccessException e) {
            log.error("Session store unavailable", e);
            HttpErrors.write(response, mapper, 503, "Session store is temporarily unavailable");
            return false;
        }

        if (user.isEmpty()) {
            HttpErrors.write(response, mapper, 401, "Unauthorized: Session expired");
            return false;
        }

        request.setAttribute(ATTR_USER, user.get());
        request.setAttribute(ATTR_SESSION_ID, sessionId);
        return true;
    }

    private static String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
