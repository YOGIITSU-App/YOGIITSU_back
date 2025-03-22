package com.YOGIITSU.dto.ResponseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SearchSuggestionResponseDto {
	private String keyword;
	private boolean isBookmarked;
}