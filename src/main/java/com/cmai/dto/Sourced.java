package com.cmai.dto;

/** A value together with where it came from ("redis" cache or "database"). */
public record Sourced<T>(T value, String source) {}
