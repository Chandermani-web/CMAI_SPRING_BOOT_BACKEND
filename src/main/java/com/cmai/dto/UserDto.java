package com.cmai.dto;

import com.cmai.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserDto(
        Long id,
        String firebaseUid,
        String username,
        String email,
        int interviewCoin,
        Instant createdAt,
        Instant updatedAt) {


    /** Compatibility with the Node API, whose documents used Mongo's "_id". */
    @JsonProperty("_id")
    public Long legacyId() {
        return id;
    }
    public static UserDto from(User u) {
        return new UserDto(
                u.getId(),
                u.getFirebaseUid(),
                u.getUsername(),
                u.getEmail(),
                u.getInterviewCoin(),
                u.getCreatedAt(),
                u.getUpdatedAt());
    }
}
