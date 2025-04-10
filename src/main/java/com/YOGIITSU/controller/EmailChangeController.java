package com.YOGIITSU.controller;

import com.YOGIITSU.dto.RequestDto.EmailChangeConfirmRequestDto;
import com.YOGIITSU.dto.ResponseDto.EmailVerificationResponseDto;
import com.YOGIITSU.service.EmailChangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/change-email")
@RequiredArgsConstructor
public class EmailChangeController {

    private final EmailChangeService emailChangeService;

    @PostMapping
    public ResponseEntity<EmailVerificationResponseDto> confirmEmailChange(
        Authentication authentication,
        @RequestBody EmailChangeConfirmRequestDto dto) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                EmailVerificationResponseDto.builder()
                    .status("error")
                    .message("인증 실패: 유효하지 않은 토큰입니다.")
                    .build()
            );
        }

        try {
            String memberId = authentication.getName();
            emailChangeService.changeEmail(memberId, dto);

            return ResponseEntity.ok(EmailVerificationResponseDto.builder()
                .status("success")
                .message("이메일이 성공적으로 변경되었습니다.")
                .email(dto.getNewEmail())
                .build());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                EmailVerificationResponseDto.builder()
                    .status("error")
                    .message("인증 실패: " + e.getMessage())
                    .email(null)
                    .build()
            );
        }
    }

}
