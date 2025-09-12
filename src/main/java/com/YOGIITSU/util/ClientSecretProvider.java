package com.YOGIITSU.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
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

    @Value("${apple.key-path}")
    private String keyPath;

    public String createClientSecret() {
        try {
            PrivateKey privateKey = leadPrivateKey(keyPath);

            Instant now = Instant.now();

            return Jwts.builder()
                .setHeaderParam("kid", keyId)
                .setIssuer(teamId)
                .setAudience("https://appleid.apple.com")
                .setSubject(clientId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(1800)))
                .signWith(privateKey, SignatureAlgorithm.ES256)
                .compact();
        } catch (Exception e) {
            throw new RuntimeException("애플 client_sercret 생성 실패", e);
        }
    }

    private PrivateKey leadPrivateKey(String keyPath) throws Exception {
        String key = new String(Files.readAllBytes(Paths.get(keyPath)))
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s", "");

        byte[] encoded = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return keyFactory.generatePrivate(keySpec);
    }

}
