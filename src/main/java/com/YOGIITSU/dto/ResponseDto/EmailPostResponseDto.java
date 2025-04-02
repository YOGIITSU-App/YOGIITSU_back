package com.YOGIITSU.dto.ResponseDto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailPostResponseDto {

	private String status; // 응답 상태(success, error)
	private String message; // 응답 메시지
	private String token; // JWT 토큰 (성공 시 null)
	private String code; // 인증 코드 (성공 시 null)
}
