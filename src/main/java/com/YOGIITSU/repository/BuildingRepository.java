package com.YOGIITSU.repository;

import com.YOGIITSU.dto.ResponseDto.BuildingListResponseDto;
import com.YOGIITSU.entity.Building;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BuildingRepository extends JpaRepository<Building, Long> {

	// 입력된 검색어(query)가 포함된 건물 이름을 최대 6개까지 반환
	List<Building> findTop6ByNameContainingOrderByNameAsc(String query);

	// 건물 ID로 건물 조회
	@Query("SELECT b FROM Building b " +
		"LEFT JOIN FETCH b.buildingTags " +
		"LEFT JOIN FETCH b.buildingFacilities " +
		"LEFT JOIN FETCH b.buildingFloorImages " +
		"LEFT JOIN FETCH b.departments " +
		"WHERE b.id = :id")
	Optional<Building> findByIdWithAllRelations(@Param("id") Long id);

	@Query("SELECT new com.YOGIITSU.dto.ResponseDto.BuildingListResponseDto(" +
		"b.id, b.name, MIN(d.collegeId), MIN(d.collegeName), b.imageUrl) " +
		"FROM Building b LEFT JOIN b.departments d " +
		"GROUP BY b.id, b.name, b.imageUrl " +
		"ORDER BY b.id")
	List<BuildingListResponseDto> findAllSimpleList();
}