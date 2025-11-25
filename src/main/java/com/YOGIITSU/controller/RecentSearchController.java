package com.YOGIITSU.controller;

import com.YOGIITSU.exception.auth.InvalidTokenException;
import com.YOGIITSU.exception.auth.MissingTokenException;
import com.YOGIITSU.dto.RequestDto.SearchKeywordRequestDto;
import com.YOGIITSU.dto.ResponseDto.RecentSearchResponseDto;
import com.YOGIITSU.service.RecentSearchService;
import com.YOGIITSU.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "검색어 API", description = "최근 검색어 저장 및 조회 기능을 제공합니다.")
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
	@Operation(
		summary = "검색어 저장",
		description = "로그인한 사용자의 검색어를 최근 검색어 목록에 저장합니다."
	)
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
	@Operation(
		summary = "최근 검색어 조회",
		description = "로그인한 사용자의 최근 검색어 목록을 조회합니다."
	)
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

	/**
	 * 검색어 단건 삭제 API
	 *
	 * @param buildingId 삭제할 검색어가 연결된 건물 ID
	 * @return 삭제 성공 메시지
	 */

	@Operation(summary = "최근 검색어 단건 삭제", description = "연결된 건물 ID를 통해 해당 검색어를 삭제합니다.")
	@DeleteMapping("/delete/{buildingId}")
	public ResponseEntity<Map<String, String>> deleteByBuildingId(
		@PathVariable Long buildingId,
		HttpServletRequest httpRequest) {

		String accessToken = jwtTokenProvider.resolveToken(httpRequest);
		if (accessToken == null) {
			throw new MissingTokenException();
		}
		if (!jwtTokenProvider.validateToken(accessToken)) {
			throw new InvalidTokenException();
		}

		String memberId = jwtTokenProvider.getAuthentication(accessToken).getName();
		recentSearchService.deleteSearchKeywordByBuildingId(memberId, buildingId);

		Map<String, String> response = new HashMap<>();
		response.put("message", "해당 건물의 검색어가 삭제되었습니다.");
		return ResponseEntity.ok(response);
	}

	/**
	 * 검색어 전체 삭제 API
	 *
	 * @return 전체 검색어 삭제 성공 메시지
	 */
	@Operation(summary = "전체 최근 검색어 삭제", description = "모든 최근 검색어를 삭제합니다.")
	@DeleteMapping("/deleteAll")
	public ResponseEntity<Map<String, String>> deleteAllKeywords(HttpServletRequest httpRequest) {

		String accessToken = jwtTokenProvider.resolveToken(httpRequest);
		if (accessToken == null) {
			throw new MissingTokenException();
		}
		if (!jwtTokenProvider.validateToken(accessToken)) {
			throw new InvalidTokenException();
		}

		String memberId = jwtTokenProvider.getAuthentication(accessToken).getName();
		recentSearchService.deleteAllSearchKeywords(memberId);

		Map<String, String> response = new HashMap<>();
		response.put("message", "전체 검색어가 삭제되었습니다.");
		return ResponseEntity.ok(response);
	}
}
