package com.YOGIITSU.dto.ResponseDto;

// 특정 셔틀의 한 정류장에 대한 정보를 담는 record
public record StopScheduleResponseDto(
	String stopId,
	String stopName,
	String estimatedArrivalTime
) {

}