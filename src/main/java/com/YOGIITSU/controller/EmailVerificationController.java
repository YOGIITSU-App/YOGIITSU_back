package com.YOGIITSU.controller;

import com.YOGIITSU.dto.RequestDto.EmailVerificationRequestDto;
import com.YOGIITSU.dto.ResponseDto.EmailVerificationResponseDto;
import com.YOGIITSU.entity.EmailMessage;
import com.YOGIITSU.jwt.EmailVerificationJwtProvider;
import com.YOGIITSU.repository.EmailMessageRepository;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "이메일 인증 코드 검증 API", description = "인증 코드 검증 기능 제공합니다.")
@RestController
@RequestMapping("/verify")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationJwtProvider emailJwtProvider;
    private final EmailMessageRepository emailMessageRepository;

    @Operation(
        summary = "이메일 인증 코드 검증",
        description = "사용자가 입력한 인증 코드를 토큰에 포함된 코드와 비교하고, 인증 성공 시 DB의 승인 상태를 true로 변경합니다."
    )
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

    @Operation(
        summary = "이메일 변경 인증 코드 검증",
        description = "이메일 변경을 위한 인증 코드와 토큰을 검증하고, 인증 성공 시 DB의 승인 상태를 true로 변경합니다."
    )
    @PostMapping("/email-change")
    public ResponseEntity<EmailVerificationResponseDto> verifyEmailChangeCode(
        @RequestHeader("X-Email-Verification-Token") String token, // 여기 변경
        @RequestBody EmailVerificationRequestDto requestDto) {

        try {
            // 1. JWT 토큰에서 이메일, 코드 추출
            Claims claims = emailJwtProvider.parseEmailToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰입니다."));

            String tokenEmail = claims.getSubject();
            String tokenCode = claims.get("code", String.class);

            // 2. 요청값과 비교
            if (!tokenEmail.equals(requestDto.getEmail()) || !tokenCode.equals(
                requestDto.getCode())) {
                return ResponseEntity.badRequest().body(EmailVerificationResponseDto.builder()
                    .status("error")
                    .message("이메일 또는 인증코드가 일치하지 않습니다.")
                    .email(null)
                    .build());
            }

            // 3. DB에서 해당 이메일과 코드가 있는지 확인
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

            // 인증 성공 → DB 승인 처리
            emailMessage.setIsApproved(true);
            emailMessageRepository.save(emailMessage);

            return ResponseEntity.ok(EmailVerificationResponseDto.builder()
                .status("success")
                .message("이메일 인증이 완료되었습니다. 이메일 변경을 진행할 수 있습니다.")
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