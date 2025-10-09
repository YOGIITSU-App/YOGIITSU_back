package com.YOGIITSU.controller;

import com.YOGIITSU.dto.RequestDto.AppleLoginRequestDto;
import com.YOGIITSU.dto.ResponseDto.TokenResponseDto;
import com.YOGIITSU.dto.ResponseDto.UserResponseDto;
import com.YOGIITSU.service.AppleAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "소셜 로그인 API", description = "애플 소셜 로그인 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Slf4j
public class AppleAuthController {

    private final AppleAuthService appleAuthService;

    @Operation(
        summary = "Apple 소셜 로그인",
        description = "애플 authorizationCode로 로그인을 처리하고, 자체 JWT를 발급합니다."
    )
    @PostMapping("/apple")
    public ResponseEntity<Map<String, Object>> loginWithApple(
        @RequestBody @Valid AppleLoginRequestDto requestDto) {

        log.info("[AppleAuthController] 애플 로그인 요청 수신");

        // 1. 서비스 호출 → 자체 JWT 발급
        TokenResponseDto tokenInfo = appleAuthService.loginWithApple(
            requestDto.getAuthorizationCode());

        // 2. 응답 헤더에 JWT 세팅
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + tokenInfo.getAccessToken());
        headers.set("X-Refresh-Token", tokenInfo.getRefreshToken());

        // 3. 응답 바디 데이터 구성
        UserResponseDto userDto = tokenInfo.getUser();
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "로그인 성공");
        responseBody.put("role", userDto.getRole());

        log.debug("[AppleAuthController] Apple 로그인 성공 - userId={}, role={}",
            userDto.getId(), userDto.getRole());

        // 4. 최종 응답 반환
        return ResponseEntity.ok()
            .headers(headers)
            .body(responseBody);
    }
}
