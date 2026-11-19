package com.YOGIITSU.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

@Component
public class EmailVerificationJwtProvider {

	private static final long EMAIL_TOKEN_EXPIRATION_MS = 300000; // 5분 (300초)
	public static final String MESSAGE_ID_CLAIM = "mid";
	private final SecretKey secretKey;

	public EmailVerificationJwtProvider() {
		this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
	}

	/**
	 * 이메일 인증 토큰 생성 (0.12.3 최신 버전 대응)
	 */
	// 토큰은 서명만 되고 암호화되지 않으므로 인증 코드를 담지 않는다.
	// 인증 코드는 서버(DB)에서만 비교한다.
	public String generateEmailToken(String email, Long emailMessageId) {
		return Jwts.builder()
			.subject(email)
			.claim(MESSAGE_ID_CLAIM, emailMessageId)
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + EMAIL_TOKEN_EXPIRATION_MS))
			.signWith(secretKey)
			.compact();
	}

	/**
	 * 이메일 인증 토큰 검증 및 파싱 (0.12.3 최신 버전 대응)
	 */
	public Optional<Claims> parseEmailToken(String token) {
		try {
			JwtParser parser = Jwts.parser()
				.verifyWith(secretKey)
				.build();

			Jws<Claims> claimsJws = parser.parseSignedClaims(token);
			return Optional.of(claimsJws.getPayload());

		} catch (ExpiredJwtException e) {
			return Optional.of(e.getClaims());  // 만료된 토큰이라도 클레임 확인 가능
		} catch (JwtException e) {
			return Optional.empty();  // 잘못된 토큰 처리
		}
	}
}
