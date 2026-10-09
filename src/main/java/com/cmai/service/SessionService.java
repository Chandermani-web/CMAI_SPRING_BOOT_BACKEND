package com.cmai.service;

import com.cmai.config.AppProperties;
import com.cmai.dto.SessionUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

/** Server-side sessions: an opaque UUID cookie pointing at a JSON blob in Redis (key "session:{id}"). */
@Service
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final AppProperties props;

    public SessionService(StringRedisTemplate redis, ObjectMapper mapper, AppProperties props) {
        this.redis = redis;
        this.mapper = mapper;
        this.props = props;
    }

    public String create(SessionUser user) {
        String id = UUID.randomUUID().toString();
        try {
            redis.opsForValue().set(key(id), mapper.writeValueAsString(user), ttl());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialise session", e);
        }
        return id;
    }

    public Optional<SessionUser> find(String sessionId) {
        String json = redis.opsForValue().get(key(sessionId));
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(mapper.readValue(json, SessionUser.class));
        } catch (JsonProcessingException e) {
            log.warn("Discarding unreadable session {}", sessionId);
            return Optional.empty();
        }
    }

    public void delete(String sessionId) {
        redis.delete(key(sessionId));
    }

    public ResponseCookie cookie(String sessionId) {
        return base(sessionId).maxAge(ttl()).build();
    }

    public ResponseCookie clearingCookie() {
        return base("").maxAge(Duration.ZERO).build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(props.session().cookieName(), value)
                .httpOnly(true)
                .secure(props.cookie().secure())
                .sameSite(props.cookie().sameSite())
                .path("/");
    }

    private Duration ttl() {
        return Duration.ofSeconds(props.session().ttlSeconds());
    }

    private static String key(String id) {
        return "session:" + id;
    }
}
