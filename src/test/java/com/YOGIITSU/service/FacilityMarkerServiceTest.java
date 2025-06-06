package com.YOGIITSU.service;

import com.YOGIITSU.dto.ResponseDto.FacilityMarkerResponseDto;
import com.YOGIITSU.entity.FacilityMarker;
import com.YOGIITSU.enums.FacilityType;
import com.YOGIITSU.repository.FacilityMarkerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FacilityMarkerServiceTest {

	@Mock
	private FacilityMarkerRepository facilityMarkerRepository;

	@InjectMocks
	private FacilityMarkerService facilityMarkerService;

	@DisplayName("시설유형_마커조회_성공")
	@Test
	void getMarkersByType_success() {
		// given
		FacilityType type = FacilityType.CONVENIENCE_CAFE;

		FacilityMarker marker1 = FacilityMarker.builder()
			.placeName("종강 카페")
			.latitude(37.2093280)
			.longitude(126.978640)
			.type(type)
			.build();

		FacilityMarker marker2 = FacilityMarker.builder()
			.placeName("인문대 CU")
			.latitude(37.2113740)
			.longitude(126.979640)
			.type(type)
			.build();

		when(facilityMarkerRepository.findByType(type)).thenReturn(List.of(marker1, marker2));

		// when
		List<FacilityMarkerResponseDto> result = facilityMarkerService.getMarkersByType(type);

		// then
		assertEquals(2, result.size());
		assertEquals("종강 카페", result.get(0).getName());
		assertEquals(126.979640, result.get(1).getLongitude());
	}

	@DisplayName("시설유형_마커없음_빈리스트반환")
	@Test
	void getMarkersByType_emptyList() {
		// given
		FacilityType type = FacilityType.RESTAURANT;
		when(facilityMarkerRepository.findByType(type)).thenReturn(Collections.emptyList());

		// when
		List<FacilityMarkerResponseDto> result = facilityMarkerService.getMarkersByType(type);

		// then
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@DisplayName("시설유형_null_예외발생")
	@Test
	void getMarkersByType_nullType_throwsException() {
		// given & when & then
		assertThrows(IllegalArgumentException.class, () -> {
			facilityMarkerService.getMarkersByType(null);
		});
	}

	@DisplayName("시설유형_중복마커_정상반환")
	@Test
	void getMarkersByType_duplicateMarkers() {
		// given
		FacilityType type = FacilityType.PARKING;

		FacilityMarker marker1 = FacilityMarker.builder()
			.placeName("음대 주차장")
			.latitude(37.210000)
			.longitude(126.970000)
			.type(type)
			.build();

		FacilityMarker marker2 = FacilityMarker.builder()
			.placeName("음대 주차장") // 중복 이름
			.latitude(37.210001)
			.longitude(126.970001)
			.type(type)
			.build();

		when(facilityMarkerRepository.findByType(type)).thenReturn(List.of(marker1, marker2));

		// when
		List<FacilityMarkerResponseDto> result = facilityMarkerService.getMarkersByType(type);

		// then
		assertEquals(2, result.size());
		assertEquals("음대 주차장", result.get(0).getName());
		assertEquals("음대 주차장", result.get(1).getName());
	}
}
