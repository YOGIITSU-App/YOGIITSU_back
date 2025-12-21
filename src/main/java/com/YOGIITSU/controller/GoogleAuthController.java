package com.YOGIITSU.controller;

import com.YOGIITSU.dto.RequestDto.GoogleLoginRequestDto;
import com.YOGIITSU.dto.ResponseDto.TokenResponseDto;
import com.YOGIITSU.dto.ResponseDto.UserResponseDto;
import com.YOGIITSU.service.GoogleAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "소셜 로그인 API", description = "구글 소셜 로그인 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class GoogleAuthController {

	private final GoogleAuthService googleAuthService;

	@Operation(
		summary = "구글 소셜 로그인",
		description = "리액트 네이티브 앱에서 받은 구글 ID 토큰으로 로그인을 처리하고, 자체 JWT를 발급합니다."
	)
	@PostMapping("/google")
	public ResponseEntity<Map<String, Object>> googleLogin(
		@Valid @RequestBody GoogleLoginRequestDto requestDto
	) {
		// 1. 서비스 호출을 통해 모든 정보가 담긴 TokenResponseDto를 받습니다.
		TokenResponseDto tokenInfo = googleAuthService.verifyGoogleIdTokenAndLogin(
			requestDto.getIdToken());

		// 2. 토큰을 헤더에 담습니다. (기존 자체 로그인 방식과 동일)
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + tokenInfo.getAccessToken());
		headers.set("X-Refresh-Token", tokenInfo.getRefreshToken());

		// 3. 응답 본문을 구성합니다. (기존 자체 로그인 방식과 동일)
		UserResponseDto userDto = tokenInfo.getUser();
		Map<String, Object> responseBody = new HashMap<>();
		responseBody.put("message", "로그인 성공");
		responseBody.put("userId", userDto.getId());
		responseBody.put("role", userDto.getRole());

		return ResponseEntity.ok()
			.headers(headers)
			.body(responseBody);
	}
}
