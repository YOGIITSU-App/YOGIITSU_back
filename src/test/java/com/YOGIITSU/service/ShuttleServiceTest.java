package com.YOGIITSU.service;

import com.YOGIITSU.dto.ResponseDto.RouteStopResponseDto;
import com.YOGIITSU.dto.ResponseDto.ShuttleScheduleDetailResponseDto;
import com.YOGIITSU.dto.ResponseDto.ShuttleScheduleResponseDto;
import com.YOGIITSU.dto.ResponseDto.UpcomingShuttleResponseDto;
import com.YOGIITSU.exception.resource.ShuttleStopNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ShuttleServiceTest {

	@InjectMocks
	private ShuttleService shuttleService;

	@DisplayName("셔틀버스시간표조회_성공")
	@Test
	void getOriginalShuttleSchedule_success() {
		// given & when
		ShuttleScheduleResponseDto result = shuttleService.getOriginalShuttleSchedule();

		// then
		assertNotNull(result);
		assertNotNull(result.nextShuttleTime());
		assertEquals(2, result.nextShuttleTime().size());
		assertNotNull(result.timeTable());
		assertEquals(10, result.timeTable().size());
		assertNotNull(result.route());
		assertEquals(8, result.route().size());
		assertTrue(result.timeTable().contains("09:10"));
		assertTrue(result.timeTable().contains("15:20"));
		assertTrue(result.route().contains("인문대 승차"));
		assertTrue(result.route().contains("인문대 하차"));
	}

	@DisplayName("특정정류장셔틀도착상세정보조회_성공")
	@Test
	void getScheduleForStop_success() {
		// given
		String stopId = "STOP_04";

		// when
		ShuttleScheduleDetailResponseDto result = shuttleService.getScheduleForStop(stopId);

		// then
		assertNotNull(result);
		assertEquals(stopId, result.selectedStopId());
		assertEquals("음악대학", result.selectedStopName());
		assertNotNull(result.upcomingShuttles());
		assertEquals(2, result.upcomingShuttles().size());
		assertNotNull(result.fullTimeTable());
		assertEquals(10, result.fullTimeTable().size());
		assertNotNull(result.fullRoute());
		assertEquals(8, result.fullRoute().size());

		// 다음 셔틀 정보 검증
		UpcomingShuttleResponseDto firstUpcoming = result.upcomingShuttles().get(0);
		assertNotNull(firstUpcoming.arrivalTimeAtSelectedStop());
		assertNotNull(firstUpcoming.remainingRoute());
		assertFalse(firstUpcoming.remainingRoute().isEmpty());

		// 전체 노선 정보 검증
		RouteStopResponseDto firstStop = result.fullRoute().get(0);
		assertEquals("STOP_01", firstStop.stopId());
		assertEquals("인문대 승차", firstStop.stopName());
	}

	@DisplayName("특정정류장셔틀도착상세정보조회_성공_첫번째정류장")
	@Test
	void getScheduleForStop_success_firstStop() {
		// given
		String stopId = "STOP_01";

		// when
		ShuttleScheduleDetailResponseDto result = shuttleService.getScheduleForStop(stopId);

		// then
		assertNotNull(result);
		assertEquals(stopId, result.selectedStopId());
		assertEquals("인문대 승차", result.selectedStopName());
		assertNotNull(result.upcomingShuttles());
		assertEquals(2, result.upcomingShuttles().size());
	}

	@DisplayName("특정정류장셔틀도착상세정보조회_성공_마지막정류장")
	@Test
	void getScheduleForStop_success_lastStop() {
		// given
		String stopId = "STOP_08";

		// when
		ShuttleScheduleDetailResponseDto result = shuttleService.getScheduleForStop(stopId);

		// then
		assertNotNull(result);
		assertEquals(stopId, result.selectedStopId());
		assertEquals("인문대 하차", result.selectedStopName());
		assertNotNull(result.upcomingShuttles());
		assertEquals(2, result.upcomingShuttles().size());
	}

	@DisplayName("특정정류장셔틀도착상세정보조회_성공_대소문자구분없음")
	@Test
	void getScheduleForStop_success_caseInsensitive() {
		// given
		String stopId = "stop_04"; // 소문자

		// when
		ShuttleScheduleDetailResponseDto result = shuttleService.getScheduleForStop(stopId);

		// then
		assertNotNull(result);
		assertEquals("stop_04", result.selectedStopId()); // 입력된 stopId가 그대로 반환됨
		assertEquals("음악대학", result.selectedStopName());
	}

	@DisplayName("특정정류장셔틀도착상세정보조회_실패_정류장없음")
	@Test
	void getScheduleForStop_fail_stopNotFound() {
		// given
		String stopId = "INVALID_STOP";

		// when & then
		assertThrows(ShuttleStopNotFoundException.class,
			() -> shuttleService.getScheduleForStop(stopId));
	}

	@DisplayName("특정정류장셔틀도착상세정보조회_실패_빈문자열")
	@Test
	void getScheduleForStop_fail_emptyString() {
		// given
		String stopId = "";

		// when & then
		assertThrows(ShuttleStopNotFoundException.class,
			() -> shuttleService.getScheduleForStop(stopId));
	}

	@DisplayName("특정정류장셔틀도착상세정보조회_실패_null")
	@Test
	void getScheduleForStop_fail_null() {
		// given
		String stopId = null;

		// when & then
		assertThrows(ShuttleStopNotFoundException.class,
			() -> shuttleService.getScheduleForStop(stopId));
	}
}

