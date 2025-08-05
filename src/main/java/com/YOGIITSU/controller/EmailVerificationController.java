package com.YOGIITSU.controller;

import com.YOGIITSU.dto.RequestDto.EmailVerificationRequestDto;
import com.YOGIITSU.dto.ResponseDto.EmailVerificationResponseDto;
import com.YOGIITSU.jwt.EmailVerificationJwtProvider;
import com.YOGIITSU.service.EmailService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "이메일 인증 코드 검증 API", description = "인증 코드 검증 기능 제공합니다.")
@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailVerificationController {

	private final EmailVerificationJwtProvider emailJwtProvider;
	private final EmailService emailService;

	@Operation(
		summary = "이메일 인증 코드 검증",
		description = "사용자가 입력한 인증 코드를 토큰에 포함된 코드와 비교하고, 인증 성공 시 DB의 승인 상태를 true로 변경합니다."
	)
	@PostMapping("/verify")
	public ResponseEntity<EmailVerificationResponseDto> verifyCode(
		@RequestHeader("X-Email-Verification-Token") String token,
		@RequestBody EmailVerificationRequestDto requestDto) {

		try {
			// 1. JWT 토큰 검증
			Claims claims = emailJwtProvider.parseEmailToken(token)
				.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 토큰입니다."));
			String tokenEmail = claims.getSubject();
			String tokenCode = claims.get("code", String.class);

			// 2. 입력 값과 토큰 비교
			if (!tokenCode.equals(requestDto.getCode())) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(EmailVerificationResponseDto.builder()
						.status("error")
						.message("인증 코드가 일치하지 않습니다.")
						.email(null)
						.build());
			}

			// isApproved = true로 바꾼다.
			emailService.approveEmailCode(tokenEmail, tokenCode);

			// 5. 응답 메시지 생성 및 반환
			return ResponseEntity.ok(
				EmailVerificationResponseDto.builder()
					.status("success")
					.message("이메일 인증에 성공하였습니다.")
					.email(tokenEmail)
					.build());

		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(EmailVerificationResponseDto.builder()
					.status("error")
					.message(e.getMessage())
					.email(null)
					.build());
		}
	}
}