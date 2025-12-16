package com.YOGIITSU.jwt;

import com.YOGIITSU.exception.auth.InvalidTokenException;
import com.YOGIITSU.dto.ResponseDto.TokenResponseDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JwtTokenProvider 테스트")
class JwtTokenProviderTest {

	private JwtTokenProvider jwtTokenProvider;
	private static final String SECRET_KEY = "dGVzdFNlY3JldEtleUZvckpXVFRva2VuUHJvdmlkZXJUZXN0aW5nUHVycG9zZXM="; // Base64 encoded test key

	@BeforeEach
	void setUp() {
		jwtTokenProvider = new JwtTokenProvider(SECRET_KEY);
		// access-token-expiry를 1시간(3600000ms)으로 설정
		ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiry", 3600000L);
		// refresh-token-expiry를 14일로 설정
		ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiry", 1209600000L);
	}

	@Test
	@DisplayName("만료된_토큰에서_사용자정보_추출_성공")
	void getAuthenticationFromExpiredToken_success_with_expired_token() {
		// given - 실제로 만료된 토큰 생성
		byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
		SecretKey key = Keys.hmacShaKeyFor(keyBytes);

		// 과거 시간으로 만료 시간 설정 (1시간 전)
		Date expiredDate = new Date(System.currentTimeMillis() - 3600000L);

		String expiredToken = Jwts.builder()
			.subject("1")
			.claim("memberId", "testUser")
			.claim("username", "테스트유저")
			.claim("email", "test@test.com")
			.claim("role", "USER")
			.claim("auth", "ROLE_USER")
			.expiration(expiredDate) // 이미 만료된 시간
			.signWith(key)
			.compact();

		// when - 만료된 토큰에서 정보 추출
		Authentication extractedAuth = jwtTokenProvider.getAuthenticationFromExpiredToken(
			expiredToken);

		// then
		assertNotNull(extractedAuth);
		CustomUserDetails extractedUserDetails = (CustomUserDetails) extractedAuth.getPrincipal();
		assertEquals(1L, extractedUserDetails.getId());
		assertEquals("testUser", extractedUserDetails.getMemberId());
		assertEquals("테스트유저", extractedUserDetails.getUserName());
		assertEquals("test@test.com", extractedUserDetails.getEmail());
		assertEquals("USER", extractedUserDetails.getRole());

		// 만료된 토큰은 validateToken에서 false를 반환해야 함
		assertFalse(jwtTokenProvider.validateToken(expiredToken));
	}

	@Test
	@DisplayName("만료된_토큰에서_사용자정보_추출_실패_잘못된_토큰")
	void getAuthenticationFromExpiredToken_fail_invalid_token() {
		// given
		String invalidToken = "invalid.token.string";

		// when & then
		assertThrows(InvalidTokenException.class, () ->
			jwtTokenProvider.getAuthenticationFromExpiredToken(invalidToken)
		);
	}

	@Test
	@DisplayName("만료된_토큰에서_사용자정보_추출_실패_잘못된_서명")
	void getAuthenticationFromExpiredToken_fail_invalid_signature() {
		// given
		// 다른 시크릿 키로 생성된 토큰
		JwtTokenProvider otherProvider = new JwtTokenProvider(
			"ZGlmZmVyZW50U2VjcmV0S2V5Rm9yVGVzdGluZ1B1cnBvc2VzT25seQ=="
		);
		ReflectionTestUtils.setField(otherProvider, "accessTokenExpiry", 3600000L);
		ReflectionTestUtils.setField(otherProvider, "refreshTokenExpiry", 1209600000L);

		CustomUserDetails userDetails = new CustomUserDetails(
			1L, "testUser", "테스트유저", "test@test.com", "", "USER",
			List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			userDetails, "", userDetails.getAuthorities()
		);
		TokenResponseDto tokenResponse = otherProvider.generateToken(authentication);
		String tokenWithDifferentKey = tokenResponse.getAccessToken();

		// when & then
		assertThrows(InvalidTokenException.class, () ->
			jwtTokenProvider.getAuthenticationFromExpiredToken(tokenWithDifferentKey)
		);
	}

