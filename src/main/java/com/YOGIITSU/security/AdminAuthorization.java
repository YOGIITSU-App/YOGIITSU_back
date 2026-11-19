package com.YOGIITSU.security;

import com.YOGIITSU.jwt.CustomUserDetails;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * 관리자 권한 판별
 * 회원 role은 "ADMIN"(ROLE_ 접두사 없음)으로 저장되므로 hasRole('ADMIN') 대신 사용한다.
 * 사용 예: @PreAuthorize("@adminAuthorization.isAdmin(authentication)")
 */
@Component("adminAuthorization")
public class AdminAuthorization {

	private static final String ADMIN = "ADMIN";
	private static final String ROLE_PREFIX = "ROLE_";

	public boolean isAdmin(Authentication authentication) {
		if (authentication == null
			|| !authentication.isAuthenticated()
			|| authentication instanceof AnonymousAuthenticationToken) {
			return false;
		}

		if (authentication.getPrincipal() instanceof CustomUserDetails userDetails
			&& isAdminValue(userDetails.getRole())) {
			return true;
		}

		return authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.anyMatch(AdminAuthorization::isAdminValue);
	}

	private static boolean isAdminValue(String value) {
		if (value == null) {
			return false;
		}
		String normalized = value.trim().toUpperCase();
		if (normalized.startsWith(ROLE_PREFIX)) {
			normalized = normalized.substring(ROLE_PREFIX.length());
		}
		return ADMIN.equals(normalized);
	}
}
