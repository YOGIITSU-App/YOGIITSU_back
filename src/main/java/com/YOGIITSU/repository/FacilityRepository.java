package com.YOGIITSU.repository;

import com.YOGIITSU.entity.BuildingFacility;
import com.YOGIITSU.repository.projection.FacilityLocationView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FacilityRepository extends JpaRepository<BuildingFacility, Long> {

	// (기존) 단일 이름 정확히 일치 - floor 문자열 정규화 추가
	@Query(value = """
		    SELECT b.name AS buildingName,
		           NULLIF(NULLIF(bf.floor, 'NULL'), '') AS floor   -- ★ CHANGED: "NULL"/'' → NULL
		    FROM building_facilities bf
		    JOIN buildings b ON b.id = bf.building_id
		    WHERE bf.name = :name
		    ORDER BY b.name ASC, CAST(bf.floor AS SIGNED) ASC
		""", nativeQuery = true)
	List<FacilityLocationView> findLocationsByName(@Param("name") String name);

	// (기존) 단일 이름 + 특정 건물 - floor 문자열 정규화 추가
	@Query(value = """
		    SELECT b.name AS buildingName,
		           NULLIF(NULLIF(bf.floor, 'NULL'), '') AS floor   -- ★ CHANGED
		    FROM building_facilities bf
		    JOIN buildings b ON b.id = bf.building_id
		    WHERE bf.name = :name
		      AND bf.building_id = :buildingId
		    ORDER BY CAST(bf.floor AS SIGNED) ASC
		""", nativeQuery = true)
	List<FacilityLocationView> findLocationsByNameAndBuilding(@Param("name") String name,
		@Param("buildingId") Long buildingId);

	// (추가) 여러 이름(IN) 조회 - floor 정규화 포함
	@Query(value = """
		    SELECT b.name AS buildingName,
		           NULLIF(NULLIF(bf.floor, 'NULL'), '') AS floor   -- ★ CHANGED
		    FROM building_facilities bf
		    JOIN buildings b ON b.id = bf.building_id
		    WHERE bf.name IN (:names)
		    ORDER BY b.name ASC, CAST(bf.floor AS SIGNED) ASC
		""", nativeQuery = true)
	List<FacilityLocationView> findLocationsByNames(@Param("names") List<String> names);

	// (추가) 여러 이름(IN) + 특정 건물 - floor 정규화 포함
	@Query(value = """
		    SELECT b.name AS buildingName,
		           NULLIF(NULLIF(bf.floor, 'NULL'), '') AS floor   -- ★ CHANGED
		    FROM building_facilities bf
		    JOIN buildings b ON b.id = bf.building_id
		    WHERE bf.name IN (:names)
		      AND bf.building_id = :buildingId
		    ORDER BY CAST(bf.floor AS SIGNED) ASC
		""", nativeQuery = true)
	List<FacilityLocationView> findLocationsByNamesAndBuilding(@Param("names") List<String> names,
		@Param("buildingId") Long buildingId);

	// ★ ADDED: 대표 표기(IN) + 접두어 정규식(REGEXP) 동시 지원 (전체)
	@Query(value = """
		    SELECT b.name AS buildingName,
		           NULLIF(NULLIF(bf.floor, 'NULL'), '') AS floor   -- 문자열 'NULL' 제거
		    FROM building_facilities bf
		    JOIN buildings b ON b.id = bf.building_id
		    WHERE (bf.name IN (:names) OR bf.name REGEXP :prefixPattern)
		    ORDER BY b.name ASC, CAST(bf.floor AS SIGNED) ASC
		""", nativeQuery = true)
	List<FacilityLocationView> findLocationsByNamesOrPrefix(@Param("names") List<String> names,
		@Param("prefixPattern") String prefixPattern);

	// ★ ADDED: 대표 표기(IN) + 접두어 정규식(REGEXP) 동시 지원 (특정 건물)
	@Query(value = """
		    SELECT b.name AS buildingName,
		           NULLIF(NULLIF(bf.floor, 'NULL'), '') AS floor   -- 문자열 'NULL' 제거
		    FROM building_facilities bf
		    JOIN buildings b ON b.id = bf.building_id
		    WHERE (bf.name IN (:names) OR bf.name REGEXP :prefixPattern)
		      AND bf.building_id = :buildingId
		    ORDER BY CAST(bf.floor AS SIGNED) ASC
		""", nativeQuery = true)
	List<FacilityLocationView> findLocationsByNamesOrPrefixAndBuilding(
		@Param("names") List<String> names,
		@Param("prefixPattern") String prefixPattern,
		@Param("buildingId") Long buildingId);
}