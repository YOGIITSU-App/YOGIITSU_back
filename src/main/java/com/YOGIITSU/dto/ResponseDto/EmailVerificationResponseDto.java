package com.YOGIITSU.dto.ResponseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationResponseDto {

    private String status; // 응답 상태 (success, error)
    private String message; // 응답 메시지
    private String email; // 인증한 이메일
}