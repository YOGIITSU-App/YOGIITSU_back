package com.YOGIITSU.scheduler;

import com.YOGIITSU.service.CafeteriaCrawler;
import com.YOGIITSU.service.CafeteriaSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.LocalDate;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Slf4j
@Component
@RequiredArgsConstructor
public class CafeteriaScheduler {

	private final CafeteriaCrawler crawler;
	private final CafeteriaSyncService sync;
	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

	@EventListener(ApplicationReadyEvent.class)
	@SchedulerLock(name = "cafeteria.startup", lockAtLeastFor = "PT10S", lockAtMostFor = "PT15M")
	public void init() {
		runSync("startup");
	}

	// 매주 월요일 06:05
	@Scheduled(cron = "0 5 6 ? * MON", zone = "Asia/Seoul")
	@SchedulerLock(name = "cafeteria.syncWeekly", lockAtLeastFor = "PT10S", lockAtMostFor = "PT15M")
	public void syncWeekly() {
		runSync("weekly");
	}

	// 평일 08:00 보정 실행
	@Scheduled(cron = "0 0 8 ? * MON-FRI", zone = "Asia/Seoul")
	@SchedulerLock(name = "cafeteria.syncDailyRetry", lockAtLeastFor = "PT10S", lockAtMostFor = "PT15M")
	public void syncDailyRetry() {
		runSync("daily-retry");
	}

	// 월요일 09:00 ~ 13:00 매시 정각 (총 5번 실행)
	@Scheduled(cron = "0 0 9-13 ? * MON", zone = "Asia/Seoul")
	@SchedulerLock(name = "cafeteria.syncMondayHourly", lockAtLeastFor = "PT10S", lockAtMostFor = "PT70M")
	public void syncMondayHourly() {
		runSync("monday-hourly");
	}

	private void runSync(String tag) {
		try {
			LocalDate today = LocalDate.now(KST);
			LocalDate monday = CafeteriaCrawler.mondayOf(today);
			// 토/일은 이미 다음 주 메뉴가 게시되므로 다음 주 기준으로 크롤링
			if (today.getDayOfWeek() == DayOfWeek.SATURDAY
				|| today.getDayOfWeek() == DayOfWeek.SUNDAY) {
				monday = monday.plusWeeks(1);
			}
			var rows = crawler.fetchAll(monday);
			sync.sync(rows);
			log.info("[식단 {}] {} 주차 {}건 처리", tag, monday, rows.size());
		} catch (Exception e) {
			log.error("[식단 {}] 동기화 실패", tag, e);
		}
	}
}