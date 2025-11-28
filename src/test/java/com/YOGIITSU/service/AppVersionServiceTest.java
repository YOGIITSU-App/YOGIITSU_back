package com.YOGIITSU.service;

import com.YOGIITSU.dto.ResponseDto.AppVersionResponseDto;
import com.YOGIITSU.entity.AppVersion;
import com.YOGIITSU.enums.Platform;
import com.YOGIITSU.exception.resource.ResourceNotFoundException;
import com.YOGIITSU.exception.validation.InvalidArgumentException;
import com.YOGIITSU.repository.AppVersionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppVersionServiceTest {

	@Mock
	private AppVersionRepository repository;

	@InjectMocks
	private AppVersionService appVersionService;

	@DisplayName("앱버전조회_성공_업데이트타입별케이스")
	@ParameterizedTest(name = "current={0}, min={1}, latest={2} => {3}")
	@CsvSource({
		"1.0.0, 1.2.0, 2.0.0, FORCE",
		"1.5.0, 1.2.0, 2.0.0, SELECT",
		"2.0.0, 1.2.0, 2.0.0, NONE",
		"2.1.0, 1.2.0, 2.0.0, NONE"
	})
	void getAppVersion_success_updateType(
		String currentVersion, String minVersion,
		String latestVersion, String expectedUpdateType
	) {
		// given
		Platform platform = Platform.ANDROID;
		AppVersion appVersion = createDummyAppVersion(platform, minVersion, latestVersion);

		when(repository.findTopByPlatformOrderByUpdatedAtDesc(platform))
			.thenReturn(Optional.of(appVersion));

		// when
		AppVersionResponseDto result = appVersionService.getAppVersion(platform, currentVersion);

		// then
		assertNotNull(result);
		assertEquals(expectedUpdateType, result.getUpdateType());
		assertEquals(currentVersion, result.getCurrentVersion());
		assertEquals(minVersion, result.getMinVersion());
		assertEquals(latestVersion, result.getLatestVersion());
		verify(repository).findTopByPlatformOrderByUpdatedAtDesc(platform);
	}

	@DisplayName("앱버전조회_성공_버전비교_복잡한케이스")
	@ParameterizedTest(name = "current={0}, min={1}, latest={2} => {3}")
	@CsvSource({
		"1.9.5, 1.2.0, 1.10.0, SELECT",
		"1, 2, 3, FORCE",
		"1.2, 1.2.0, 1.2.1, SELECT",
		"1.2.0, 1.2.0, 2.0.0, SELECT",
		"1.2.1, 1.2.0, 2.0.0, SELECT"
	})
	void getAppVersion_success_complexVersion(
		String currentVersion, String minVersion,
		String latestVersion, String expectedUpdateType
	) {
		// given
		Platform platform = Platform.ANDROID;
		AppVersion appVersion = createDummyAppVersion(platform, minVersion, latestVersion);

		when(repository.findTopByPlatformOrderByUpdatedAtDesc(platform))
			.thenReturn(Optional.of(appVersion));

		// when
		AppVersionResponseDto result = appVersionService.getAppVersion(platform, currentVersion);

		// then
		assertNotNull(result);
		assertEquals(expectedUpdateType, result.getUpdateType());
		verify(repository).findTopByPlatformOrderByUpdatedAtDesc(platform);
	}

	@DisplayName("앱버전조회_성공_플랫폼별케이스")
	@ParameterizedTest(name = "platform={0}")
	@ValueSource(strings = {"ANDROID", "IOS"})
	void getAppVersion_success_platform(String platformName) {
		// given
		Platform platform = Platform.valueOf(platformName);
		String currentVersion = "1.5.0";
		String minVersion = "1.2.0";
		String latestVersion = "2.0.0";
		AppVersion appVersion = createDummyAppVersion(platform, minVersion, latestVersion);

		when(repository.findTopByPlatformOrderByUpdatedAtDesc(platform))
			.thenReturn(Optional.of(appVersion));

		// when
		AppVersionResponseDto result = appVersionService.getAppVersion(platform, currentVersion);

		// then
		assertNotNull(result);
		assertEquals("SELECT", result.getUpdateType());
		verify(repository).findTopByPlatformOrderByUpdatedAtDesc(platform);
	}

	@DisplayName("앱버전조회_실패_플랫폼없음")
	@Test
	void getAppVersion_fail_platformNotFound() {
		// given
		Platform platform = Platform.ANDROID;
		String currentVersion = "1.0.0";

		when(repository.findTopByPlatformOrderByUpdatedAtDesc(platform))
			.thenReturn(Optional.empty());

		// when, then
		assertThrows(ResourceNotFoundException.class,
			() -> appVersionService.getAppVersion(platform, currentVersion));

		verify(repository).findTopByPlatformOrderByUpdatedAtDesc(platform);
	}

	@DisplayName("앱버전조회_실패_잘못된버전형식")
	@ParameterizedTest(name = "currentVersion={0}, minVersion={1}")
	@CsvSource({
		"v1.0.0, 1.2.0",
		"1.0.0, 1.2.0-beta",
		"1.0.0a, 1.2.0",
		"'', 1.2.0"
	})
	void getAppVersion_fail_invalidVersionFormat(String currentVersion, String minVersion) {
		// given
		Platform platform = Platform.ANDROID;
		String latestVersion = "2.0.0";
		AppVersion appVersion = createDummyAppVersion(platform, minVersion, latestVersion);

		when(repository.findTopByPlatformOrderByUpdatedAtDesc(platform))
			.thenReturn(Optional.of(appVersion));

		// when, then
		assertThrows(InvalidArgumentException.class,
			() -> appVersionService.getAppVersion(platform, currentVersion));

		verify(repository).findTopByPlatformOrderByUpdatedAtDesc(platform);
	}

	private AppVersion createDummyAppVersion(Platform platform, String minVersion, String latestVersion) {
		return AppVersion.builder()
			.id(1L)
			.platform(platform)
			.minVersion(minVersion)
			.latestVersion(latestVersion)
			.updatedBy("admin")
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build();
	}
}

