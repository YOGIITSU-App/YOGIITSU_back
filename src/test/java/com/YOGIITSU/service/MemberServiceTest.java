package com.YOGIITSU.service;

import com.YOGIITSU.dto.RequestDto.PasswordResetRequestDto;
import com.YOGIITSU.exception.auth.EmailVerificationNotApprovedException;
import com.YOGIITSU.exception.auth.InvalidLoginException;
import com.YOGIITSU.dto.ResponseDto.TokenResponseDto;
import com.YOGIITSU.entity.EmailMessage;
import com.YOGIITSU.entity.EmailPurpose;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.exception.system.SystemException;
import com.YOGIITSU.exception.user.MemberNotFoundException;
import com.YOGIITSU.exception.user.PasswordMismatchException;
import com.YOGIITSU.exception.user.PasswordNotEqualsException;
import com.YOGIITSU.exception.user.SamePasswordException;
import com.YOGIITSU.jwt.CustomUserDetails;
import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.repository.EmailMessageRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
	@Mock
	private EmailMessageRepository emailMessageRepository;

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

	@DisplayName("이메일로아이디찾기_성공")
	@Test
	void findIdByEmail_success() {
		// given
		String email = "test@email.com";
		String memberId = "user1";
		Member member = createDummyMember(memberId, email);

		when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

		// when
		String result = memberService.findIdByEmail(email);

		// then
		assertEquals(memberId, result);
		verify(memberRepository).findByEmail(email);
	}

	@DisplayName("이메일로아이디찾기_실패_이메일없음")
	@Test
	void findIdByEmail_fail_emailNotFound() {
		// given
		String email = "notfound@email.com";

		when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

		// when
		String result = memberService.findIdByEmail(email);

		// then
		assertNull(result);
		verify(memberRepository).findByEmail(email);
	}

	@DisplayName("회원탈퇴_성공")
	@Test
	void deleteMember_success() {
		// given
		String memberId = "user1";
		Member member = createDummyMember(memberId, "test@email.com");

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(member));
		when(memberRepository.existsByMemberId(memberId)).thenReturn(false);
		doNothing().when(memberRepository).delete(member);

		// when
		assertDoesNotThrow(() -> memberService.deleteMember(memberId));

		// then
		verify(memberRepository).findByMemberId(memberId);
		verify(memberRepository).delete(member);
		verify(memberRepository).existsByMemberId(memberId);
	}

	@DisplayName("회원탈퇴_실패_회원없음")
	@Test
	void deleteMember_fail_memberNotFound() {
		// given
		String memberId = "notfound";

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

		// when, then
		assertThrows(MemberNotFoundException.class, () -> memberService.deleteMember(memberId));

		verify(memberRepository).findByMemberId(memberId);
		verify(memberRepository, never()).delete(any());
	}

	@DisplayName("회원탈퇴_실패_삭제실패")
	@Test
	void deleteMember_fail_deletionFailed() {
		// given
		String memberId = "user1";
		Member member = createDummyMember(memberId, "test@email.com");

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(member));
		when(memberRepository.existsByMemberId(memberId)).thenReturn(true);
		doNothing().when(memberRepository).delete(member);

		// when, then
		assertThrows(SystemException.class, () -> memberService.deleteMember(memberId));

		verify(memberRepository).findByMemberId(memberId);
		verify(memberRepository).delete(member);
		verify(memberRepository).existsByMemberId(memberId);
	}

	@DisplayName("비밀번호변경_성공")
	@Test
	void changePassword_success() {
		// given
		String memberId = "user1";
		String oldPassword = "oldPassword";
		String newPassword = "newPassword";
		String confirmPassword = "newPassword";
		Member member = createDummyMember(memberId, "test@email.com");
		member.changePassword(oldPassword);

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(member));
		when(passwordEncoder.matches(newPassword, oldPassword)).thenReturn(false);
		when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
		when(memberRepository.save(member)).thenReturn(member);

		// when
		assertDoesNotThrow(() -> memberService.changePassword(memberId, newPassword, confirmPassword));

		// then
		verify(memberRepository).findByMemberId(memberId);
		verify(passwordEncoder).matches(newPassword, oldPassword);
		verify(passwordEncoder).encode(newPassword);
		verify(memberRepository).save(member);
	}

	@DisplayName("비밀번호변경_실패_비밀번호불일치")
	@Test
	void changePassword_fail_passwordNotEquals() {
		// given
		String memberId = "user1";
		String newPassword = "newPassword";
		String confirmPassword = "differentPassword";

		// when, then
		assertThrows(PasswordNotEqualsException.class,
			() -> memberService.changePassword(memberId, newPassword, confirmPassword));

		verify(memberRepository, never()).findByMemberId(any());
	}

	@DisplayName("비밀번호변경_실패_회원없음")
	@Test
	void changePassword_fail_memberNotFound() {
		// given
		String memberId = "notfound";
		String newPassword = "newPassword";
		String confirmPassword = "newPassword";

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

		// when, then
		assertThrows(MemberNotFoundException.class,
			() -> memberService.changePassword(memberId, newPassword, confirmPassword));

		verify(memberRepository).findByMemberId(memberId);
		verify(passwordEncoder, never()).encode(any());
	}

	@DisplayName("비밀번호변경_실패_같은비밀번호")
	@Test
	void changePassword_fail_samePassword() {
		// given
		String memberId = "user1";
		String oldPassword = "oldPassword";
		String newPassword = "oldPassword";
		String confirmPassword = "oldPassword";
		Member member = createDummyMember(memberId, "test@email.com");
		member.changePassword(oldPassword);

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(member));
		when(passwordEncoder.matches(newPassword, oldPassword)).thenReturn(true);

		// when, then
		assertThrows(SamePasswordException.class,
			() -> memberService.changePassword(memberId, newPassword, confirmPassword));

		verify(memberRepository).findByMemberId(memberId);
		verify(passwordEncoder).matches(newPassword, oldPassword);
		verify(passwordEncoder, never()).encode(any());
	}

	@DisplayName("비밀번호재설정_성공")
	@Test
	void resetPasswordAfterEmailVerification_success() {
		// given
		String email = "test@email.com";
		String oldPassword = "oldPassword";
		String newPassword = "newPassword";
		String confirmPassword = "newPassword";
		PasswordResetRequestDto requestDto = new PasswordResetRequestDto(email, newPassword, confirmPassword);

		EmailMessage emailMessage = createDummyEmailMessage(email, true);
		Member member = createDummyMember("user1", email);
		member.changePassword(oldPassword);

		when(emailMessageRepository.findByEmail(email)).thenReturn(Optional.of(emailMessage));
		when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
		when(passwordEncoder.matches(newPassword, oldPassword)).thenReturn(false);
		when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
		when(memberRepository.save(member)).thenReturn(member);
		doNothing().when(emailMessageRepository).delete(emailMessage);

		// when
		assertDoesNotThrow(() -> memberService.resetPasswordAfterEmailVerification(requestDto));

		// then
		verify(emailMessageRepository).findByEmail(email);
		verify(memberRepository).findByEmail(email);
		verify(passwordEncoder).matches(newPassword, oldPassword);
		verify(passwordEncoder).encode(newPassword);
		verify(memberRepository).save(member);
		verify(emailMessageRepository).delete(emailMessage);
	}

	@DisplayName("비밀번호재설정_실패_이메일메시지없음")
	@Test
	void resetPasswordAfterEmailVerification_fail_emailMessageNotFound() {
		// given
		String email = "test@email.com";
		String newPassword = "newPassword";
		String confirmPassword = "newPassword";
		PasswordResetRequestDto requestDto = new PasswordResetRequestDto(email, newPassword, confirmPassword);

		when(emailMessageRepository.findByEmail(email)).thenReturn(Optional.empty());

		// when, then
		assertThrows(EmailVerificationNotApprovedException.class,
			() -> memberService.resetPasswordAfterEmailVerification(requestDto));

		verify(emailMessageRepository).findByEmail(email);
		verify(memberRepository, never()).findByEmail(any());
	}

	@DisplayName("비밀번호재설정_실패_인증안됨")
	@Test
	void resetPasswordAfterEmailVerification_fail_notApproved() {
		// given
		String email = "test@email.com";
		String newPassword = "newPassword";
		String confirmPassword = "newPassword";
		PasswordResetRequestDto requestDto = new PasswordResetRequestDto(email, newPassword, confirmPassword);

		EmailMessage emailMessage = createDummyEmailMessage(email, false);

		when(emailMessageRepository.findByEmail(email)).thenReturn(Optional.of(emailMessage));

		// when, then
		assertThrows(EmailVerificationNotApprovedException.class,
			() -> memberService.resetPasswordAfterEmailVerification(requestDto));

		verify(emailMessageRepository).findByEmail(email);
		verify(memberRepository, never()).findByEmail(any());
	}

	@DisplayName("비밀번호재설정_실패_비밀번호불일치")
	@Test
	void resetPasswordAfterEmailVerification_fail_passwordMismatch() {
		// given
		String email = "test@email.com";
		String newPassword = "newPassword";
		String confirmPassword = "differentPassword";
		PasswordResetRequestDto requestDto = new PasswordResetRequestDto(email, newPassword, confirmPassword);

		EmailMessage emailMessage = createDummyEmailMessage(email, true);

		when(emailMessageRepository.findByEmail(email)).thenReturn(Optional.of(emailMessage));

		// when, then
		assertThrows(PasswordMismatchException.class,
			() -> memberService.resetPasswordAfterEmailVerification(requestDto));

		verify(emailMessageRepository).findByEmail(email);
		verify(memberRepository, never()).findByEmail(any());
	}

	@DisplayName("비밀번호재설정_실패_회원없음")
	@Test
	void resetPasswordAfterEmailVerification_fail_memberNotFound() {
		// given
		String email = "test@email.com";
		String newPassword = "newPassword";
		String confirmPassword = "newPassword";
		PasswordResetRequestDto requestDto = new PasswordResetRequestDto(email, newPassword, confirmPassword);

		EmailMessage emailMessage = createDummyEmailMessage(email, true);

		when(emailMessageRepository.findByEmail(email)).thenReturn(Optional.of(emailMessage));
		when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

		// when, then
		assertThrows(MemberNotFoundException.class,
			() -> memberService.resetPasswordAfterEmailVerification(requestDto));

		verify(emailMessageRepository).findByEmail(email);
		verify(memberRepository).findByEmail(email);
		verify(passwordEncoder, never()).encode(any());
	}

	@DisplayName("비밀번호재설정_실패_같은비밀번호")
	@Test
	void resetPasswordAfterEmailVerification_fail_samePassword() {
		// given
		String email = "test@email.com";
		String oldPassword = "oldPassword";
		String newPassword = "oldPassword";
		String confirmPassword = "oldPassword";
		PasswordResetRequestDto requestDto = new PasswordResetRequestDto(email, newPassword, confirmPassword);

		EmailMessage emailMessage = createDummyEmailMessage(email, true);
		Member member = createDummyMember("user1", email);
		member.changePassword(oldPassword);

		when(emailMessageRepository.findByEmail(email)).thenReturn(Optional.of(emailMessage));
		when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
		when(passwordEncoder.matches(newPassword, oldPassword)).thenReturn(true);

		// when, then
		assertThrows(SamePasswordException.class,
			() -> memberService.resetPasswordAfterEmailVerification(requestDto));

		verify(emailMessageRepository).findByEmail(email);
		verify(memberRepository).findByEmail(email);
		verify(passwordEncoder).matches(newPassword, oldPassword);
		verify(passwordEncoder, never()).encode(any());
	}

	@DisplayName("전체회원수조회_성공")
	@Test
	void getMemberCount_success() {
		// given
		Long expectedCount = 100L;

		when(memberRepository.count()).thenReturn(expectedCount);

		// when
		Long result = memberService.getMemberCount();

		// then
		assertEquals(expectedCount, result);
		verify(memberRepository).count();
	}

	private Member createDummyMember(String memberId, String email) {
		return Member.builder()
			.id(1L)
			.memberId(memberId)
			.password("password")
			.email(email)
			.userName("테스트사용자")
			.role("USER")
			.joinAt(LocalDateTime.now())
			.provider("local")
			.build();
	}

	private EmailMessage createDummyEmailMessage(String email, boolean isApproved) {
		return EmailMessage.builder()
			.id(1L)
			.email(email)
			.code("123456")
			.isApproved(isApproved)
			.expiresAt(LocalDateTime.now().plusMinutes(5))
			.purpose(EmailPurpose.FIND_PASSWORD)
			.build();
	}
}
