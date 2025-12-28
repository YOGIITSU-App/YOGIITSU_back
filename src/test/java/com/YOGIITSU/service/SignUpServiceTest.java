package com.YOGIITSU.service;

import com.YOGIITSU.dto.RequestDto.MemberSignUpRequestDto;
import com.YOGIITSU.entity.EmailMessage;
import com.YOGIITSU.entity.EmailPurpose;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.exception.auth.EmailVerificationNotApprovedException;
import com.YOGIITSU.exception.auth.VerificationCodeExpiredException;
import com.YOGIITSU.exception.user.IdAlreadyExistsException;
import com.YOGIITSU.exception.validation.*;
import com.YOGIITSU.repository.EmailMessageRepository;
import com.YOGIITSU.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignUpServiceTest {

	@Mock
	MemberRepository memberRepository;

	@Mock
	EmailMessageRepository emailMessageRepository;

	@Mock
	PasswordEncoder passwordEncoder;

	@InjectMocks
	SignUpService signUpService;

	/**
	 * 유효한 회원가입 요청 DTO 생성
	 */
	private MemberSignUpRequestDto validDto() {
		return new MemberSignUpRequestDto(
			"testuser",
			"Test1234!",
			"test@suwon.ac.kr",
			"박소미"
		);
	}

	/**
	 * 유효한 이메일 인증 메시지 생성
	 */
	private EmailMessage validEmailMessage() {
		return EmailMessage.builder()
			.email("test@suwon.ac.kr")
			.code("123456")
			.isApproved(true)
			.purpose(EmailPurpose.SIGNUP)
			.expiresAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(5))
			.build();
	}

	/**
	 * 회원가입 성공 테스트
	 */
	@Test
	@DisplayName("회원가입_성공")
	void register_success() {
		when(memberRepository.findByEmail(any())).thenReturn(Optional.empty());
		when(memberRepository.findByMemberId(any())).thenReturn(Optional.empty());
		when(memberRepository.findByUserName(any())).thenReturn(Optional.empty());
		when(emailMessageRepository.findByEmailAndIsApprovedTrue(any()))
			.thenReturn(Optional.of(validEmailMessage()));
		when(passwordEncoder.encode(any())).thenReturn("ENCODED");

		signUpService.register(validDto());

		verify(memberRepository).save(any(Member.class));
	}


	/**
	 * 이메일 중복 검사 실패 테스트
	 */
	@Test
	@DisplayName("이메일_중복")
	void emailAlreadyExists() {
		when(memberRepository.findByEmail(any()))
			.thenReturn(Optional.of(mock(Member.class)));

		assertThatThrownBy(() -> signUpService.register(validDto()))
			.isInstanceOf(EmailAlreadyExistsException.class);
	}

	@Test
	@DisplayName("이메일_미인증")
	void emailNotVerified() {
		when(memberRepository.findByEmail(any())).thenReturn(Optional.empty());
		when(emailMessageRepository.findByEmailAndIsApprovedTrue(any()))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> signUpService.register(validDto()))
			.isInstanceOf(EmailVerificationNotApprovedException.class);
	}

	@Test
	@DisplayName("이메일_인증_만료")
	void emailVerificationExpired() {
		EmailMessage expired = EmailMessage.builder()
			.email("test@suwon.ac.kr")
			.code("123456")
			.isApproved(true)
			.purpose(EmailPurpose.SIGNUP)
			.expiresAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusMinutes(1))
			.build();

		when(memberRepository.findByEmail(any())).thenReturn(Optional.empty());
		when(emailMessageRepository.findByEmailAndIsApprovedTrue(any()))
			.thenReturn(Optional.of(expired));

		assertThatThrownBy(() -> signUpService.register(validDto()))
			.isInstanceOf(VerificationCodeExpiredException.class);
	}

	@Test
	@DisplayName("이메일_인증_용도_불일치")
	void emailPurposeMismatch() {
		EmailMessage wrongPurpose = EmailMessage.builder()
			.email("test@suwon.ac.kr")
			.code("123456")
			.isApproved(true)
			.purpose(EmailPurpose.FIND_PASSWORD)
			.expiresAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(5))
			.build();

		when(memberRepository.findByEmail(any())).thenReturn(Optional.empty());
		when(emailMessageRepository.findByEmailAndIsApprovedTrue(any()))
			.thenReturn(Optional.of(wrongPurpose));

		assertThatThrownBy(() -> signUpService.register(validDto()))
			.isInstanceOf(EmailVerificationNotApprovedException.class);
	}

	@Test
	@DisplayName("이메일_도메인_불일치")
	void invalidEmailDomain() {
		MemberSignUpRequestDto dto =
			new MemberSignUpRequestDto(
				"testuser",
				"Test1234!",
				"test@abc.com",
				"박소미"
			);

		when(memberRepository.findByEmail(any())).thenReturn(Optional.empty());
		when(emailMessageRepository.findByEmailAndIsApprovedTrue(any()))
			.thenReturn(Optional.of(validEmailMessage()));
		when(memberRepository.findByMemberId(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> signUpService.register(dto))
			.isInstanceOf(InvalidEmailDomainException.class);
	}

	/**
	 * 아이디 중복 및 형식 검사 실패 테스트
	 */
	@Test
	@DisplayName("아이디_중복")
	void memberIdAlreadyExists() {
		when(memberRepository.findByEmail(any())).thenReturn(Optional.empty());
		when(emailMessageRepository.findByEmailAndIsApprovedTrue(any()))
			.thenReturn(Optional.of(validEmailMessage()));
		when(memberRepository.findByMemberId(any()))
			.thenReturn(Optional.of(mock(Member.class)));

		assertThatThrownBy(() -> signUpService.register(validDto()))
			.isInstanceOf(IdAlreadyExistsException.class);
	}

	@Test
	@DisplayName("아이디_형식_불일치_1 (너무_짧음)")
	void memberIdTooShort() {
		MemberSignUpRequestDto dto =
			new MemberSignUpRequestDto(
				"ab",
				"Test1234!",
				"test@suwon.ac.kr",
				"박소미"
			);
		when(memberRepository.findByEmail(any())).thenReturn(Optional.empty());
		when(emailMessageRepository.findByEmailAndIsApprovedTrue(any()))
			.thenReturn(Optional.of(validEmailMessage()));

		assertThatThrownBy(() -> signUpService.register(dto))
			.isInstanceOf(InvalidMemberIdFormatException.class);
	}

	@Test
	@DisplayName("아이디_형식_불일치_2 (첫글자_숫자)")
	void memberIdNotStartWithLetter() {

		MemberSignUpRequestDto dto =
			new MemberSignUpRequestDto(
				"1test",
				"Test1234!",
				"test@suwon.ac.kr",
				"박소미"
			);
		when(memberRepository.findByEmail(any())).thenReturn(Optional.empty());
		when(emailMessageRepository.findByEmailAndIsApprovedTrue(any()))
			.thenReturn(Optional.of(validEmailMessage()));

		assertThatThrownBy(() -> signUpService.register(dto))
			.isInstanceOf(InvalidMemberIdFormatException.class);
	}

	@Test
	@DisplayName("아이디_형식_불일치_3 (특수문자_포함)")
	void memberIdContainsSpecialChar() {
		MemberSignUpRequestDto dto =
			new MemberSignUpRequestDto(
				"test@12",
				"Test1234!",
				"test@suwon.ac.kr",
				"박소미"
			);
		when(memberRepository.findByEmail(any())).thenReturn(Optional.empty());
		when(emailMessageRepository.findByEmailAndIsApprovedTrue(any()))
			.thenReturn(Optional.of(validEmailMessage()));

		assertThatThrownBy(() -> signUpService.register(dto))
			.isInstanceOf(InvalidMemberIdFormatException.class);
	}

	/**
	 * 사용자 이름 중복 및 형식 검사 실패 테스트
	 */
	@Test
	@DisplayName("사용자이름_중복")
	void usernameAlreadyExists() {
		when(memberRepository.findByEmail(any())).thenReturn(Optional.empty());
		when(emailMessageRepository.findByEmailAndIsApprovedTrue(any()))
			.thenReturn(Optional.of(validEmailMessage()));
		when(memberRepository.findByMemberId(any())).thenReturn(Optional.empty());
		when(memberRepository.findByUserName(any()))
			.thenReturn(Optional.of(mock(Member.class)));

		assertThatThrownBy(() -> signUpService.register(validDto()))
			.isInstanceOf(UsernameAlreadyExistsException.class);
	}

	@Test
	@DisplayName("사용자이름_형식_불일치")
	void invalidUsernameFormat() {
		MemberSignUpRequestDto dto =
			new MemberSignUpRequestDto(
				"testuser",
				"Test1234!",
				"test@suwon.ac.kr",
				"박!" // 특수문자
			);

		when(memberRepository.findByEmail(any())).thenReturn(Optional.empty());
		when(emailMessageRepository.findByEmailAndIsApprovedTrue(any()))
			.thenReturn(Optional.of(validEmailMessage()));
		when(memberRepository.findByMemberId(any())).thenReturn(Optional.empty());
		when(memberRepository.findByUserName(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> signUpService.register(dto))
			.isInstanceOf(InvalidUsernameFormatException.class);
	}


	/**
	 * 비밀번호 형식 검사 실패 테스트
	 */
	@Test
	@DisplayName("비밀번호_형식_불일치_1 (빈칸)")
	void passwordBlank() {
		MemberSignUpRequestDto dto =
			new MemberSignUpRequestDto(
				"testuser",
				" ",
				"test@suwon.ac.kr",
				"박소미"
			);

		when(memberRepository.findByEmail(any())).thenReturn(Optional.empty());
		when(emailMessageRepository.findByEmailAndIsApprovedTrue(any()))
			.thenReturn(Optional.of(validEmailMessage()));
		when(memberRepository.findByMemberId(any())).thenReturn(Optional.empty());
		when(memberRepository.findByUserName(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> signUpService.register(dto))
			.isInstanceOf(InvalidPasswordFormatException.class);
	}


	@Test
	@DisplayName("비밀번호_형식_불일치_2 (너무_짧음)")
	void passwordTooShort() {
		MemberSignUpRequestDto dto =
			new MemberSignUpRequestDto(
				"testuser",
				"A1!",
				"test@suwon.ac.kr",
				"박소미"
			);
		when(memberRepository.findByEmail(any())).thenReturn(Optional.empty());
		when(emailMessageRepository.findByEmailAndIsApprovedTrue(any()))
			.thenReturn(Optional.of(validEmailMessage()));
		when(memberRepository.findByMemberId(any())).thenReturn(Optional.empty());
		when(memberRepository.findByUserName(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> signUpService.register(dto))
			.isInstanceOf(InvalidPasswordFormatException.class);
	}

	@Test
	@DisplayName("비밀번호_형식_불일치_3 (영문자_누락)")
	void passwordNoLetter() {
		MemberSignUpRequestDto dto =
			new MemberSignUpRequestDto(
				"testuser",
				"12345678!",
				"test@suwon.ac.kr",
				"박소미"
			);
		when(memberRepository.findByEmail(any())).thenReturn(Optional.empty());
		when(emailMessageRepository.findByEmailAndIsApprovedTrue(any()))
			.thenReturn(Optional.of(validEmailMessage()));
		when(memberRepository.findByMemberId(any())).thenReturn(Optional.empty());
		when(memberRepository.findByUserName(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> signUpService.register(dto))
			.isInstanceOf(InvalidPasswordFormatException.class);
	}

	@Test
	@DisplayName("비밀번호_형식_불일치_4 (숫자_누락)")
	void passwordNoNumber() {
		MemberSignUpRequestDto dto =
			new MemberSignUpRequestDto(
				"testuser",
				"Password!",
				"test@suwon.ac.kr",
				"박소미"
			);
		when(memberRepository.findByEmail(any())).thenReturn(Optional.empty());
		when(emailMessageRepository.findByEmailAndIsApprovedTrue(any()))
			.thenReturn(Optional.of(validEmailMessage()));
		when(memberRepository.findByMemberId(any())).thenReturn(Optional.empty());
		when(memberRepository.findByUserName(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> signUpService.register(dto))
			.isInstanceOf(InvalidPasswordFormatException.class);
	}

	@Test
	@DisplayName("비밀번호_형식_불일치_5 (허용되지않은_특수문자)")
	void passwordForbiddenSpecialChar() {
		MemberSignUpRequestDto dto =
			new MemberSignUpRequestDto(
				"testuser",
				"Test1234%^",
				"test@suwon.ac.kr",
				"박소미"
			);
		when(memberRepository.findByEmail(any())).thenReturn(Optional.empty());
		when(emailMessageRepository.findByEmailAndIsApprovedTrue(any()))
			.thenReturn(Optional.of(validEmailMessage()));
		when(memberRepository.findByMemberId(any())).thenReturn(Optional.empty());
		when(memberRepository.findByUserName(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> signUpService.register(dto))
			.isInstanceOf(InvalidPasswordFormatException.class);
	}

	@Test
	@DisplayName("비밀번호_형식_불일치_6 (특수문자_누락)")
	void passwordNoAllowedSpecialChar() {
		MemberSignUpRequestDto dto =
			new MemberSignUpRequestDto(
				"testuser",
				"Test12345",
				"test@suwon.ac.kr",
				"박소미"
			);
		when(memberRepository.findByEmail(any())).thenReturn(Optional.empty());
		when(emailMessageRepository.findByEmailAndIsApprovedTrue(any()))
			.thenReturn(Optional.of(validEmailMessage()));
		when(memberRepository.findByMemberId(any())).thenReturn(Optional.empty());
		when(memberRepository.findByUserName(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> signUpService.register(dto))
			.isInstanceOf(InvalidPasswordFormatException.class);
	}
}
