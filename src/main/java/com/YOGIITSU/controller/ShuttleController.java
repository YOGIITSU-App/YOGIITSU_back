package com.YOGIITSU.controller;

import com.YOGIITSU.dto.ResponseDto.ShuttleScheduleDetailResponseDto;
import com.YOGIITSU.dto.ResponseDto.ShuttleScheduleResponseDto;
import com.YOGIITSU.service.ShuttleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "셔틀버스 API", description = "셔틀버스 운행 시간표 및 다음 운행 시간 정보를 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/shuttles")
public class ShuttleController {

	private final ShuttleService shuttleService;

	@Operation(
		summary = "셔틀버스 시간표 및 다음 운행 시간 조회",
		description = "현재 시간을 기준으로 가장 가까운 2개의 셔틀 시간과 전체 시간표, 셔틀 노선 경로를 반환합니다."
	)
	@GetMapping("/schedule")
	public ShuttleScheduleResponseDto getShuttleSchedule() {
		return shuttleService.getOriginalShuttleSchedule();
	}

	@Operation(
		summary = "특정 정류장의 셔틀 도착 상세 정보 조회",
		description = "정류장 고유 ID를 기준으로, 해당 정류장의 다음 셔틀 정보와 전체 시간표/노선을 함께 반환합니다."
	)
	@GetMapping("/schedule/{stopId}")
	public ShuttleScheduleDetailResponseDto getShuttleScheduleForStop(
		@Parameter(description = "조회할 정류장 고유 ID", required = true, example = "STOP_04")
		@PathVariable String stopId
	) {
		return shuttleService.getScheduleForStop(stopId);
	}
}