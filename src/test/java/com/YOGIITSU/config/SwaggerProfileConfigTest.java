package com.YOGIITSU.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * application.yml의 프로필별 Swagger 노출 설정 검증
 */
class SwaggerProfileConfigTest {

	@Configuration
	static class EmptyConfig {
	}

	private Environment loadEnvironment(String profile) {
		try (ConfigurableApplicationContext context = new SpringApplicationBuilder(EmptyConfig.class)
			.web(WebApplicationType.NONE)
			.profiles(profile)
			.run()) {
			Environment env = context.getEnvironment();
			// 컨텍스트 종료 전에 필요한 값만 복사해 반환
			return new org.springframework.mock.env.MockEnvironment()
				.withProperty("springdoc.api-docs.enabled",
					String.valueOf(env.getProperty("springdoc.api-docs.enabled")))
				.withProperty("springdoc.swagger-ui.enabled",
					String.valueOf(env.getProperty("springdoc.swagger-ui.enabled")));
		}
	}

	@Test
	@DisplayName("prod_프로필에서_Swagger_비활성화")
	void prodProfile_disablesSwagger() {
		Environment env = loadEnvironment("prod");

		assertEquals("false", env.getProperty("springdoc.api-docs.enabled"));
		assertEquals("false", env.getProperty("springdoc.swagger-ui.enabled"));
	}

	@Test
	@DisplayName("dev_프로필에서_Swagger_유지")
	void devProfile_keepsSwagger() {
		Environment env = loadEnvironment("dev");

		assertNotEquals("false", env.getProperty("springdoc.api-docs.enabled"));
		assertNotEquals("false", env.getProperty("springdoc.swagger-ui.enabled"));
	}
}
