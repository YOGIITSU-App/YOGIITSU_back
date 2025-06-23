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
		description = """
			건물 ID를 기반으로 상세 정보를 조회합니다. <br>
			각 건물에 대한 사용자의 즐겨찾기 여부를 포함한 결과를 반환합니다. <br><br>

			사용 가능한 건물 ID 목록:<br>
			1: 인문사회융합대학<br>
			2: 체육관<br>
			3: 미래혁신관<br>
			4: 혁신공과대학<br>
			5: ACE 교육관<br>
			6: 디자인앤아트대학<br>
			7: 조형관<br>
			8: 음악테크놀로지대학<br>
			9: 지능형SW융합대학<br>
			10: 라이프케어사이언스대학<br>
			11: 사회관<br>
			12: 문화예술융합대학<br>
			13: 경영공학대학<br>
			14: 고운첨단과학기술연구원<br>
			15: 제1공학관<br>
			16: 제2공학관<br>
			17: 제3공학관<br>
			18: 제4공학관<br>
			19: 글로벌인재대학<br>
			20: 대학본부<br>
			21: 학생회관<br>
			22: 중앙도서관<br>
			23: ROTC
			"""
	)

	@GetMapping("/{id}")
	public BuildingDetailResponseDto getBuildingDetail(
		@Parameter(
			description = "조회할 건물의 ID<br><br>" +
				"예시:<br>9 → 지능형SW융합대학<br>13 → 경영공학대학",
			example = "9"
		)

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
