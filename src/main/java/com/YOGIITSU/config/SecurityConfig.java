package com.YOGIITSU.config;

import com.YOGIITSU.jwt.JwtAuthenticationFilter;
import com.YOGIITSU.jwt.JwtTokenProvider;
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
				.anyRequest().permitAll() // 모든 요청 허용
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
			);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder(); // 패스워드 인코딩
	}
}