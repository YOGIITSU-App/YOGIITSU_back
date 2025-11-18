package com.YOGIITSU.config;

import com.YOGIITSU.config.handler.LogoutSuccessHandler;
import com.YOGIITSU.exception.ErrorCode;
import com.YOGIITSU.jwt.JwtAuthenticationFilter;
import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.util.ErrorResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

	private final JwtTokenProvider jwtTokenProvider;
	private final ObjectMapper objectMapper;
	@Value("${cors.allowed-origin}")
	private String allowedOrigin;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// CSRF 비활성화
		http
			.csrf(csrf -> csrf.disable())  // CSRF 보호 비활성화
			.authorizeHttpRequests(authz -> authz
				// Swagger 경로 허용
				.requestMatchers(
					"/swagger-ui/**",
					"/v3/api-docs/**",
					"/swagger-resources/**",
					"/webjars/**",
					"/members/find-password",
					"/api/auth/google/verify",
					"/auth/reissue"
				).permitAll()

				// 로그아웃은 인증된 사용자만 가능
				.requestMatchers("/logout").authenticated()

				// 나머지 요청은 모두 허용
				.anyRequest().permitAll()
			)
			.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
				UsernamePasswordAuthenticationFilter.class) // JWT 인증 필터 추가
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint((request, response, authException) -> {
					// UNAUTHORIZED: 인증이 필요합니다.
					log.warn("Authentication failed - URI: {}, ErrorCode: {}, Message: {}",
						request.getRequestURI(),
						ErrorCode.UNAUTHORIZED.getCode(),
						authException.getMessage());
					handleSecurityError(response, ErrorCode.UNAUTHORIZED);
				})
				.accessDeniedHandler((request, response, accessDeniedException) -> {
					// ACCESS_DENIED: 접근 권한이 없습니다.
					log.warn("Access denied - URI: {}, ErrorCode: {}, Message: {}",
						request.getRequestURI(),
						ErrorCode.ACCESS_DENIED.getCode(),
						accessDeniedException.getMessage());
					handleSecurityError(response, ErrorCode.ACCESS_DENIED);
				})
			)
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Stateless 기반 세션 관리
			)
			// 로그아웃 경로 설정
			.logout(logout -> logout
				.logoutUrl("/logout") // 로그아웃 URL
				.logoutSuccessHandler(logoutSuccessHandler()) // 커스텀 핸들러 등록
				.permitAll() // 로그아웃 경로는 모두 허용

			);

		return http.build();
	}

	@Bean
	public LogoutSuccessHandler logoutSuccessHandler() {
		return new LogoutSuccessHandler(jwtTokenProvider);  // 커스텀 로그아웃 성공 핸들러 사용
	}

	/**
	 * Spring Security 오류 발생 시 응답 처리
	 */
	private void handleSecurityError(HttpServletResponse response, ErrorCode errorCode) {
		try {
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.setStatus(errorCode.getHttpStatus().value());

			// 공통 유틸리티를 사용하여 ErrorResponse 형식으로 JSON 응답 생성
			java.util.Map<String, Object> errorResponse = ErrorResponseUtil.createErrorResponse(
				errorCode);
			String jsonResponse = objectMapper.writeValueAsString(errorResponse);
			response.getWriter().write(jsonResponse);
		} catch (Exception e) {
			log.error("Failed to write security error response", e);
		}
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder(); // 패스워드 인코딩
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList(allowedOrigin)); // 허용할 서버 IP
		configuration.setAllowedMethods(
			Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH")); // 허용할 HTTP 메서드
		configuration.setAllowedHeaders(
			Arrays.asList("Authorization", "Content-Type", "X-Requested-With",
				"cookie")); // 허용할 요청 헤더
		configuration.setExposedHeaders(Arrays.asList("Authorization", "verify")); // 노출할 응답 헤더
		configuration.setAllowCredentials(true); // 자격 증명 허용

		configuration.setExposedHeaders(
			Arrays.asList("Authorization", "verify", "X-Refresh-Token"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}