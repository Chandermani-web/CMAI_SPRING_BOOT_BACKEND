package com.cmai.dto;

/** What we keep in the Redis session. Coin balance is always read fresh from MySQL. */
public record SessionUser(Long userId, String username, String email) {}
