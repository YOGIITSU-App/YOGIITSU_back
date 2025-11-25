package com.YOGIITSU.controller;

import com.YOGIITSU.dto.ResponseDto.AppVersionResponseDto;
import com.YOGIITSU.enums.Platform;
import com.YOGIITSU.service.AppVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "앱 버전 체크 API", description = "클라이언트 앱의 버전을 서버의 최신 정책과 비교하여 업데이트 필요 여부를 반환합니다.")
@RestController
@RequestMapping("/app/version")
@RequiredArgsConstructor
@Validated
public class AppVersionController {

	private final AppVersionService service;

	@Operation(summary = "앱 버전 정보 조회", description = "앱의 현재 버전과 플랫폼 정보를 받아 업데이트 타입을 반환합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "조회 성공",
			content = @Content(schema = @Schema(implementation = AppVersionResponseDto.class))),
		@ApiResponse(responseCode = "404", description = "해당 플랫폼의 버전 정책을 찾을 수 없음",
			content = @Content(schema = @Schema(example = "{\"message\": \"해당 플랫폼의 버전 정책을 찾을 수 없습니다: IOS\"}")))
	})
	@GetMapping
	public AppVersionResponseDto getAppVersion(
		@Parameter(description = "앱의 운영체제", required = true, example = "ANDROID") @RequestParam Platform platform,
		@Parameter(description = "앱의 현재 버전", required = true, example = "1.0.0") @RequestParam String currentVersion
	) {
		return service.getAppVersion(platform, currentVersion);
	}
}

