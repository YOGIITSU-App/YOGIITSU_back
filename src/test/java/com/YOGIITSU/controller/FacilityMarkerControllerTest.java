package com.YOGIITSU.controller;

import com.YOGIITSU.dto.ResponseDto.FacilityMarkerResponseDto;
import com.YOGIITSU.enums.FacilityType;
import com.YOGIITSU.service.FacilityMarkerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
	controllers = FacilityMarkerController.class,
	excludeAutoConfiguration = {
		SecurityAutoConfiguration.class,
		UserDetailsServiceAutoConfiguration.class,
		OAuth2ClientAutoConfiguration.class,
		OAuth2ResourceServerAutoConfiguration.class
	}
)
class FacilityMarkerControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private FacilityMarkerService facilityMarkerService;

	@DisplayName("시설마커조회_성공_주차장")
	@Test
	void getMarkersByType_success_parking() throws Exception {
		// given
		FacilityType type = FacilityType.PARKING;
		List<FacilityMarkerResponseDto> expectedResponse = List.of(
			new FacilityMarkerResponseDto("주차장1", 37.123456, 127.123456, 1L, "PARKING", 1L),
			new FacilityMarkerResponseDto("주차장2", 37.234567, 127.234567, 2L, "PARKING", 2L)
		);

		when(facilityMarkerService.getMarkersByType(type)).thenReturn(expectedResponse);

		// when & then
		mockMvc.perform(get("/facilities")
				.param("type", "PARKING")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(2)))
			.andExpect(jsonPath("$[0].name").value("주차장1"))
			.andExpect(jsonPath("$[0].latitude").value(37.123456))
			.andExpect(jsonPath("$[0].longitude").value(127.123456))
			.andExpect(jsonPath("$[0].buildingId").value(1L))
			.andExpect(jsonPath("$[0].type").value("PARKING"))
			.andExpect(jsonPath("$[0].id").value(1L))
			.andExpect(jsonPath("$[1].name").value("주차장2"))
			.andExpect(jsonPath("$[1].latitude").value(37.234567))
			.andExpect(jsonPath("$[1].longitude").value(127.234567))
			.andExpect(jsonPath("$[1].buildingId").value(2L))
			.andExpect(jsonPath("$[1].type").value("PARKING"))
			.andExpect(jsonPath("$[1].id").value(2L));
	}

	@DisplayName("시설마커조회_성공_식당")
	@Test
	void getMarkersByType_success_restaurant() throws Exception {
		// given
		FacilityType type = FacilityType.RESTAURANT;
		List<FacilityMarkerResponseDto> expectedResponse = List.of(
			new FacilityMarkerResponseDto("식당1", 37.123456, 127.123456, 1L, "RESTAURANT", 1L)
		);

		when(facilityMarkerService.getMarkersByType(type)).thenReturn(expectedResponse);

		// when & then
		mockMvc.perform(get("/facilities")
				.param("type", "RESTAURANT")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].name").value("식당1"))
			.andExpect(jsonPath("$[0].latitude").value(37.123456))
			.andExpect(jsonPath("$[0].longitude").value(127.123456))
			.andExpect(jsonPath("$[0].buildingId").value(1L))
			.andExpect(jsonPath("$[0].type").value("RESTAURANT"))
			.andExpect(jsonPath("$[0].id").value(1L));
	}

	@DisplayName("시설마커조회_성공_카페")
	@Test
	void getMarkersByType_success_convenienceCafe() throws Exception {
		// given
		FacilityType type = FacilityType.CONVENIENCE_CAFE;
		List<FacilityMarkerResponseDto> expectedResponse = List.of(
			new FacilityMarkerResponseDto("카페1", 37.123456, 127.123456, 1L, "CONVENIENCE_CAFE", 1L),
			new FacilityMarkerResponseDto("편의점1", 37.234567, 127.234567, 2L, "CONVENIENCE_CAFE",
				2L),
			new FacilityMarkerResponseDto("카페2", 37.345678, 127.345678, 3L, "CONVENIENCE_CAFE", 3L)
		);

		when(facilityMarkerService.getMarkersByType(type)).thenReturn(expectedResponse);

		// when & then
		mockMvc.perform(get("/facilities")
				.param("type", "CONVENIENCE_CAFE")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(3)))
			.andExpect(jsonPath("$[0].name").value("카페1"))
			.andExpect(jsonPath("$[0].type").value("CONVENIENCE_CAFE"))
			.andExpect(jsonPath("$[1].name").value("편의점1"))
			.andExpect(jsonPath("$[1].type").value("CONVENIENCE_CAFE"))
			.andExpect(jsonPath("$[2].name").value("카페2"))
			.andExpect(jsonPath("$[2].type").value("CONVENIENCE_CAFE"));
	}

	@DisplayName("시설마커조회_성공_셔틀버스")
	@Test
	void getMarkersByType_success_shuttleBus() throws Exception {
		// given
		FacilityType type = FacilityType.SHUTTLE_BUS;
		List<FacilityMarkerResponseDto> expectedResponse = List.of(
			new FacilityMarkerResponseDto("셔틀버스정류장1", 37.123456, 127.123456, 1L, "SHUTTLE_BUS", 1L)
		);

		when(facilityMarkerService.getMarkersByType(type)).thenReturn(expectedResponse);

		// when & then
		mockMvc.perform(get("/facilities")
				.param("type", "SHUTTLE_BUS")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].name").value("셔틀버스정류장1"))
			.andExpect(jsonPath("$[0].type").value("SHUTTLE_BUS"));
	}

	@DisplayName("시설마커조회_성공_프린터")
	@Test
	void getMarkersByType_success_printer() throws Exception {
		// given
		FacilityType type = FacilityType.PRINTER;
		List<FacilityMarkerResponseDto> expectedResponse = List.of(
			new FacilityMarkerResponseDto("프린터1", 37.123456, 127.123456, 1L, "PRINTER", 1L)
		);

		when(facilityMarkerService.getMarkersByType(type)).thenReturn(expectedResponse);

		// when & then
		mockMvc.perform(get("/facilities")
				.param("type", "PRINTER")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].name").value("프린터1"))
			.andExpect(jsonPath("$[0].type").value("PRINTER"));
	}

	@DisplayName("시설마커조회_성공_빈목록")
	@Test
	void getMarkersByType_success_emptyList() throws Exception {
		// given
		FacilityType type = FacilityType.PARKING;
		List<FacilityMarkerResponseDto> expectedResponse = List.of();

		when(facilityMarkerService.getMarkersByType(type)).thenReturn(expectedResponse);

		// when & then
		mockMvc.perform(get("/facilities")
				.param("type", "PARKING")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(0)));
	}

	@DisplayName("시설마커조회_성공_빌딩없음")
	@Test
	void getMarkersByType_success_noBuilding() throws Exception {
		// given
		FacilityType type = FacilityType.PARKING;
		List<FacilityMarkerResponseDto> expectedResponse = List.of(
			new FacilityMarkerResponseDto("주차장1", 37.123456, 127.123456, null, "PARKING", 1L)
		);

		when(facilityMarkerService.getMarkersByType(type)).thenReturn(expectedResponse);

		// when & then
		mockMvc.perform(get("/facilities")
				.param("type", "PARKING")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].name").value("주차장1"))
			.andExpect(jsonPath("$[0].buildingId").isEmpty())
			.andExpect(jsonPath("$[0].type").value("PARKING"));
	}

	@DisplayName("시설마커조회_실패_타입없음")
	@Test
	void getMarkersByType_fail_missingType() throws Exception {
		// when & then
		mockMvc.perform(get("/facilities")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest()); // MissingServletRequestParameterException -> 400
	}

	@DisplayName("시설마커조회_실패_잘못된타입")
	@Test
	void getMarkersByType_fail_invalidType() throws Exception {
		// when & then
		mockMvc.perform(get("/facilities")
				.param("type", "INVALID_TYPE")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest()); // MethodArgumentTypeMismatchException -> 400
	}

	@DisplayName("시설마커조회_실패_서비스예외")
	@Test
	void getMarkersByType_fail_serviceException() throws Exception {
		// given
		FacilityType type = FacilityType.PARKING;

		when(facilityMarkerService.getMarkersByType(type))
			.thenThrow(
				new com.YOGIITSU.exception.validation.InvalidArgumentException("시설 유형은 필수값입니다."));

		// when & then
		mockMvc.perform(get("/facilities")
				.param("type", "PARKING")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());
	}
}
