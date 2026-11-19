package com.YOGIITSU.scheduler;

import com.YOGIITSU.repository.EmailMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiredCodeCleaner {

	// 인증 코드 만료 시각(expiresAt)은 KST 기준으로 저장되므로 같은 기준으로 비교한다.
	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

	private final EmailMessageRepository emailMessageRepository;

	@Scheduled(fixedRate = 60000) // 1분마다 실행
	@Transactional
	public void deleteExpiredCodes() {
		LocalDateTime now = LocalDateTime.now(KST);
		int deletedCount = emailMessageRepository.deleteAllExpired(now);
		if (deletedCount > 0) {
			log.info("만료된 인증 코드 {}건 삭제 완료", deletedCount);
		}
	}
}

