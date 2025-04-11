package com.YOGIITSU.controller;

import com.YOGIITSU.dto.RequestDto.EmailPostRequestDto;
import com.YOGIITSU.dto.ResponseDto.EmailPostResponseDto;
import com.YOGIITSU.entity.EmailMessage;
import com.YOGIITSU.jwt.EmailVerificationJwtProvider;
import com.YOGIITSU.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증 코드 전송 API", description = "인증 메일 전송 기능 제공합니다.")
@RestController
@RequestMapping("/send-mail")
@RequiredArgsConstructor
public class EmailController {

	private final EmailService emailService;
	private final EmailVerificationJwtProvider emailJwtProvider;

	/**
	 * 회원가입 이메일 인증 코드 전송
	 *
	 * @param emailPostRequestDto 이메일 요청 DTO
	 * @return ResponseEntity<EmailResponseDto>
	 */
	@Operation(summary = "회원가입 인증 메일 전송", description = "입력한 이메일로 인증 코드를 전송하고, 이메일 + 인증코드를 포함한 토큰을 반환합니다.")
	@PostMapping("/email")
	public ResponseEntity<EmailPostResponseDto> sendJoinMail(@RequestBody @Valid EmailPostRequestDto emailPostRequestDto) {
		// EmailMessage 객체 생성
		EmailMessage emailMessage = EmailMessage.builder()
			.email(emailPostRequestDto.getEmail()) // 수신자 이메일
			.code("") // 인증 코드 생성 로직에서 설정됨
			.isApproved(false) // 초기 승인 상태
			.build();

		// 인증 코드 생성 및 이메일 발송
		String code = emailService.sendMail(emailMessage, "email");

		// JWT 토큰 생성 (이메일 + 인증 코드 포함)
		String token = emailJwtProvider.generateEmailToken(emailPostRequestDto.getEmail(), code);

		// JSON 형식 응답 반환
		EmailPostResponseDto emailPostResponseDto = EmailPostResponseDto.builder()
			.status("success")
			.message("이메일 인증 코드가 발송되었습니다.")
			.code(code)
			.token(token)
			.build();

		return ResponseEntity.ok(emailPostResponseDto);
	}
}
