package com.YOGIITSU.dto.ResponseDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class WeeklyCafeteriaResponseDto {

	private String tz;                 // "Asia/Seoul"
	private String serverTime;         // ISO-8601 (e.g. 2025-09-10T09:42:10+09:00)
	private String weekStart;          // ISO date of Monday (e.g. "2025-09-08")
	private Integer todayIndex;        // 0~6 (Mon~Sun), 주 외면 -1
	private List<Integer> availableIndices; // 메뉴가 실제 존재하는 요일 인덱스
	private Map<String, String> indexToDate; // "0".."6" -> "yyyy-MM-dd"
	private List<CafeteriaMenuWithIndexDto> menus; // 각 메뉴 + dayIndex, date
	private String lastUpdatedAt;      // ISO-8601 (지금 시각; 스케줄 타임스탬프 없으니 서버 시각 사용)

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonInclude(JsonInclude.Include.ALWAYS)
	public static class CafeteriaMenuWithIndexDto {

		private Long buildingId;
		private String buildingName;
		private String place;
		private LocalDate mealDate;
		private String mealType;
		private String corner;
		private List<String> items;
		private List<String> itemsChoice;
		private List<String> itemsCommon;

		// 주간 응답 전용 필드
		private Integer dayIndex;  // 0~6 (Mon~Sun)
		private String date;       // "yyyy-MM-dd"
	}
}