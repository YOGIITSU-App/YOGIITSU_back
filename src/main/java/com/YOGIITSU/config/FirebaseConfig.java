package com.YOGIITSU.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirebaseConfig {

	@Value("${firebase.service-account-json}")
	private String serviceAccountJson;

	@Bean
	public FirebaseApp firebaseApp() throws IOException {
		if (!FirebaseApp.getApps().isEmpty()) {
			return FirebaseApp.getInstance();
		}
		GoogleCredentials credentials = GoogleCredentials.fromStream(
			new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8)));
		FirebaseOptions options = FirebaseOptions.builder()
			.setCredentials(credentials)
			.build();
		return FirebaseApp.initializeApp(options);
	}
}
