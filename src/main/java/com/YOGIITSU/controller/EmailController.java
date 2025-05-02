package com.YOGIITSU.controller;

import com.YOGIITSU.dto.RequestDto.EmailChangeRequestDto;
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
import java.time.LocalDateTime;

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
    public ResponseEntity<EmailPostResponseDto> sendJoinMail(
        @RequestBody @Valid EmailPostRequestDto emailPostRequestDto) {
        String email = emailPostRequestDto.getEmail();

        // 1. 인증 코드 생성
        String code = emailService.generateVerificationCode();

        // 2. EmailMessage 생성
        EmailMessage emailMessage = EmailMessage.builder()
            .email(email)
            .code(code)
            .isApproved(false)
            .expiresAt(LocalDateTime.now().plusMinutes(5))
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

    @Operation(
        summary = "이메일 변경 인증 메일 전송",
        description = "입력한 새 이메일로 인증 코드를 전송하고, 이메일 + 인증 코드를 포함한 토큰을 반환합니다."
    )
    @PostMapping("/email-change")
    public ResponseEntity<EmailPostResponseDto> sendEmailChangeCode(
        @RequestBody EmailChangeRequestDto requestDto) {

        String email = requestDto.getNewEmail();

        // 1. 인증 코드 생성
        String code = emailService.generateVerificationCode();

        // 2. EmailMessage 객체 생성
        EmailMessage emailMessage = EmailMessage.builder()
            .email(email)
            .code(code)
            .isApproved(false)
            .expiresAt(LocalDateTime.now().plusMinutes(5))
            .build();

        // 3. DB에 저장
        emailService.saveEmailMessage(emailMessage);

        // 4. 메일 전송
        emailService.sendMail(emailMessage, "email");

        // 5. JWT 토큰 생성
        String token = emailJwtProvider.generateEmailToken(email, code);

        return ResponseEntity.ok(EmailPostResponseDto.builder()
            .status("success")
            .message("이메일 변경 인증 코드가 발송되었습니다.")
            .code(code)
            .token(token)
            .build());
    }

    @Operation(
        summary = "이메일 변경 인증 메일 재전송",
        description = "입력한 이메일로 새 인증 코드를 재전송하고, 새로운 이메일 인증 토큰을 반환합니다."
    )
    @PostMapping("/email-change/resend")
    public ResponseEntity<EmailPostResponseDto> resendEmailChangeCode(
        @RequestBody EmailPostRequestDto requestDto) {

        String email = requestDto.getEmail();

        // 1. 인증 코드 생성
        String code = emailService.generateVerificationCode();

        // 2. EmailMessage 객체 생성
        EmailMessage emailMessage = EmailMessage.builder()
            .email(email)
            .code(code)
            .isApproved(false)
            .expiresAt(LocalDateTime.now().plusMinutes(5))
            .build();

        // 3. DB에 저장
        emailService.saveEmailMessage(emailMessage);

        // 4. 메일 전송
        emailService.sendMail(emailMessage, "email-change");

        // 5. JWT 토큰 생성
        String token = emailJwtProvider.generateEmailToken(email, code);

        return ResponseEntity.ok(EmailPostResponseDto.builder()
            .status("success")
            .message("새 인증 코드가 이메일로 재전송되었습니다.")
            .code(code)
            .token(token)
            .build());
    }
}