package com.YOGIITSU.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttemptLimiterTest {

	/** 시간을 직접 진행시킬 수 있는 테스트용 Clock */
	static class MutableClock extends Clock {
		private Instant now = Instant.parse("2026-01-01T00:00:00Z");

		void advance(Duration d) {
			now = now.plus(d);
		}

		@Override
		public ZoneOffset getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(java.time.ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return now;
		}
	}

	@Test
	@DisplayName("윈도우_내_제한횟수_도달시_제한")
	void limitedWithinWindow() {
		MutableClock clock = new MutableClock();
		AttemptLimiter limiter = new AttemptLimiter(clock);

		for (int i = 0; i < 4; i++) {
			limiter.record("k");
		}
		assertFalse(limiter.isLimited("k", 5, Duration.ofMinutes(15)));

		limiter.record("k");
		assertTrue(limiter.isLimited("k", 5, Duration.ofMinutes(15)));
	}

	@Test
	@DisplayName("윈도우가_지나면_제한_해제")
	void releasedAfterWindow() {
		MutableClock clock = new MutableClock();
		AttemptLimiter limiter = new AttemptLimiter(clock);
		limiter.record("k");
		assertTrue(limiter.isLimited("k", 1, Duration.ofMinutes(1)));

		clock.advance(Duration.ofSeconds(61));
		assertFalse(limiter.isLimited("k", 1, Duration.ofMinutes(1)));
	}

	@Test
	@DisplayName("reset_후_제한_해제")
	void reset() {
		AttemptLimiter limiter = new AttemptLimiter(new MutableClock());
		limiter.record("k");
		limiter.reset("k");
		assertFalse(limiter.isLimited("k", 1, Duration.ofMinutes(1)));
	}

	@Test
	@DisplayName("보관기간_지난_기록_정리")
	void cleanup() {
		MutableClock clock = new MutableClock();
		AttemptLimiter limiter = new AttemptLimiter(clock);
		limiter.record("k");

		clock.advance(Duration.ofHours(2));
		limiter.cleanup();

		assertFalse(limiter.isLimited("k", 1, Duration.ofDays(1)));
	}
}