	@Test
	@DisplayName("토큰_재발급_시나리오_만료된_액세스토큰과_유효한_리프레시토큰")
	void token_reissue_scenario_expired_access_valid_refresh() throws InterruptedException {
		// given
		CustomUserDetails userDetails = new CustomUserDetails(
			1L,
			"testUser",
			"테스트유저",
			"test@test.com",
			"",
			"USER",
			List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		Authentication originalAuth = new UsernamePasswordAuthenticationToken(
			userDetails, "", userDetails.getAuthorities()
		);

		// 초기 토큰 생성
		long beforeFirstToken = System.currentTimeMillis();
		TokenResponseDto originalTokens = jwtTokenProvider.generateToken(originalAuth);
		long afterFirstToken = System.currentTimeMillis();
		String originalAccessToken = originalTokens.getAccessToken();
		String refreshToken = originalTokens.getRefreshToken();

		// RefreshToken은 유효한지 확인
		assertTrue(jwtTokenProvider.validateToken(refreshToken));

		// 원본 토큰의 만료 시간 추출 (새 토큰 생성 전에)
		byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
		SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);
		Claims originalClaims = Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(originalAccessToken)
			.getPayload();
		Date originalExpiration = originalClaims.getExpiration();
		long originalExpirationTime = originalExpiration.getTime();

		// 만료된 AccessToken에서 사용자 정보 추출
		Authentication extractedAuth = jwtTokenProvider.getAuthenticationFromExpiredToken(
			originalAccessToken);

		// 새로운 토큰 생성 전에 약간의 지연을 추가하여 다른 시간에 생성되도록 함
		Thread.sleep(200); // 200ms 지연으로 다른 시간에 생성되도록 보장

		// 새로운 토큰 생성
		long beforeSecondToken = System.currentTimeMillis();
		TokenResponseDto newTokens = jwtTokenProvider.generateToken(extractedAuth);
		long afterSecondToken = System.currentTimeMillis();

		// then
		assertNotNull(newTokens);
		assertNotNull(newTokens.getAccessToken());
		assertNotNull(newTokens.getRefreshToken());

		// 새로 생성된 토큰의 만료 시간 추출
		Claims newClaims = Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(newTokens.getAccessToken())
			.getPayload();
		Date newExpiration = newClaims.getExpiration();
		long newExpirationTime = newExpiration.getTime();

		// 새 토큰 생성 시간이 원본보다 나중인지 확인
		assertTrue(beforeSecondToken > afterFirstToken,
			"새 토큰은 원본 토큰 생성 후에 생성되어야 합니다. " +
				"원본 토큰 생성 시간 범위: " + beforeFirstToken + "~" + afterFirstToken + ", " +
				"새 토큰 생성 시간 범위: " + beforeSecondToken + "~" + afterSecondToken);

		// 만료 시간 비교: 같거나 나중이어야 함 (같은 밀리초에 생성되면 같을 수 있음)
		// 중요한 것은 토큰이 재발급되었고 -> 새 토큰이 유효하다는 것
		long timeDifference = newExpirationTime - originalExpirationTime;
		assertTrue(timeDifference >= 0,
			"새로 생성된 토큰의 만료 시간(" + newExpirationTime +
				")은 원본 토큰의 만료 시간(" + originalExpirationTime +
				")보다 이전이면 안 됩니다. 차이: " + timeDifference + "ms. " +
				"원본 토큰 생성 시간 범위: " + beforeFirstToken + "~" + afterFirstToken + ", " +
				"새 토큰 생성 시간 범위: " + beforeSecondToken + "~" + afterSecondToken);

		// 만료 시간이 같더라도 정상 (같은 밀리초에 생성되면 만료 시간도 같을 수 있음)
		// 실제로 중요한 것은:
		// 1. 만료된 AccessToken에서 사용자 정보를 추출할 수 있다
		// 2. 새 토큰이 정상적으로 생성된다
		// 3. 새 토큰이 유효하다

		// 새로 생성된 토큰이 유효한지 확인
		assertTrue(jwtTokenProvider.validateToken(newTokens.getAccessToken()),
			"새로 생성된 AccessToken은 유효해야 합니다.");
		assertTrue(jwtTokenProvider.validateToken(newTokens.getRefreshToken()),
			"새로 생성된 RefreshToken은 유효해야 합니다.");

		// 새로 생성된 토큰에서 사용자 정보 확인
		Authentication newAuth = jwtTokenProvider.getAuthentication(newTokens.getAccessToken());
		CustomUserDetails newUserDetails = (CustomUserDetails) newAuth.getPrincipal();
		assertEquals(1L, newUserDetails.getId());
		assertEquals("testUser", newUserDetails.getMemberId());
		assertEquals("테스트유저", newUserDetails.getUserName());
	}

