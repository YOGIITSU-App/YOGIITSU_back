package com.YOGIITSU.config;

import com.YOGIITSU.config.handler.LogoutSuccessHandler;
import com.YOGIITSU.jwt.JwtAuthenticationFilter;
import com.YOGIITSU.jwt.JwtTokenProvider;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
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
public class SecurityConfig {

	private final JwtTokenProvider jwtTokenProvider;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// CSRF 비활성화
		http
			.csrf(csrf -> csrf.disable())  // CSRF 보호 비활성화
			.authorizeHttpRequests(authz -> authz
				.requestMatchers("/logout").authenticated()  // 로그아웃 경로는 인증된 사용자만 접근 가능
				.anyRequest().permitAll()  // 나머지 요청은 모두 허용
			)
			.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
				UsernamePasswordAuthenticationFilter.class) // JWT 인증 필터 추가
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint((request, response, authException) -> {
					response.setContentType("application/json");
					response.setStatus(
						HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized 상태 설정
					response.getWriter().write("{\"error\": \"Unauthorized\"}");
				})
				.accessDeniedHandler((request, response, accessDeniedException) -> {
					response.setContentType("application/json"); // 401 Unauthorized 상태 설정
					response.setStatus(HttpServletResponse.SC_FORBIDDEN);
					response.getWriter().write("{\"error\": \"Access Denied\"}");
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

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder(); // 패스워드 인코딩
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("http://15.165.2.118")); // 허용할 서버 IP
		configuration.setAllowedMethods(
			Arrays.asList("GET", "POST", "PUT", "DELETE")); // 허용할 HTTP 메서드
		configuration.setAllowedHeaders(
			Arrays.asList("Authorization", "Content-Type", "X-Requested-With",
				"cookie")); // 허용할 요청 헤더
		configuration.setExposedHeaders(Arrays.asList("Authorization", "verify")); // 노출할 응답 헤더
		configuration.setAllowCredentials(true); // 자격 증명 허용

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}