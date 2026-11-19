package com.YOGIITSU.service;

import com.YOGIITSU.entity.EmailMessage;
import com.YOGIITSU.entity.EmailPurpose;
import com.YOGIITSU.repository.EmailMessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class EmailServiceSaveTest {

	@Mock
	private EmailMessageRepository emailMessageRepository;

	@InjectMocks
	private EmailService emailService;

	@Test
	@DisplayName("인증코드_저장시_기존_코드_삭제후_저장")
	void saveEmailMessage_deletesExistingCodesBeforeSave() {
		// given
		EmailMessage message = EmailMessage.builder()
			.email("test@suwon.ac.kr")
			.code("123456")
			.isApproved(false)
			.expiresAt(LocalDateTime.now().plusMinutes(5))
			.purpose(EmailPurpose.SIGNUP)
			.build();

		// when
		emailService.saveEmailMessage(message);

		// then: 이메일당 1건만 유지되도록 기존 코드를 먼저 삭제
		InOrder order = inOrder(emailMessageRepository);
		order.verify(emailMessageRepository).deleteByEmail("test@suwon.ac.kr");
		order.verify(emailMessageRepository).save(message);
	}
}
