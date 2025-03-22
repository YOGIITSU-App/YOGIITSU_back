package com.YOGIITSU.controller;

import com.YOGIITSU.config.handler.GlobalExceptionHandler.InvalidTokenException;
import com.YOGIITSU.config.handler.GlobalExceptionHandler.MissingTokenException;
import com.YOGIITSU.dto.ResponseDto.BuildingResponseDto;
import com.YOGIITSU.dto.ResponseDto.FavoriteListResponseDto;
import com.YOGIITSU.entity.Favorite;
import com.YOGIITSU.service.FavoriteService;
import com.YOGIITSU.jwt.JwtTokenProvider;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

	private final FavoriteService favoriteService;
	private final JwtTokenProvider jwtTokenProvider;

	/**
	 * 즐겨찾기 추가
	 *
	 * @param request    HTTP 요청 객체
	 * @param buildingId 즐겨찾기에 추가할 건물 ID
	 * @return ResponseEntity<String> 응답 객체
	 */
	@PostMapping("/{buildingId}")
	public ResponseEntity<Map<String, String>> addFavorite(HttpServletRequest request,
		@PathVariable Long buildingId) {
		// 1. 요청에서 JWT 토큰 추출
		String accessToken = jwtTokenProvider.resolveToken(request);

		// 2. 토큰이 없으면 예외 발생
		if (accessToken == null) {
			throw new MissingTokenException();
		}

		// 3. 토큰이 유효한지 확인, 유효하지 않으면 예외 발생
		if (!jwtTokenProvider.validateToken(accessToken)) {
			throw new InvalidTokenException();
		}

		// 4. 유효한 토큰이라면 사용자 ID 추출
		String memberId = jwtTokenProvider.getAuthentication(accessToken).getName();

		// 5. 즐겨찾기 추가
		favoriteService.addFavorite(memberId, buildingId);

		return ResponseEntity.ok(Map.of("message", "즐겨찾기에 추가되었습니다."));
	}

	/**
	 * 즐겨찾기 삭제
	 *
	 * @param request    HTTP 요청 객체
	 * @param buildingId 즐겨찾기에서 삭제할 건물 ID
	 * @return ResponseEntity<String> 응답 객체
	 */
	@DeleteMapping("/{buildingId}")
	public ResponseEntity<Map<String, String>> removeFavorite(HttpServletRequest request,
		@PathVariable Long buildingId) {
		// 1. 요청에서 JWT 토큰 추출
		String accessToken = jwtTokenProvider.resolveToken(request);

		// 2. 토큰이 없으면 예외 발생
		if (accessToken == null) {
			throw new MissingTokenException();
		}

		// 3. 토큰이 유효한지 확인, 유효하지 않으면 예외 발생
		if (!jwtTokenProvider.validateToken(accessToken)) {
			throw new InvalidTokenException();
		}

		// 4. 유효한 토큰이라면 사용자 ID 추출
		String memberId = jwtTokenProvider.getAuthentication(accessToken).getName();

		// 5. 즐겨찾기 삭제
		favoriteService.removeFavorite(memberId, buildingId);

		return ResponseEntity.ok(Map.of("message", "즐겨찾기에서 삭제되었습니다."));
	}

	/**
	 * 즐겨찾기 목록 조회
	 *
	 * @param request HTTP 요청 객체
	 * @return ResponseEntity<FavoriteListResponseDto> 응답 객체
	 */
	@GetMapping
	@Transactional(readOnly = true)
	public ResponseEntity<FavoriteListResponseDto> getFavorites(HttpServletRequest request) {
		// 1. 요청에서 JWT 토큰 추출
		String accessToken = jwtTokenProvider.resolveToken(request);

		// 2. 토큰이 없으면 예외 발생
		if (accessToken == null) {
			throw new MissingTokenException();
		}

		// 3. 토큰이 유효한지 확인, 유효하지 않으면 예외 발생
		if (!jwtTokenProvider.validateToken(accessToken)) {
			throw new InvalidTokenException();
		}

		// 4. 유효한 토큰이라면 사용자 ID 추출
		String memberId = jwtTokenProvider.getAuthentication(accessToken).getName();

		// 5. 즐겨찾기 목록 조회
		List<Favorite> favorites = favoriteService.getFavorites(memberId);

		// 6. 건물 정보를 리스트로 변환
		List<BuildingResponseDto> buildings = favorites.stream()
			.map(f -> new BuildingResponseDto(f.getBuilding().getId(), f.getBuilding().getName()))
			.toList();

		return ResponseEntity.ok(new FavoriteListResponseDto(memberId, buildings));
	}
}