package com.YOGIITSU.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AdminAuthorizationTest {

	private final AdminAuthorization adminAuthorization = new AdminAuthorization();

	@ParameterizedTest(name = "{0} -> {1}")
	@CsvSource({
		"ADMIN, true",
		"admin, true",
		"ROLE_ADMIN, true",
		"USER, false",
		"ROLE_USER, false",
		"ADMINISTRATOR, false"
	})
	@DisplayName("권한_값별_관리자_판별")
	void isAdmin_byAuthority(String authority, boolean expected) {
		var authentication = new UsernamePasswordAuthenticationToken(
			"user", "", List.of(new SimpleGrantedAuthority(authority)));

		assertEquals(expected, adminAuthorization.isAdmin(authentication));
	}

	@org.junit.jupiter.api.Test
	@DisplayName("인증정보_없으면_관리자_아님")
	void isAdmin_nullAuthentication() {
		assertFalse(adminAuthorization.isAdmin(null));
	}
}
