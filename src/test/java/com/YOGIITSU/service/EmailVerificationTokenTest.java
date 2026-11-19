package com.YOGIITSU.service;

import com.YOGIITSU.entity.EmailMessage;
import com.YOGIITSU.entity.EmailPurpose;
import com.YOGIITSU.exception.auth.InvalidTokenException;
import com.YOGIITSU.exception.auth.VerificationCodeExpiredException;
import com.YOGIITSU.exception.auth.VerificationCodeMismatchException;
import com.YOGIITSU.jwt.EmailVerificationJwtProvider;
import com.YOGIITSU.repository.EmailMessageRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationTokenTest {

	private static final String EMAIL = "test@suwon.ac.kr";
	private static final String CODE = "123456";
	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

	@Spy
	private EmailVerificationJwtProvider emailJwtProvider = new EmailVerificationJwtProvider();

	@Mock
	private EmailMessageRepository emailMessageRepository;

	@InjectMocks
	private EmailService emailService;

	private EmailMessage message(LocalDateTime expiresAt) {
		return EmailMessage.builder()
			.id(10L)
			.email(EMAIL)
			.code(CODE)
			.isApproved(false)
			.expiresAt(expiresAt)
			.purpose(EmailPurpose.FIND_PASSWORD)
			.build();
	}

	private Claims claimsFor(String email, Long messageId) {
		String token = emailJwtProvider.generateEmailToken(email, messageId);
		return emailJwtProvider.parseEmailToken(token).orElseThrow();
	}

	@Test
	@DisplayName("이메일_인증_토큰에_인증코드가_포함되지_않음")
	void token_doesNotContainCode() {
		String token = emailJwtProvider.generateEmailToken(EMAIL, 10L);
		String payload = new String(Base64.getUrlDecoder().decode(token.split("[.]")[1]),
			StandardCharsets.UTF_8);

		assertFalse(payload.contains("code"), payload);
		assertFalse(payload.contains(CODE), payload);
	}

	@Test
	@DisplayName("올바른_코드_입력시_승인")
	void verifyAndApprove_success() {
		EmailMessage message = message(LocalDateTime.now(KST).plusMinutes(5));
		when(emailMessageRepository.findById(10L)).thenReturn(Optional.of(message));

		String email = emailService.verifyAndApprove(claimsFor(EMAIL, 10L), CODE);

		assertEquals(EMAIL, email);
		assertTrue(message.isApproved());
		verify(emailMessageRepository).save(message);
	}

	@Test
	@DisplayName("잘못된_코드_입력시_실패")
	void verifyAndApprove_wrongCode() {
		EmailMessage message = message(LocalDateTime.now(KST).plusMinutes(5));
		when(emailMessageRepository.findById(10L)).thenReturn(Optional.of(message));

		assertThrows(VerificationCodeMismatchException.class,
			() -> emailService.verifyAndApprove(claimsFor(EMAIL, 10L), "000000"));
		assertFalse(message.isApproved());
		verify(emailMessageRepository, never()).save(any());
	}

	@Test
	@DisplayName("만료된_코드_입력시_실패")
	void verifyAndApprove_expired() {
		EmailMessage message = message(LocalDateTime.now(KST).minusMinutes(1));
		when(emailMessageRepository.findById(10L)).thenReturn(Optional.of(message));

		assertThrows(VerificationCodeExpiredException.class,
			() -> emailService.verifyAndApprove(claimsFor(EMAIL, 10L), CODE));
		assertFalse(message.isApproved());
	}

	@Test
	@DisplayName("토큰_이메일과_인증요청_이메일이_다르면_실패")
	void verifyAndApprove_emailMismatch() {
		EmailMessage message = message(LocalDateTime.now(KST).plusMinutes(5));
		when(emailMessageRepository.findById(10L)).thenReturn(Optional.of(message));

		assertThrows(InvalidTokenException.class,
			() -> emailService.verifyAndApprove(claimsFor("other@suwon.ac.kr", 10L), CODE));
		assertFalse(message.isApproved());
	}
}
