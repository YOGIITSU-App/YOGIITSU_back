package com.YOGIITSU.controller;

import com.YOGIITSU.dto.RequestDto.KakaoLoginRequestDto;
import com.YOGIITSU.dto.ResponseDto.TokenResponseDto;
import com.YOGIITSU.service.KakaoAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "소셜 로그인 API", description = "카카오 소셜 로그인 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class KakaoAuthController {

	private final KakaoAuthService kakaoAuthService;

	@Operation(summary = "카카오 소셜 로그인")
	@PostMapping("/kakao")
	public ResponseEntity<Map<String, Object>> kakaoLogin(@Valid @RequestBody KakaoLoginRequestDto requestDto) {
		// 서비스 호출을 통해 토큰 정보 받기
		TokenResponseDto tokenInfo = kakaoAuthService.loginWithKakao(requestDto.getAccessToken());

		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + tokenInfo.getAccessToken());
		headers.set("X-Refresh-Token", tokenInfo.getRefreshToken());

		Map<String, Object> responseBody = new HashMap<>();
		responseBody.put("message", "로그인 성공");
		responseBody.put("userId", tokenInfo.getUser().getId());
		responseBody.put("role", tokenInfo.getUser().getRole());

		return ResponseEntity.ok()
			.headers(headers)
			.body(responseBody);
	}
}
