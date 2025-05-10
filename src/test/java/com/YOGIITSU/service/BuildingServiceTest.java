package com.YOGIITSU.service;

import com.YOGIITSU.converter.BuildingConverter;
import com.YOGIITSU.dto.ResponseDto.BuildingDetailResponseDto;
import com.YOGIITSU.entity.Building;
import com.YOGIITSU.exception.building.BuildingNotFoundException;
import com.YOGIITSU.repository.BuildingRepository;
import com.YOGIITSU.repository.FavoriteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BuildingServiceTest {

	@Mock
	private BuildingRepository buildingRepository;

	@Mock
	private FavoriteRepository favoriteRepository;

	@Mock
	private BuildingConverter buildingConverter;

	@InjectMocks
	private BuildingService buildingService;

	@DisplayName("건물상세조회_성공")
	@Test
	void getBuildingDetail_success() {
		// given
		Long buildingId = 1L;
		Long memberId = 1L;
		Building building = createDummyBuilding();
		when(buildingRepository.findByIdWithAllRelations(buildingId)).thenReturn(
			Optional.of(building));
		when(favoriteRepository.existsByMemberIdAndBuildingId(memberId, buildingId)).thenReturn(
			true);
		when(buildingConverter.convertToBuildingDetailResponseDto(building, true))
			.thenReturn(BuildingDetailResponseDto.builder().build());

		// when
		BuildingDetailResponseDto result = buildingService.getBuildingDetail(buildingId, memberId);

		// then
		assertNotNull(result);
	}

	@DisplayName("건물상세조회_실패_건물없음")
	@Test
	void getBuildingDetail_fail_buildingNotFound() {
		// given
		Long buildingId = 999L;
		Long memberId = 1L;
		when(buildingRepository.findByIdWithAllRelations(buildingId)).thenReturn(Optional.empty());

		// when, then
		assertThrows(BuildingNotFoundException.class,
			() -> buildingService.getBuildingDetail(buildingId, memberId));
	}

	private Building createDummyBuilding() {
		return Building.builder()
			.name("테스트건물")
			.build();
	}
}
