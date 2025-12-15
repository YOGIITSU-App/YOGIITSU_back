package com.YOGIITSU.service;

import com.YOGIITSU.dto.ResponseDto.TokenResponseDto;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.exception.validation.InvalidArgumentException;
import com.YOGIITSU.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KakaoAuthServiceTest {

	@Mock
	private UserService userService;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@InjectMocks
	private KakaoAuthService kakaoAuthService;

	// WebClient 체인 Mock
	private WebClient webClient;
	private WebClient.RequestBodyUriSpec uriSpec;
	private WebClient.RequestHeadersSpec<?> headersSpec;
	private WebClient.ResponseSpec responseSpec;

	@BeforeEach
	void setUp() {
		webClient = mock(WebClient.class);
		uriSpec = mock(WebClient.RequestBodyUriSpec.class);
		headersSpec = mock(WebClient.RequestHeadersSpec.class);
		responseSpec = mock(WebClient.ResponseSpec.class);

		ReflectionTestUtils.setField(kakaoAuthService, "webClient", webClient);
	}

	@DisplayName("카카오 로그인 성공")
	@Test
	void loginWithKakao_success() {
		// given
		String accessToken = "valid-token";
		Map<String, Object> kakaoUserInfo = createKakaoUserInfo();
		Member member = createDummyMember();

		when(webClient.post()).thenReturn(uriSpec);
		when(uriSpec.uri(anyString())).thenReturn(uriSpec);
		when(uriSpec.header(anyString(), anyString())).thenReturn(uriSpec);
		when(uriSpec.contentType(any())).thenReturn(uriSpec);
		when(uriSpec.retrieve()).thenReturn(responseSpec);
		when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
			.thenReturn(Mono.just(kakaoUserInfo));

		when(userService.processOAuthUser("kakao", "test@kakao.com", "카카오유저"))
			.thenReturn(member);

		when(jwtTokenProvider.generateToken(any(Authentication.class)))
			.thenReturn(TokenResponseDto.builder()
				.accessToken("access")
				.refreshToken("refresh")
				.build());

		// when
		TokenResponseDto result = kakaoAuthService.loginWithKakao(accessToken);

		// then
		assertNotNull(result);
		assertEquals("access", result.getAccessToken());
		assertEquals("refresh", result.getRefreshToken());

		verify(userService).processOAuthUser("kakao", "test@kakao.com", "카카오유저");
		verify(jwtTokenProvider).generateToken(any(Authentication.class));
	}

	@DisplayName("카카오 로그인 실패 - 카카오 API 에러")
	@Test
	void loginWithKakao_fail_kakaoApiError() {
		// given
		String accessToken = "invalid-token";

		when(webClient.post()).thenReturn(uriSpec);
		when(uriSpec.uri(anyString())).thenReturn(uriSpec);
		when(uriSpec.header(anyString(), anyString())).thenReturn(uriSpec);
		when(uriSpec.contentType(any())).thenReturn(uriSpec);
		when(uriSpec.retrieve()).thenReturn(responseSpec);
		when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
			.thenThrow(new WebClientResponseException(
				401, "Unauthorized", null,
				"{}".getBytes(StandardCharsets.UTF_8),
				StandardCharsets.UTF_8
			));

		// when & then
		assertThrows(InvalidArgumentException.class,
			() -> kakaoAuthService.loginWithKakao(accessToken));

		verify(userService, never()).processOAuthUser(any(), any(), any());
		verify(jwtTokenProvider, never()).generateToken(any());
	}

	private Map<String, Object> createKakaoUserInfo() {
		Map<String, Object> profile = new HashMap<>();
		profile.put("nickname", "카카오유저");

		Map<String, Object> account = new HashMap<>();
		account.put("email", "test@kakao.com");
		account.put("profile", profile);

		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put("kakao_account", account);
		return userInfo;
	}

	private Member createDummyMember() {
		return Member.builder()
			.id(1L)
			.memberId("kakao_123")
			.userName("카카오유저")
			.email("test@kakao.com")
			.password("oauth")
			.role("ROLE_USER")
			.build();
	}
}
