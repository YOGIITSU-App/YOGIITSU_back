package com.YOGIITSU.service;

import com.YOGIITSU.dto.ResponseDto.TokenResponseDto;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.jwt.CustomUserDetails;
import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.exception.validation.InvalidArgumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;


import java.util.Map;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
public class KakaoAuthService {

	private final UserService userService;

	private final JwtTokenProvider jwtTokenProvider;
	private final WebClient webClient = WebClient.create();
	private static final Logger logger = LoggerFactory.getLogger(KakaoAuthService.class);

	private static final String KAKAO_USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";

	@Transactional
	public TokenResponseDto loginWithKakao(String accessToken) {
		try {
			// 1. 카카오 서버로부터 사용자 정보 받아오기
			Map<String, Object> userInfo = getUserInfoFromKakao(accessToken);

			// 2. 받은 정보에서 이메일과 닉네임 추출
			Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
			Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

			String email = (String) kakaoAccount.get("email");
			String nickname = (String) profile.get("nickname");

			// 3. UserService를 통해 사용자 조회 또는 신규 가입 처리
			Member member = userService.processOAuthUser("kakao", email, nickname);

			// 4. 자체 인증 토큰 생성을 위한 Authentication 객체 생성
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

			// 5. 자체 JWT를 생성하여 반환
			return jwtTokenProvider.generateToken(authentication);

		} catch (WebClientResponseException e) {
			logger.error("카카오 API 에러 발생! 상태 코드: {}", e.getStatusCode(), e);
			if (logger.isDebugEnabled()) {
				logger.debug("카카오 API 응답 본문: {}", e.getResponseBodyAsString());
			}
			throw new InvalidArgumentException("카카오 서버로부터 사용자 정보를 가져오는 데 실패했습니다.");
		}
	}

	/**
	 * 카카오 서버로부터 사용자 정보를 가져오는 메소드
	 *
	 * @param accessToken 카카오 액세스 토큰
	 * @return 사용자 정보 맵
	 */
	private Map<String, Object> getUserInfoFromKakao(String accessToken) {
		return webClient.post()
			.uri(KAKAO_USER_INFO_URI)
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.retrieve()
			.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
			})
			.block();
	}
}
