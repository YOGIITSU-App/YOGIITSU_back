package com.YOGIITSU.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "에러 응답")
public record ErrorResponse(
	@Schema(description = "에러 메시지", example = "해당 건물에는 식당이 없습니다.")
	String message
) {

}