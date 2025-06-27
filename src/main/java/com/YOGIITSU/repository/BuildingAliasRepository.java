package com.YOGIITSU.repository;

import com.YOGIITSU.entity.BuildingAlias;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuildingAliasRepository extends JpaRepository<BuildingAlias, Long> {

	// 입력된 검색어(query)가 포함된 별칭을 반환
	List<BuildingAlias> findByAliasContaining(String alias);

	// 입력된 검색어가 포함된 별칭 중 가장 빠른 ID 순으로 하나 반환
	Optional<BuildingAlias> findFirstByAliasContainingOrderByIdAsc(String alias);
}
