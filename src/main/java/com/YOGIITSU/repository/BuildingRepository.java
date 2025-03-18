package com.YOGIITSU.repository;

import com.YOGIITSU.entity.Building;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingRepository extends JpaRepository<Building, Long> {

	// 입력된 검색어(query)가 포함된 건물 이름을 최대 6개까지 반환
	List<Building> findTop6ByNameContainingOrderByNameAsc(String query);

}