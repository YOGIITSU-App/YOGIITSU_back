package com.YOGIITSU.service;

import com.YOGIITSU.dto.ResponseDto.FacilityMarkerResponseDto;
import com.YOGIITSU.entity.FacilityMarker;
import com.YOGIITSU.entity.Building;
import com.YOGIITSU.enums.FacilityType;
import com.YOGIITSU.exception.validation.InvalidArgumentException;
import com.YOGIITSU.repository.FacilityMarkerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FacilityMarkerServiceTest {

	@Mock
	private FacilityMarkerRepository facilityMarkerRepository;

	@InjectMocks
	private FacilityMarkerService facilityMarkerService;

	@DisplayName("시설마커조회_성공_주차장")
	@Test
	void getMarkersByType_success_parking() {
		// given
		FacilityType type = FacilityType.PARKING;
		Building building = createDummyBuilding();
		List<FacilityMarker> facilityMarkers = List.of(
			createDummyFacilityMarker(1L, "주차장1", type, building),
			createDummyFacilityMarker(2L, "주차장2", type, building)
		);

		when(facilityMarkerRepository.findByType(type)).thenReturn(facilityMarkers);

		// when
		List<FacilityMarkerResponseDto> result = facilityMarkerService.getMarkersByType(type);

		// then
		assertNotNull(result);
		assertEquals(2, result.size());

		// 첫 번째 마커 검증
		FacilityMarkerResponseDto firstMarker = result.get(0);
		assertEquals("주차장1", firstMarker.getName());
		assertEquals(37.123456, firstMarker.getLatitude());
		assertEquals(127.123456, firstMarker.getLongitude());
		assertEquals(1L, firstMarker.getBuildingId());
		assertEquals("PARKING", firstMarker.getType());
		assertEquals(1L, firstMarker.getId());

		// 두 번째 마커 검증
		FacilityMarkerResponseDto secondMarker = result.get(1);
		assertEquals("주차장2", secondMarker.getName());
		assertEquals(37.123456, secondMarker.getLatitude());
		assertEquals(127.123456, secondMarker.getLongitude());
		assertEquals(1L, secondMarker.getBuildingId());
		assertEquals("PARKING", secondMarker.getType());
		assertEquals(2L, secondMarker.getId());

		verify(facilityMarkerRepository).findByType(type);
	}

	@DisplayName("시설마커조회_성공_식당")
	@Test
	void getMarkersByType_success_restaurant() {
		// given
		FacilityType type = FacilityType.RESTAURANT;
		Building building = createDummyBuilding();
		List<FacilityMarker> facilityMarkers = List.of(
			createDummyFacilityMarker(1L, "식당1", type, building)
		);

		when(facilityMarkerRepository.findByType(type)).thenReturn(facilityMarkers);

		// when
		List<FacilityMarkerResponseDto> result = facilityMarkerService.getMarkersByType(type);

		// then
		assertNotNull(result);
		assertEquals(1, result.size());

		FacilityMarkerResponseDto marker = result.get(0);
		assertEquals("식당1", marker.getName());
		assertEquals(37.123456, marker.getLatitude());
		assertEquals(127.123456, marker.getLongitude());
		assertEquals(1L, marker.getBuildingId());
		assertEquals("RESTAURANT", marker.getType());
		assertEquals(1L, marker.getId());

		verify(facilityMarkerRepository).findByType(type);
	}

	@DisplayName("시설마커조회_성공_카페")
	@Test
	void getMarkersByType_success_convenienceCafe() {
		// given
		FacilityType type = FacilityType.CONVENIENCE_CAFE;
		Building building = createDummyBuilding();
		List<FacilityMarker> facilityMarkers = List.of(
			createDummyFacilityMarker(1L, "카페1", type, building),
			createDummyFacilityMarker(2L, "편의점1", type, building),
			createDummyFacilityMarker(3L, "카페2", type, building)
		);

		when(facilityMarkerRepository.findByType(type)).thenReturn(facilityMarkers);

		// when
		List<FacilityMarkerResponseDto> result = facilityMarkerService.getMarkersByType(type);

		// then
		assertNotNull(result);
		assertEquals(3, result.size());

		// 모든 마커가 올바른 타입인지 확인
		for (FacilityMarkerResponseDto marker : result) {
			assertEquals("CONVENIENCE_CAFE", marker.getType());
			assertNotNull(marker.getName());
			assertNotNull(marker.getLatitude());
			assertNotNull(marker.getLongitude());
			assertNotNull(marker.getBuildingId());
			assertNotNull(marker.getId());
		}

		verify(facilityMarkerRepository).findByType(type);
	}

	@DisplayName("시설마커조회_성공_셔틀버스")
	@Test
	void getMarkersByType_success_shuttleBus() {
		// given
		FacilityType type = FacilityType.SHUTTLE_BUS;
		Building building = createDummyBuilding();
		List<FacilityMarker> facilityMarkers = List.of(
			createDummyFacilityMarker(1L, "셔틀버스정류장1", type, building)
		);

		when(facilityMarkerRepository.findByType(type)).thenReturn(facilityMarkers);

		// when
		List<FacilityMarkerResponseDto> result = facilityMarkerService.getMarkersByType(type);

		// then
		assertNotNull(result);
		assertEquals(1, result.size());

		FacilityMarkerResponseDto marker = result.get(0);
		assertEquals("셔틀버스정류장1", marker.getName());
		assertEquals("SHUTTLE_BUS", marker.getType());

		verify(facilityMarkerRepository).findByType(type);
	}

	@DisplayName("시설마커조회_성공_프린터")
	@Test
	void getMarkersByType_success_printer() {
		// given
		FacilityType type = FacilityType.PRINTER;
		Building building = createDummyBuilding();
		List<FacilityMarker> facilityMarkers = List.of(
			createDummyFacilityMarker(1L, "프린터1", type, building)
		);

		when(facilityMarkerRepository.findByType(type)).thenReturn(facilityMarkers);

		// when
		List<FacilityMarkerResponseDto> result = facilityMarkerService.getMarkersByType(type);

		// then
		assertNotNull(result);
		assertEquals(1, result.size());

		FacilityMarkerResponseDto marker = result.get(0);
		assertEquals("프린터1", marker.getName());
		assertEquals("PRINTER", marker.getType());

		verify(facilityMarkerRepository).findByType(type);
	}

	@DisplayName("시설마커조회_성공_빈목록")
	@Test
	void getMarkersByType_success_emptyList() {
		// given
		FacilityType type = FacilityType.PARKING;
		List<FacilityMarker> facilityMarkers = List.of();

		when(facilityMarkerRepository.findByType(type)).thenReturn(facilityMarkers);

		// when
		List<FacilityMarkerResponseDto> result = facilityMarkerService.getMarkersByType(type);

		// then
		assertNotNull(result);
		assertTrue(result.isEmpty());

		verify(facilityMarkerRepository).findByType(type);
	}

	@DisplayName("시설마커조회_성공_빌딩없음")
	@Test
	void getMarkersByType_success_noBuilding() {
		// given
		FacilityType type = FacilityType.PARKING;
		FacilityMarker facilityMarker = FacilityMarker.builder()
			.id(1L)
			.placeName("주차장1")
			.type(type)
			.latitude(37.123456)
			.longitude(127.123456)
			.building(null) // 빌딩이 없는 경우
			.build();

		List<FacilityMarker> facilityMarkers = List.of(facilityMarker);

		when(facilityMarkerRepository.findByType(type)).thenReturn(facilityMarkers);

		// when
		List<FacilityMarkerResponseDto> result = facilityMarkerService.getMarkersByType(type);

		// then
		assertNotNull(result);
		assertEquals(1, result.size());

		FacilityMarkerResponseDto marker = result.get(0);
		assertEquals("주차장1", marker.getName());
		assertNull(marker.getBuildingId()); // 빌딩 ID가 null이어야 함
		assertEquals("PARKING", marker.getType());

		verify(facilityMarkerRepository).findByType(type);
	}

	@DisplayName("시설마커조회_실패_타입없음")
	@Test
	void getMarkersByType_fail_nullType() {
		// given
		FacilityType type = null;

		// when, then
		assertThrows(InvalidArgumentException.class,
			() -> facilityMarkerService.getMarkersByType(type));

		verify(facilityMarkerRepository, never()).findByType(any());
	}

	private Building createDummyBuilding() {
		return Building.builder()
			.id(1L)
			.name("테스트건물")
			.latitude(37.123456)
			.longitude(127.123456)
			.imageUrl("test.jpg")
			.build();
	}

	private FacilityMarker createDummyFacilityMarker(Long id, String placeName, FacilityType type,
		Building building) {
		return FacilityMarker.builder()
			.id(id)
			.placeName(placeName)
			.type(type)
			.latitude(37.123456)
			.longitude(127.123456)
			.building(building)
			.build();
	}
}

