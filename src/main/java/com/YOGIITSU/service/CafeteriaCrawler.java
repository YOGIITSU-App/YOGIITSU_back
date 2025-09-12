package com.YOGIITSU.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CafeteriaCrawler {

	private static final String UA = "Mozilla/5.0 (YogiitsuBot/1.0)";
	private static final int TIMEOUT = 10_000;

	private static final Map<String, String> BUILDING_CANON = Map.of(
		"아마랜스관", "문화예술융합대학",
		"아마랜스홀", "문화예술융합대학"
	);
	private static final Map<String, String> PLACE_CANON = Map.of(
		"학생식단", "학생식당"
	);

	private String canonicalBuilding(String s) {
		return BUILDING_CANON.getOrDefault(s, s);
	}

	private String canonicalPlace(String s) {
		return PLACE_CANON.getOrDefault(s, s);
	}

	/**
	 * 수집 결과 DTO
	 */
	public record ParsedRow(
		String building,
		String placeName,
		LocalDate mealDate,
		String mealType,
		String corner,        // 코너(없으면 null; Sync 단계에서 ""로 정규화)
		String itemsText,     // 실제 메뉴 본문
		LocalDate versionWeek,
		String hashBody
	) {

	}

	/**
	 * 장소 정의
	 */
	private record Place(String building, String place, String url, String selector) {

	}

	/**
	 * 대상 페이지 목록
	 */
	private static final List<Place> PLACES = List.of(
		new Place("ACE교육관", "학생식당",
			"https://www.suwon.ac.kr/index.html?menuno=762",
			"div#contents_table2 table"),
		new Place("ACE교육관", "교직원식당",
			"https://www.suwon.ac.kr/index.html?menuno=762",
			"div#teMn table"),
		new Place("문화예술융합대학", "학생식당",
			"https://www.suwon.ac.kr/index.html?menuno=1793",
			"div#contents_table22 table")
	);

	/**
	 * 지정한 주(월요일 기준) 전체 장소 크롤링
	 */
	public List<ParsedRow> fetchAll(LocalDate monday) {
		List<ParsedRow> out = new ArrayList<>();
		int maxRetries = 3;
		for (Place p : PLACES) {
			int retryCount = 0;
			while (retryCount < maxRetries) {
				try {
					List<ParsedRow> rows = fetchPlace(p, monday);
					if (rows.isEmpty()) {
						log.warn("[Crawler] {}-{} 결과 0건", p.building(), p.place());
					}
					out.addAll(rows);
					break; // 성공 시 탈출
				} catch (Exception e) {
					retryCount++;
					if (retryCount >= maxRetries) {
						log.error("[Crawler] {}-{} 크롤 실패 ({}회 재시도)", p.building(), p.place(),
							maxRetries, e);
					} else {
						log.warn("[Crawler] {}-{} 크롤 실패, 재시도 {}/{}", p.building(), p.place(),
							retryCount, maxRetries);
						try {
							Thread.sleep(1000L * retryCount);
						} catch (InterruptedException ie) {
							Thread.currentThread().interrupt();
							break;
						}
					}
				}
			}
		}

		var byPlace = out.stream()
			.collect(Collectors.groupingBy(r -> r.building() + "-" + r.placeName(),
				Collectors.counting()));
		log.info("[Crawler] 주간 파싱 건수: {}", byPlace);

		return out;
	}

	/**
	 * 장소별 테이블 파싱(코너/배너/rowspan 및 월~금 보정) - FIXED
	 */
	private List<ParsedRow> fetchPlace(Place place, LocalDate monday) throws Exception {
		Document doc = Jsoup.connect(place.url())
			.userAgent(UA)
			.timeout(TIMEOUT)
			.maxBodySize(0)  // 큰 HTML 페이지 대응
			.get();
		Element table;
		try {
			table = doc.selectFirst(place.selector());
		} catch (Exception selEx) { // SelectorParseException 등
			log.error("[Crawler] 셀렉터 파싱 오류: {} / selector='{}'", place, place.selector(), selEx);
			return List.of(); // 이 장소만 건너뜀
		}

		if (table == null) {
			// 디버깅 도움 로그(원하면 유지)
			Elements allTables = doc.select("table");
			log.warn("표를 못 찾음: {} (tables: {})", place, allTables.size());
			return List.of();
		}

		HeaderIdx idx = parseHeaderIndices(table);
		if (idx.days().isEmpty()) {
			log.warn("요일 헤더를 찾지 못함: {}", place);
			return List.of();
		}

		List<ParsedRow> result = new ArrayList<>();
		String lastCorner = null;                // rowspan 코너 유지용
		final int daysCount = idx.days().size();

		String lastMealType = null;

		for (Element tr : table.select("tbody tr")) {
			Elements tds = tr.select("td");
			if (tds.isEmpty()) {
				continue;
			}

			// --- 이 행에서 '요일 블록' 시작 위치를 동적으로 계산 (rowspan 보정의 핵심)
			int base = tds.size() - daysCount;
			if (base < 0 || base > tds.size()) {
				log.debug("[Crawler] 예상치 못한 테이블 구조: tds={}, daysCount={}", tds.size(), daysCount);
				continue;
			}

			// --- 요일 블록 앞쪽(0..base-1)의 모든 칸 텍스트 수집
			List<String> leftTexts = new ArrayList<>();
			for (int i = 0; i < base; i++) {
				leftTexts.add(cleanText(tds.get(i).html()).trim());
			}

			// 1) mealType: 조식/중식/석식(B/L/D) 탐지 전용
			String mealTypeFound = leftTexts.stream()
				.filter(this::looksLikeMealType)
				.findFirst()
				.orElse(null);
			if (mealTypeFound != null) {
				lastMealType = mealTypeFound;   // 이번 행에서 발견되면 갱신
			}
			String mealType = (lastMealType != null) ? lastMealType : ""; // 없으면 직전값 유지

			// 2) corner: 영문 짧은 배너만 채택(식사구분 문구는 제외), 없으면 lastCorner 유지
			String corner = leftTexts.stream()
				.filter(s -> !looksLikeMealType(s))
				.filter(this::looksLikeCornerBanner)
				.findFirst()
				.orElse((lastCorner != null && !lastCorner.isBlank()) ? lastCorner : null);
			if (looksLikeCornerBanner(corner)) {
				lastCorner = corner;
			}

			// --- 월~금 순회: base + i 로 정확히 매핑
			for (int i = 0; i < daysCount; i++) {
				int tdIdx = base + i;
				if (tdIdx < 0 || tdIdx >= tds.size()) {
					continue;
				}

				String items = cleanText(tds.get(tdIdx).html());
				if (items.isBlank()) {
					continue;
				}

				LocalDate mealDate = monday.plusDays(i);

				// 요일 칸 안에 배너가 들어온 경우: corner만 저장하고 items_text는 비움
				if (looksLikeCornerBanner(items)) {
					String banner = items.trim();
					String hash = DigestUtils.sha1Hex(banner + "|" + mealDate);
					result.add(new ParsedRow(
						canonicalBuilding(place.building()),
						canonicalPlace(place.place()),
						mealDate,
						mealType,
						banner, "", monday, hash
					));
					lastCorner = banner; // 이후 행에서도 유지될 수 있음
					continue;
				}

				// corner가 메뉴처럼 보이면 코너로 쓰지 않음(실제 본문으로 오판 방지)
				String useCorner = (looksLikeMenu(corner) ? null : corner);

				String hash = DigestUtils.sha1Hex(items);
				result.add(new ParsedRow(
					canonicalBuilding(place.building()),
					canonicalPlace(place.place()),
					mealDate,
					mealType,
					useCorner, items, monday, hash
				));
			}
		}

		// --- 추가: 월~금에 비어있는 날을 '(메뉴 준비 중)'으로 보정해서 채워 넣기
		Set<String> mealTypes = result.stream()
			.map(ParsedRow::mealType)
			.map(s -> s == null ? "" : s.trim())
			.filter(s -> !s.isEmpty())
			.collect(Collectors.toCollection(LinkedHashSet::new));
		if (mealTypes.isEmpty()) {
			mealTypes = Set.of("중식"); // 기본값
		}

		//월~금에 비어있는 날을 '(메뉴 준비 중)'으로 보정해서 채워 넣기
		for (String mt : mealTypes) {
			for (int i = 0; i < 5; i++) {
				LocalDate d = monday.plusDays(i);
				boolean exists = result.stream().anyMatch(r ->
					Objects.equals(r.mealType(), mt) && Objects.equals(r.mealDate(), d)
				);
				if (!exists) {
					String placeholder = "메뉴 준비 중";
					String hash = DigestUtils.sha1Hex("EMPTY|" + d + "|" + mt);
					result.add(new ParsedRow(
						canonicalBuilding(place.building()),
						canonicalPlace(place.place()),
						d,
						mt,
						null,
						placeholder,
						monday,
						hash
					));
				}
			}
		}

		return result;
	}

	/**
	 * thead에서 각 열 인덱스 계산 (코너 열이 없을 수도 있음)
	 */
	private HeaderIdx parseHeaderIndices(Element table) {
		Element headRow = table.selectFirst("thead tr:last-of-type");
		List<String> headers = new ArrayList<>();
		if (headRow != null) {
			for (Element th : headRow.select("th")) {
				headers.add(th.text().trim());
			}
		}

		List<String> dayNames = List.of("월", "화", "수", "목", "금");
		List<Integer> dayIdxList = new ArrayList<>();
		for (int i = 0; i < headers.size(); i++) {
			if (dayNames.contains(headers.get(i))) {
				dayIdxList.add(i);
			}
		}

		int mealTypeIdx = indexOf(headers, "구분", "식사", "식단구분", "메뉴구분");
		int cornerIdx = indexOf(headers, "코너", "분류");

		// 헤더가 부정확할 때의 폴백 (일반적인 구조 가정)
		if (headers.isEmpty() || dayIdxList.isEmpty()) {
			mealTypeIdx = 0;
			cornerIdx = -1;                      // 코너 없음 가정
			dayIdxList = List.of(1, 2, 3, 4, 5);  // [식사][월][화][수][목][금]
		}
		return new HeaderIdx(mealTypeIdx, cornerIdx, dayIdxList);
	}

	private static int indexOf(List<String> headers, String... keys) {
		for (int i = 0; i < headers.size(); i++) {
			String h = headers.get(i);
			for (String k : keys) {
				if (h.contains(k)) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * 헤더 인덱스 묶음 → record 로 변경
	 */
	private record HeaderIdx(int mealTypeIdx, int cornerIdx, List<Integer> days) {

		@SuppressWarnings("unused")
		boolean hasCorner() {
			return cornerIdx >= 0;
		}
	}

	/**
	 * HTML → 줄바꿈 정리된 텍스트
	 */
	private String cleanText(String html) {
		String replaced = html.replaceAll("(?i)<br\\s*/?>", "\n")
			.replaceAll("(?i)</p>\\s*<p>", "\n");
		String plain = Jsoup.parse(replaced).text();
		return Arrays.stream(plain.split("\\R"))
			.map(String::trim)
			.filter(s -> !s.isBlank())
			.collect(Collectors.joining("\n"));
	}

	/**
	 * 메뉴처럼 보이는지(Null-safe)
	 */
	private boolean looksLikeMenu(String s) {
		if (s == null) {
			return false;
		}
		String t = s.trim();
		// 문장 길이가 충분히 길면 메뉴로 간주
		if (t.length() >= 20) {
			return true;
		}
		boolean hasHangul = t.codePoints().anyMatch(this::isHangul);
		return hasHangul && (t.contains("밥") || t.contains("국") || t.contains("면")
			|| t.contains("샐러드") || t.contains("돈까스") || t.contains("*"));
	}

	/**
	 * 영문 배너(예: Little Kitchen)인지(Null-safe)
	 */
	private boolean looksLikeCornerBanner(String s) {
		if (s == null) {
			return false;
		}
		String t = s.trim();
		if (t.isEmpty()) {
			return false;
		}
		boolean hasHangul = t.codePoints().anyMatch(this::isHangul);
		boolean asciiOnly = t.chars().allMatch(ch -> ch == ' ' || (ch >= 33 && ch <= 126));
		// 한글이 없고 ASCII로만 구성, 길이가 짧은 영문/숫자 배너 → 코너 배너로 판단
		return !hasHangul && asciiOnly && t.length() <= 20;
	}

	/**
	 * 조식/중식/석식 or Breakfast/Lunch/Dinner 같은 식사 구분인지
	 */
	private boolean looksLikeMealType(String s) {
		if (s == null) {
			return false;
		}
		String t = s.trim();
		if (t.isEmpty()) {
			return false;
		}
		return t.contains("조식") || t.contains("중식") || t.contains("석식")
			|| t.equalsIgnoreCase("breakfast")
			|| t.equalsIgnoreCase("lunch")
			|| t.equalsIgnoreCase("dinner");
	}

	private boolean isHangul(int cp) {
		Character.UnicodeBlock b = Character.UnicodeBlock.of(cp);
		return b == Character.UnicodeBlock.HANGUL_SYLLABLES
			|| b == Character.UnicodeBlock.HANGUL_JAMO
			|| b == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO;
	}

	/**
	 * 임의 날짜가 속한 주의 월요일 반환
	 */
	public static LocalDate mondayOf(LocalDate any) {
		return any.with(DayOfWeek.MONDAY);
	}
}