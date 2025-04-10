package com.YOGIITSU.controller;

import com.YOGIITSU.dto.RequestDto.EmailChangeRequestDto;
import com.YOGIITSU.dto.RequestDto.EmailPostRequestDto;
import com.YOGIITSU.dto.ResponseDto.EmailPostResponseDto;
import com.YOGIITSU.entity.EmailMessage;
import com.YOGIITSU.jwt.EmailVerificationJwtProvider;
import com.YOGIITSU.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/email")
    public ResponseEntity<EmailPostResponseDto> sendJoinMail(
        @RequestBody EmailPostRequestDto emailPostRequestDto) {
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

    @PostMapping("/email-change")
    public ResponseEntity<EmailPostResponseDto> sendEmailChangeCode(
        @RequestBody EmailChangeRequestDto requestDto) {
        String email = requestDto.getNewEmail();

        EmailMessage emailMessage = EmailMessage.builder()
            .email(email)
            .code("")
            .isApproved(false)
            .build();

        String code = emailService.sendMail(emailMessage, "email"); // 인증코드 발송
        String token = emailJwtProvider.generateEmailToken(email, code); // 토큰 생성

        return ResponseEntity.ok(EmailPostResponseDto.builder()
            .status("success")
            .message("이메일 변경 인증 코드가 발송되었습니다.")
            .code(code)
            .token(token)
            .build());
    }

    @PostMapping("/email-change/resend")
    public ResponseEntity<EmailPostResponseDto> resendEmailChangeCode(
        @RequestBody EmailPostRequestDto requestDto) {
        EmailMessage emailMessage = EmailMessage.builder()
            .email(requestDto.getEmail())
            .code("")
            .isApproved(false)
            .build();

        String code = emailService.sendMail(emailMessage, "email-change");
        String token = emailJwtProvider.generateEmailToken(requestDto.getEmail(), code);

        return ResponseEntity.ok(EmailPostResponseDto.builder()
            .status("success")
            .message("새 인증 코드가 이메일로 재전송되었습니다.")
            .code(code)
            .token(token)
            .build());
    }


}
