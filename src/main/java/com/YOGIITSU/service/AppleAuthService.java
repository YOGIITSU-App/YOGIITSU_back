package com.YOGIITSU.service;

import com.YOGIITSU.dto.ResponseDto.TokenResponseDto;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.exception.external.AppleExchangeException;
import com.YOGIITSU.exception.external.AppleTokenInvalidException;
import com.YOGIITSU.exception.external.AppleVerificationException;
import com.YOGIITSU.jwt.CustomUserDetails;
import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.util.AppleJwtUtil;
import com.YOGIITSU.util.ClientSecretProvider;
import java.util.Date;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class AppleAuthService {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ClientSecretProvider clientSecretProvider;
    private final RestTemplate restTemplate;

    @Transactional
    public TokenResponseDto loginWithApple(String authorizationCode) {
        try {
            log.info("[AppleAuthService] 애플 로그인 시작 ");

            // 1. client_secret 생성
            String clientSecret = clientSecretProvider.createClientSecret();
            log.info("[AppleAuthService] client_secret 생성 완료");

            // 2. Apple 서버 토큰 교환 요청
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientSecretProvider.getClientId());
            body.add("client_secret", clientSecret);
            body.add("code", authorizationCode);
            body.add("grant_type", "authorization_code");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response;
            try {
                log.info("[AppleAuthService] Apple 서버에 토큰 교환 요청 시작");
                response = restTemplate.exchange(
                    "https://appleid.apple.com/auth/token",
                    HttpMethod.POST,
                    request,
                    Map.class
                );
                log.info("[AppleAuthService] Apple 서버 응답 수신: {}", response.getStatusCode());
            } catch (Exception e) {
                log.error("[AppleAuthService] Apple 서버 요청 실패", e);
                throw new AppleExchangeException(-1, "Apple 서버 요청 자체 실패");
            }

            if (response.getBody() == null || response.getBody().get("id_token") == null) {
                log.error("[AppleAuthService] Apple 토큰 응답이 비어 있음");
                throw new AppleExchangeException(
                    response.getStatusCode().value(),
                    response.getBody() != null ? response.getBody().toString() : "empty body");
            }

            // 3. id_token 검증 및 claims 추출
            String idToken = response.getBody().get("id_token").toString();
            log.info("[AppleAuthService] id_token 추출 완료");

            Map<String, Object> claims;
            try {
                claims = AppleJwtUtil.verifyAndGetClaims(idToken);
                log.debug("[AppleAuthService] claims 추출 완료 - keys: {}", claims.keySet());
            } catch (Exception e) {
                log.error("[AppleAuthService] id_token 검증 실패", e);
                throw new AppleVerificationException();
            }

            String iss = getClaimAsString(claims, "iss");
            if (!"https://appleid.apple.com".equals(iss)) {
                log.error("[AppleAuthService] iss 검증 실패: {}", iss);
                throw new AppleVerificationException();
            }

            String aud = getClaimAsString(claims, "aud");
            if (!clientSecretProvider.getClientId().equals(aud)) {
                log.error("[AppleAuthService] aud 검증 실패");
                log.debug("[AppleAuthService] aud 값: {}", aud);
                throw new AppleTokenInvalidException();
            }

            // exp 검증 (Number / Date 모두 처리)
            Object expObj = claims.get("exp");
            long expTimeMillis;
            if (expObj instanceof Number) {
                expTimeMillis = ((Number) expObj).longValue() * 1000L;
            } else if (expObj instanceof Date) {
                expTimeMillis = ((Date) expObj).getTime();
            } else {
                log.error("[AppleAuthService] exp 클레임 타입 예외: {}", expObj);
                throw new AppleTokenInvalidException();
            }

            if (System.currentTimeMillis() >= expTimeMillis) {
                log.error("[AppleAuthService] 토큰 만료됨");
                throw new AppleTokenInvalidException();
            }

            String sub = getClaimAsString(claims, "sub");
            String email = getClaimAsString(claims, "email");
            log.debug("[AppleAuthService] 사용자 정보 추출 완료");

            // 4. 사용자 등록/갱신 처리
            Member member = userService.processOAuthUser(
                "apple",
                (email != null) ? email : sub + "@appleuser.com",
                "AppleUser_" + sub.substring(0, 6)
            );
            log.info("[AppleAuthService] 사용자 등록/갱신 완료");

            // 5. 인증 객체 생성
            CustomUserDetails userDetails = new CustomUserDetails(
                member.getId(),
                member.getMemberId(),
                member.getUserName(),
                member.getEmail(),
                member.getPassword(),
                member.getRole(),
                member.getAuthorities()
            );

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
            );
            log.info("[AppleAuthService] 인증 객체 생성 완료");

            // 6. JWT 발급
            TokenResponseDto tokenResponse = jwtTokenProvider.generateToken(authentication);
            log.info("[AppleAuthService] JWT 발급 완료");

            return tokenResponse;

        } catch (AppleVerificationException | AppleTokenInvalidException |
                 AppleExchangeException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("[AppleAuthService] Apple 로그인 처리 중 오류 발생", e);
            throw new AppleExchangeException(-1, "Apple 로그인 중 내부 오류 발생");
        }
    }

    /**
     * Claim을 안전하게 String으로 변환
     */
    private String getClaimAsString(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof java.util.List) {
            return ((java.util.List<?>) value).isEmpty() ? null
                : value.toString().replaceAll("[\\[\\]]", "");
        }
        return null;
    }
}