package com.YOGIITSU.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.FileInputStream;
import java.io.InputStream;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Component
@Getter
public class ClientSecretProvider {

    @Value("${apple.team-id}")
    private String teamId;

    @Value("${apple.key-id}")
    private String keyId;

    @Value("${apple.client-id}")
    private String clientId;

    @Value("${apple.private-key-path}")
    private String privateKeyPath;

    public String createClientSecret() {
        try {
            PrivateKey privateKey = loadPrivateKey(privateKeyPath);
            Instant now = Instant.now();

            return Jwts.builder()
                .setHeaderParam("kid", keyId)
                .setIssuer(teamId)  // iss: Apple Developer Team ID
                .setAudience("https://appleid.apple.com")  // aud: 고정
                .setSubject(clientId)  // sub: Service ID
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(1800)))  // 30분 유효 (Apple은 최대 6개월 허용)
                .signWith(privateKey, SignatureAlgorithm.ES256)
                .compact();
        } catch (Exception e) {
            throw new RuntimeException("Apple client_secret 생성 실패", e);
        }
    }

    private PrivateKey loadPrivateKey(String keyPath) throws Exception {
        InputStream is;

        if (!keyPath.startsWith("file:")) {
            throw new IllegalArgumentException("지원하지 않는 keyPath 형식: " + keyPath);
        }

        String filePath = keyPath.replace("file:", "");
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] keyBytes = fis.readAllBytes();

            String privateKeyPem = new String(keyBytes)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

            byte[] decoded = Base64.getDecoder().decode(privateKeyPem);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return keyFactory.generatePrivate(keySpec);
        }
    }

}
