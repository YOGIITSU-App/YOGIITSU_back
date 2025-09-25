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

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MypageProfileController {

    private final MypageProfileService mypageProfileService;

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