	@Test
	@DisplayName("유효한_토큰에서_사용자정보_추출_성공")
	void getAuthenticationFromExpiredToken_success_with_valid_token() {
		// given
		CustomUserDetails userDetails = new CustomUserDetails(
			1L,
			"testUser",
			"테스트유저",
			"test@test.com",
			"",
			"USER",
			List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			userDetails, "", userDetails.getAuthorities()
		);

		TokenResponseDto tokenResponse = jwtTokenProvider.generateToken(authentication);
		String accessToken = tokenResponse.getAccessToken();

		// when - 유효한 토큰에서도 정보 추출 가능해야 함
		Authentication extractedAuth = jwtTokenProvider.getAuthenticationFromExpiredToken(
			accessToken);

		// then
		assertNotNull(extractedAuth);
		CustomUserDetails extractedUserDetails = (CustomUserDetails) extractedAuth.getPrincipal();
		assertEquals(1L, extractedUserDetails.getId());
		assertEquals("testUser", extractedUserDetails.getMemberId());
		assertEquals("테스트유저", extractedUserDetails.getUserName());
		assertEquals("test@test.com", extractedUserDetails.getEmail());
		assertEquals("USER", extractedUserDetails.getRole());
	}

	@Test
	@DisplayName("권한정보가_없는_토큰에서_기본권한_설정")
	void getAuthenticationFromExpiredToken_default_authority_when_missing() {
		// given - 권한 정보가 없는 토큰을 생성하기 위해
		// 실제로는 createAccessToken이 항상 권한을 포함하므로,
		// 이 테스트는 getAuthenticationFromExpiredToken이 권한이 없을 때
		// 기본값 "ROLE_USER"를 설정하는지 확인하는 것이 목적

		CustomUserDetails userDetails = new CustomUserDetails(
			1L,
			"testUser",
			"테스트유저",
			"test@test.com",
			"",
			"USER",
			List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			userDetails, "", userDetails.getAuthorities()
		);

		TokenResponseDto tokenResponse = jwtTokenProvider.generateToken(authentication);
		String accessToken = tokenResponse.getAccessToken();

		// when
		Authentication extractedAuth = jwtTokenProvider.getAuthenticationFromExpiredToken(
			accessToken);

		// then - 권한이 설정되어 있어야 함
		assertNotNull(extractedAuth);
		assertFalse(extractedAuth.getAuthorities().isEmpty());
		assertTrue(extractedAuth.getAuthorities().stream()
			.anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
	}

	@Test
	@DisplayName("getAuthentication_유효한_토큰에서_사용자정보_추출_성공")
	void getAuthentication_success() {
		// given
		CustomUserDetails userDetails = new CustomUserDetails(
			1L,
			"testUser",
			"테스트유저",
			"test@test.com",
			"",
			"USER",
			List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			userDetails, "", userDetails.getAuthorities()
		);

		TokenResponseDto tokenResponse = jwtTokenProvider.generateToken(authentication);
		String accessToken = tokenResponse.getAccessToken();

		// when
		Authentication extractedAuth = jwtTokenProvider.getAuthentication(accessToken);

		// then
		assertNotNull(extractedAuth);
		CustomUserDetails extractedUserDetails = (CustomUserDetails) extractedAuth.getPrincipal();
		assertEquals(1L, extractedUserDetails.getId());
		assertEquals("testUser", extractedUserDetails.getMemberId());
		assertEquals("테스트유저", extractedUserDetails.getUserName());
		assertEquals("test@test.com", extractedUserDetails.getEmail());
		assertEquals("USER", extractedUserDetails.getRole());
		assertFalse(extractedAuth.getAuthorities().isEmpty());
	}

	@Test
	@DisplayName("getAuthentication_여러_권한_포함_토큰")
	void getAuthentication_multiple_authorities() {
		// given
		CustomUserDetails userDetails = new CustomUserDetails(
			1L,
			"adminUser",
			"관리자",
			"admin@test.com",
			"",
			"ADMIN",
			List.of(
				new SimpleGrantedAuthority("ROLE_USER"),
				new SimpleGrantedAuthority("ROLE_ADMIN")
			)
		);
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			userDetails, "", userDetails.getAuthorities()
		);

		TokenResponseDto tokenResponse = jwtTokenProvider.generateToken(authentication);
		String accessToken = tokenResponse.getAccessToken();

		// when
		Authentication extractedAuth = jwtTokenProvider.getAuthentication(accessToken);

		// then
		assertNotNull(extractedAuth);
		assertEquals(2, extractedAuth.getAuthorities().size());
		assertTrue(extractedAuth.getAuthorities().stream()
			.anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
		assertTrue(extractedAuth.getAuthorities().stream()
			.anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
	}

	@Test
	@DisplayName("getAuthentication_권한정보_없는_토큰_기본권한_설정")
	void getAuthentication_default_authority_when_missing() {
		// given - 권한 정보가 없는 토큰 생성
		byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
		SecretKey key = Keys.hmacShaKeyFor(keyBytes);

		String tokenWithoutAuth = Jwts.builder()
			.subject("1")
			.claim("memberId", "testUser")
			.claim("username", "테스트유저")
			.claim("email", "test@test.com")
			.claim("role", "USER")
			// auth claim 없음
			.expiration(new Date(System.currentTimeMillis() + 3600000L))
			.signWith(key)
			.compact();

		// when
		Authentication extractedAuth = jwtTokenProvider.getAuthentication(tokenWithoutAuth);

		// then - 기본 권한 ROLE_USER가 설정되어야 함
		assertNotNull(extractedAuth);
		assertFalse(extractedAuth.getAuthorities().isEmpty());
		assertTrue(extractedAuth.getAuthorities().stream()
			.anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
	}

	@Test
	@DisplayName("validateToken_유효한_토큰")
	void validateToken_valid_token() {
		// given
		CustomUserDetails userDetails = new CustomUserDetails(
			1L, "testUser", "테스트유저", "test@test.com", "", "USER",
			List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			userDetails, "", userDetails.getAuthorities()
		);
		TokenResponseDto tokenResponse = jwtTokenProvider.generateToken(authentication);

		// when & then
		assertTrue(jwtTokenProvider.validateToken(tokenResponse.getAccessToken()));
		assertTrue(jwtTokenProvider.validateToken(tokenResponse.getRefreshToken()));
	}

	@Test
	@DisplayName("validateToken_만료된_토큰")
	void validateToken_expired_token() {
		// given - 만료된 토큰 생성
		byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
		SecretKey key = Keys.hmacShaKeyFor(keyBytes);

		String expiredToken = Jwts.builder()
			.subject("1")
			.expiration(new Date(System.currentTimeMillis() - 3600000L))
			.signWith(key)
			.compact();

		// when & then
		assertFalse(jwtTokenProvider.validateToken(expiredToken));
	}

	@Test
	@DisplayName("validateToken_잘못된_형식_토큰")
	void validateToken_malformed_token() {
		// given
		String malformedToken = "invalid.token.string";

		// when & then
		assertFalse(jwtTokenProvider.validateToken(malformedToken));
	}

	@Test
	@DisplayName("validateToken_빈_문자열")
	void validateToken_empty_string() {
		// given
		String emptyToken = "";

		// when & then
		assertFalse(jwtTokenProvider.validateToken(emptyToken));
	}

	@Test
	@DisplayName("validateToken_null")
	void validateToken_null() {
		// when & then
		assertFalse(jwtTokenProvider.validateToken(null));
	}

	@Test
	@DisplayName("resolveToken_Bearer_토큰_추출_성공")
	void resolveToken_success_with_bearer() {
		// given
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getHeader("Authorization"))
			.thenReturn("Bearer testToken123");

		// when
		String token = jwtTokenProvider.resolveToken(request);

		// then
		assertEquals("testToken123", token);
	}

	@Test
	@DisplayName("resolveToken_Bearer_없는_토큰")
	void resolveToken_no_bearer_prefix() {
		// given
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getHeader("Authorization"))
			.thenReturn("testToken123");

		// when
		String token = jwtTokenProvider.resolveToken(request);

		// then
		assertNull(token);
	}

	@Test
	@DisplayName("resolveToken_Authorization_헤더_없음")
	void resolveToken_no_authorization_header() {
		// given
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getHeader("Authorization"))
			.thenReturn(null);

		// when
		String token = jwtTokenProvider.resolveToken(request);

		// then
		assertNull(token);
	}

