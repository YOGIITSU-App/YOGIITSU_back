package com.YOGIITSU.controller;

import com.YOGIITSU.service.EmailService;
import com.YOGIITSU.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "이메일 변경 API", description = "인증된 새 이메일로 DB의 이메일을 변경합니다.")
@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailChangeController {

    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    @Operation(summary = "이메일 변경 실행", description = "검증된 인증 토큰을 기반으로 사용자 이메일을 변경합니다.")
    @PostMapping("/change")
    public ResponseEntity<Map<String, String>> changeEmail(
        @RequestHeader("X-Email-Verification-Token") String emailToken,
        HttpServletRequest request
    ) {
        // 1. 로그인 인증 (AccessToken)
        String accessToken = jwtTokenProvider.resolveToken(request);
        if (accessToken == null || !jwtTokenProvider.validateToken(accessToken)) {
            throw new IllegalArgumentException("유효하지 않은 로그인 토큰입니다.");
        }

        // 2. AccessToken 기반 사용자 인증 정보 획득
        Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);

        // 3. 이메일 인증 토큰을 기반으로 이메일 변경 처리 수행
        emailService.changeEmail(emailToken, authentication);

        // 4. 성공 응답
        return ResponseEntity.ok(Map.of("message", "이메일이 성공적으로 변경되었습니다."));
    }
}
