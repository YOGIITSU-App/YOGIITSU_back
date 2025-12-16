package com.YOGIITSU.util;

import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.exception.auth.MissingTokenException;
import com.YOGIITSU.exception.auth.InvalidTokenException;
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
			throw new MissingTokenException();
		}

		// 3. 토큰이 유효한지 확인, 유효하지 않으면 예외 발생
		if (!jwtTokenProvider.validateToken(accessToken)) {
			throw new InvalidTokenException();
		}

		// 4. 유효한 토큰이라면 사용자 ID 추출
		return jwtTokenProvider.getMemberId(accessToken);
	}

	/**
	 * JWT 토큰에서 사용자 ID를 추출하는 메서드 (토큰이 없어도 예외 발생하지 않음)
	 *
	 * @param request HTTP 요청 객체
	 * @return 사용자 ID(Long), 토큰이 없거나 유효하지 않으면 null
	 */
	public Long extractMemberIdSafely(HttpServletRequest request) {
		try {
			// 1. 요청에서 JWT 토큰 추출
			String accessToken = jwtTokenProvider.resolveToken(request);

			// 2. 토큰이 없으면 null 반환
			if (accessToken == null) {
				return null;
			}

			// 3. 토큰이 유효한지 확인, 유효하지 않으면 null 반환
			if (!jwtTokenProvider.validateToken(accessToken)) {
				return null;
			}

			// 4. 유효한 토큰이라면 사용자 ID 추출
			return jwtTokenProvider.getMemberId(accessToken);
		} catch (Exception e) {
			// 토큰 관련 예외가 발생하면 null 반환
			return null;
		}
	}

	/**
	 * JWT 토큰에서 사용자 ID를 추출하는 메서드 (String 타입, 토큰이 없어도 예외 발생하지 않음)
	 *
	 * @param request HTTP 요청 객체
	 * @return 사용자 ID(String), 토큰이 없거나 유효하지 않으면 null
	 */
	public String extractMemberIdStringSafely(HttpServletRequest request) {
		try {
			// 1. 요청에서 JWT 토큰 추출
			String accessToken = jwtTokenProvider.resolveToken(request);

			// 2. 토큰이 없으면 null 반환
			if (accessToken == null) {
				return null;
			}

			// 3. 토큰이 유효한지 확인, 유효하지 않으면 null 반환
			if (!jwtTokenProvider.validateToken(accessToken)) {
				return null;
			}

			// 4. 유효한 토큰이라면 사용자 ID 추출
			return jwtTokenProvider.getAuthentication(accessToken).getName();
		} catch (Exception e) {
			// 토큰 관련 예외가 발생하면 null 반환
			return null;
		}
	}
}