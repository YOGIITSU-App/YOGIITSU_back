package com.YOGIITSU.scheduler;

import com.YOGIITSU.repository.RecentSearchRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchCleanupScheduler {

	private final RecentSearchRepository recentSearchRepository;

	// 매일 새벽 3시에 실행
	@Scheduled(cron = "0 0 3 * * *")
	@Transactional
	public void deleteOldSearches() {
		LocalDateTime threshold = LocalDateTime.now().minusDays(30);
		int deletedCount = recentSearchRepository.deleteBySearchedAtBefore(threshold);

		log.info("[RecentSearchCleanup] 30일 지난 검색어 {}건 삭제 완료 (기준일시: {})", deletedCount, threshold);
	}
}
