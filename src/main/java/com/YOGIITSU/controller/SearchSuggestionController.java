package com.YOGIITSU.controller;

import com.YOGIITSU.config.handler.GlobalExceptionHandler.InvalidTokenException;
import com.YOGIITSU.config.handler.GlobalExceptionHandler.MissingTokenException;
import com.YOGIITSU.dto.ResponseDto.SearchSuggestionResponseDto;
import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.service.SearchSuggestionService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Collections;


@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchSuggestionController {

	private final SearchSuggestionService searchSuggestionService;
	private final JwtTokenProvider jwtTokenProvider;

	/**
	 * 자동완성 검색어 추천 API
	 *
	 * @param query 검색어
	 * @return 검색어 추천 목록
	 */
	@GetMapping("/suggestions")
	public ResponseEntity<List<SearchSuggestionResponseDto>> getSearchSuggestions(
		@RequestParam(required = false) String query,
		HttpServletRequest httpRequest) {

		// 1. JWT 토큰 검증
		String accessToken = jwtTokenProvider.resolveToken(httpRequest);
		if (accessToken == null) {
			throw new MissingTokenException();
		}
		if (!jwtTokenProvider.validateToken(accessToken)) {
			throw new InvalidTokenException();
		}

		// 2. 검색어가 없거나 공백만 있는 경우 빈 리스트 반환
		if (query == null || query.isBlank()) {
			return ResponseEntity.ok(Collections.emptyList());
		}

		// 3. 사용자 ID 추출
		String memberId = jwtTokenProvider.getAuthentication(accessToken).getName();

		// 4. 자동완성 검색어 반환
		return ResponseEntity.ok(searchSuggestionService.getSearchSuggestions(query, memberId));
	}
}
