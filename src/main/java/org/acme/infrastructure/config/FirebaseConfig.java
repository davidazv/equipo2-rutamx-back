package org.acme.infrastructure.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.FileInputStream;
import java.util.logging.Logger;

@ApplicationScoped
public class FirebaseConfig {

    private static final Logger log = Logger.getLogger(FirebaseConfig.class.getName());

    private final String credentialsPath;

    @Inject
    public FirebaseConfig(@ConfigProperty(name = "firebase.credentials") String credentialsPath) {
        this.credentialsPath = credentialsPath;
    }

    void onStart(@Observes StartupEvent ev) {
        try (FileInputStream serviceAccount = new FileInputStream(credentialsPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized successfully");
            }
        } catch (Exception e) {
            log.warning("Firebase initialization skipped (expected in test environment): " + e.getMessage());
        }
    }
}
