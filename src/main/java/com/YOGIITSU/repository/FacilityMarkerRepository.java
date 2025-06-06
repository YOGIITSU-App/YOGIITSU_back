package com.YOGIITSU.repository;

import com.YOGIITSU.entity.FacilityMarker;
import com.YOGIITSU.enums.FacilityType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacilityMarkerRepository extends JpaRepository<FacilityMarker, Long> {

	List<FacilityMarker> findByType(FacilityType type);
}