package com.YOGIITSU.repository;

import com.YOGIITSU.entity.Cafeteria;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CafeteriaRepository extends JpaRepository<Cafeteria, Long> {

	List<Cafeteria> findByBuildingId(Long buildingId);

	Optional<Cafeteria> findByBuildingIdAndName(Long buildingId, String name);
}