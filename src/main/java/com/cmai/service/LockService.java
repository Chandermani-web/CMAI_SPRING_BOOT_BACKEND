package com.cmai.service;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/** Minimal Redis lock (SET NX with expiry) to stop the same interview being answered twice at once. */
@Service
public class LockService {

    private final StringRedisTemplate redis;

    public LockService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public boolean tryLock(String key, Duration ttl) {
        return Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(key, "1", ttl));
    }

    public void unlock(String key) {
        redis.delete(key);
    }
}