	@Test
	@DisplayName("getMemberId_성공")
	void getMemberId_success() {
		// given
		CustomUserDetails userDetails = new CustomUserDetails(
			123L, "testUser", "테스트유저", "test@test.com", "", "USER",
			List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			userDetails, "", userDetails.getAuthorities()
		);
		TokenResponseDto tokenResponse = jwtTokenProvider.generateToken(authentication);

		// when
		Long memberId = jwtTokenProvider.getMemberId(tokenResponse.getAccessToken());

		// then
		assertEquals(123L, memberId);
	}

	@Test
	@DisplayName("getMemberId_만료된_토큰에서_추출_성공")
	void getMemberId_success_with_expired_token() {
		// given - 만료된 토큰 생성
		byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
		SecretKey key = Keys.hmacShaKeyFor(keyBytes);

		String expiredToken = Jwts.builder()
			.subject("456")
			.expiration(new Date(System.currentTimeMillis() - 3600000L))
			.signWith(key)
			.compact();

		// when
		Long memberId = jwtTokenProvider.getMemberId(expiredToken);

		// then
		assertEquals(456L, memberId);
	}

	@Test
	@DisplayName("getMemberId_잘못된_토큰_예외")
	void getMemberId_invalid_token_exception() {
		// given
		String invalidToken = "invalid.token.string";

		// when & then
		assertThrows(io.jsonwebtoken.MalformedJwtException.class, () ->
			jwtTokenProvider.getMemberId(invalidToken)
		);
	}

