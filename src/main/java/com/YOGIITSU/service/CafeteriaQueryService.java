package com.YOGIITSU.service;

import com.YOGIITSU.dto.ResponseDto.WeeklyCafeteriaResponseDto;
import com.YOGIITSU.dto.ResponseDto.WeeklyCafeteriaResponseDto.CafeteriaMenuWithIndexDto;
import com.YOGIITSU.entity.Cafeteria;
import com.YOGIITSU.entity.CafeteriaMenu;
import com.YOGIITSU.exception.building.BuildingNotFoundException;
import com.YOGIITSU.exception.validation.InvalidArgumentException;
import com.YOGIITSU.repository.CafeteriaMenuRepository;
import com.YOGIITSU.repository.CafeteriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CafeteriaQueryService {

	private final CafeteriaRepository cafeteriaRepo;
	private final CafeteriaMenuRepository menuRepo;
	private final BuildingLookupService buildingLookup;

	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

	// 라벨 상수
	private static final String TAG_CHOICE = "(선택식)";
	private static final String TAG_COMMON = "(공통찬)";

	// (공백 제거): 점/중점/쉼표/슬래시/파이프 주변 공백만 분리
	private static final String DISH_SPLIT_REGEX = "\\s*[·ㆍ,|]\\s*";

	private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_DATE;
	private static final DateTimeFormatter ISO_OFFSET_DT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

	// 줄 정규화: 따옴표/이상 공백/불릿 등 정리
	private String normalizeLine(String raw) {
		if (raw == null) {
			return "";
		}
		String s = raw.replace('\u00A0', ' ')
			.replaceAll("[\"“”‘’]", "")    // 따옴표류 제거
			.replaceAll("\\s{2,}", " ")    // 다중 공백
			.trim();
		if (s.startsWith("-")) {
			s = s.substring(1).trim(); // 앞 불릿 제거
		}
		return s;
	}

	// 라벨 포함 여부
	private boolean containsChoiceOrCommonTag(String text) {
		return text != null && (text.contains(TAG_CHOICE) || text.contains(TAG_COMMON));
	}

	// (선택식)/(공통찬)으로 아이템 분리
	private record TwoLists(List<String> choice, List<String> common) {

	}

	private TwoLists splitChoiceCommon(String itemsText) {
		List<String> choice = new ArrayList<>();
		List<String> common = new ArrayList<>();
		if (itemsText == null || itemsText.isBlank()) {
			return new TwoLists(choice, common);
		}

		enum Mode {CHOICE, COMMON}
		Mode mode = Mode.CHOICE; // 태그 전까지는 선택식

		for (String raw : itemsText.split("\\R")) {
			String line = normalizeLine(raw);
			if (line.isEmpty()) {
				continue;
			}

			while (true) {
				int idxChoice = line.indexOf(TAG_CHOICE);
				int idxCommon = line.indexOf(TAG_COMMON);

				if (idxChoice == -1 && idxCommon == -1) {
					if (!line.isBlank()) {
						if (mode == Mode.CHOICE) {
							choice.add(line);
						} else {
							common.add(line);
						}
					}
					break;
				}

				boolean nextIsCommon =
					(idxCommon != -1) && (idxChoice == -1 || idxCommon < idxChoice);
				int tagIdx = nextIsCommon ? idxCommon : idxChoice;
				String before = normalizeLine(line.substring(0, tagIdx));
				if (!before.isEmpty()) {
					if (mode == Mode.CHOICE) {
						choice.add(before);
					} else {
						common.add(before);
					}
				}

				mode = nextIsCommon ? Mode.COMMON : Mode.CHOICE;
				line = normalizeLine(line.substring(
					tagIdx + (nextIsCommon ? TAG_COMMON.length() : TAG_CHOICE.length())));
			}
		}

		// 중복 제거
		choice = new ArrayList<>(new LinkedHashSet<>(choice));
		common = new ArrayList<>(new LinkedHashSet<>(common));
		return new TwoLists(choice, common);
	}

	// 한/여러 줄로 된 조각들을 ‘메뉴 토큰(단품)’ 단위로 쪼개기
	private List<String> explodeDishes(List<String> chunks) {
		List<String> out = new ArrayList<>();
		if (chunks == null) {
			return out;
		}

		for (String chunk : chunks) {
			if (chunk == null) {
				continue;
			}
			String norm = normalizeLine(chunk);
			if (norm.isEmpty()) {
				continue;
			}

			for (String t : norm.split(DISH_SPLIT_REGEX)) {
				String dish = t.trim();
				if (!dish.isEmpty()) {
					out.add(dish);
				}
			}
		}
		return out;
	}

	// 배열 그대로 반환 (콤마로 합치지 않음)
	private List<String> toList(List<String> tokens) {
		if (tokens == null || tokens.isEmpty()) {
			return List.of();
		}
		return tokens;
	}

	private static LocalDate mondayOf(LocalDate any) {
		return any.with(DayOfWeek.MONDAY);
	}

	private static int dayIndex(LocalDate monday, LocalDate d) {
		return (int) (d.toEpochDay() - monday.toEpochDay()); // 0~6
	}

	/**
	 * 주간(월~일) 기준 메타 + 메뉴 묶음 반환 (프론트 요구 형식) 입력: buildingId 하나만
	 */
	@Transactional(readOnly = true)
	public WeeklyCafeteriaResponseDto getWeeklyByBuilding(Long buildingId) {
		if (buildingId == null || buildingId <= 0) {
			throw new InvalidArgumentException("buildingId는 양수여야 합니다.");
		}

		// 건물명 확인
		String buildingName = buildingLookup.findBuildingNameById(buildingId)
			.orElseThrow(() -> new BuildingNotFoundException(buildingId));

		// 이번 주 월~금 범위 (토/일은 다음 주 기준)
		LocalDate nowKstDate = LocalDate.now(KST);
		LocalDate monday = mondayOf(nowKstDate);
		if (nowKstDate.getDayOfWeek() == DayOfWeek.SATURDAY
			|| nowKstDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
			monday = monday.plusWeeks(1);
		}
		LocalDate friday = monday.plusDays(4);
		LocalDate sunday = monday.plusDays(6);

		// 메뉴 → 프론트용 한 끼 DTO(+dayIndex/date)로 변환
		List<CafeteriaMenuWithIndexDto> menusOut = new ArrayList<>();

		List<Cafeteria> cafeterias = cafeteriaRepo.findByBuildingId(buildingId);
		if (!cafeterias.isEmpty()) {
			Map<Long, Cafeteria> idToCafeteria = cafeterias.stream()
				.collect(Collectors.toMap(Cafeteria::getId, c -> c));

			List<Long> cafeteriaIds = new ArrayList<>(idToCafeteria.keySet());

			List<CafeteriaMenu> menus = menuRepo
				.findByCafeteriaIdInAndMealDateBetweenOrderByMealDateAscMealTypeAscCornerAsc(
					cafeteriaIds, monday, friday);

			menus = menus.stream()
				.filter(m -> {
					String t = Optional.ofNullable(m.getItemsText()).orElse("").trim();
					if (t.isEmpty()) {
						return false;
					}
					return !Set.of("메뉴 준비 중", "준비중", "준비 중", "-").contains(t);
				})
				.toList();

			for (CafeteriaMenu m : menus) {
				Cafeteria c = idToCafeteria.get(m.getCafeteriaId());
				if (c == null) {
					continue;
				}

				// 원본 라인 목록
				List<String> rawLines = Arrays.stream(
						Optional.ofNullable(m.getItemsText()).orElse("").split("\n"))
					.map(this::normalizeLine)
					.filter(s -> !s.isBlank())
					.toList();

				// (선택식/공통찬) 분리 적용 대상: 문화예술융합대학-학생식당 + 태그 포함 시
				boolean shouldSplit =
					"문화예술융합대학".equals(buildingName)
						&& "학생식당".equals(c.getName())
						&& containsChoiceOrCommonTag(m.getItemsText());

				List<String> itemsChoiceOut = List.of();
				List<String> itemsCommonOut = List.of();
				List<String> itemsOut;

				if (shouldSplit) {
					TwoLists t = splitChoiceCommon(m.getItemsText());
					List<String> choiceTokens = explodeDishes(t.choice());
					List<String> commonTokens = explodeDishes(t.common());
					itemsChoiceOut = toList(choiceTokens);
					itemsCommonOut = toList(commonTokens);

					// 전체 items = choice + common 합치기
					List<String> merged = new ArrayList<>();
					merged.addAll(choiceTokens);
					merged.addAll(commonTokens);
					itemsOut = toList(merged);
				} else {
					List<String> tokens = explodeDishes(rawLines);
					itemsOut = toList(tokens);
				}

				int idx = dayIndex(monday, m.getMealDate());

				menusOut.add(CafeteriaMenuWithIndexDto.builder()
					.buildingId(buildingId)
					.buildingName(buildingName)
					.place(c.getName())
					.mealType(m.getMealType())
					.corner(m.getCorner())
					.mealDate(m.getMealDate())
					.dayIndex(idx)
					.date(m.getMealDate().format(ISO_DATE))
					.items(itemsOut)
					.itemsChoice(itemsChoiceOut)
					.itemsCommon(itemsCommonOut)
					.build());
			}
		}

		// 메뉴 없는 날짜에도 빈 항목 보장 (0~6 전체 날짜)
		Set<Integer> coveredIndices = menusOut.stream()
			.map(CafeteriaMenuWithIndexDto::getDayIndex)
			.collect(Collectors.toSet());
		for (int i = 0; i < 7; i++) {
			if (!coveredIndices.contains(i)) {
				menusOut.add(CafeteriaMenuWithIndexDto.builder()
					.buildingId(buildingId)
					.buildingName(buildingName)
					.dayIndex(i)
					.date(monday.plusDays(i).format(ISO_DATE))
					.mealDate(monday.plusDays(i))
					.items(List.of())
					.itemsChoice(List.of())
					.itemsCommon(List.of())
					.build());
			}
		}
		menusOut.sort(Comparator.comparingInt(CafeteriaMenuWithIndexDto::getDayIndex));

		// 메타 구성
		String tz = KST.getId();
		String serverTime = OffsetDateTime.now(KST).format(ISO_OFFSET_DT);
		String weekStart = monday.format(ISO_DATE);

		int todayIdx = (nowKstDate.isBefore(monday) || nowKstDate.isAfter(sunday))
			? -1 : dayIndex(monday, nowKstDate);

		List<Integer> availableIndices = List.of(0, 1, 2, 3, 4, 5, 6);

		Map<String, String> indexToDate = new LinkedHashMap<>();
		for (int i = 0; i < 7; i++) {
			indexToDate.put(String.valueOf(i), monday.plusDays(i).format(ISO_DATE));
		}

		return WeeklyCafeteriaResponseDto.builder()
			.tz(tz)
			.serverTime(serverTime)
			.weekStart(weekStart)
			.todayIndex(todayIdx)
			.availableIndices(availableIndices)
			.indexToDate(indexToDate)
			.menus(menusOut)
			.lastUpdatedAt(serverTime)
			.build();
	}
}