package com.YOGIITSU.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

	private final JwtTokenProvider jwtTokenProvider;
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	/**
	 * 요청 필터링을 처리하며, JWT 토큰을 검증하고 인증 정보를 SecurityContext 에 저장
	 *
	 * @param request  ServletRequest 객체
	 * @param response ServletResponse 객체
	 * @param chain    FilterChain 객체로 다음 필터로 요청 전달
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String path = httpRequest.getRequestURI();

		// 토큰 없이도 접근 가능한 URL은 필터 제외
		if (path.equals("/members/find-password")) {
			chain.doFilter(request, response);
			return;
		}

		// 1. Request Header 에서 JWT 토큰 추출
		String token = resolveToken((HttpServletRequest) request);
		// 2. validateToken 으로 토큰 유효성 검사
		if (isValidToken(token)) {
			// 3. 토큰이 유효할 경우 토큰에서 Authentication 객체를 가지고 와서 SecurityContext 에 저장
			setAuthentication(token);
		}
		chain.doFilter(request, response);
	}

	/**
	 * 요청 헤더에서 JWT 토큰을 추출
	 *
	 * @param request HttpServletRequest 객체
	 * @return 추출된 JWT 토큰 문자열, 없으면 null 반환
	 */
	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
			return bearerToken.substring(BEARER_PREFIX.length());
		}
		return null;
	}

	/**
	 * JWT 토큰의 유효성을 검사
	 *
	 * @param token JWT 토큰 문자열
	 * @return 토큰이 유효하면 true, 그렇지 않으면 false
	 */
	private boolean isValidToken(String token) {
		return token != null && jwtTokenProvider.validateToken(token);
	}

	/**
	 * JWT 토큰에서 인증 정보를 추출하여 SecurityContext 에 저장
	 *
	 * @param token JWT 토큰 문자열
	 */
	private void setAuthentication(String token) {
		Authentication authentication = jwtTokenProvider.getAuthentication(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}