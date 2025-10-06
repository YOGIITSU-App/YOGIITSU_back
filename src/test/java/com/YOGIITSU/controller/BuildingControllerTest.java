package com.YOGIITSU.controller;

import com.YOGIITSU.dto.ResponseDto.BuildingDetailResponseDto;
import com.YOGIITSU.dto.ResponseDto.BuildingListResponseDto;
import com.YOGIITSU.service.BuildingService;
import com.YOGIITSU.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
	controllers = BuildingController.class,
	excludeAutoConfiguration = {
		SecurityAutoConfiguration.class,
		UserDetailsServiceAutoConfiguration.class,
		OAuth2ClientAutoConfiguration.class,
		OAuth2ResourceServerAutoConfiguration.class
	}
)
class BuildingControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private BuildingService buildingService;

	@MockBean
	private JwtUtil jwtUtil;

	@DisplayName("건물상세조회_성공_회원")
	@Test
	@WithMockUser
	void getBuildingDetail_success_member() throws Exception {
		// given
		Long buildingId = 1L;
		Long memberId = 1L;
		BuildingDetailResponseDto expectedResponse = BuildingDetailResponseDto.builder()
			.isFavorite(true)
			.build();

		when(jwtUtil.extractMemberIdSafely(any(HttpServletRequest.class))).thenReturn(memberId);
		when(buildingService.getBuildingDetail(buildingId, memberId)).thenReturn(expectedResponse);

		// when & then
		mockMvc.perform(get("/buildings/{id}", buildingId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.favorite").value(true));
	}

	@DisplayName("건물상세조회_성공_비회원")
	@Test
	void getBuildingDetail_success_guest() throws Exception {
		// given
		Long buildingId = 1L;
		BuildingDetailResponseDto expectedResponse = BuildingDetailResponseDto.builder()
			.isFavorite(false)
			.build();

		when(jwtUtil.extractMemberIdSafely(any(HttpServletRequest.class))).thenReturn(null);
		when(buildingService.getBuildingDetail(buildingId, null)).thenReturn(expectedResponse);

		// when & then
		mockMvc.perform(get("/buildings/{id}", buildingId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.favorite").value(false));
	}

	@DisplayName("건물상세조회_실패_건물없음")
	@Test
	@WithMockUser
	void getBuildingDetail_fail_buildingNotFound() throws Exception {
		// given
		Long buildingId = 999L;
		Long memberId = 1L;

		when(jwtUtil.extractMemberIdSafely(any(HttpServletRequest.class))).thenReturn(memberId);
		when(buildingService.getBuildingDetail(buildingId, memberId))
			.thenThrow(new com.YOGIITSU.exception.building.BuildingNotFoundException(buildingId));

		// when & then
		mockMvc.perform(get("/buildings/{id}", buildingId))
			.andExpect(status().isNotFound());
	}

	@DisplayName("전체건물목록조회_성공_회원")
	@Test
	@WithMockUser
	void getAllBuildings_success_member() throws Exception {
		// given
		Long memberId = 1L;
		List<BuildingListResponseDto> expectedResponse = List.of(
			BuildingListResponseDto.builder()
				.buildingId(1L)
				.buildingName("건물1")
				.collegeId(1L)
				.collegeName("단과대1")
				.imageUrl("image1.jpg")
				.favorite(true)
				.build(),
			BuildingListResponseDto.builder()
				.buildingId(2L)
				.buildingName("건물2")
				.collegeId(2L)
				.collegeName("단과대2")
				.imageUrl("image2.jpg")
				.favorite(false)
				.build()
		);

		when(jwtUtil.extractMemberIdSafely(any(HttpServletRequest.class))).thenReturn(memberId);
		when(buildingService.getAllBuildings(memberId)).thenReturn(expectedResponse);

		// when & then
		mockMvc.perform(get("/buildings"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(2)))
			.andExpect(jsonPath("$[0].buildingId").value(1L))
			.andExpect(jsonPath("$[0].buildingName").value("건물1"))
			.andExpect(jsonPath("$[0].isFavorite").value(true))
			.andExpect(jsonPath("$[1].buildingId").value(2L))
			.andExpect(jsonPath("$[1].buildingName").value("건물2"))
			.andExpect(jsonPath("$[1].isFavorite").value(false));
	}

	@DisplayName("전체건물목록조회_성공_비회원")
	@Test
	void getAllBuildings_success_guest() throws Exception {
		// given
		List<BuildingListResponseDto> expectedResponse = List.of(
			BuildingListResponseDto.builder()
				.buildingId(1L)
				.buildingName("건물1")
				.collegeId(1L)
				.collegeName("단과대1")
				.imageUrl("image1.jpg")
				.favorite(false)
				.build()
		);

		when(jwtUtil.extractMemberIdSafely(any(HttpServletRequest.class))).thenReturn(null);
		when(buildingService.getAllBuildings(null)).thenReturn(expectedResponse);

		// when & then
		mockMvc.perform(get("/buildings"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].buildingId").value(1L))
			.andExpect(jsonPath("$[0].buildingName").value("건물1"))
			.andExpect(jsonPath("$[0].isFavorite").value(false));
	}

	@DisplayName("전체건물목록조회_성공_빈목록")
	@Test
	@WithMockUser
	void getAllBuildings_success_emptyList() throws Exception {
		// given
		Long memberId = 1L;
		List<BuildingListResponseDto> expectedResponse = List.of();

		when(jwtUtil.extractMemberIdSafely(any(HttpServletRequest.class))).thenReturn(memberId);
		when(buildingService.getAllBuildings(memberId)).thenReturn(expectedResponse);

		// when & then
		mockMvc.perform(get("/buildings"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(0)));
	}
}