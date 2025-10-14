package com.YOGIITSU.jwt;

import com.YOGIITSU.exception.ErrorCode;
import com.YOGIITSU.util.ErrorResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends GenericFilterBean {

	private final JwtTokenProvider jwtTokenProvider;
	private final ObjectMapper objectMapper = new ObjectMapper();
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
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		String path = httpRequest.getRequestURI();

		// 토큰 없이도 접근 가능한 URL은 필터 제외
		if (path.equals("/members/find-password")) {
			chain.doFilter(request, response);
			return;
		}

		try {
			// 1. Request Header 에서 JWT 토큰 추출
			String token = resolveToken(httpRequest);

			// 2. 토큰이 있는 경우 파싱 및 검증 (JWT 예외는 외부 catch 블록에서 처리됨)
			if (token != null) {
				// 토큰 파싱 및 검증
				jwtTokenProvider.parseAndValidateToken(token);
				
				// 파싱 성공 시 인증 설정
				setAuthentication(token);
			}

		} catch (io.jsonwebtoken.ExpiredJwtException e) {
			// TOKEN_EXPIRED: 토큰이 만료되었습니다.
			log.warn("JWT token expired - URI: {}, ErrorCode: {}, Message: {}, Current Time: {}",
				path, ErrorCode.TOKEN_EXPIRED.getCode(), e.getMessage(),
				java.time.LocalDateTime.now().toString());
			handleAuthenticationError(httpResponse, ErrorCode.TOKEN_EXPIRED);
			return;

		} catch (io.jsonwebtoken.MalformedJwtException e) {
			// INVALID_TOKEN: 유효하지 않은 토큰입니다.
			log.warn("Malformed JWT token - URI: {}, ErrorCode: {}, Message: {}",
				path, ErrorCode.INVALID_TOKEN.getCode(), e.getMessage());
			handleAuthenticationError(httpResponse, ErrorCode.INVALID_TOKEN);
			return;

		} catch (io.jsonwebtoken.UnsupportedJwtException e) {
			// INVALID_TOKEN: 유효하지 않은 토큰입니다.
			log.warn("Unsupported JWT token - URI: {}, ErrorCode: {}, Message: {}",
				path, ErrorCode.INVALID_TOKEN.getCode(), e.getMessage());
			handleAuthenticationError(httpResponse, ErrorCode.INVALID_TOKEN);
			return;

		} catch (io.jsonwebtoken.security.SignatureException e) {
			// INVALID_TOKEN: 유효하지 않은 토큰입니다.
			log.warn("Invalid JWT signature - URI: {}, ErrorCode: {}, Message: {}",
				path, ErrorCode.INVALID_TOKEN.getCode(), e.getMessage());
			handleAuthenticationError(httpResponse, ErrorCode.INVALID_TOKEN);
			return;

		} catch (io.jsonwebtoken.JwtException e) {
			// 기타 JWT 처리 오류
			log.warn("JWT processing error - URI: {}, ErrorCode: {}, Message: {}",
				path, ErrorCode.UNAUTHORIZED.getCode(), e.getMessage());
			handleAuthenticationError(httpResponse, ErrorCode.UNAUTHORIZED);
			return;
		}
		// 나머지 예외는 전파하여 전역 예외 처리(5xx 등)에 맡깁니다.
		chain.doFilter(request, response);
	}

	/**
	 * 인증 오류 발생 시 응답 처리
	 */
	private void handleAuthenticationError(HttpServletResponse response, ErrorCode errorCode)
		throws IOException {
		SecurityContextHolder.clearContext();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setStatus(errorCode.getHttpStatus().value());

		// 공통 유틸리티를 사용하여 ErrorResponse 형식으로 JSON 응답 생성
		java.util.Map<String, Object> errorResponse = ErrorResponseUtil.createErrorResponse(errorCode);
		String jsonResponse = objectMapper.writeValueAsString(errorResponse);
		response.getWriter().write(jsonResponse);
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
	 * JWT 토큰에서 인증 정보를 추출하여 SecurityContext 에 저장
	 *
	 * @param token JWT 토큰 문자열
	 */
	private void setAuthentication(String token) {
		Authentication authentication = jwtTokenProvider.getAuthentication(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}