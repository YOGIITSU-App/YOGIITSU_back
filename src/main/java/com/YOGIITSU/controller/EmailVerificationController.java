package com.YOGIITSU.controller;

import com.YOGIITSU.dto.RequestDto.EmailVerificationRequestDto;
import com.YOGIITSU.dto.ResponseDto.EmailVerificationResponseDto;
import com.YOGIITSU.entity.EmailPurpose;
import com.YOGIITSU.jwt.EmailVerificationJwtProvider;
import com.YOGIITSU.service.EmailService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    private final EmailService emailService;

    @Operation(
        summary = "이메일 인증 코드 검증",
        description = "사용자가 입력한 인증 코드를 토큰에 포함된 코드와 비교하고, 인증 성공 시 DB의 승인 상태를 true로 변경합니다."
    )
    @PostMapping("/verify")
    public ResponseEntity<EmailVerificationResponseDto> verifyCode(
        @RequestParam(name = "purpose") EmailPurpose purpose,
        @RequestHeader("X-Email-Verification-Token") String token,
        @RequestBody EmailVerificationRequestDto requestDto, Authentication authentication) {

        try {
            // 1. JWT 토큰 검증
            Claims claims = emailJwtProvider.parseEmailToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 토큰입니다."));
            String tokenEmail = claims.getSubject();
            String tokenCode = claims.get("code", String.class);

            // 2. 입력 값과 토큰 비교
            if (!tokenEmail.equals(requestDto.getEmail()) || !tokenCode.equals(
                requestDto.getCode())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(EmailVerificationResponseDto.builder()
                        .status("error")
                        .message("인증 실패: 이메일 또는 인증 코드가 일치하지 않습니다.")
                        .email(null)
                        .build());
            }

            // 3. 인증이 필요한 목적이라면 로그인 체크
            if (EmailPurpose.requiresLogin(purpose)) {
                emailService.checkAuthentication(authentication);
            }

            // 4. 인증 처리: 변경 목적이면 변경, 아니면 승인만
            if (purpose == EmailPurpose.EMAIL_CHANGE_NEW) {
                emailService.verifyAndMaybeChangeEmail(tokenEmail, tokenCode, token,
                    authentication);
            } else {
                emailService.approveEmailCode(tokenEmail, tokenCode);
            }

            // 5. 응답 메시지 생성 및 반환
            return ResponseEntity.ok(
                EmailVerificationResponseDto.builder()
                    .status("success")
                    .message(purpose.getSuccessMessage())
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