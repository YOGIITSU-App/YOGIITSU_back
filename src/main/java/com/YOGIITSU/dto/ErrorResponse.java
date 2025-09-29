package com.YOGIITSU.dto;

import com.YOGIITSU.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Schema(description = "에러 응답")
@Builder
public record ErrorResponse(
	@Schema(description = "에러 코드", example = "RESOURCE_001")
	String code,

	@Schema(description = "에러 메시지", example = "존재하지 않는 건물입니다.")
	String message,

	@Schema(description = "상세 메시지", example = "buildingId=123")
	String detail,

	@Schema(description = "에러 발생 시간")
	LocalDateTime timestamp,

	@Schema(description = "HTTP 상태 코드", example = "404")
	int status
) {

	/**
	 * ErrorCode로부터 ErrorResponse를 생성하는 정적 팩토리 메서드
	 */
	public static ErrorResponse of(ErrorCode errorCode) {
		return ErrorResponse.builder()
			.code(errorCode.getCode())
			.message(errorCode.getMessage())
			.detail(errorCode.getMessage())
			.timestamp(LocalDateTime.now())
			.status(errorCode.getHttpStatus().value())
			.build();
	}

	/**
	 * ErrorCode와 상세 메시지로 ErrorResponse를 생성하는 정적 팩토리 메서드
	 */
	public static ErrorResponse of(ErrorCode errorCode, String detail) {
		return ErrorResponse.builder()
			.code(errorCode.getCode())
			.message(errorCode.getMessage())
			.detail(detail)
			.timestamp(LocalDateTime.now())
			.status(errorCode.getHttpStatus().value())
			.build();
	}
}