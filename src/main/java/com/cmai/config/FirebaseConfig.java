package com.cmai.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Initialises the Firebase Admin SDK. It is only used to verify the Google sign-in ID token
 * that the React app sends to /api/auth/login.
 */
@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Bean
    public FirebaseApp firebaseApp(AppProperties props) throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        AppProperties.Firebase fb = props.firebase();
        GoogleCredentials credentials;

        if (fb.serviceAccountJson() != null && !fb.serviceAccountJson().isBlank()) {
            log.info("Firebase: using service account from FIREBASE_SERVICE_ACCOUNT_JSON");
            credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(fb.serviceAccountJson().getBytes(StandardCharsets.UTF_8)));
        } else if (fb.serviceAccountPath() != null
                && !fb.serviceAccountPath().isBlank()
                && Files.isReadable(Path.of(fb.serviceAccountPath()))) {
            log.info("Firebase: using service account file {}", fb.serviceAccountPath());
            try (InputStream in = Files.newInputStream(Path.of(fb.serviceAccountPath()))) {
                credentials = GoogleCredentials.fromStream(in);
            }
        } else {
            throw new IllegalStateException(
                    "Firebase credentials not found. Put serviceAccountKey.json next to the app "
                            + "(or set FIREBASE_SERVICE_ACCOUNT_PATH / FIREBASE_SERVICE_ACCOUNT_JSON).");
        }

        FirebaseOptions.Builder options = FirebaseOptions.builder().setCredentials(credentials);
        if (fb.projectId() != null && !fb.projectId().isBlank()) {
            options.setProjectId(fb.projectId());
        }
        return FirebaseApp.initializeApp(options.build());
    }

    @Bean
    public FirebaseAuth firebaseAuth(FirebaseApp app) {
        return FirebaseAuth.getInstance(app);
    }
}
