package com.YOGIITSU.util;

import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.config.handler.GlobalExceptionHandler.InvalidTokenException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtUtil {

	private final JwtTokenProvider jwtTokenProvider;

	/**
	 * JWT 토큰에서 사용자 ID를 추출하는 메서드
	 *
	 * @param request HTTP 요청 객체
	 * @return 사용자 ID(Long)
	 * @throws InvalidTokenException 유효하지 않은 토큰인 경우 발생
	 */
	public Long extractMemberId(HttpServletRequest request) {
		// 1. 요청에서 JWT 토큰 추출
		String accessToken = jwtTokenProvider.resolveToken(request);

		// 2. 토큰이 없으면 예외 발생
		if (accessToken == null) {
			throw new InvalidTokenException();
		}

		// 3. 토큰이 유효한지 확인, 유효하지 않으면 예외 발생
		if (!jwtTokenProvider.validateToken(accessToken)) {
			throw new InvalidTokenException();
		}

		// 4. 유효한 토큰이라면 사용자 ID 추출
		return jwtTokenProvider.getMemberId(accessToken);
	}
}