package com.YOGIITSU.service;

import com.YOGIITSU.converter.BuildingConverter;
import com.YOGIITSU.dto.ResponseDto.BuildingDetailResponseDto;
import com.YOGIITSU.dto.ResponseDto.BuildingListResponseDto;
import com.YOGIITSU.entity.Building;
import com.YOGIITSU.exception.building.BuildingNotFoundException;
import com.YOGIITSU.repository.BuildingRepository;
import com.YOGIITSU.repository.FavoriteRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BuildingService {

	private final BuildingRepository buildingRepository;
	private final FavoriteRepository favoriteRepository;
	private final BuildingConverter buildingConverter;

	/**
	 * 건물 상세 정보를 조회하는 메서드
	 *
	 * @param buildingId 조회할 건물의 ID
	 * @param memberId   조회하는 사용자의 ID (즐겨찾기 여부 확인용)
	 * @return 건물의 상세 정보를 담은 BuildingDetailResponseDto
	 */
	@Transactional(readOnly = true)
	public BuildingDetailResponseDto getBuildingDetail(Long buildingId, Long memberId) {
		Building building = findBuildingById(buildingId);

		//즐겨찾기 여부 조회
		boolean isFavorite = favoriteRepository.existsByMemberIdAndBuildingId(memberId, buildingId);

		return buildingConverter.convertToBuildingDetailResponseDto(building, isFavorite);
	}

	/**
	 * 건물 ID로 건물을 조회하는 메서드
	 *
	 * @param buildingId 조회할 건물의 ID
	 * @return 조회된 Building 엔티티
	 * @throws BuildingNotFoundException 건물이 존재하지 않을 경우 발생하는 예외
	 */
	private Building findBuildingById(Long buildingId) {
		return buildingRepository.findByIdWithAllRelations(buildingId)
			.orElseThrow(() -> new BuildingNotFoundException(buildingId));
	}

	/**
	 * 모든 건물의 간단한 정보를 조회하는 메서드
	 *
	 * @return 모든 건물의 간단한 정보를 담은 BuildingListResponseDto 리스트
	 */
	@Transactional(readOnly = true)
	public List<BuildingListResponseDto> getAllBuildings() {
		return buildingRepository.findAllSimpleList();
	}
}
