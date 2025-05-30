package com.YOGIITSU.controller;

import com.YOGIITSU.config.handler.GlobalExceptionHandler.InvalidTokenException;
import com.YOGIITSU.config.handler.GlobalExceptionHandler.MissingTokenException;
import com.YOGIITSU.dto.ResponseDto.TokenResponseDto;
import com.YOGIITSU.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증 관련 API", description = "토큰 재발급, 인증 관련 기능 제공")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

	private final JwtTokenProvider jwtTokenProvider;

	@Operation(
		summary = "토큰 재발급",
		description = """
			만료된 AccessToken과 유효한 RefreshToken을 이용하여 새 AccessToken과 RefreshToken을 재발급합니다.  
						
			Swagger 사용 시:
			- 위쪽 Authorize 버튼 클릭 → Bearer {AccessToken} 입력  
			- 아래 요청 파라미터는 **비워두세요**  
			- X-Refresh-Token은 Headers 항목에 직접 추가하세요  
						
			응답:
			- 새 AccessToken은 Authorization 헤더로 전달  
			- 새 RefreshToken은 X-Refresh-Token 헤더로 전달
			""",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "토큰 재발급 성공",
				headers = {
					@Header(name = "Authorization", description = "Bearer {new AccessToken}"),
					@Header(name = "X-Refresh-Token", description = "{new RefreshToken}")
				}
			),
			@ApiResponse(responseCode = "400", description = "토큰 누락 또는 유효하지 않음")
		}
	)
	@PostMapping("/reissue")
	public ResponseEntity<Map<String, String>> reissue(
		@Parameter(hidden = true)
		@RequestHeader(value = "Authorization", required = false) String accessTokenHeader,

		@Parameter(description = "RefreshToken은 반드시 X-Refresh-Token 헤더에 넣어주세요. <br> 예시: eyJhbGciOiJIUzI1NiJ9...", required = true)
		@RequestHeader(value = "X-Refresh-Token", required = false) String refreshToken
	) {
		if (accessTokenHeader == null || refreshToken == null) {
			throw new MissingTokenException();
		}

		String accessToken = accessTokenHeader.replace("Bearer ", "");

		if (!jwtTokenProvider.validateToken(refreshToken)) {
			throw new InvalidTokenException();
		}

		Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
		TokenResponseDto newTokens = jwtTokenProvider.generateToken(authentication);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + newTokens.getAccessToken());
		headers.set("X-Refresh-Token", newTokens.getRefreshToken());

		return ResponseEntity.ok()
			.headers(headers)
			.body(Map.of("message", "토큰이 재발급되었습니다."));
	}
}
