package com.YOGIITSU.controller;

import com.YOGIITSU.dto.ErrorResponse;
import com.YOGIITSU.dto.ResponseDto.WeeklyCafeteriaResponseDto;
import com.YOGIITSU.service.CafeteriaQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Positive;

@Validated
@RestController
@RequestMapping("/cafeteria")
@RequiredArgsConstructor
public class CafeteriaController {

	private final CafeteriaQueryService queryService;

	@Operation(
		summary = "건물별 주간 학식 + 메타 조회 (이번 주)",
		description = """
			KST 기준 '오늘이 속한 주(월~금)'의 학식과 메타 정보를 반환합니다.
			- tz/serverTime/lastUpdatedAt: 서버 KST 시각(ISO-8601)
			- weekStart: 이번 주 월요일(yyyy-MM-dd)
			- todayIndex: 0(월)~4(금), 이번 주 밖이면 -1
			- availableIndices: 실제 메뉴가 존재하는 요일 인덱스 목록
			- indexToDate: 0~4 인덱스와 날짜 매핑
			""")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청",
			content = @Content(mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class))),
		@ApiResponse(responseCode = "401", description = "인증 실패",
			content = @Content(mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class))),
		@ApiResponse(responseCode = "404", description = "건물/식당/메뉴 없음",
			content = @Content(mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class)))
	})
	@GetMapping("/menus/weekly")
	public ResponseEntity<WeeklyCafeteriaResponseDto> getWeekly(
		@Parameter(description = "건물 ID", required = true, example = "5")
		@RequestParam(name = "buildingId") @Positive(message = "buildingId는 양수여야 합니다.") Long buildingId
	) {
		return ResponseEntity.ok(queryService.getWeeklyByBuilding(buildingId));
	}
}