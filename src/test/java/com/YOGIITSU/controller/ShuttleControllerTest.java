package com.YOGIITSU.controller;

import com.YOGIITSU.dto.ResponseDto.RouteStopResponseDto;
import com.YOGIITSU.dto.ResponseDto.ShuttleScheduleDetailResponseDto;
import com.YOGIITSU.dto.ResponseDto.ShuttleScheduleResponseDto;
import com.YOGIITSU.dto.ResponseDto.StopScheduleResponseDto;
import com.YOGIITSU.dto.ResponseDto.UpcomingShuttleResponseDto;
import com.YOGIITSU.exception.resource.ShuttleStopNotFoundException;
import com.YOGIITSU.service.ShuttleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
	controllers = ShuttleController.class,
	excludeAutoConfiguration = {
		SecurityAutoConfiguration.class,
		UserDetailsServiceAutoConfiguration.class,
		OAuth2ClientAutoConfiguration.class,
		OAuth2ResourceServerAutoConfiguration.class
	}
)
class ShuttleControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ShuttleService shuttleService;

	@DisplayName("셔틀버스시간표조회_성공")
	@Test
	void getShuttleSchedule_success() throws Exception {
		// given
		ShuttleScheduleResponseDto expectedResponse = new ShuttleScheduleResponseDto(
			List.of("09:10", "09:20"),
			List.of("09:10", "09:20", "10:10", "10:20", "11:10", "11:20", "12:10", "13:20", "14:20",
				"15:20"),
			List.of("인문대 승차", "학생회관 사거리", "ICT 융합대학", "음악대학", "제1공학관", "후문(제4공학관)", "미술대학(조형관)",
				"인문대 하차")
		);

		when(shuttleService.getOriginalShuttleSchedule()).thenReturn(expectedResponse);

		// when & then
		mockMvc.perform(get("/shuttles/schedule"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nextShuttleTime", hasSize(2)))
			.andExpect(jsonPath("$.nextShuttleTime[0]").value("09:10"))
			.andExpect(jsonPath("$.nextShuttleTime[1]").value("09:20"))
			.andExpect(jsonPath("$.timeTable", hasSize(10)))
			.andExpect(jsonPath("$.route", hasSize(8)));
	}

	@DisplayName("특정정류장셔틀도착상세정보조회_성공")
	@Test
	void getShuttleScheduleForStop_success() throws Exception {
		// given
		String stopId = "STOP_04";
		String stopName = "음악대학";
		List<UpcomingShuttleResponseDto> upcomingShuttles = List.of(
			new UpcomingShuttleResponseDto(
				"09:13",
				List.of(
					new StopScheduleResponseDto("STOP_04", "음악대학", "09:13"),
					new StopScheduleResponseDto("STOP_05", "제1공학관", "09:14"),
					new StopScheduleResponseDto("STOP_06", "후문(제4공학관)", "09:16"),
					new StopScheduleResponseDto("STOP_07", "미술대학(조형관)", "09:18"),
					new StopScheduleResponseDto("STOP_08", "인문대 하차", "09:20")
				)
			),
			new UpcomingShuttleResponseDto(
				"09:23",
				List.of(
					new StopScheduleResponseDto("STOP_04", "음악대학", "09:23"),
					new StopScheduleResponseDto("STOP_05", "제1공학관", "09:24"),
					new StopScheduleResponseDto("STOP_06", "후문(제4공학관)", "09:26"),
					new StopScheduleResponseDto("STOP_07", "미술대학(조형관)", "09:28"),
					new StopScheduleResponseDto("STOP_08", "인문대 하차", "09:30")
				)
			)
		);
		List<String> fullTimeTable = List.of("09:13", "09:23", "10:13", "10:23", "11:13", "11:23",
			"12:13", "13:23", "14:23", "15:23");
		List<RouteStopResponseDto> fullRoute = List.of(
			new RouteStopResponseDto("STOP_01", "인문대 승차"),
			new RouteStopResponseDto("STOP_02", "학생회관 사거리"),
			new RouteStopResponseDto("STOP_03", "ICT 융합대학"),
			new RouteStopResponseDto("STOP_04", "음악대학"),
			new RouteStopResponseDto("STOP_05", "제1공학관"),
			new RouteStopResponseDto("STOP_06", "후문(제4공학관)"),
			new RouteStopResponseDto("STOP_07", "미술대학(조형관)"),
			new RouteStopResponseDto("STOP_08", "인문대 하차")
		);

		ShuttleScheduleDetailResponseDto expectedResponse = new ShuttleScheduleDetailResponseDto(
			stopId,
			stopName,
			upcomingShuttles,
			fullTimeTable,
			fullRoute
		);

		when(shuttleService.getScheduleForStop(stopId)).thenReturn(expectedResponse);

		// when & then
		mockMvc.perform(get("/shuttles/schedule/{stopId}", stopId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.selectedStopId").value(stopId))
			.andExpect(jsonPath("$.selectedStopName").value(stopName))
			.andExpect(jsonPath("$.upcomingShuttles", hasSize(2)))
			.andExpect(jsonPath("$.upcomingShuttles[0].arrivalTimeAtSelectedStop").value("09:13"))
			.andExpect(jsonPath("$.upcomingShuttles[1].arrivalTimeAtSelectedStop").value("09:23"))
			.andExpect(jsonPath("$.fullTimeTable", hasSize(10)))
			.andExpect(jsonPath("$.fullRoute", hasSize(8)));
	}

	@DisplayName("특정정류장셔틀도착상세정보조회_실패_정류장없음")
	@Test
	void getShuttleScheduleForStop_fail_stopNotFound() throws Exception {
		// given
		String stopId = "INVALID_STOP";

		when(shuttleService.getScheduleForStop(stopId))
			.thenThrow(new ShuttleStopNotFoundException(stopId));

		// when & then
		mockMvc.perform(get("/shuttles/schedule/{stopId}", stopId))
			.andExpect(status().isNotFound());
	}
}

