package com.YOGIITSU.service;

import com.YOGIITSU.config.handler.GlobalExceptionHandler.ShuttleStopNotFoundException;
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
		new BusStop("STOP_06", "후문(제4공학관)", Duration.ofMinutes(1).plusSeconds(30)),
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
		List<List<StopScheduleResponseDto>> fullSchedule = generateFullSchedule();

		// 지정한 정류장에 도착할 셔틀들을 찾습니다.
		List<List<StopScheduleResponseDto>> upcomingSchedules = fullSchedule.stream()
			.filter(schedule -> {
				LocalTime arrivalTimeAtStop = LocalTime.parse(
					schedule.get(stopIndex).estimatedArrivalTime());
				return !arrivalTimeAtStop.isBefore(now);
			})
			.toList();

		// 곧 도착할 셔틀 2개를 선택합니다. (오늘 남은 셔틀 + 내일 첫 셔틀)
		List<List<StopScheduleResponseDto>> nextTwoSchedules = new ArrayList<>(upcomingSchedules);
		if (nextTwoSchedules.size() < 2) {
			nextTwoSchedules.addAll(fullSchedule.subList(0, 2 - nextTwoSchedules.size()));
		}
		nextTwoSchedules = nextTwoSchedules.subList(0, 2);

		// DTO 형태로 가공
		List<UpcomingShuttleResponseDto> upcomingShuttles = nextTwoSchedules.stream()
			.map(schedule -> {
				List<StopScheduleResponseDto> remainingRoute = schedule.subList(stopIndex,
					schedule.size());
				String arrivalTime = remainingRoute.get(0).estimatedArrivalTime();
				return new UpcomingShuttleResponseDto(arrivalTime, remainingRoute);
			})
			.toList();

		// 전체 시간표와 노선 정보를 가져옵니다.
		List<String> fullTimeTableString = departureTimes.stream()
			.map(time -> time.format(timeFormatter))
			.toList();

		List<String> fullRouteString = route.stream()
			.map(BusStop::name)
			.toList();

		// 새로 만든 통합 DTO로 모든 정보를 담아 반환합니다.
		return new ShuttleScheduleDetailResponseDto(stopName, upcomingShuttles, fullTimeTableString,
			fullRouteString);
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
					singleRunSchedule.add(new StopScheduleResponseDto(stop.name(),
						arrivalTime.format(timeFormatter)));
					cumulativeDuration = cumulativeDuration.plus(stop.durationToNextStop());
				}
				return singleRunSchedule;
			})
			.toList();
	}

	/**
	 * 정류장 이름으로 경로 목록에서 인덱스를 찾습니다.
	 */
	private int findStopIndexById(String stopId) {
		return IntStream.range(0, route.size())
			.filter(i -> route.get(i).id().equalsIgnoreCase(stopId))
			.findFirst()
			.orElseThrow(() -> new ShuttleStopNotFoundException(stopId));
	}
}