package com.YOGIITSU.controller;

import com.YOGIITSU.config.handler.GlobalExceptionHandler.InvalidTokenException;
import com.YOGIITSU.config.handler.GlobalExceptionHandler.MissingTokenException;
import com.YOGIITSU.dto.ResponseDto.TokenResponseDto;
import com.YOGIITSU.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

	@InjectMocks
	private AuthController authController;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private Authentication authentication;

	@Test
	@DisplayName("재발급_성공")
	void reissue_success() {
		// given
		String accessTokenHeader = "Bearer oldAccess";
		String refreshToken = "validRefresh";

		when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
		when(jwtTokenProvider.getAuthentication("oldAccess")).thenReturn(authentication);
		when(jwtTokenProvider.generateToken(authentication)).thenReturn(
			TokenResponseDto.builder()
				.accessToken("newAccess")
				.refreshToken("newRefresh")
				.build()
		);

		ResponseEntity<Map<String, String>> response = authController.reissue(accessTokenHeader,
			refreshToken);

		// then
		assertEquals("토큰이 재발급되었습니다.", response.getBody().get("message"));
		assertEquals("Bearer newAccess", response.getHeaders().getFirst("Authorization"));
		assertEquals("newRefresh", response.getHeaders().getFirst("X-Refresh-Token"));
	}


	@Test
	@DisplayName("재발급_실패_토큰없음")
	void reissue_missing_token() {
		assertThrows(MissingTokenException.class, () ->
			authController.reissue(null, null));
	}


	@Test
	@DisplayName("재발급_실패_리프레시토큰_유효X")
	void reissue_invalid_refresh() {
		// given
		when(jwtTokenProvider.validateToken("validRefresh")).thenReturn(false);

		// then
		assertThrows(InvalidTokenException.class, () ->
			authController.reissue("Bearer oldAccess", "validRefresh"));
	}
}
