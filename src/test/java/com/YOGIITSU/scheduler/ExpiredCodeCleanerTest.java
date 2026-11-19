package com.YOGIITSU.scheduler;

import com.YOGIITSU.repository.EmailMessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExpiredCodeCleanerTest {

	@Mock
	private EmailMessageRepository emailMessageRepository;

	@InjectMocks
	private ExpiredCodeCleaner expiredCodeCleaner;

	@Test
	@DisplayName("만료_코드_삭제시_KST_기준_현재시각_사용")
	void deleteExpiredCodes_usesKstNow() {
		// when
		expiredCodeCleaner.deleteExpiredCodes();

		// then: 인증 코드 expiresAt이 KST로 저장되므로 비교 기준도 KST여야 한다
		ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
		verify(emailMessageRepository).deleteAllExpired(captor.capture());

		LocalDateTime kstNow = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
		long diffSeconds = Math.abs(Duration.between(captor.getValue(), kstNow).getSeconds());
		assertTrue(diffSeconds < 5, "KST 현재 시각과의 차이(초): " + diffSeconds);
	}
}
