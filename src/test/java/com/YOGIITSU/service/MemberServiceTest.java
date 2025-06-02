package com.YOGIITSU.service;

import com.YOGIITSU.config.handler.GlobalExceptionHandler;
import com.YOGIITSU.config.handler.GlobalExceptionHandler.InvalidLoginException;
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

	@DisplayName("회원 탈퇴 성공")
	@Test
	void deleteMember_success() {
		// given
		String memberId = "user123";
		String rawPassword = "password123";
		String encodedPassword = "encoded123";

		Member member = Member.builder()
			.id(1L)
			.memberId(memberId)
			.password(encodedPassword)
			.email("test@email.com")
			.userName("테스트유저")
			.role("USER")
			.joinAt(java.time.LocalDateTime.now())
			.build();

		when(memberRepository.findByMemberId(memberId)).thenReturn(java.util.Optional.of(member));
		when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
		when(memberRepository.existsByMemberId(memberId)).thenReturn(false); // 삭제 이후 체크

		// when
		memberService.deleteMember(memberId, rawPassword);

		// then
		verify(memberRepository).delete(member);
		verify(memberRepository).existsByMemberId(memberId);
	}

	@DisplayName("회원 탈퇴 실패 - 비밀번호 불일치")
	@Test
	void deleteMember_fail_wrongPassword() {
		// given
		String memberId = "user123";
		String rawPassword = "wrongPass";

		Member member = Member.builder()
			.id(1L)
			.memberId(memberId)
			.password("encoded123")
			.build();

		when(memberRepository.findByMemberId(memberId)).thenReturn(java.util.Optional.of(member));
		when(passwordEncoder.matches(rawPassword, "encoded123")).thenReturn(false);

		// then
		assertThrows(GlobalExceptionHandler.PasswordMismatchException.class, () ->
			memberService.deleteMember(memberId, rawPassword)
		);
	}

	@DisplayName("회원 탈퇴 실패 - 존재하지 않는 회원 ID")
	@Test
	void deleteMember_fail_memberNotFound() {
		// given
		String memberId = "nonexistent";
		String rawPassword = "anyPassword";

		when(memberRepository.findByMemberId(memberId)).thenReturn(java.util.Optional.empty());

		// then
		assertThrows(RuntimeException.class, () ->
			memberService.deleteMember(memberId, rawPassword)
		);
	}

	@DisplayName("회원 탈퇴 실패 - 비밀번호가 null일 경우")
	@Test
	void deleteMember_fail_nullPassword() {
		// given
		String memberId = "user123";
		String rawPassword = null;

		Member member = Member.builder()
			.id(1L)
			.memberId(memberId)
			.password("encodedPassword")
			.build();

		when(memberRepository.findByMemberId(memberId)).thenReturn(java.util.Optional.of(member));
		when(passwordEncoder.matches(null, "encodedPassword")).thenReturn(false);

		// then
		assertThrows(GlobalExceptionHandler.PasswordMismatchException.class, () ->
			memberService.deleteMember(memberId, rawPassword)
		);
	}

}
