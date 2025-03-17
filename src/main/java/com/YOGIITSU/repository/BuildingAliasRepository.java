package com.YOGIITSU.repository;

import com.YOGIITSU.entity.BuildingAlias;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuildingAliasRepository extends JpaRepository<BuildingAlias, Long> {

	// 입력된 검색어(query)가 포함된 별칭을 반환
	List<BuildingAlias> findByAliasContaining(String alias);
}
