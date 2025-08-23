package com.YOGIITSU.service.handler.impl;

import com.YOGIITSU.enums.FacilityType;
import com.YOGIITSU.repository.FacilityRepository;
import com.YOGIITSU.repository.projection.FacilityLocationView;
import com.YOGIITSU.service.handler.DynamicResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FacilityHandler implements DynamicResponseHandler {

	private final FacilityRepository facilityRepository;

	@Override
	public boolean supports(String key) {
		return key != null && (key.startsWith("FACILITY_") || key.startsWith(
			"FACILITY_BY_BUILDING"));
	}

	@Override
	public String buildRawAnswer(Long nodeId, String key, Map<String, Object> ctx) {
		log.debug("[FacilityHandler] nodeId={}, key={}, ctx={}", nodeId, key, ctx);

		FacilityType type;
		List<FacilityLocationView> list;

		if (key.startsWith("FACILITY_BY_BUILDING")) {
			String[] parts = key.split(":");
			if (parts.length < 2) {
				throw new IllegalArgumentException("잘못된 key 형식: " + key);
			}

			type = FacilityType.valueOf(parts[1]);
			Long buildingId = (Long) ctx.get("buildingId");
			if (buildingId == null) {
				throw new IllegalArgumentException("buildingId가 필요합니다.");
			}

			// 대표 표기 + 접두어 정규식으로 조회
			var ko = koQuery(type);
			if (ko.prefixPattern() != null) {
				list = facilityRepository.findLocationsByNamesOrPrefixAndBuilding(
					ko.names(), ko.prefixPattern(), buildingId
				);
			} else if (ko.names().size() == 1) {
				list = facilityRepository.findLocationsByNameAndBuilding(ko.names().get(0),
					buildingId);
			} else {
				list = facilityRepository.findLocationsByNamesAndBuilding(ko.names(), buildingId);
			}
		} else {
			// 형식: FACILITY_CONVENIENCE_CAFE
			type = FacilityType.valueOf(key.replace("FACILITY_", ""));

			// 대표 표기 + 접두어 정규식으로 조회
			var ko = koQuery(type);
			if (ko.prefixPattern() != null) {
				list = facilityRepository.findLocationsByNamesOrPrefix(ko.names(),
					ko.prefixPattern());
			} else if (ko.names().size() == 1) {
				list = facilityRepository.findLocationsByName(ko.names().get(0));
			} else {
				list = facilityRepository.findLocationsByNames(ko.names());
			}
		}

		return toRaw(type, list);
	}

	private String toRaw(FacilityType type, List<FacilityLocationView> list) {
		String label = nameKoLabel(type);

		if (list == null || list.isEmpty()) {
			return "시설종류: " + label + "\n" +
				"개수: 0\n" +
				"목록: (없음)";
		}

		int limit = 12;
		List<String> items = list.stream()
			.limit(limit)
			.map(v -> v.getBuildingName() + normalizeFloor(v.getFloor()))
			.toList();

		String more = list.size() > limit ? " (외 " + (list.size() - limit) + "곳)" : "";

		return "시설종류: " + label + "\n" +
			"개수: " + list.size() + "\n" +
			"목록:\n- " + String.join("\n- ", items) + more;
	}

	// 층 문자열 정규화(문자열 'NULL', '-', 공백 등 숨김)
	private String normalizeFloor(String floor) {
		if (floor == null) {
			return "";
		}
		String f = floor.trim();
		if (f.isEmpty()) {
			return "";
		}
		if ("NULL".equalsIgnoreCase(f) || "-".equals(f) || "미상".equals(f)) {
			return "";
		}
		return " " + f; // 보기 좋게 앞 공백
	}

	// 사용자에게 보여줄 대표 라벨(단일)
	private String nameKoLabel(FacilityType t) {
		return switch (t) {
			case PARKING -> "주차장";
			case RESTAURANT -> "식당";
			case CONVENIENCE_CAFE -> "편의점·카페";
			case PRINTER -> "프린터기";
			case READING_ROOM -> "열람실";
			case VENDING_MACHINE -> "자판기";
			case ELEVATOR -> "엘리베이터";
			case STUDY_ROOM -> "스터디룸";
			case GYM -> "헬스장";
			case DELIVERY_BOX -> "무인택배함";
			default -> t.name();
		};
	}

	// 실제 조회에 사용할 대표 표기 + 접두어 정규식
	// names: IN 매칭용 표준 표기들
	// prefixPattern: REGEXP '^(편의점|카페)' 처럼 접두어 매칭(브랜드 괄호 포함)
	private KoQuery koQuery(FacilityType t) {
		if (t == FacilityType.CONVENIENCE_CAFE) {
			return new KoQuery(
				List.of("편의점·카페", "편의점/카페", "편의점", "카페"),
				"^(편의점|카페)"
			);
		} else {
			return new KoQuery(List.of(nameKoLabel(t)), null);
		}
	}

	// 간단한 record로 묶어 관리
	private record KoQuery(List<String> names, String prefixPattern) {

	}
}