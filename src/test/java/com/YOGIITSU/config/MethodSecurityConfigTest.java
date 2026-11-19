package com.YOGIITSU.config;

import com.YOGIITSU.controller.AdminInquiryController;
import com.YOGIITSU.jwt.CustomUserDetails;
import com.YOGIITSU.security.AdminAuthorization;
import com.YOGIITSU.service.AdminInquiryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 관리자 전용 API의 메서드 보안(@PreAuthorize) 적용 검증
 */
@SpringJUnitConfig({MethodSecurityConfig.class, AdminAuthorization.class, AdminInquiryController.class})
class MethodSecurityConfigTest {

	@Autowired
	private AdminInquiryController adminInquiryController;

	@MockitoBean
	private AdminInquiryService adminInquiryService;

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	private void loginAs(String role) {
		CustomUserDetails user = new CustomUserDetails(1L, "tester", "테스터", "t@suwon.ac.kr", "",
			role, List.of(new SimpleGrantedAuthority(role)));
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities()));
	}

	@Test
	@DisplayName("비로그인_사용자는_관리자_API_호출_불가")
	void anonymous_denied() {
		SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
			"key", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

		assertThrows(AccessDeniedException.class,
			() -> adminInquiryController.deleteInquiryByAdmin(1L, null));
		verify(adminInquiryService, never()).deleteInquiryByAdmin(anyLong());
	}

	@Test
	@DisplayName("일반_회원은_관리자_API_호출_불가")
	void user_denied() {
		loginAs("USER");

		assertThrows(AccessDeniedException.class,
			() -> adminInquiryController.deleteInquiryByAdmin(1L, null));
		verify(adminInquiryService, never()).deleteInquiryByAdmin(anyLong());
	}

	@Test
	@DisplayName("관리자는_관리자_API_호출_가능")
	void admin_allowed() {
		loginAs("ADMIN");

		assertDoesNotThrow(() -> adminInquiryController.deleteInquiryByAdmin(1L, null));
		verify(adminInquiryService).deleteInquiryByAdmin(1L);
	}
}
