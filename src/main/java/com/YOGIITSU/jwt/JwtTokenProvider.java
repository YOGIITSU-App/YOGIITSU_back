package com.YOGIITSU.jwt;

import com.YOGIITSU.config.handler.GlobalExceptionHandler.InvalidTokenException;
import com.YOGIITSU.dto.ResponseDto.TokenResponseDto;
import com.YOGIITSU.dto.ResponseDto.UserResponseDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Slf4j
@Component
public class JwtTokenProvider {

	private final Key key;

	// Access Token 만료 시간
	@Value("${jwt.access-token-expiry}")
	private long accessTokenExpiry;

	// Refresh Token 만료 시간
	@Value("${jwt.refresh-token-expiry}")
	private long refreshTokenExpiry;

	private static final String AUTHORITIES_KEY = "auth";
	private static final String BEARER_PREFIX = "Bearer ";

	// secretKey를 Base64로 디코딩해 HMAC-SHA 키를 생성
	public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		this.key = Keys.hmacShaKeyFor(keyBytes);
	}

	/**
	 * 유저 정보를 기반으로 Access Token과 Refresh Token 생성
	 *
	 * @param authentication 인증 정보를 담고 있는 객체
	 * @return 생성된 Access Token과 Refresh Token 정보를 담고 있는 TokenInfo 객체
	 */
	public TokenResponseDto generateToken(Authentication authentication) {
		// 유저의 권한 정보를 ","로 구분된 문자열로 변환
		String authorities = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(","));

		long now = (new Date()).getTime(); // 현재 시간

		// CustomUserDetails 객체를 통해 유저 정보를 가져옴
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

		// AccessToken 생성
		String accessToken = createAccessToken(
			userDetails, // userDetails 전체 넘김
			authorities,
			now
		);

		// RefreshToken 생성
		String refreshToken = createRefreshToken(now);

		// UserResponseDto 생성
		UserResponseDto userResponseDto = UserResponseDto.builder()
			.id(userDetails.getId())                          // id
			.memberId(userDetails.getMemberId())              // memberId
			.username(userDetails.getUserName())              // username
			.email(userDetails.getEmail())                    // email
			.role(userDetails.getRole())                      // role
			.build();

		return TokenResponseDto.builder()
			.grantType("Bearer") // 인증 타입 (JWT 기본값)
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.user(userResponseDto)
			.build();
	}

	private String createAccessToken(CustomUserDetails userDetails, String authorities, long now) {
		Date expiry = new Date(now + accessTokenExpiry);

		return Jwts.builder()
			.subject(String.valueOf(userDetails.getId()))
			.claim("memberId", userDetails.getMemberId())
			.claim("username", userDetails.getUserName())
			.claim("email", userDetails.getEmail())
			.claim("role", userDetails.getRole())
			.claim(AUTHORITIES_KEY, authorities)
			.expiration(expiry)
			.signWith(key, SignatureAlgorithm.HS256)
			.compact();
	}

	private String createRefreshToken(long now) {
		return Jwts.builder()
			.setExpiration(new Date(now + refreshTokenExpiry)) // 만료 시간 설정
			.signWith(key, SignatureAlgorithm.HS256) // 서명 생성
			.compact();
	}

	/**
	 * Access Token을 기반으로 사용자 정보를 추출하고 Authentication 객체를 반환
	 *
	 * @param accessToken 유효한 JWT Access Token
	 * @return Authentication 객체 (유저 정보와 권한 포함)
	 */
	public Authentication getAuthentication(String accessToken) {
		// 토큰을 복호화하여 클레임 추출
		Claims claims = parseClaims(accessToken)
			.orElseThrow(() -> new RuntimeException("권한 정보가 없는 토큰입니다."));

		// 권한 정보가 없으면 기본값으로 "ROLE_USER"를 설정
		String authoritiesClaim = claims.get(AUTHORITIES_KEY, String.class);
		if (authoritiesClaim == null || authoritiesClaim.isEmpty()) {
			authoritiesClaim = "ROLE_USER";  // 기본 권한 설정
		}

		// 권한 정보를 SimpleGrantedAuthority로 변환
		Collection<? extends GrantedAuthority> authorities =
			Arrays.stream(authoritiesClaim.split(","))
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());

		//CustomUserDetails로 직접 복구
		CustomUserDetails userDetails = new CustomUserDetails(
			Long.parseLong(claims.getSubject()),                 // id
			claims.get("memberId", String.class),             // memberId
			claims.get("username", String.class),             // username
			claims.get("email", String.class),                // email
			"",                                                  // password는 빈 값
			claims.get("role", String.class),                 // role
			authorities
		);

		return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
	}

	/**
	 * JWT 토큰의 유효성을 검사
	 *
	 * @param token 검사할 JWT 토큰
	 * @return 유효한 경우 true, 그렇지 않은 경우 false
	 */
	public boolean validateToken(String token) {
		try {
			// 서명 키로 토큰 파싱 및 검증
			Jwts.parser()
				.setSigningKey(key)  // 서명 키 설정
				.build()  // `build()` 호출 추가
				.parseClaimsJws(token);  // JWT 파싱 및 검증

			return true; // 유효한 토큰

		} catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
			log.warn("Invalid JWT Token", e); // 잘못된 서명 또는 구조
		} catch (ExpiredJwtException e) {
			log.warn("Expired JWT Token", e); // 만료된 토큰
		} catch (UnsupportedJwtException e) {
			log.warn("Unsupported JWT Token", e); // 지원되지 않는 토큰
		} catch (IllegalArgumentException e) {
			log.warn("JWT claims string is empty.", e); // 클레임이 비어 있음
		}
		return false; // 유효하지 않은 토큰
	}

	/**
	 * JWT 토큰을 복호화하고 클레임 정보를 반환, 만료된 토큰도 클레임을 추출할 수 있도록 처리
	 *
	 * @param accessToken JWT Access Token
	 * @return 클레임 정보
	 */
	private Optional<Claims> parseClaims(String accessToken) {
		try {
			return Optional.of(Jwts.parser()
				.setSigningKey(key)  // 서명 키 설정
				.build()
				.parseClaimsJws(accessToken)  // JWT 파싱
				.getBody());  // 클레임 반환
		} catch (ExpiredJwtException e) {
			// 만료된 토큰의 클레임 반환
			return Optional.of(e.getClaims());
		}
	}

	// 요청에서 JWT 토큰을 추출
	public String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
			return bearerToken.substring(BEARER_PREFIX.length()); // "Bearer " 부분을 제거하고 반환
		}
		return null;
	}

	// Access Token에서 사용자 ID를 추출
	public Long getMemberId(String accessToken) {
		Claims claims = parseClaims(accessToken)
			.orElseThrow(() -> new InvalidTokenException());

		try {
			return Long.valueOf(claims.getSubject());
		} catch (NumberFormatException e) {
			throw new InvalidTokenException();
		}
	}
}