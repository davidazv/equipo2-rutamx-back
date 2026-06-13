package org.acme.infrastructure.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class FirebaseConfig {

    private static final Logger log = Logger.getLogger(FirebaseConfig.class.getName());

    private final String credentialsPath;
    private final String projectId;
    private final boolean failOnError;

    @Inject
    public FirebaseConfig(
            @ConfigProperty(name = "firebase.credentials") String credentialsPath,
            @ConfigProperty(name = "firebase.project-id") String projectId,
            @ConfigProperty(name = "firebase.fail-on-error", defaultValue = "true") boolean failOnError) {
        this.credentialsPath = credentialsPath;
        this.projectId = projectId;
        this.failOnError = failOnError;
    }

    void onStart(@Observes StartupEvent ev) {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }
        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(loadCredentials())
                    .setProjectId(projectId)
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase initialized successfully (project=" + projectId + ")");
        } catch (Exception e) {
            if (failOnError) {
                log.log(Level.SEVERE, "Firebase initialization FAILED", e);
                throw new IllegalStateException("Firebase initialization failed", e);
            }
            log.warning("Firebase initialization skipped: " + e.getMessage());
        }
    }

    /**
     * Local/dev: read the service-account JSON file if present.
     * Cloud Run: file absent -> use Application Default Credentials (the
     * runtime service account's identity). No secret file shipped in the image.
     */
    private GoogleCredentials loadCredentials() throws Exception {
        File file = new File(credentialsPath);
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                log.info("Firebase using credentials file: " + credentialsPath);
                return GoogleCredentials.fromStream(in);
            }
        }
        log.info("Firebase credentials file not found; using Application Default Credentials");
        return GoogleCredentials.getApplicationDefault();
    }
}
