package com.YOGIITSU.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class GoogleConfig {

	@Value("${spring.security.oauth2.client.registration.google.client-id}")
	private String googleClientId;

	@Bean
	public GoogleIdTokenVerifier googleIdTokenVerifier() {
		// Google ID 토큰 검증을 위한 인스턴스를 생성하고 Bean으로 등록
		return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
			.setAudience(Collections.singletonList(googleClientId))
			.build();
	}
}