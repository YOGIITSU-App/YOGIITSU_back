package com.YOGIITSU.service;

import com.YOGIITSU.dto.ResponseDto.TokenResponseDto;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.jwt.CustomUserDetails;
import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.util.AppleJwtUtil;
import com.YOGIITSU.util.ClientSecretProvider;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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
            log.info("[AppleAuthService] 애플 로그인 시작 - authorizationCode: {}", authorizationCode);

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

            log.info("[AppleAuthService] Apple 서버에 토큰 교환 요청 시작");
            ResponseEntity<Map> response = restTemplate.exchange(
                "https://appleid.apple.com/auth/token",
                HttpMethod.POST,
                request,
                Map.class
            );
            log.info("AppleAuthService] Apple 서버 응답 수신: {}", response.getStatusCode());

            if (response.getBody() == null || response.getBody().get("id_token") == null) {
                log.error("[AppleAuthService] Apple 토큰 응답이 비어 있음");
                throw new IllegalArgumentException("Apple 토큰 응답이 비어있음");
            }

            // 3. id_token 검증 및 claims 추출
            String idToken = (String) response.getBody().get("id_token");
            log.info("[AppleAuthService] id_token 추출 완료");
            Map<String, Object> claims = AppleJwtUtil.verifyAndGetClaims(idToken);
            log.info("[AppleAuthService] claims 추출 완료: {}", claims);
            
            String iss = (String) claims.get("iss");
            if (!"https://appleid.apple.com".equals(iss)) {
                log.error("[AppleAuthService] iss 검증 실패: {}", iss);
                throw new com.YOGIITSU.config.handler.GlobalExceptionHandler.AppleVerificationException();
            }

            String aud = (String) claims.get("aud");
            if (!clientSecretProvider.getClientId().equals(aud)) {
                log.error("[AppleAuthService] aud 검증 실패: {}", aud);
                throw new com.YOGIITSU.config.handler.GlobalExceptionHandler.AppleTokenInvalidException();
            }

            Number exp = (Number) claims.get("exp");
            if (exp == null || System.currentTimeMillis() >= exp.longValue() * 1000L) {
                log.error("[AppleAuthService] 토큰 만료됨");
                throw new com.YOGIITSU.config.handler.GlobalExceptionHandler.AppleTokenInvalidException();
            }

            String sub = (String) claims.get("sub");
            String email = (String) claims.get("email");
            log.info("[AppleAuthService] sub: {}, email: {}", sub, email);

            // 4. 사용자 등록/갱신 처리
            Member member = userService.processOAuthUser(
                "apple",
                (email != null) ? email : sub + "@appleuser.com",
                "AppleUser_" + sub.substring(0, 6)
            );
            log.info("[AppleAuthService] 사용자 등록/갱신 완료 - memberId: {}", member.getId());

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
            log.info("[AppleAuthService] JWT 발급 완료 - accessToken: {}", tokenResponse.getAccessToken());

            return tokenResponse;

        } catch (Exception e) {
            log.error("Apple 로그인 실패 : {}", e.getMessage());
            throw new org.springframework.security.authentication.AuthenticationServiceException(
                "Apple 로그인 실패", e
            );
        }
    }
}