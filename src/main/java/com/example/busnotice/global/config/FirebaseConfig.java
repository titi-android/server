package com.example.busnotice.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            ClassPathResource serviceAccountResource = new ClassPathResource(
                    "firebase/firebase-service-key.json");

            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    serviceAccountResource.getInputStream());

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            return FirebaseApp.initializeApp(options);
        } else {
            // 이미 초기화된 인스턴스 반환
            return FirebaseApp.getInstance();
        }
    }
}
