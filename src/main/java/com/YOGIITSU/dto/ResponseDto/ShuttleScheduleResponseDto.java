package com.YOGIITSU.dto.ResponseDto;

import java.util.List;

public record ShuttleScheduleResponseDto(
	List<String> nextShuttleTime,
	List<String> timeTable,
	List<String> route
) {
	public ShuttleScheduleResponseDto {
		nextShuttleTime = List.copyOf(nextShuttleTime);
		timeTable = List.copyOf(timeTable);
		route = List.copyOf(route);
	}
}