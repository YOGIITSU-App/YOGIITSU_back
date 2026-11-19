package com.YOGIITSU.config;

import com.YOGIITSU.entity.Shortcut;
import com.YOGIITSU.enums.FacilityType;
import com.YOGIITSU.exception.resource.ShortcutNotFoundException;
import com.YOGIITSU.repository.FacilityMarkerRepository;
import com.YOGIITSU.repository.ShortcutCoordinateRepository;
import com.YOGIITSU.repository.ShortcutRepository;
import com.YOGIITSU.service.FacilityMarkerService;
import com.YOGIITSU.service.ShortcutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringJUnitConfig({CacheConfig.class, FacilityMarkerService.class, ShortcutService.class})
class CacheConfigTest {

	@Autowired
	private FacilityMarkerService facilityMarkerService;

	@Autowired
	private ShortcutService shortcutService;

	@Autowired
	private CacheManager cacheManager;

	@MockitoBean
	private FacilityMarkerRepository facilityMarkerRepository;

	@MockitoBean
	private ShortcutRepository shortcutRepository;

	@MockitoBean
	private ShortcutCoordinateRepository shortcutCoordinateRepository;

	@BeforeEach
	void clearCaches() {
		cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
	}

	@Test
	@DisplayName("시설_마커_조회_시설유형별_캐시")
	void facilityMarkers_cachedPerType() {
		when(facilityMarkerRepository.findByType(any())).thenReturn(List.of());

		facilityMarkerService.getMarkersByType(FacilityType.RESTAURANT);
		facilityMarkerService.getMarkersByType(FacilityType.RESTAURANT);
		facilityMarkerService.getMarkersByType(FacilityType.PARKING);

		verify(facilityMarkerRepository, times(1)).findByType(FacilityType.RESTAURANT);
		verify(facilityMarkerRepository, times(1)).findByType(FacilityType.PARKING);
	}

	@Test
	@DisplayName("지름길_목록_조회_캐시")
	void shortcuts_cached() {
		when(shortcutRepository.findAll()).thenReturn(List.of());

		shortcutService.getAllShortcuts();
		shortcutService.getAllShortcuts();

		verify(shortcutRepository, times(1)).findAll();
	}

	@Test
	@DisplayName("지름길_상세_조회_캐시")
	void shortcutDetail_cached() {
		Shortcut shortcut = mock(Shortcut.class);
		when(shortcut.getShortcutId()).thenReturn(1L);
		when(shortcutRepository.findById(1L)).thenReturn(Optional.of(shortcut));
		when(shortcutCoordinateRepository.findCoordinateByShortcutId(1L)).thenReturn(List.of());

		shortcutService.getShortcutDetail(1L);
		shortcutService.getShortcutDetail(1L);

		verify(shortcutRepository, times(1)).findById(1L);
	}

	@Test
	@DisplayName("지름길_상세_조회_실패시_캐시하지_않음")
	void shortcutDetail_notFound_notCached() {
		when(shortcutRepository.findById(999L)).thenReturn(Optional.empty());

		assertThrows(ShortcutNotFoundException.class, () -> shortcutService.getShortcutDetail(999L));
		assertThrows(ShortcutNotFoundException.class, () -> shortcutService.getShortcutDetail(999L));

		verify(shortcutRepository, times(2)).findById(999L);
	}
}
