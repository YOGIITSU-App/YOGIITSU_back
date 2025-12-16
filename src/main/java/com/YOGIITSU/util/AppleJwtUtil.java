package com.YOGIITSU.util;

import com.YOGIITSU.exception.external.ApplePublicKeyNotFoundException;
import com.YOGIITSU.exception.external.AppleTokenInvalidException;
import com.YOGIITSU.exception.external.AppleVerificationException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import java.io.InputStream;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppleJwtUtil {

    private static final String APPLE_KEYS_URL = "https://appleid.apple.com/auth/keys";

    public static Map<String, Object> verifyAndGetClaims(String identityToken) {
        try {
            // 1. 토큰 파싱
            SignedJWT signedJWT = SignedJWT.parse(identityToken);
            String kid = signedJWT.getHeader().getKeyID();
            log.debug("[AppleJwtUtil] Apple identityToken 파싱 완료 - kid: {}", kid);

            // 2. 애플 공개키 세트 가져오기
            JWKSet jwkSet;
            try (InputStream is = new URL(APPLE_KEYS_URL).openStream()) {
                jwkSet = JWKSet.load(is);
            }
            log.debug("[AppleJwtUtil] Apple 공개키 세트 로드 완료");

            // 3. kid에 맞는 공개키 찾기
            JWK jwk = jwkSet.getKeyByKeyId(kid);
            if (jwk == null) {
                log.error("[AppleJwtUtil] kid에 해당하는 공개키를 찾을 수 없음: {}", kid);
                throw new ApplePublicKeyNotFoundException();
            }

            RSAKey rsaKey = (RSAKey) jwk;
            RSAPublicKey publicKey = rsaKey.toRSAPublicKey();

            // 4. 서명 검증
            JWSVerifier verifier = new RSASSAVerifier(publicKey);
            if (!signedJWT.verify(verifier)) {
                log.error("[AppleJwtUtil] Apple JWT 서명 검증 실패");
                throw new AppleTokenInvalidException();
            }

            // 5. payload 반환
            log.debug("[AppleJwtUtil] Apple JWT 서명 검증 성공, payload 추출 완료");
            return signedJWT.getJWTClaimsSet().getClaims();
        } catch (ApplePublicKeyNotFoundException | AppleTokenInvalidException e) {
            throw e;
        } catch (Exception e) {
            log.error("[AppleJwtUtil] Apple 토큰 검증 중 예외 발생", e);
            throw new AppleVerificationException();
        }
    }
}
