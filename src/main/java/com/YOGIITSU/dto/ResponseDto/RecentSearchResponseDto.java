package com.YOGIITSU.dto.ResponseDto;

import com.YOGIITSU.entity.RecentSearch;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RecentSearchResponseDto {

	private String keyword;
	private LocalDateTime searchedAt;
	private Long buildingId;

	public RecentSearchResponseDto(RecentSearch recentSearch) {
		this.keyword = recentSearch.getKeyword();
		this.searchedAt = recentSearch.getSearchedAt();
		this.buildingId = recentSearch.getBuilding() != null ? recentSearch.getBuilding().getId() : null;
	}
}