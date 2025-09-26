package com.YOGIITSU.controller;

import com.YOGIITSU.dto.ResponseDto.MypageProfileResponseDto;
import com.YOGIITSU.jwt.CustomUserDetails;
import com.YOGIITSU.service.MypageProfileService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MypageProfileController
 * - 마이페이지 프로필 관련 API 제공
 * - 로그인된 사용자의 프로필 조회 기능 포함
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MypageProfileController {

    private final MypageProfileService mypageProfileService;

    /**
     * 마이페이지 프로필 조회 API
     * - 인증된 사용자의 프로필 정보를 조회
     * - 사용자 ID 기반으로 서비스 호출
     * - 응답 body에 MypageProfileResponseDto 반환
     */
    @Operation(summary = "마이페이지 프로필 조회", description = "로그인된 사용자의 프로필 정보를 조회합니다.")
    @GetMapping("/profile")
    public ResponseEntity<MypageProfileResponseDto> getMyProfile(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
            mypageProfileService.getProfile(userDetails.getId())
        );
    }
}
