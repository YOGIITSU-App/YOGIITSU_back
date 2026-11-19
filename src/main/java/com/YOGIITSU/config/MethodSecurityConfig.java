package com.YOGIITSU.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * 메서드 단위 보안(@PreAuthorize) 활성화
 * 이 설정이 없으면 컨트롤러의 @PreAuthorize가 무시된다.
 */
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {
}
