package com.YOGIITSU.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 키(아이디, 이메일 등)별 시도 횟수를 슬라이딩 윈도우로 제한하는 메모리 기반 리미터
 * 서버가 nginx 뒤에 있어 클라이언트 IP 대신 요청 대상(아이디/이메일) 기준으로 제한한다.
 * 단일 인스턴스 기준이며, 서버 재시작 시 기록은 초기화된다.
 */
@Component
public class AttemptLimiter {

	// 기록 보관 최대 기간 (가장 긴 제한 윈도우 이상)
	private static final Duration RETENTION = Duration.ofHours(1);

	private final Map<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();
	private final Clock clock;

	@Autowired
	public AttemptLimiter() {
		this(Clock.systemUTC());
	}

	AttemptLimiter(Clock clock) {
		this.clock = clock;
	}

	/**
	 * window 기간 내 기록이 limit회 이상이면 true
	 */
	public boolean isLimited(String key, int limit, Duration window) {
		Deque<Instant> deque = attempts.get(key);
		if (deque == null) {
			return false;
		}
		Instant threshold = clock.instant().minus(window);
		synchronized (deque) {
			long count = deque.stream().filter(t -> t.isAfter(threshold)).count();
			return count >= limit;
		}
	}

	public void record(String key) {
		Deque<Instant> deque = attempts.computeIfAbsent(key, k -> new ArrayDeque<>());
		synchronized (deque) {
			deque.addLast(clock.instant());
		}
	}

	public void reset(String key) {
		attempts.remove(key);
	}

	/**
	 * 보관 기간이 지난 기록 정리 (메모리 누수 방지)
	 */
	@Scheduled(fixedDelay = 600000) // 10분마다
	public void cleanup() {
		Instant threshold = clock.instant().minus(RETENTION);
		attempts.entrySet().removeIf(entry -> {
			Deque<Instant> deque = entry.getValue();
			synchronized (deque) {
				while (!deque.isEmpty() && !deque.peekFirst().isAfter(threshold)) {
					deque.pollFirst();
				}
				return deque.isEmpty();
			}
		});
	}
}
