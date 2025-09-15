package com.YOGIITSU.scheduler;

import com.YOGIITSU.service.CafeteriaCrawler;
import com.YOGIITSU.service.CafeteriaSyncService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.ZoneId;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class CafeteriaScheduler {

	private final CafeteriaCrawler crawler;
	private final CafeteriaSyncService sync;
	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

	@PostConstruct
	public void init() {
		runSync("manual");
	}

	// 매주 월요일 06:05
	@Scheduled(cron = "0 5 6 ? * MON", zone = "Asia/Seoul")
	public void syncWeekly() {
		runSync("weekly");
	}

	// 평일 08:00 보정 실행
	@Scheduled(cron = "0 0 8 ? * MON-FRI", zone = "Asia/Seoul")
	public void syncDailyRetry() {
		runSync("daily-retry");
	}

	// 월요일 09:00 ~ 13:00 매시 정각 (총 5번 실행)
	@Scheduled(cron = "0 0 9-13 ? * MON", zone = "Asia/Seoul")
	public void syncMondayHourly() {
		runSync("monday-hourly");
	}

	private void runSync(String tag) {
		try {
			LocalDate monday = CafeteriaCrawler.mondayOf(LocalDate.now(KST));
			var rows = crawler.fetchAll(monday);
			sync.sync(rows);
			log.info("[식단 {}] {} 주차 {}건 처리", tag, monday, rows.size());
		} catch (Exception e) {
			log.error("[식단 {}] 동기화 실패", tag, e);
		}
	}
}