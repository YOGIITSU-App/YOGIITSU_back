package com.YOGIITSU.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 로컬(Caffeine) 캐시 설정
 * API로 변경되지 않는 정적 데이터 조회에만 사용한다.
 * 데이터는 DB에서 직접 수정될 수 있으므로 TTL로 반영 지연을 제한한다.
 */
@Configuration
@EnableCaching
public class CacheConfig {

	public static final String FACILITY_MARKERS = "facilityMarkers";
	public static final String SHORTCUTS = "shortcuts";
	public static final String SHORTCUT_DETAIL = "shortcutDetail";

	private static final Duration TTL = Duration.ofHours(1);
	private static final long MAX_ENTRIES_PER_CACHE = 200;

	@Bean
	public CacheManager cacheManager() {
		CaffeineCacheManager cacheManager = new CaffeineCacheManager(
			FACILITY_MARKERS, SHORTCUTS, SHORTCUT_DETAIL);
		cacheManager.setCaffeine(Caffeine.newBuilder()
			.expireAfterWrite(TTL)
			.maximumSize(MAX_ENTRIES_PER_CACHE));
		cacheManager.setAllowNullValues(false);
		return cacheManager;
	}
}
