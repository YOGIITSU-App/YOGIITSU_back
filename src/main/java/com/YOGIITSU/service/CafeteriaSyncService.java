package com.YOGIITSU.service;

import com.YOGIITSU.entity.Cafeteria;
import com.YOGIITSU.entity.CafeteriaMenu;
import com.YOGIITSU.repository.CafeteriaMenuRepository;
import com.YOGIITSU.repository.CafeteriaRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.YOGIITSU.exception.resource.ResourceException;
import com.YOGIITSU.exception.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class CafeteriaSyncService {

	private final BuildingLookupService buildingLookup;
	private final CafeteriaRepository cafeteriaRepo;
	private final CafeteriaMenuRepository menuRepo;

	@Transactional
	public void sync(List<CafeteriaCrawler.ParsedRow> rows) {
		if (rows == null || rows.isEmpty()) {
			log.warn("[식단] 이번 실행에서 수집된 행이 없습니다. 삭제 정리는 수행하지 않습니다.");
			return;
		}

		// 이번 동기화에서 감지된 "가장 최신 주차"를 기준으로 유지
		LocalDate latestWeek = rows.stream()
			.map(CafeteriaCrawler.ParsedRow::versionWeek)
			.max(LocalDate::compareTo)
			.orElseThrow();

		int upserts = 0;
		int skipped = 0;

		for (var r : rows) {
			try {
				Long buildingId = buildingLookup.findBuildingIdByName(r.building())
					.orElseThrow(() ->
						new ResourceException(ErrorCode.BUILDING_NOT_FOUND, "building=" + r.building()));

				Cafeteria cafeteria = cafeteriaRepo.findByBuildingIdAndName(buildingId, r.placeName())
					.orElseGet(() -> cafeteriaRepo.save(
						Cafeteria.builder().buildingId(buildingId).name(r.placeName()).build()
					));

				// corner NOT NULL 스키마 대응: null/blank → ""
				String corner = (r.corner() == null || r.corner().isBlank()) ? "" : r.corner().trim();
				String mealType = (r.mealType() == null) ? "" : r.mealType().trim();
				String itemsText = (r.itemsText() == null) ? "" : r.itemsText();

				// 기존 행 조회 (corner = "" 우선)
				var existing = menuRepo.findByCafeteriaIdAndMealDateAndMealTypeAndCorner(
					cafeteria.getId(), r.mealDate(), mealType, corner
				);

				// 과거 데이터에 corner가 NULL로 들어간 경우까지 보정
				if (existing.isEmpty() && corner.isEmpty()) {
					existing = menuRepo.findByCafeteriaIdAndMealDateAndMealTypeAndCornerIsNull(
						cafeteria.getId(), r.mealDate(), mealType
					);
				}

				if (existing.isPresent()) {
					var ex = existing.get();

					// 기존 NULL corner를 ""로 정규화(키 충돌 없음)
					ex.normalizeCornerIfNull();

					boolean changed =
						!itemsText.equals(ex.getItemsText()) ||
							!r.hashBody().equals(ex.getHashBody()) ||
							!r.versionWeek().equals(ex.getVersionWeek());

					if (changed) {
						ex.updateMenu(itemsText, r.hashBody(), r.versionWeek());
						upserts++;
					}
				} else {
					menuRepo.save(CafeteriaMenu.builder()
						.cafeteriaId(cafeteria.getId())
						.mealDate(r.mealDate())
						.mealType(mealType)
						.corner(corner)      // 항상 NOT NULL(빈 문자열 포함)로 저장
						.itemsText(itemsText)
						.versionWeek(r.versionWeek())
						.hashBody(r.hashBody())
						.build());
					upserts++;
				}

			} catch (ResourceException re) {
				skipped++;
				log.warn("[식단] 동기화 스킵(리소스 오류): building='{}', place='{}', date={}, detail={}",
					r.building(), r.placeName(), r.mealDate(), re.getDetailMessage());

			} catch (RuntimeException e) {
				skipped++;
				log.warn("[식단] 동기화 스킵(행 처리 오류): building='{}', place='{}', date={}, msg={}",
					r.building(), r.placeName(), r.mealDate(), e.getMessage());
			}
		}

		// 업서트가 끝난 뒤, 최신 주차 이외의 데이터는 모두 정리
		int deleted = menuRepo.deleteByVersionWeekNot(latestWeek);

		log.info("[식단] 동기화 완료: upsert {}건, 삭제 {}건, 스킵 {}건, 유지 주차={}", upserts, deleted, skipped, latestWeek);
	}
}