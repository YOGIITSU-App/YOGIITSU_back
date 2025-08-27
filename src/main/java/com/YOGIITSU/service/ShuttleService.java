package com.YOGIITSU.service;

import com.YOGIITSU.dto.ResponseDto.ShuttleScheduleResponseDto;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShuttleService {

	private final List<LocalTime> timeTable = Arrays.asList(
		LocalTime.of(9, 10), LocalTime.of(9, 20),
		LocalTime.of(10, 10), LocalTime.of(10, 20),
		LocalTime.of(11, 10), LocalTime.of(11, 20),
		LocalTime.of(12, 10), LocalTime.of(13, 20),
		LocalTime.of(14, 20), LocalTime.of(15, 20)
	);

	private final List<String> route = Arrays.asList(
		"인문대 승차",
		"학생회관 사거리",
		"ICT 융합대학",
		"음악대학",
		"제1공학관",
		"후문(제4공학관)",
		"미술대학(조형관)",
		"인문대 하차"
	);

	public ShuttleScheduleResponseDto getShuttleSchedule() {
		List<String> nextShuttleTime = getNextTwoShuttles();
		List<String> timeTableString = timeTable.stream()
			.map(LocalTime::toString)
			.collect(Collectors.toList());

		return new ShuttleScheduleResponseDto(nextShuttleTime, timeTableString, route);
	}

	private List<String> getNextTwoShuttles() {
		LocalTime now = LocalTime.now(ZoneId.of("Asia/Seoul"));

		List<LocalTime> upcoming = timeTable.stream()
			.filter(t -> !t.isBefore(now))
			.toList();

		List<LocalTime> result = new ArrayList<>();
		result.addAll(upcoming);

		int remaining = 2 - result.size();
		if (remaining > 0) {
			result.addAll(timeTable.subList(0, remaining));
		}

		return result.subList(0, 2).stream()
			.map(LocalTime::toString)
			.collect(Collectors.toList());
	}
}
