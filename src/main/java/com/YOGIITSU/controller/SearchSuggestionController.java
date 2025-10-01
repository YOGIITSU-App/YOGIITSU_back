package com.YOGIITSU.controller;

import com.YOGIITSU.dto.ResponseDto.SearchSuggestionResponseDto;
import com.YOGIITSU.service.SearchSuggestionService;
import com.YOGIITSU.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Tag(name = "검색어 자동완성 API", description = "입력한 검색어(query)를 기준으로 추천 검색어 리스트를 제공합니다.")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchSuggestionController {

	private final SearchSuggestionService searchSuggestionService;
	private final JwtUtil jwtUtil;

	/**
	 * 자동완성 검색어 추천 API
	 *
	 * @param query 검색어
	 * @return 검색어 추천 목록
	 */
	@Operation(
		summary = "검색어 자동완성 추천",
		description = """
			사용자가 입력한 query 값을 기준으로 자동완성 검색어 리스트를 반환합니다. <br>
			로그인한 사용자의 경우 즐겨찾기한 검색어가 우선적으로 상단에 배치됩니다. <br>
			비회원의 경우 일반 검색 결과만 반환됩니다.
		"""
	)
	@GetMapping("/suggestions")
	public ResponseEntity<List<SearchSuggestionResponseDto>> getSearchSuggestions(
		@Parameter(description = "자동완성 추천 기준이 되는 검색어", example = "대학")
		@RequestParam(required = false) String query,
		HttpServletRequest httpRequest) {

		// 1. 검색어가 없거나 공백만 있는 경우 빈 리스트 반환
		if (query == null || query.isBlank()) {
			return ResponseEntity.ok(Collections.emptyList());
		}

		// 2. 토큰이 있으면 MemberId 추출, 없으면 null
		String memberId = jwtUtil.extractMemberIdStringSafely(httpRequest);

		// 3. 자동완성 검색어 반환
		return ResponseEntity.ok(searchSuggestionService.getSearchSuggestions(query, memberId));
	}
}
