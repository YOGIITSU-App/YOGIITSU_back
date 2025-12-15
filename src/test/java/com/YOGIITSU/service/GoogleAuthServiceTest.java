package com.YOGIITSU.service;

import com.YOGIITSU.entity.Member;
import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.dto.ResponseDto.TokenResponseDto;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleAuthServiceTest {

	@Mock
	private GoogleIdTokenVerifier googleIdTokenVerifier;

	@Mock
	private UserService userService;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@InjectMocks
	private GoogleAuthService googleAuthService;

	@DisplayName("구글 로그인 성공")
	@Test
	void verifyGoogleIdTokenAndLogin_success() throws Exception {
		// given
		String idTokenString = "valid-token";

		GoogleIdToken googleIdToken = mock(GoogleIdToken.class);
		Payload payload = mock(Payload.class);
		Member member = createDummyMember();

		TokenResponseDto expectedTokenResponse = TokenResponseDto.builder()
			.accessToken("access-token")
			.refreshToken("refresh-token")
			.build();

		when(googleIdTokenVerifier.verify(idTokenString)).thenReturn(googleIdToken);
		when(googleIdToken.getPayload()).thenReturn(payload);
		when(payload.getEmail()).thenReturn("test@gmail.com");
		when(payload.get("name")).thenReturn("테스트유저");

		when(userService.processOAuthUser("google", "test@gmail.com", "테스트유저"))
			.thenReturn(member);

		when(jwtTokenProvider.generateToken(any(Authentication.class)))
			.thenReturn(expectedTokenResponse);

		// when
		TokenResponseDto result =
			googleAuthService.verifyGoogleIdTokenAndLogin(idTokenString);

		// then
		assertNotNull(result);
		assertEquals(expectedTokenResponse, result);

		verify(googleIdTokenVerifier).verify(idTokenString);
		verify(userService).processOAuthUser("google", "test@gmail.com", "테스트유저");
		verify(jwtTokenProvider).generateToken(any(Authentication.class));
	}

	@DisplayName("구글 로그인 실패 - ID Token 검증 결과 null")
	@Test
	void verifyGoogleIdTokenAndLogin_fail_invalidToken() throws Exception {
		// given
		String idTokenString = "invalid-token";
		when(googleIdTokenVerifier.verify(idTokenString)).thenReturn(null);

		// when & then
		assertThrows(AuthenticationServiceException.class,
			() -> googleAuthService.verifyGoogleIdTokenAndLogin(idTokenString));

		verify(googleIdTokenVerifier).verify(idTokenString);
		verify(userService, never()).processOAuthUser(any(), any(), any());
		verify(jwtTokenProvider, never()).generateToken(any());
	}

	@DisplayName("구글 로그인 실패 - 토큰 검증 중 예외 발생")
	@Test
	void verifyGoogleIdTokenAndLogin_fail_verifierException() throws Exception {
		// given
		String idTokenString = "error-token";
		when(googleIdTokenVerifier.verify(idTokenString))
			.thenThrow(new RuntimeException("Google verify error"));

		// when & then
		assertThrows(AuthenticationServiceException.class,
			() -> googleAuthService.verifyGoogleIdTokenAndLogin(idTokenString));

		verify(googleIdTokenVerifier).verify(idTokenString);
		verify(userService, never()).processOAuthUser(any(), any(), any());
		verify(jwtTokenProvider, never()).generateToken(any());
	}

	private Member createDummyMember() {
		return Member.builder()
			.id(1L)
			.memberId("google_123")
			.userName("테스트유저")
			.email("test@gmail.com")
			.password("oauth")
			.role("ROLE_USER")
			.build();
	}
}
