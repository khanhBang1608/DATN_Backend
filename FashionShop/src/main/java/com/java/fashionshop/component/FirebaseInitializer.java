package com.java.fashionshop.component;

import java.io.InputStream;

import org.springframework.stereotype.Component;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;

@Component
public class FirebaseInitializer {

    @PostConstruct
    public void initialize() {
        try (InputStream serviceAccount = getClass().getResourceAsStream("/firebase-service-account.json")) {
            if (FirebaseApp.getApps().isEmpty()) { // ✅ Kiểm tra trước khi khởi tạo
                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

                FirebaseApp.initializeApp(options);
                System.out.println("✅ FirebaseApp initialized.");
            } else {
                System.out.println("⚠️ FirebaseApp already initialized.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khởi tạo Firebase", e);
        }
    }
}


