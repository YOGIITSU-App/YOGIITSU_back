package com.YOGIITSU.controller;

import com.YOGIITSU.dto.RequestDto.EmailVerificationRequestDto;
import com.YOGIITSU.dto.ResponseDto.EmailVerificationResponseDto;
import com.YOGIITSU.entity.EmailMessage;
import com.YOGIITSU.entity.EmailPurpose;
import com.YOGIITSU.jwt.EmailVerificationJwtProvider;
import com.YOGIITSU.repository.EmailMessageRepository;
import com.YOGIITSU.service.EmailService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;


@Tag(name = "이메일 인증 코드 검증 API", description = "인증 코드 검증 기능 제공합니다.")
@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationJwtProvider emailJwtProvider;
    private final EmailMessageRepository emailMessageRepository;
    private final EmailService emailService;

    @Operation(
        summary = "이메일 인증 코드 검증",
        description = "사용자가 입력한 인증 코드를 토큰에 포함된 코드와 비교하고, 인증 성공 시 DB의 승인 상태를 true로 변경합니다."
    )
    @PostMapping("/verify")
    public ResponseEntity<EmailVerificationResponseDto> verifyCode(
        @RequestHeader("X-Email-Verification-Token") String token,
        @RequestBody EmailVerificationRequestDto requestDto, Authentication authentication) {

        try {
            // 1. JWT 토큰에서 이메일과 인증 코드 가져오기
            Claims claims = emailJwtProvider.parseEmailToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 토큰입니다."));

            String tokenEmail = claims.getSubject();
            String tokenCode = claims.get("code", String.class);

            // 2. 요청값과 비교
            if (!tokenEmail.equals(requestDto.getEmail()) || !tokenCode.equals(
                requestDto.getCode())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(EmailVerificationResponseDto.builder()
                        .status("error")
                        .message("인증 실패: 이메일 또는 인증 코드가 일치하지 않습니다.")
                        .email(null)
                        .build());
            }

            // 3. DB에서 해당 이메일과 코드 확인
            Optional<EmailMessage> optional = emailMessageRepository.findByEmailAndCode(tokenEmail,
                tokenCode);
            if (optional.isEmpty()) {
                return ResponseEntity.badRequest().body(EmailVerificationResponseDto.builder()
                    .status("error")
                    .message("DB에 일치하는 인증 코드가 없습니다.")
                    .email(null)
                    .build());
            }

            EmailMessage emailMessage = optional.get();
            if (emailMessage.getExpiresAt().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(EmailVerificationResponseDto.builder()
                    .status("error")
                    .message("인증 코드가 만료되었습니다.")
                    .email(null)
                    .build());
            }

            // 4. 인증 성공 → 승인 처리
            emailMessage.setIsApproved(true);
            emailMessageRepository.save(emailMessage);

            // 목적이 EMAIL_CHANGE인 경우 Authorization 필요(로그인 토큰 입력 위해서)
            if (emailMessage.getPurpose() == EmailPurpose.EMAIL_CHANGE) {
                emailService.verifyAndMaybeChangeEmail(tokenEmail, tokenCode, token,
                    authentication);
            }

            // 5. 응답 → 메시지 하나로 통일
            return ResponseEntity.ok(EmailVerificationResponseDto.builder()
                .status("success")
                .message("이메일 인증이 완료되었습니다.")
                .email(tokenEmail)
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