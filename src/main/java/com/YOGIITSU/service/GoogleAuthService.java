package com.YOGIITSU.service;

import com.YOGIITSU.entity.Member;
import com.YOGIITSU.jwt.CustomUserDetails;
import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.exception.validation.InvalidArgumentException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.YOGIITSU.dto.ResponseDto.TokenResponseDto;

@Service
@lombok.extern.slf4j.Slf4j
@RequiredArgsConstructor
public class GoogleAuthService {

	private final GoogleIdTokenVerifier googleIdTokenVerifier; // 1번에서 Bean으로 등록한 검증 객체
	private final UserService userService; // 2번에서 만든 사용자 처리 서비스
	private final JwtTokenProvider jwtTokenProvider; // 기존의 JWT 발급 Provider

	@Transactional
	public TokenResponseDto verifyGoogleIdTokenAndLogin(String idTokenString) {
		try {
			// 1. 구글 ID 토큰 검증
			GoogleIdToken idToken = googleIdTokenVerifier.verify(idTokenString);
			if (idToken == null) {
				throw new InvalidArgumentException("ID Token is invalid.");
			}
			Payload payload = idToken.getPayload();

			// 2. 토큰에서 사용자 정보 추출
			String email = payload.getEmail();
			String name = (String) payload.get("name");

			// 3. UserService를 통해 사용자 조회 또는 신규 가입 처리
			Member member = userService.processOAuthUser("google", email, name);

			// 4. 우리 서비스의 인증 토큰 생성을 위한 Authentication 객체 생성
			CustomUserDetails userDetails = new CustomUserDetails(
				member.getId(),
				member.getMemberId(),
				member.getUserName(),
				member.getEmail(),
				member.getPassword(),
				member.getRole(),
				member.getAuthorities()
			);
			Authentication authentication = new UsernamePasswordAuthenticationToken(
				userDetails, null, userDetails.getAuthorities()
			);

			return jwtTokenProvider.generateToken(authentication);

		} catch (Exception e) {
			log.warn("Google ID Token verification failed: {}", e.getMessage());
			throw new org.springframework.security.authentication.AuthenticationServiceException(
				"Google ID Token verification failed.", e
			);
		}
	}
}