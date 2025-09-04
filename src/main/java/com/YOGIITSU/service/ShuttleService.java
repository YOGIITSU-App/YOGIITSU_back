package com.YOGIITSU.service;

import com.YOGIITSU.config.handler.GlobalExceptionHandler.ShuttleStopNotFoundException;
import com.YOGIITSU.dto.ResponseDto.RouteStopResponseDto;
import com.YOGIITSU.dto.ResponseDto.ShuttleScheduleDetailResponseDto;
import com.YOGIITSU.dto.ResponseDto.ShuttleScheduleResponseDto;
import com.YOGIITSU.dto.ResponseDto.StopScheduleResponseDto;
import com.YOGIITSU.dto.ResponseDto.UpcomingShuttleResponseDto;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class ShuttleService {

	// 정류장 이름과 다음 정류장까지의 소요 시간을 관리하는 내부 record
	private record BusStop(String id, String name, Duration durationToNextStop) {

	}

	// 인문대 출발 시간표
	private final List<LocalTime> departureTimes = Arrays.asList(
		LocalTime.of(9, 10), LocalTime.of(9, 20),
		LocalTime.of(10, 10), LocalTime.of(10, 20),
		LocalTime.of(11, 10), LocalTime.of(11, 20),
		LocalTime.of(12, 10), LocalTime.of(13, 20),
		LocalTime.of(14, 20), LocalTime.of(15, 20)
	);

	// 정류장 경로 및 소요 시간 정보
	private final List<BusStop> route = Arrays.asList(
		new BusStop("STOP_01", "인문대 승차", Duration.ofMinutes(1)),
		new BusStop("STOP_02", "학생회관 사거리", Duration.ofMinutes(1)),
		new BusStop("STOP_03", "ICT 융합대학", Duration.ofMinutes(1)),
		new BusStop("STOP_04", "음악대학", Duration.ofMinutes(3)),
		new BusStop("STOP_05", "제1공학관", Duration.ofMinutes(1)),
		new BusStop("STOP_06", "후문(제4공학관)", Duration.ofMinutes(2)),
		new BusStop("STOP_07", "미술대학(조형관)", Duration.ofMinutes(2)),
		new BusStop("STOP_08", "인문대 하차", Duration.ZERO)
	);

	private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");


	/**
	 * 현재 시간을 기준으로 가장 가까운 2개의 셔틀 시간과 전체 시간표, 셔틀 노선 경로를 반환합니다.
	 */
	public ShuttleScheduleResponseDto getOriginalShuttleSchedule() {
		LocalTime now = LocalTime.now(ZoneId.of("Asia/Seoul"));

		// 현재 시간 이후에 출발하는 셔틀들을 필터링
		List<LocalTime> upcomingDepartures = departureTimes.stream()
			.filter(t -> !t.isBefore(now))
			.toList();

		List<LocalTime> nextTwoDepartures = new ArrayList<>(upcomingDepartures);
		if (nextTwoDepartures.size() < 2) {
			nextTwoDepartures.addAll(departureTimes.subList(0, 2 - nextTwoDepartures.size()));
		}
		nextTwoDepartures = nextTwoDepartures.subList(0, 2);

		List<String> nextShuttleTime = nextTwoDepartures.stream()
			.map(time -> time.format(timeFormatter))
			.toList();

		// 전체 시간표와 노선을 문자열 리스트로 변환
		List<String> timeTableString = departureTimes.stream()
			.map(time -> time.format(timeFormatter))
			.toList();

		List<String> routeString = route.stream()
			.map(BusStop::name)
			.toList();

		return new ShuttleScheduleResponseDto(nextShuttleTime, timeTableString, routeString);
	}

	/**
	 * 특정 정류장을 기준으로 상세 정보와 전체 정보를 함께 반환합니다.
	 *
	 * @param stopId 조회할 정류장 ID
	 * @return 상세 정보와 전체 정보가 포함된 DTO
	 */
	public ShuttleScheduleDetailResponseDto getScheduleForStop(String stopId) {
		int stopIndex = findStopIndexById(stopId);
		String stopName = route.get(stopIndex).name();

		LocalTime now = LocalTime.now(ZoneId.of("Asia/Seoul"));

		// DTO 생성을 위해 포맷팅된 전체 시간표를 미리 생성합니다.
		List<List<StopScheduleResponseDto>> fullSchedule = generateFullSchedule();

		// 정확한 도착 시간을 계산하여 다음 셔틀의 '인덱스'를 찾습니다.
		List<Integer> upcomingIndices = IntStream.range(0, departureTimes.size())
			.filter(i -> !estimateArrivalAtStop(departureTimes.get(i), stopIndex).isBefore(now))
			.boxed()
			.toList();

		// 인덱스를 기반으로 다음 셔틀 2개를 선택합니다.
		List<Integer> nextTwoIndices = new ArrayList<>(upcomingIndices);
		if (nextTwoIndices.size() < 2) {
			int remaining = 2 - nextTwoIndices.size();
			for (int i = 0; i < remaining; i++) {
				nextTwoIndices.add(i);
			}
		}
		nextTwoIndices = nextTwoIndices.subList(0, 2);

		// 찾아낸 인덱스를 사용해 최종 DTO를 가공합니다.
		List<UpcomingShuttleResponseDto> upcomingShuttles = nextTwoIndices.stream()
			.map(idx -> {
				List<StopScheduleResponseDto> fullRouteForThisShuttle = fullSchedule.get(idx);
				List<StopScheduleResponseDto> remainingRoute = fullRouteForThisShuttle.subList(
					stopIndex, fullRouteForThisShuttle.size());
				String arrivalTime = remainingRoute.get(0).estimatedArrivalTime();
				return new UpcomingShuttleResponseDto(arrivalTime, remainingRoute);
			})
			.toList();

		// 전체 시간표와 노선 정보를 가져옵니다.
		List<String> fullTimeTableString = departureTimes.stream()
			.map(time -> time.format(timeFormatter))
			.toList();

		List<RouteStopResponseDto> fullRouteObjectList = route.stream()
			.map(stop -> new RouteStopResponseDto(stop.id(), stop.name()))
			.toList();

		// 새로 만든 통합 DTO로 모든 정보를 담아 반환합니다.
		return new ShuttleScheduleDetailResponseDto(stopId, stopName, upcomingShuttles, fullTimeTableString,
			fullRouteObjectList);
	}

	/**
	 * 모든 출발 시간을 바탕으로 전체 운행 시간표(모든 정류장의 도착 시간)를 생성합니다.
	 */
	private List<List<StopScheduleResponseDto>> generateFullSchedule() {
		return departureTimes.stream()
			.map(departureTime -> {
				List<StopScheduleResponseDto> singleRunSchedule = new ArrayList<>();
				Duration cumulativeDuration = Duration.ZERO;

				for (BusStop stop : route) {
					LocalTime arrivalTime = departureTime.plus(cumulativeDuration);
					singleRunSchedule.add(new StopScheduleResponseDto(stop.id(), stop.name(),
						arrivalTime.format(timeFormatter)));
					cumulativeDuration = cumulativeDuration.plus(stop.durationToNextStop());
				}
				return singleRunSchedule;
			})
			.toList();
	}

	/**
	 * 출발시각과 정류장 인덱스로 정확한 도착시각(초 단위 포함)을 계산합니다.
	 */
	private LocalTime estimateArrivalAtStop(LocalTime departureTime, int stopIndex) {
		Duration cumulativeDuration = Duration.ZERO;
		for (int i = 0; i < stopIndex; i++) {
			cumulativeDuration = cumulativeDuration.plus(route.get(i).durationToNextStop());
		}
		return departureTime.plus(cumulativeDuration);
	}

	/**
	 * 정류장 ID로 정류장 인덱스를 찾습니다. 없으면 예외를 던집니다.
	 */
	private int findStopIndexById(String stopId) {
		return IntStream.range(0, route.size())
			.filter(i -> route.get(i).id().equalsIgnoreCase(stopId))
			.findFirst()
			.orElseThrow(() -> new ShuttleStopNotFoundException(stopId));
	}
}