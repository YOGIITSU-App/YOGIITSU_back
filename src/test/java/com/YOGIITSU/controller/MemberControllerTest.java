package com.YOGIITSU.controller;

import com.YOGIITSU.dto.RequestDto.ChangePasswordRequestDto;
import com.YOGIITSU.dto.RequestDto.FindMemberIdRequestDto;
import com.YOGIITSU.dto.RequestDto.MemberLoginRequestDto;
import com.YOGIITSU.dto.RequestDto.PasswordResetRequestDto;
import com.YOGIITSU.exception.user.MemberNotFoundException;
import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
	controllers = MemberController.class,
	excludeAutoConfiguration = {
		SecurityAutoConfiguration.class,
		UserDetailsServiceAutoConfiguration.class,
		OAuth2ClientAutoConfiguration.class,
		OAuth2ResourceServerAutoConfiguration.class
	}
)
class MemberControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private MemberService memberService;

	@MockBean
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private ObjectMapper objectMapper;

	@DisplayName("로그인_성공")
	@Test
	void login_success() throws Exception {
		// given
		MemberLoginRequestDto request = new MemberLoginRequestDto("user1", "password1");
		Map<String, Object> responseBody = new HashMap<>();
		responseBody.put("message", "로그인 성공");
		responseBody.put("userId", 1L);
		responseBody.put("role", "USER");

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer accessToken");
		headers.set("X-Refresh-Token", "refreshToken");

		org.springframework.http.ResponseEntity<Map<String, Object>> responseEntity =
			org.springframework.http.ResponseEntity.ok()
				.headers(headers)
				.body(responseBody);

		when(memberService.login("user1", "password1")).thenReturn(responseEntity);

		// when & then
		mockMvc.perform(post("/members/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(header().string("Authorization", "Bearer accessToken"))
			.andExpect(header().string("X-Refresh-Token", "refreshToken"))
			.andExpect(jsonPath("$.message").value("로그인 성공"))
			.andExpect(jsonPath("$.userId").value(1L))
			.andExpect(jsonPath("$.role").value("USER"));

		verify(memberService).login("user1", "password1");
	}

	@DisplayName("아이디찾기_성공")
	@Test
	void findId_success() throws Exception {
		// given
		FindMemberIdRequestDto request = new FindMemberIdRequestDto("test@email.com");
		String memberId = "user1";

		when(memberService.findIdByEmail("test@email.com")).thenReturn(memberId);

		// when & then
		mockMvc.perform(post("/members/find-id")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("이메일 정보와 일치하는 아이디가 있습니다."))
			.andExpect(jsonPath("$.id").value(memberId));

		verify(memberService).findIdByEmail("test@email.com");
	}

	@DisplayName("아이디찾기_실패_이메일없음")
	@Test
	void findId_fail_emailNotRegistered() throws Exception {
		// given
		FindMemberIdRequestDto request = new FindMemberIdRequestDto("notfound@email.com");

		when(memberService.findIdByEmail("notfound@email.com")).thenReturn(null);

		// when & then
		mockMvc.perform(post("/members/find-id")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound());

		verify(memberService).findIdByEmail("notfound@email.com");
	}

	@DisplayName("회원탈퇴_성공")
	@Test
	@WithMockUser
	void deleteMember_success() throws Exception {
		// given
		String memberId = "user1";
		String accessToken = "validToken";

		Authentication authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn(memberId);

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(accessToken);
		when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
		when(jwtTokenProvider.getAuthentication(accessToken)).thenReturn(authentication);
		doNothing().when(memberService).deleteMember(memberId);

		// when & then
		mockMvc.perform(delete("/members/delete")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value(memberId + "님의 회원 탈퇴가 완료되었습니다."));

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(jwtTokenProvider).getAuthentication(accessToken);
		verify(memberService).deleteMember(memberId);
	}

	@DisplayName("회원탈퇴_실패_토큰없음")
	@Test
	void deleteMember_fail_missingToken() throws Exception {
		// given
		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(null);

		// when & then
		mockMvc.perform(delete("/members/delete")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider, never()).validateToken(any());
		verify(memberService, never()).deleteMember(any());
	}

	@DisplayName("회원탈퇴_실패_유효하지않은토큰")
	@Test
	void deleteMember_fail_invalidToken() throws Exception {
		// given
		String accessToken = "invalidToken";

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(accessToken);
		when(jwtTokenProvider.validateToken(accessToken)).thenReturn(false);

		// when & then
		mockMvc.perform(delete("/members/delete")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(memberService, never()).deleteMember(any());
	}

	@DisplayName("회원탈퇴_실패_회원없음")
	@Test
	@WithMockUser
	void deleteMember_fail_memberNotFound() throws Exception {
		// given
		String memberId = "notfound";
		String accessToken = "validToken";

		Authentication authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn(memberId);

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(accessToken);
		when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
		when(jwtTokenProvider.getAuthentication(accessToken)).thenReturn(authentication);
		doThrow(new MemberNotFoundException()).when(memberService).deleteMember(memberId);

		// when & then
		mockMvc.perform(delete("/members/delete")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(memberService).deleteMember(memberId);
	}

	@DisplayName("비밀번호변경_성공")
	@Test
	@WithMockUser
	void changePassword_success() throws Exception {
		// given
		String memberId = "user1";
		String accessToken = "validToken";
		ChangePasswordRequestDto request = new ChangePasswordRequestDto("newPassword", "newPassword");

		Authentication authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn(memberId);

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(accessToken);
		when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
		when(jwtTokenProvider.getAuthentication(accessToken)).thenReturn(authentication);
		doNothing().when(memberService).changePassword(memberId, "newPassword", "newPassword");

		// when & then
		mockMvc.perform(patch("/members/change-password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("비밀번호가 성공적으로 변경되었습니다."));

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(jwtTokenProvider).getAuthentication(accessToken);
		verify(memberService).changePassword(memberId, "newPassword", "newPassword");
	}

	@DisplayName("비밀번호변경_실패_토큰없음")
	@Test
	void changePassword_fail_missingToken() throws Exception {
		// given
		ChangePasswordRequestDto request = new ChangePasswordRequestDto("newPassword", "newPassword");

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(null);

		// when & then
		mockMvc.perform(patch("/members/change-password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider, never()).validateToken(any());
		verify(memberService, never()).changePassword(any(), any(), any());
	}

	@DisplayName("비밀번호변경_실패_유효하지않은토큰")
	@Test
	void changePassword_fail_invalidToken() throws Exception {
		// given
		String accessToken = "invalidToken";
		ChangePasswordRequestDto request = new ChangePasswordRequestDto("newPassword", "newPassword");

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(accessToken);
		when(jwtTokenProvider.validateToken(accessToken)).thenReturn(false);

		// when & then
		mockMvc.perform(patch("/members/change-password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(memberService, never()).changePassword(any(), any(), any());
	}

	@DisplayName("비밀번호재설정_성공")
	@Test
	void resetPassword_success() throws Exception {
		// given
		PasswordResetRequestDto request = new PasswordResetRequestDto(
			"test@email.com", "newPassword", "newPassword");

		doNothing().when(memberService).resetPasswordAfterEmailVerification(any(PasswordResetRequestDto.class));

		// when & then
		mockMvc.perform(post("/members/find-password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("비밀번호가 성공적으로 변경되었습니다."));

		verify(memberService).resetPasswordAfterEmailVerification(any(PasswordResetRequestDto.class));
	}

	@DisplayName("전체회원수조회_성공")
	@Test
	void getMemberCount_success() throws Exception {
		// given
		Long memberCount = 100L;

		when(memberService.getMemberCount()).thenReturn(memberCount);

		// when & then
		mockMvc.perform(get("/members/count")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.memberCount").value(memberCount));

		verify(memberService).getMemberCount();
	}
}

