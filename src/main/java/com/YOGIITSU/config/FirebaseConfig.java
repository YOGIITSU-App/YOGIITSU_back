package com.YOGIITSU.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.FileInputStream;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FirebaseConfig {

	@Value("${firebase.service-account-path:}")
	private String serviceAccountPath;

	@Bean
	public FirebaseApp firebaseApp() throws IOException {
		if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
			log.warn("FIREBASE_SERVICE_ACCOUNT_PATH 환경변수가 설정되지 않았습니다. FCM 기능이 비활성화됩니다.");
			return null;
		}
		if (!FirebaseApp.getApps().isEmpty()) {
			return FirebaseApp.getInstance();
		}
		GoogleCredentials credentials;
		try (FileInputStream fis = new FileInputStream(serviceAccountPath)) {
			credentials = GoogleCredentials.fromStream(fis);
		}
		FirebaseOptions options = FirebaseOptions.builder()
			.setCredentials(credentials)
			.build();
		FirebaseApp app = FirebaseApp.initializeApp(options);
		log.info("FirebaseApp initialized successfully. path={}", serviceAccountPath);
		return app;
	}
}
