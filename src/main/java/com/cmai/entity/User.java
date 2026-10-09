package com.cmai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(name = "firebase_uid", nullable = false, unique = true, length = 128)
    private String firebaseUid;

    @Column(nullable = false)
    private String username;

    /** Nullable on purpose: MySQL allows many NULLs under a UNIQUE index (e.g. phone-only sign-ins). */
    @Column(unique = true)
    private String email;

    @Column(name = "interview_coin", nullable = false)
    private int interviewCoin;

    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getInterviewCoin() { return interviewCoin; }
    public void setInterviewCoin(int interviewCoin) { this.interviewCoin = interviewCoin; }
}
