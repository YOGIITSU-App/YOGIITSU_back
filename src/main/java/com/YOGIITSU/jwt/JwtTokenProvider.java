package com.YOGIITSU.jwt;

import com.YOGIITSU.dto.TokenInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
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
	public TokenInfo generateToken(Authentication authentication) {
		// 유저의 권한 정보를 ","로 구분된 문자열로 변환
		String authorities = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(","));

		long now = (new Date()).getTime(); // 현재 시간

		// Access Token 생성
		String accessToken = createAccessToken(authentication.getName(), authorities, now);

		// Refresh Token 생성
		String refreshToken = createRefreshToken(now);

		// TokenInfo 객체를 반환
		return TokenInfo.builder()
			.grantType("Bearer") // 인증 타입 (JWT 기본값)
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}

	private String createAccessToken(String subject, String authorities, long now) {
		Date accessTokenExpiresIn = new Date(now + accessTokenExpiry);
		return Jwts.builder()
			.setSubject(subject) // 토큰 소유자 (유저 ID 등)
			.claim(AUTHORITIES_KEY, authorities) // 권한 정보 추가
			.setExpiration(accessTokenExpiresIn) // 만료 시간 설정
			.signWith(key, SignatureAlgorithm.HS256) // 서명 생성
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

		// 권한 정보를 SimpleGrantedAuthority 로 변환
		Collection<? extends GrantedAuthority> authorities =
			Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());

		// UserDetails 객체를 생성하여 Authentication 객체 반환
		UserDetails principal = new User(claims.getSubject(), "", authorities);
		return new UsernamePasswordAuthenticationToken(principal, "", authorities);
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
}