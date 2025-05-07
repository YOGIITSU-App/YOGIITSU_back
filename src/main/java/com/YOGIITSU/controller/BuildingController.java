package com.YOGIITSU.controller;

import com.YOGIITSU.dto.ResponseDto.BuildingDetailResponseDto;
import com.YOGIITSU.service.BuildingService;
import com.YOGIITSU.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "건물 관련 API", description = "건물 상세 정보 및 즐겨찾기 여부 조회 기능을 제공합니다.")
@RestController
@RequestMapping("/buildings")
@RequiredArgsConstructor
public class BuildingController {

	private final BuildingService buildingService;
	private final JwtUtil jwtUtil;

	/**
	 * 건물 상세 정보를 조회하는 API (즐겨찾기 여부 포함)
	 *
	 * @param id      조회할 건물의 ID
	 * @param request HttpServletRequest (AccessToken 추출용)
	 * @return 건물의 상세 정보를 담은 BuildingDetailResponseDto
	 */
	@Operation(
		summary = "건물 상세 조회",
		description = "건물 ID를 기반으로 상세 정보를 조회합니다.\n\n" +
			"각 건물에 대한 사용자의 즐겨찾기 여부를 포함한 결과를 반환합니다."
	)
	@GetMapping("/{id}")
	public BuildingDetailResponseDto getBuildingDetail(
		@Parameter(description = "조회할 건물의 ID", example = "10")
		@PathVariable Long id,

		@Parameter(hidden = true)
		HttpServletRequest request
	) {
		// 1. 로그인한 사용자인 경우 MemberId 추출
		Long memberId = jwtUtil.extractMemberId(request);

		// 2. 서비스 호출 → 즐겨찾기 여부까지 포함해서 반환
		return buildingService.getBuildingDetail(id, memberId);
	}
}
