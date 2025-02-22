package com.YOGIITSU.controller;

import com.YOGIITSU.config.handler.GlobalExceptionHandler.InvalidTokenException;
import com.YOGIITSU.config.handler.GlobalExceptionHandler.MissingTokenException;
import com.YOGIITSU.service.FavoriteService;
import com.YOGIITSU.jwt.JwtTokenProvider;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}
