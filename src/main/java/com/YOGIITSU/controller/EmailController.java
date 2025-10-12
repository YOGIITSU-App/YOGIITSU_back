package com.YOGIITSU.controller;

import com.YOGIITSU.dto.RequestDto.EmailPostRequestDto;
import com.YOGIITSU.dto.ResponseDto.EmailPostResponseDto;
import com.YOGIITSU.entity.EmailMessage;
import com.YOGIITSU.entity.EmailPurpose;
import com.YOGIITSU.jwt.EmailVerificationJwtProvider;
import com.YOGIITSU.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@Tag(name = "인증 코드 전송 API", description = "인증 메일 전송 기능 제공합니다.")
@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {

	private final EmailService emailService;
	private final EmailVerificationJwtProvider emailJwtProvider;

	/**
	 * 이메일 인증 코드 전송
	 *
	 * @param emailPostRequestDto 이메일 요청 DTO
	 * @return ResponseEntity<EmailResponseDto>
	 */
	@Operation(summary = "인증 메일 전송", description = "입력한 이메일로 인증 코드를 전송하고, 이메일 + 인증코드를 포함한 토큰을 반환합니다.")
	@PostMapping("/send-code")
	public ResponseEntity<EmailPostResponseDto> sendJoinMail(
		@RequestParam(name = "purpose") EmailPurpose purpose,
		@RequestBody @Valid EmailPostRequestDto emailPostRequestDto,
		Authentication authentication) {

		String email = emailPostRequestDto.getEmail();

		// 목적에 따라 유효성 검사 실행 (서비스로 위임했다.)
		emailService.validateEmailRequest(email, purpose, authentication);

		// 1. 인증 코드 생성
		String code = emailService.generateVerificationCode();

		// 2. EmailMessage 생성
		EmailMessage emailMessage = EmailMessage.builder()
			.email(email)
			.code(code)
			.isApproved(false)
			.expiresAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(5))
			.purpose(purpose) // 목적 포함
			.build();

		// 3. DB에 저장
		emailService.saveEmailMessage(emailMessage);

		// 4. 메일 전송
		emailService.sendMail(emailMessage, "email");

		// 5. 토큰 생성
		String token = emailJwtProvider.generateEmailToken(email, code);

		// 6. 응답
		EmailPostResponseDto response = EmailPostResponseDto.builder()
			.status("success")
			.message("이메일 인증 코드가 발송되었습니다.")
			.code(code)
			.token(token)
			.build();

		return ResponseEntity.ok(response);
	}
}