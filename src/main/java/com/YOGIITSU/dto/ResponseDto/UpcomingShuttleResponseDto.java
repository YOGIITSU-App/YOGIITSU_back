package com.YOGIITSU.dto.ResponseDto;

import java.util.List;

// 곧 도착할 셔틀 1대의 정보를 담는 record
public record UpcomingShuttleResponseDto(
	String arrivalTimeAtSelectedStop, // 사용자가 선택한 정류장에 도착하는 시간
	List<StopScheduleResponseDto> remainingRoute // 해당 셔틀의 남은 경로 정보
) {

}