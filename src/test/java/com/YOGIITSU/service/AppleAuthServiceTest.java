package com.YOGIITSU.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.YOGIITSU.dto.ResponseDto.TokenResponseDto;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.exception.external.AppleExchangeException;
import com.YOGIITSU.exception.external.AppleTokenInvalidException;
import com.YOGIITSU.exception.external.AppleVerificationException;
import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.util.AppleJwtUtil;
import com.YOGIITSU.util.ClientSecretProvider;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class AppleAuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private ClientSecretProvider clientSecretProvider;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AppleAuthService appleAuthService;

    /* ================= Helpers ================= */
    private static final String TOKEN_URL = "https://appleid.apple.com/auth/token";

    private void givenClient(String clientId, String clientSecret) {
        when(clientSecretProvider.getClientId()).thenReturn(clientId);
        when(clientSecretProvider.createClientSecret()).thenReturn(clientSecret);
    }

    private void givenAppleExchangeSuccess(String idToken) {
        Map<String, Object> body = new HashMap<>();
        body.put("id_token", idToken);
        ResponseEntity<Map> response = new ResponseEntity<>(body, HttpStatus.OK);

        when(restTemplate.exchange(eq(TOKEN_URL), eq(HttpMethod.POST), any(HttpEntity.class),
            eq(Map.class))).thenReturn(response);
    }

    private Map<String, Object> baseClaims(String clientId, Object exp, String sub, Object email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", "https://appleid.apple.com");
        claims.put("aud", clientId);
        claims.put("exp", exp);
        claims.put("sub", sub);
        claims.put("email", email);
        return claims;
    }

    private Member dummyMember(String email) {
        return Member.builder().id(1L).memberId("apple_member_1").userName("AppleUser_test")
            .email(email).password("pw").role("ROLE_USER").build();
    }

    /* ================= Success: 애플로그인 성공 ================= */
    @DisplayName("애플로그인_성공")
    @Test
    void loginWithApple_success() {
        String code = "auth-code";
        String clientId = "client-id";
        String clientSecret = "client-secret";
        String idToken = "id-token";
        String sub = "sub1234567890";

        givenClient(clientId, clientSecret);
        givenAppleExchangeSuccess(idToken);

        long exp = (System.currentTimeMillis() + 60_000) / 1000;
        Map<String, Object> claims = baseClaims(clientId, exp, sub, "test@apple.com");
        claims.put("aud", List.of(clientId));

        when(userService.processOAuthUser(eq("apple"), anyString(), anyString()))
            .thenReturn(dummyMember("test@apple.com"));
        when(jwtTokenProvider.generateToken(any(Authentication.class)))
            .thenReturn(mock(TokenResponseDto.class));

        try (MockedStatic<AppleJwtUtil> mocked = mockStatic(AppleJwtUtil.class)) {
            mocked.when(() -> AppleJwtUtil.verifyAndGetClaims(idToken)).thenReturn(claims);

            assertDoesNotThrow(() -> appleAuthService.loginWithApple(code));
        }
    }

    @DisplayName("애플로그인_성공_email대체")
    @Test
    void loginWithApple_success_emailFallback() {
        String clientId = "client-id";
        String clientSecret = "client-secret";
        String idToken = "id-token";
        String sub = "sub123456";

        givenClient(clientId, clientSecret);
        givenAppleExchangeSuccess(idToken);

        long exp = (System.currentTimeMillis() + 60_000) / 1000;
        Map<String, Object> claims = baseClaims(clientId, exp, sub, null);
        claims.remove("email");

        when(userService.processOAuthUser(eq("apple"), anyString(), anyString()))
            .thenReturn(dummyMember(sub + "@appleuser.com"));
        when(jwtTokenProvider.generateToken(any(Authentication.class)))
            .thenReturn(mock(TokenResponseDto.class));

        try (MockedStatic<AppleJwtUtil> mocked = mockStatic(AppleJwtUtil.class)) {
            mocked.when(() -> AppleJwtUtil.verifyAndGetClaims(idToken)).thenReturn(claims);

            assertDoesNotThrow(() -> appleAuthService.loginWithApple("auth-code"));
        }
    }

    @DisplayName("애플로그인_성공_expDate")
    @Test
    void loginWithApple_success_expDate() {
        String clientId = "client-id";
        String clientSecret = "client-secret";
        String idToken = "id-token";
        String sub = "sub123456";

        givenClient(clientId, clientSecret);
        givenAppleExchangeSuccess(idToken);

        Map<String, Object> claims = baseClaims(clientId,
            new Date(System.currentTimeMillis() + 60_000), sub, "test@apple.com");

        when(userService.processOAuthUser(eq("apple"), anyString(), anyString()))
            .thenReturn(dummyMember("test@apple.com"));
        when(jwtTokenProvider.generateToken(any(Authentication.class)))
            .thenReturn(mock(TokenResponseDto.class));

        try (MockedStatic<AppleJwtUtil> mocked = mockStatic(AppleJwtUtil.class)) {
            mocked.when(() -> AppleJwtUtil.verifyAndGetClaims(idToken)).thenReturn(claims);

            assertDoesNotThrow(() -> appleAuthService.loginWithApple("auth-code"));
        }
    }

    /* ================= FAIL: 애플로그인 실패 ================= */

    @DisplayName("애플로그인_실패_exchange")
    @Test
    void loginWithApple_fail_exchange() {
        givenClient("client-id", "client-secret");

        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenThrow(new RuntimeException());

        assertThrows(AppleExchangeException.class,
            () -> appleAuthService.loginWithApple("auth-code"));
    }

    @DisplayName("애플로그인_실패_bodyNull")
    @Test
    void loginWithApple_fail_bodyNull() {
        givenClient("client-id", "client-secret");

        ResponseEntity<Map> response = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
            eq(TOKEN_URL),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(Map.class)
        )).thenReturn(response);

        assertThrows(AppleExchangeException.class,
            () -> appleAuthService.loginWithApple("auth-code"));
    }

    @DisplayName("애플로그인_실패_idToken없음")
    @Test
    void loginWithApple_fail_idTokenMissing() {
        givenClient("client-id", "client-secret");

        ResponseEntity<Map> response = new ResponseEntity<>(new HashMap<>(), HttpStatus.OK);

        when(restTemplate.exchange(eq(TOKEN_URL), eq(HttpMethod.POST), any(HttpEntity.class),
            eq(Map.class))).thenReturn(response);

        assertThrows(AppleExchangeException.class,
            () -> appleAuthService.loginWithApple("auth-code"));
    }

    @DisplayName("애플로그인_실패_verify")
    @Test
    void loginWithApple_fail_verify() {
        String idToken = "id-token";

        givenClient("client-id", "client-secret");
        givenAppleExchangeSuccess(idToken);

        try (MockedStatic<AppleJwtUtil> mocked = mockStatic(AppleJwtUtil.class)) {
            mocked.when(() -> AppleJwtUtil.verifyAndGetClaims(idToken))
                .thenThrow(new RuntimeException());

            assertThrows(AppleVerificationException.class,
                () -> appleAuthService.loginWithApple("auth-code"));
        }
    }

    @DisplayName("애플로그인_실패_iss")
    @Test
    void loginWithApple_fail_invalidIss() {
        String clientId = "client-id";
        String clientSecret = "client-secret";
        String idToken = "id-token";

        givenClient(clientId, clientSecret);
        givenAppleExchangeSuccess(idToken);

        long exp = (System.currentTimeMillis() + 60_000) / 1000;
        Map<String, Object> claims = baseClaims(clientId, exp, "sub123", "test@apple.com");
        claims.put("iss", "https://evil.com");

        try (MockedStatic<AppleJwtUtil> mocked = mockStatic(AppleJwtUtil.class)) {
            mocked.when(() -> AppleJwtUtil.verifyAndGetClaims(idToken)).thenReturn(claims);

            assertThrows(AppleVerificationException.class,
                () -> appleAuthService.loginWithApple("auth-code"));
        }
    }

    @DisplayName("애플로그인_실패_claim타입")
    @Test
    void loginWithApple_fail_invalidClaimType() {
        String clientId = "client-id";
        String clientSecret = "client-secret";
        String idToken = "id-token";

        givenClient(clientId, clientSecret);
        givenAppleExchangeSuccess(idToken);

        long exp = (System.currentTimeMillis() + 60_000) / 1000;
        Map<String, Object> claims = baseClaims(clientId, exp, "sub123", "test@apple.com");

        // iss를 String/List가 아닌 타입으로 넣어서 getClaimAsString()이 null 반환하도록 유도
        claims.put("iss", 123);

        try (MockedStatic<AppleJwtUtil> mocked = mockStatic(AppleJwtUtil.class)) {
            mocked.when(() -> AppleJwtUtil.verifyAndGetClaims(idToken)).thenReturn(claims);

            assertThrows(AppleVerificationException.class,
                () -> appleAuthService.loginWithApple("auth-code"));
        }
    }

    @DisplayName("애플로그인_실패_aud")
    @Test
    void loginWithApple_fail_invalidAud() {
        String clientId = "client-id";
        String clientSecret = "client-secret";
        String idToken = "id-token";

        givenClient(clientId, clientSecret);
        givenAppleExchangeSuccess(idToken);

        Map<String, Object> claims = baseClaims("other-client",
            (System.currentTimeMillis() + 60_000) / 1000, "sub123", "test@apple.com");

        try (MockedStatic<AppleJwtUtil> mocked = mockStatic(AppleJwtUtil.class)) {
            mocked.when(() -> AppleJwtUtil.verifyAndGetClaims(idToken)).thenReturn(claims);

            assertThrows(AppleTokenInvalidException.class,
                () -> appleAuthService.loginWithApple("auth-code"));
        }
    }

    @DisplayName("애플로그인_실패_exp타입")
    @Test
    void loginWithApple_fail_invalidExpType() {
        String clientId = "client-id";
        String clientSecret = "client-secret";
        String idToken = "id-token";

        givenClient(clientId, clientSecret);
        givenAppleExchangeSuccess(idToken);

        Map<String, Object> claims = baseClaims(clientId, "invalid", "sub123", "test@apple.com");

        try (MockedStatic<AppleJwtUtil> mocked = mockStatic(AppleJwtUtil.class)) {
            mocked.when(() -> AppleJwtUtil.verifyAndGetClaims(idToken)).thenReturn(claims);

            assertThrows(AppleTokenInvalidException.class,
                () -> appleAuthService.loginWithApple("auth-code"));
        }
    }

    @DisplayName("애플로그인_실패_exp만료")
    @Test
    void loginWithApple_fail_expired() {
        String clientId = "client-id";
        String clientSecret = "client-secret";
        String idToken = "id-token";

        givenClient(clientId, clientSecret);
        givenAppleExchangeSuccess(idToken);

        Map<String, Object> claims = baseClaims(clientId,
            (System.currentTimeMillis() - 60_000) / 1000, "sub123", "test@apple.com");

        try (MockedStatic<AppleJwtUtil> mocked = mockStatic(AppleJwtUtil.class)) {
            mocked.when(() -> AppleJwtUtil.verifyAndGetClaims(idToken)).thenReturn(claims);

            assertThrows(AppleTokenInvalidException.class,
                () -> appleAuthService.loginWithApple("auth-code"));
        }
    }

    @DisplayName("애플로그인_실패_internal")
    @Test
    void loginWithApple_fail_internal() {
        String clientId = "client-id";
        String clientSecret = "client-secret";
        String idToken = "id-token";

        givenClient(clientId, clientSecret);
        givenAppleExchangeSuccess(idToken);

        long exp = (System.currentTimeMillis() + 60_000) / 1000;
        Map<String, Object> claims = baseClaims(clientId, exp, "sub123", "test@apple.com");

        when(userService.processOAuthUser(anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("db error"));

        try (MockedStatic<AppleJwtUtil> mocked = mockStatic(AppleJwtUtil.class)) {
            mocked.when(() -> AppleJwtUtil.verifyAndGetClaims(idToken)).thenReturn(claims);

            assertThrows(AppleExchangeException.class,
                () -> appleAuthService.loginWithApple("auth-code"));
        }
    }
}
