package com.YOGIITSU.controller;

import com.YOGIITSU.dto.RequestDto.GoogleLoginRequestDto;
import com.YOGIITSU.dto.ResponseDto.TokenResponseDto;
import com.YOGIITSU.dto.ResponseDto.UserResponseDto;
import com.YOGIITSU.service.GoogleAuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleAuthControllerTest {

	@InjectMocks
	private GoogleAuthController googleAuthController;

	@Mock
	private GoogleAuthService googleAuthService;

	@DisplayName("구글 소셜 로그인 성공")
	@Test
	void googleLogin_success() {
		// given
		GoogleLoginRequestDto requestDto =
			new GoogleLoginRequestDto("valid-google-id-token");

		UserResponseDto user = UserResponseDto.builder()
			.id(1L)
			.role("ROLE_USER")
			.build();

		TokenResponseDto tokenResponse = TokenResponseDto.builder()
			.accessToken("access-token")
			.refreshToken("refresh-token")
			.user(user)
			.build();

		when(googleAuthService.verifyGoogleIdTokenAndLogin("valid-google-id-token"))
			.thenReturn(tokenResponse);

		// when
		ResponseEntity<Map<String, Object>> response =
			googleAuthController.googleLogin(requestDto);

		// then
		assertEquals(200, response.getStatusCode().value());
		assertEquals("Bearer access-token",
			response.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
		assertEquals("refresh-token",
			response.getHeaders().getFirst("X-Refresh-Token"));

		assertEquals("로그인 성공", response.getBody().get("message"));
		assertEquals(1L, response.getBody().get("userId"));
		assertEquals("ROLE_USER", response.getBody().get("role"));

		verify(googleAuthService).verifyGoogleIdTokenAndLogin("valid-google-id-token");
	}
}
