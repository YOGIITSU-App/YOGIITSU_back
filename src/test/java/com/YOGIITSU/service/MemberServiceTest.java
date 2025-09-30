package com.YOGIITSU.service;

import com.YOGIITSU.exception.auth.InvalidLoginException;
import com.YOGIITSU.dto.ResponseDto.TokenResponseDto;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.jwt.CustomUserDetails;
import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

	@Mock
	private AuthenticationManagerBuilder authenticationManagerBuilder;
	@Mock
	private AuthenticationManager authenticationManager;
	@Mock
	private JwtTokenProvider jwtTokenProvider;
	@Mock
	private Authentication authentication;

	@Mock
	private MemberRepository memberRepository;
	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private MemberService memberService;

	@DisplayName("로그인_성공")
	@Test
	void login_success() {
		// given
		String memberId = "user1";
		String password = "pass1";

		List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

		CustomUserDetails userDetails = new CustomUserDetails(
			1L, memberId, "박소미", "test@email.com", "", "USER", authorities
		);

		when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
		when(authenticationManager.authenticate(any())).thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn(userDetails);
		when(jwtTokenProvider.generateToken(any())).thenReturn(
			TokenResponseDto.builder()
				.accessToken("access")
				.refreshToken("refresh")
				.build()
		);

		// when
		ResponseEntity<Map<String, Object>> response = memberService.login(memberId, password);

		// then
		assertEquals("로그인 성공", response.getBody().get("message"));
		assertEquals(1L, response.getBody().get("userId"));
		assertEquals("USER", response.getBody().get("role"));
	}

	@DisplayName("로그인_실패")
	@Test
	void login_fail_invalid() {
		// given
		when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
		when(authenticationManager.authenticate(any()))
			.thenThrow(new BadCredentialsException("잘못된 비밀번호"));

		// then
		assertThrows(InvalidLoginException.class, () ->
			memberService.login("wrong", "wrong"));
	}
}
