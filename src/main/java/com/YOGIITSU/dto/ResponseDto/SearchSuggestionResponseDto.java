package com.YOGIITSU.dto.ResponseDto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SearchSuggestionResponseDto {
	private Long buildingId;     // 건물 ID
	private String keyword;      // 건물 이름 or 별칭
	private boolean isBookmarked; // 즐겨찾기 여부
	private List<String> tags;   // 태그 목록
}
