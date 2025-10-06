package com.YOGIITSU.service;

import com.YOGIITSU.converter.BuildingConverter;
import com.YOGIITSU.dto.ResponseDto.BuildingDetailResponseDto;
import com.YOGIITSU.dto.ResponseDto.BuildingListResponseDto;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

	@DisplayName("건물상세조회_성공_회원_즐겨찾기있음")
	@Test
	void getBuildingDetail_success_member_withFavorite() {
		// given
		Long buildingId = 1L;
		Long memberId = 1L;
		Building building = createDummyBuilding();
		BuildingDetailResponseDto expectedResponse = BuildingDetailResponseDto.builder()
			.isFavorite(true)
			.build();

		when(buildingRepository.findByIdWithAllRelations(buildingId)).thenReturn(
			Optional.of(building));
		when(favoriteRepository.existsByMemberIdAndBuildingId(memberId, buildingId)).thenReturn(
			true);
		when(buildingConverter.convertToBuildingDetailResponseDto(building, true))
			.thenReturn(expectedResponse);

		// when
		BuildingDetailResponseDto result = buildingService.getBuildingDetail(buildingId, memberId);

		// then
		assertNotNull(result);
		assertEquals(expectedResponse, result);
		verify(buildingRepository).findByIdWithAllRelations(buildingId);
		verify(favoriteRepository).existsByMemberIdAndBuildingId(memberId, buildingId);
		verify(buildingConverter).convertToBuildingDetailResponseDto(building, true);
	}

	@DisplayName("간단한_테스트")
	@Test
	void simpleTest() {
		// given
		Long buildingId = 1L;
		Long memberId = 1L;
		Building building = createDummyBuilding();
		BuildingDetailResponseDto expectedResponse = BuildingDetailResponseDto.builder()
			.isFavorite(false)
			.build();

		when(buildingRepository.findByIdWithAllRelations(buildingId)).thenReturn(
			Optional.of(building));
		when(favoriteRepository.existsByMemberIdAndBuildingId(memberId, buildingId)).thenReturn(
			false);
		when(buildingConverter.convertToBuildingDetailResponseDto(building, false))
			.thenReturn(expectedResponse);

		// when
		BuildingDetailResponseDto result = buildingService.getBuildingDetail(buildingId, memberId);

		// then
		assertNotNull(result);
		assertEquals(expectedResponse, result);
	}

	@DisplayName("건물상세조회_성공_회원_즐겨찾기없음")
	@Test
	void getBuildingDetail_success_member_withoutFavorite() {
		// given
		Long buildingId = 1L;
		Long memberId = 1L;
		Building building = createDummyBuilding();
		BuildingDetailResponseDto expectedResponse = BuildingDetailResponseDto.builder()
			.isFavorite(false)
			.build();

		when(buildingRepository.findByIdWithAllRelations(buildingId)).thenReturn(
			Optional.of(building));
		when(favoriteRepository.existsByMemberIdAndBuildingId(memberId, buildingId)).thenReturn(
			false);
		when(buildingConverter.convertToBuildingDetailResponseDto(building, false))
			.thenReturn(expectedResponse);

		// when
		BuildingDetailResponseDto result = buildingService.getBuildingDetail(buildingId, memberId);

		// then
		assertNotNull(result);
		assertEquals(expectedResponse, result);
		verify(buildingRepository).findByIdWithAllRelations(buildingId);
		verify(favoriteRepository).existsByMemberIdAndBuildingId(memberId, buildingId);
		verify(buildingConverter).convertToBuildingDetailResponseDto(building, false);
	}

	@DisplayName("건물상세조회_성공_비회원")
	@Test
	void getBuildingDetail_success_guest() {
		// given
		Long buildingId = 1L;
		Long memberId = null;
		Building building = createDummyBuilding();
		BuildingDetailResponseDto expectedResponse = BuildingDetailResponseDto.builder()
			.isFavorite(false)
			.build();

		when(buildingRepository.findByIdWithAllRelations(buildingId)).thenReturn(
			Optional.of(building));
		when(buildingConverter.convertToBuildingDetailResponseDto(building, false))
			.thenReturn(expectedResponse);

		// when
		BuildingDetailResponseDto result = buildingService.getBuildingDetail(buildingId, memberId);

		// then
		assertNotNull(result);
		assertEquals(expectedResponse, result);
		verify(buildingRepository).findByIdWithAllRelations(buildingId);
		verify(favoriteRepository, never()).existsByMemberIdAndBuildingId(any(), any());
		verify(buildingConverter).convertToBuildingDetailResponseDto(building, false);
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

		verify(buildingRepository).findByIdWithAllRelations(buildingId);
		verify(favoriteRepository, never()).existsByMemberIdAndBuildingId(any(), any());
		verify(buildingConverter, never()).convertToBuildingDetailResponseDto(any(), anyBoolean());
	}

	@DisplayName("전체건물목록조회_성공_회원_즐겨찾기있음")
	@Test
	void getAllBuildings_success_member_withFavorites() {
		// given
		Long memberId = 1L;
		List<BuildingListResponseDto> buildingList = List.of(
			BuildingListResponseDto.builder()
				.buildingId(1L)
				.buildingName("건물1")
				.collegeId(1L)
				.collegeName("단과대1")
				.imageUrl("image1.jpg")
				.favorite(false)
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
		Set<Long> favoriteBuildingIds = Set.of(1L);
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

		when(buildingRepository.findAllSimpleList()).thenReturn(buildingList);
		when(favoriteRepository.findBuildingIdsByMemberId(memberId)).thenReturn(
			favoriteBuildingIds);
		when(buildingConverter.convertToBuildingListResponseDto(buildingList, favoriteBuildingIds))
			.thenReturn(expectedResponse);

		// when
		List<BuildingListResponseDto> result = buildingService.getAllBuildings(memberId);

		// then
		assertNotNull(result);
		assertEquals(2, result.size());
		assertTrue(result.get(0).isFavorite());
		assertFalse(result.get(1).isFavorite());
		verify(buildingRepository).findAllSimpleList();
		verify(favoriteRepository).findBuildingIdsByMemberId(memberId);
		verify(buildingConverter).convertToBuildingListResponseDto(buildingList,
			favoriteBuildingIds);
	}

	@DisplayName("전체건물목록조회_성공_회원_즐겨찾기없음")
	@Test
	void getAllBuildings_success_member_withoutFavorites() {
		// given
		Long memberId = 1L;
		List<BuildingListResponseDto> buildingList = List.of(
			BuildingListResponseDto.builder()
				.buildingId(1L)
				.buildingName("건물1")
				.collegeId(1L)
				.collegeName("단과대1")
				.imageUrl("image1.jpg")
				.favorite(false)
				.build()
		);
		Set<Long> favoriteBuildingIds = Set.of();
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

		when(buildingRepository.findAllSimpleList()).thenReturn(buildingList);
		when(favoriteRepository.findBuildingIdsByMemberId(memberId)).thenReturn(
			favoriteBuildingIds);
		when(buildingConverter.convertToBuildingListResponseDto(buildingList, favoriteBuildingIds))
			.thenReturn(expectedResponse);

		// when
		List<BuildingListResponseDto> result = buildingService.getAllBuildings(memberId);

		// then
		assertNotNull(result);
		assertEquals(1, result.size());
		assertFalse(result.get(0).isFavorite());
		verify(buildingRepository).findAllSimpleList();
		verify(favoriteRepository).findBuildingIdsByMemberId(memberId);
		verify(buildingConverter).convertToBuildingListResponseDto(buildingList,
			favoriteBuildingIds);
	}

	@DisplayName("전체건물목록조회_성공_비회원")
	@Test
	void getAllBuildings_success_guest() {
		// given
		Long memberId = null;
		List<BuildingListResponseDto> buildingList = List.of(
			BuildingListResponseDto.builder()
				.buildingId(1L)
				.buildingName("건물1")
				.collegeId(1L)
				.collegeName("단과대1")
				.imageUrl("image1.jpg")
				.favorite(false)
				.build()
		);
		Set<Long> favoriteBuildingIds = Set.of();
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

		when(buildingRepository.findAllSimpleList()).thenReturn(buildingList);
		when(buildingConverter.convertToBuildingListResponseDto(buildingList, favoriteBuildingIds))
			.thenReturn(expectedResponse);

		// when
		List<BuildingListResponseDto> result = buildingService.getAllBuildings(memberId);

		// then
		assertNotNull(result);
		assertEquals(1, result.size());
		assertFalse(result.get(0).isFavorite());
		verify(buildingRepository).findAllSimpleList();
		verify(favoriteRepository, never()).findBuildingIdsByMemberId(any());
		verify(buildingConverter).convertToBuildingListResponseDto(buildingList,
			favoriteBuildingIds);
	}

	@DisplayName("전체건물목록조회_성공_빈목록")
	@Test
	void getAllBuildings_success_emptyList() {
		// given
		Long memberId = 1L;
		List<BuildingListResponseDto> buildingList = List.of();
		Set<Long> favoriteBuildingIds = Set.of();
		List<BuildingListResponseDto> expectedResponse = List.of();

		when(buildingRepository.findAllSimpleList()).thenReturn(buildingList);
		when(favoriteRepository.findBuildingIdsByMemberId(memberId)).thenReturn(
			favoriteBuildingIds);
		when(buildingConverter.convertToBuildingListResponseDto(buildingList, favoriteBuildingIds))
			.thenReturn(expectedResponse);

		// when
		List<BuildingListResponseDto> result = buildingService.getAllBuildings(memberId);

		// then
		assertNotNull(result);
		assertTrue(result.isEmpty());
		verify(buildingRepository).findAllSimpleList();
		verify(favoriteRepository).findBuildingIdsByMemberId(memberId);
		verify(buildingConverter).convertToBuildingListResponseDto(buildingList,
			favoriteBuildingIds);
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
}
