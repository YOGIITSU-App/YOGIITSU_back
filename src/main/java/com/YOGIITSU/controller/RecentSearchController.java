package com.YOGIITSU.controller;

import com.YOGIITSU.config.handler.GlobalExceptionHandler.InvalidTokenException;
import com.YOGIITSU.config.handler.GlobalExceptionHandler.MissingTokenException;
import com.YOGIITSU.dto.RequestDto.SearchKeywordRequestDto;
import com.YOGIITSU.dto.ResponseDto.RecentSearchResponseDto;
import com.YOGIITSU.service.RecentSearchService;
import com.YOGIITSU.jwt.JwtTokenProvider;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class RecentSearchController {

	private final RecentSearchService recentSearchService;
	private final JwtTokenProvider jwtTokenProvider;

	/**
	 * 검색어 저장 API
	 *
	 * @param request 요청 데이터 (검색어)
	 * @return message 응답 메시지
	 */
	@PostMapping("/save")
	public ResponseEntity<Map<String, String>> saveSearchKeyword(
		@RequestBody SearchKeywordRequestDto request, HttpServletRequest httpRequest) {

		// 1. JWT 토큰 검증
		String accessToken = jwtTokenProvider.resolveToken(httpRequest);
		if (accessToken == null) {
			throw new MissingTokenException();
		}
		if (!jwtTokenProvider.validateToken(accessToken)) {
			throw new InvalidTokenException();
		}

		// 2. 사용자 ID 추출
		String memberId = jwtTokenProvider.getAuthentication(accessToken).getName();

		// 3. 검색어 저장
		recentSearchService.saveSearchKeyword(memberId, request.getKeyword());

		// 4. 응답 반환
		Map<String, String> response = new HashMap<>();
		response.put("message", "검색어가 저장되었습니다.");
		return ResponseEntity.ok(response);
	}

	/**
	 * 최근 검색어 조회 API
	 *
	 * @return 최근 검색어 목록
	 */
	@GetMapping("/recent")
	public ResponseEntity<List<RecentSearchResponseDto>> getRecentSearches(
		HttpServletRequest httpRequest) {

		// 1. JWT 토큰 검증
		String accessToken = jwtTokenProvider.resolveToken(httpRequest);
		if (accessToken == null) {
			throw new MissingTokenException();
		}
		if (!jwtTokenProvider.validateToken(accessToken)) {
			throw new InvalidTokenException();
		}

		// 2. 사용자 ID 추출
		String memberId = jwtTokenProvider.getAuthentication(accessToken).getName();

		// 3. 최근 검색어 조회 후 반환
		return ResponseEntity.ok(recentSearchService.getRecentSearches(memberId));
	}
}