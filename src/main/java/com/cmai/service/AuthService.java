package com.cmai.service;

import com.cmai.config.AppProperties;
import com.cmai.entity.User;
import com.cmai.exception.ApiException;
import com.cmai.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final FirebaseAuth firebaseAuth;
    private final UserRepository users;
    private final AppProperties props;

    public AuthService(FirebaseAuth firebaseAuth, UserRepository users, AppProperties props) {
        this.firebaseAuth = firebaseAuth;
        this.users = users;
        this.props = props;
    }

    /** Verifies the Firebase ID token from Google sign-in and returns the matching user (created on first login). */
    public User loginWithFirebaseToken(String idToken) {
        FirebaseToken decoded;
        try {
            decoded = firebaseAuth.verifyIdToken(idToken);
        } catch (FirebaseAuthException | IllegalArgumentException e) {
            log.info("Rejected Firebase token: {}", e.getMessage());
            throw new ApiException(401, "Invalid Firebase token");
        }

        String uid = decoded.getUid();
        if (uid == null || uid.isBlank()) {
            throw new ApiException(401, "Invalid Firebase token");
        }

        return users.findByFirebaseUid(uid).orElseGet(() -> register(decoded));
    }

    private User register(FirebaseToken decoded) {
        String email = decoded.getEmail() == null || decoded.getEmail().isBlank() ? null : decoded.getEmail();
        String name = decoded.getName();

        User user = new User();
        user.setFirebaseUid(decoded.getUid());
        user.setEmail(email);
        user.setUsername(displayName(name, email));
        user.setInterviewCoin(props.coins().signupBonus());

        try {
            return users.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            // Two first-time logins racing each other, or the e-mail already belongs to another account.
            return users.findByFirebaseUid(decoded.getUid())
                    .orElseThrow(() -> new ApiException(409, "An account with this email already exists"));
        }
    }

    private static String displayName(String name, String email) {
        if (name != null && !name.isBlank()) {
            return name;
        }
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf('@'));
        }
        return "User";
    }
}