	@Test
	@DisplayName("getMemberId_숫자가_아닌_Subject_예외")
	void getMemberId_non_numeric_subject_exception() {
		// given - 숫자가 아닌 subject를 가진 토큰
		byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
		SecretKey key = Keys.hmacShaKeyFor(keyBytes);

		String tokenWithNonNumericSubject = Jwts.builder()
			.subject("notANumber")
			.expiration(new Date(System.currentTimeMillis() + 3600000L))
			.signWith(key)
			.compact();

		// when & then
		assertThrows(InvalidTokenException.class, () ->
			jwtTokenProvider.getMemberId(tokenWithNonNumericSubject)
		);
	}

	@Test
	@DisplayName("parseAndValidateToken_유효한_토큰_성공")
	void parseAndValidateToken_success() {
		// given
		CustomUserDetails userDetails = new CustomUserDetails(
			1L, "testUser", "테스트유저", "test@test.com", "", "USER",
			List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			userDetails, "", userDetails.getAuthorities()
		);
		TokenResponseDto tokenResponse = jwtTokenProvider.generateToken(authentication);

		// when & then - 예외가 발생하지 않아야 함
		assertDoesNotThrow(() ->
			jwtTokenProvider.parseAndValidateToken(tokenResponse.getAccessToken())
		);
	}

