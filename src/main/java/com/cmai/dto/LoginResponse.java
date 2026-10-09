package com.cmai.dto;

public record LoginResponse(boolean success, String message, UserDto user) {}
