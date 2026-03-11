package com.YOGIITSU.controller;

import com.YOGIITSU.dto.RequestDto.FcmTokenRequestDto;
import com.YOGIITSU.exception.auth.UnauthorizedException;
import com.YOGIITSU.jwt.CustomUserDetails;
import com.YOGIITSU.service.FcmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "FCM API", description = "FCM 푸시 알림 토큰 등록 기능을 제공합니다.")
@RestController
@RequestMapping("/fcm")
@RequiredArgsConstructor
public class FcmController {

	private final FcmService fcmService;

	@Operation(summary = "FCM 토큰 등록", description = "로그인한 사용자의 FCM 토큰을 등록합니다.")
	@PostMapping("/token")
	public ResponseEntity<Map<String, String>> registerToken(
		@RequestBody @Valid FcmTokenRequestDto dto,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		if (userDetails == null) {
			throw new UnauthorizedException();
		}
		fcmService.saveToken(dto, userDetails.getId());
		return ResponseEntity.ok(Map.of("message", "FCM 토큰이 등록되었습니다."));
	}
}