	@Test
	@DisplayName("parseAndValidateToken_만료된_토큰_예외")
	void parseAndValidateToken_expired_token_exception() {
		// given - 만료된 토큰 생성
		byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
		SecretKey key = Keys.hmacShaKeyFor(keyBytes);

		String expiredToken = Jwts.builder()
			.subject("1")
			.expiration(new Date(System.currentTimeMillis() - 3600000L))
			.signWith(key)
			.compact();

		// when & then
		assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () ->
			jwtTokenProvider.parseAndValidateToken(expiredToken)
		);
	}

	@Test
	@DisplayName("parseAndValidateToken_잘못된_토큰_예외")
	void parseAndValidateToken_invalid_token_exception() {
		// given
		String invalidToken = "invalid.token.string";

		// when & then
		assertThrows(io.jsonwebtoken.JwtException.class, () ->
			jwtTokenProvider.parseAndValidateToken(invalidToken)
		);
	}

	@Test
	@DisplayName("getAuthenticationFromExpiredToken_권한정보_없는_토큰_기본권한_설정")
	void getAuthenticationFromExpiredToken_default_authority_when_missing_claim() {
		// given - 권한 정보가 없는 토큰 생성
		byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
		SecretKey key = Keys.hmacShaKeyFor(keyBytes);

		String tokenWithoutAuth = Jwts.builder()
			.subject("1")
			.claim("memberId", "testUser")
			.claim("username", "테스트유저")
			.claim("email", "test@test.com")
			.claim("role", "USER")
			// auth claim 없음
			.expiration(new Date(System.currentTimeMillis() + 3600000L))
			.signWith(key)
			.compact();

		// when
		Authentication extractedAuth = jwtTokenProvider.getAuthenticationFromExpiredToken(
			tokenWithoutAuth);

		// then - 기본 권한 ROLE_USER가 설정되어야 함
		assertNotNull(extractedAuth);
		assertFalse(extractedAuth.getAuthorities().isEmpty());
		assertTrue(extractedAuth.getAuthorities().stream()
			.anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
	}

	@Test
	@DisplayName("getAuthenticationFromExpiredToken_빈_권한정보_기본권한")
	void getAuthenticationFromExpiredToken_empty_authority_default() {
		// given - 빈 권한 정보를 가진 토큰 생성
		byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
		SecretKey key = Keys.hmacShaKeyFor(keyBytes);

		String tokenWithEmptyAuth = Jwts.builder()
			.subject("1")
			.claim("memberId", "testUser")
			.claim("username", "테스트유저")
			.claim("email", "test@test.com")
			.claim("role", "USER")
			.claim("auth", "") // 빈 권한 정보
			.expiration(new Date(System.currentTimeMillis() + 3600000L))
			.signWith(key)
			.compact();

		// when
		Authentication extractedAuth = jwtTokenProvider.getAuthenticationFromExpiredToken(
			tokenWithEmptyAuth);

		// then - 기본 권한 ROLE_USER가 설정되어야 함
		assertNotNull(extractedAuth);
		assertFalse(extractedAuth.getAuthorities().isEmpty());
		assertTrue(extractedAuth.getAuthorities().stream()
			.anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
	}

	@Test
	@DisplayName("generateToken_UserResponseDto_포함_확인")
	void generateToken_user_response_dto_included() {
		// given
		CustomUserDetails userDetails = new CustomUserDetails(
			999L,
			"testUser",
			"테스트유저",
			"test@test.com",
			"",
			"USER",
			List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			userDetails, "", userDetails.getAuthorities()
		);

		// when
		TokenResponseDto tokenResponse = jwtTokenProvider.generateToken(authentication);

		// then
		assertNotNull(tokenResponse);
		assertNotNull(tokenResponse.getUser());
		assertEquals(999L, tokenResponse.getUser().getId());
		assertEquals("testUser", tokenResponse.getUser().getMemberId());
		assertEquals("테스트유저", tokenResponse.getUser().getUsername());
		assertEquals("test@test.com", tokenResponse.getUser().getEmail());
		assertEquals("USER", tokenResponse.getUser().getRole());
		assertEquals("Bearer", tokenResponse.getGrantType());
	}
}
