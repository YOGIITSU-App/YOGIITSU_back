package com.YOGIITSU.controller;

import com.YOGIITSU.dto.ResponseDto.FacilityMarkerResponseDto;
import com.YOGIITSU.enums.FacilityType;
import com.YOGIITSU.service.FacilityMarkerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 시설 마커 관련 API를 제공하는 컨트롤러입니다. 예: 주차장, 카페, 식당 등의 위치 정보 조회
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/facilities")
@Tag(name = "시설 마커 API", description = "지도에 표시할 주차장, 카페, 식당 등의 위치 데이터를 조회하는 기능을 제공합니다.")
public class FacilityMarkerController {

	private final FacilityMarkerService facilityMarkerService;

	@Operation(
		summary = "시설 마커 위치 조회",
		description = "시설 유형(FacilityType)을 기준으로 해당 위치들의 위도(latitude)와 경도(longitude)를 반환합니다."
	)
	@GetMapping
	public List<FacilityMarkerResponseDto> getMarkersByType(
		@Parameter(
			description = "시설 유형 (RESTAURANT: 식당, PARKING: 주차장, CONVENIENCE_CAFE: 카페 및 편의점, SHUTTLE_BUS: 셔틀버스 정류장)",
			example = "PARKING",
			required = true
		)
		@RequestParam(name = "type", required = true) FacilityType type
	) {
		return facilityMarkerService.getMarkersByType(type);
	}
}
