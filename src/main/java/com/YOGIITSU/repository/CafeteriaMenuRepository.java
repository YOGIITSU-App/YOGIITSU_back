package com.YOGIITSU.repository;

import com.YOGIITSU.entity.CafeteriaMenu;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CafeteriaMenuRepository extends JpaRepository<CafeteriaMenu, Long> {

	List<CafeteriaMenu> findByCafeteriaIdInAndMealDateBetweenOrderByMealDateAscMealTypeAscCornerAsc(
		List<Long> cafeteriaIds, LocalDate start, LocalDate end
	);

	Optional<CafeteriaMenu> findByCafeteriaIdAndMealDateAndMealTypeAndCorner(
		Long cafeteriaId, LocalDate mealDate, String mealType, String corner
	);

	// 과거 corner가 NULL로 저장된 레코드 보정용
	Optional<CafeteriaMenu> findByCafeteriaIdAndMealDateAndMealTypeAndCornerIsNull(
		Long cafeteriaId, LocalDate mealDate, String mealType
	);

	// 최신 주차만 남기고 나머지 삭제
	int deleteByVersionWeekNot(LocalDate versionWeek);
}