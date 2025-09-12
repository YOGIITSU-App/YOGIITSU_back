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
    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public TokenResponseDto loginWithApple(String authorizationCode) {
        try {
            // 1. client_secret 생성
            String clientSecret = clientSecretProvider.createClientSecret();

            // 2. Apple 서버 토큰 교환 요청
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientSecretProvider.getClientId());
            body.add("client_secret", clientSecret);
            body.add("code", authorizationCode);
            body.add("grant_type", "authorization_code");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                "https://appleid.apple.com/auth/token",
                HttpMethod.POST,
                request,
                Map.class
            );

            String idToken = (String) response.getBody().get("id_token");
            Map<String, Object> claims = AppleJwtUtil.verifyAndGetClaims(idToken);

            String sub = (String) claims.get("sub");
            String email = (String) claims.get("email");

            Member member = userService.processOAuthUser(
                "apple",
                (email != null) ? email : sub + "@appleuser.com",
                "AppleUser_" + sub.substring(0, 6)
            );

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

            return jwtTokenProvider.generateToken(authentication);
        } catch (Exception e) {
            log.error("Apple 로그인 실패 : {}", e.getMessage());
            throw new org.springframework.security.authentication.AuthenticationServiceException(
                "Apple 로그인 실패", e
            );
        }
    }
}