package com.YOGIITSU.controller;

import com.YOGIITSU.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "이메일 변경 API", description = "인증된 새 이메일로 DB의 이메일을 변경합니다.")
@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailChangeController {

    private final EmailService emailService;

    @Operation(summary = "이메일 변경 실행", description = "검증된 인증 토큰을 기반으로 사용자 이메일을 변경합니다.")
    @PostMapping("/change")
    public ResponseEntity<Map<String, String>> changeEmail(
        @RequestHeader("X-Email-Verification-Token") String emailToken,
        HttpServletRequest request) {
        // 토큰 검증과 이메일 변경을 서비스에서 처리
        emailService.changeEmail(emailToken, request);

        // 성공 응답
        return ResponseEntity.ok(Map.of("message", "이메일이 성공적으로 변경되었습니다."));
    }
}