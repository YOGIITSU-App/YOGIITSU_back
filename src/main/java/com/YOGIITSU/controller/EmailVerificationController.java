package com.YOGIITSU.controller;

import com.YOGIITSU.dto.RequestDto.EmailVerificationRequestDto;
import com.YOGIITSU.dto.ResponseDto.EmailVerificationResponseDto;
import com.YOGIITSU.entity.EmailMessage;
import com.YOGIITSU.jwt.EmailVerificationJwtProvider;
import com.YOGIITSU.repository.EmailMessageRepository;
import io.jsonwebtoken.Claims;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/verify")
@RequiredArgsConstructor
public class EmailVerificationController {

	private final EmailVerificationJwtProvider emailJwtProvider;
	private final EmailMessageRepository emailMessageRepository;

	@PostMapping("/code")
	public ResponseEntity<EmailVerificationResponseDto> verifyCode(
		@RequestHeader("X-Email-Verification-Token") String token, // 커스텀 헤더로 변경
		@RequestBody EmailVerificationRequestDto request) {

		try {
			// 1. JWT 토큰에서 이메일과 인증 코드 가져오기
			Claims claims = emailJwtProvider.parseEmailToken(token)
				.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 토큰입니다."));

			String storedEmail = claims.getSubject(); // 이메일 정보
			String storedCode = claims.get("code", String.class); // 저장된 인증 코드

			// 2. 사용자가 입력한 코드와 JWT 내부 코드 비교
			if (!request.getCode().equals(storedCode)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(EmailVerificationResponseDto.builder()
						.status("error")
						.message("인증 실패: 잘못된 인증 코드입니다.")
						.email(null)
						.build());
			}

			// 3. DB에서 해당 이메일과 코드가 있는지 확인
			Optional<EmailMessage> emailMessageOptional = emailMessageRepository.findByEmailAndCode(
				storedEmail, storedCode);
			if (emailMessageOptional.isEmpty()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(EmailVerificationResponseDto.builder()
						.status("error")
						.message("인증 실패: DB에서 해당 이메일과 인증 코드를 찾을 수 없습니다.")
						.email(null)
						.build());
			}

			// 4. 인증 성공 → is_approved = true로 변경
			EmailMessage emailMessage = emailMessageOptional.get();
			emailMessage.setIsApproved(true);
			emailMessageRepository.save(emailMessage);

			// 5. 인증 성공 응답
			return ResponseEntity.ok(EmailVerificationResponseDto.builder()
				.status("success")
				.message("이메일 인증이 완료되었습니다. 회원가입을 진행하세요.")
				.email(storedEmail)
				.build());

		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(EmailVerificationResponseDto.builder()
					.status("error")
					.message("인증 실패: " + e.getMessage())
					.email(null)
					.build());
		}
	}
}
