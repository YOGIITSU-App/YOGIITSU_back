package com.YOGIITSU.dto.ResponseDto;

import java.util.List;

// 특정 정류장 기준 셔틀 시간표와 노선을 반환하는 record
public record ShuttleScheduleDetailResponseDto(
	String selectedStopName,
	List<UpcomingShuttleResponseDto> upcomingShuttles,

	List<String> fullTimeTable, // 인문대 출발 전체 시간표
	List<String> fullRoute      // 전체 노선 정보
) {

}