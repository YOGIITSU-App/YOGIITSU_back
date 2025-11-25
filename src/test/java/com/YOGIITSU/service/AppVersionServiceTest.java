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

	@DisplayName("앱버전조회_성공_FORCE업데이트")
	@Test
	void getAppVersion_success_forceUpdate() {
		// given
		Platform platform = Platform.ANDROID;
		String currentVersion = "1.0.0";
		String minVersion = "1.2.0";
		String latestVersion = "2.0.0";
		AppVersion appVersion = createDummyAppVersion(platform, minVersion, latestVersion);

		when(repository.findTopByPlatformOrderByUpdatedAtDesc(platform))
			.thenReturn(Optional.of(appVersion));

		// when
		AppVersionResponseDto result = appVersionService.getAppVersion(platform, currentVersion);

		// then
		assertNotNull(result);
		assertEquals("FORCE", result.getUpdateType());
		assertEquals(currentVersion, result.getCurrentVersion());
		assertEquals(minVersion, result.getMinVersion());
		assertEquals(latestVersion, result.getLatestVersion());
		verify(repository).findTopByPlatformOrderByUpdatedAtDesc(platform);
	}

	@DisplayName("앱버전조회_성공_SELECT업데이트")
	@Test
	void getAppVersion_success_selectUpdate() {
		// given
		Platform platform = Platform.ANDROID;
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
		assertEquals(currentVersion, result.getCurrentVersion());
		assertEquals(minVersion, result.getMinVersion());
		assertEquals(latestVersion, result.getLatestVersion());
		verify(repository).findTopByPlatformOrderByUpdatedAtDesc(platform);
	}

	@DisplayName("앱버전조회_성공_NONE업데이트_같은버전")
	@Test
	void getAppVersion_success_noneUpdate_sameVersion() {
		// given
		Platform platform = Platform.IOS;
		String currentVersion = "2.0.0";
		String minVersion = "1.2.0";
		String latestVersion = "2.0.0";
		AppVersion appVersion = createDummyAppVersion(platform, minVersion, latestVersion);

		when(repository.findTopByPlatformOrderByUpdatedAtDesc(platform))
			.thenReturn(Optional.of(appVersion));

		// when
		AppVersionResponseDto result = appVersionService.getAppVersion(platform, currentVersion);

		// then
		assertNotNull(result);
		assertEquals("NONE", result.getUpdateType());
		assertEquals(currentVersion, result.getCurrentVersion());
		assertEquals(minVersion, result.getMinVersion());
		assertEquals(latestVersion, result.getLatestVersion());
		verify(repository).findTopByPlatformOrderByUpdatedAtDesc(platform);
	}

	@DisplayName("앱버전조회_성공_NONE업데이트_더높은버전")
	@Test
	void getAppVersion_success_noneUpdate_higherVersion() {
		// given
		Platform platform = Platform.IOS;
		String currentVersion = "2.1.0";
		String minVersion = "1.2.0";
		String latestVersion = "2.0.0";
		AppVersion appVersion = createDummyAppVersion(platform, minVersion, latestVersion);

		when(repository.findTopByPlatformOrderByUpdatedAtDesc(platform))
			.thenReturn(Optional.of(appVersion));

		// when
		AppVersionResponseDto result = appVersionService.getAppVersion(platform, currentVersion);

		// then
		assertNotNull(result);
		assertEquals("NONE", result.getUpdateType());
		assertEquals(currentVersion, result.getCurrentVersion());
		assertEquals(minVersion, result.getMinVersion());
		assertEquals(latestVersion, result.getLatestVersion());
		verify(repository).findTopByPlatformOrderByUpdatedAtDesc(platform);
	}

	@DisplayName("앱버전조회_성공_버전비교_복잡한버전")
	@Test
	void getAppVersion_success_complexVersion() {
		// given
		Platform platform = Platform.ANDROID;
		String currentVersion = "1.9.5";
		String minVersion = "1.2.0";
		String latestVersion = "1.10.0";
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

	@DisplayName("앱버전조회_성공_버전비교_단일숫자버전")
	@Test
	void getAppVersion_success_singleDigitVersion() {
		// given
		Platform platform = Platform.ANDROID;
		String currentVersion = "1";
		String minVersion = "2";
		String latestVersion = "3";
		AppVersion appVersion = createDummyAppVersion(platform, minVersion, latestVersion);

		when(repository.findTopByPlatformOrderByUpdatedAtDesc(platform))
			.thenReturn(Optional.of(appVersion));

		// when
		AppVersionResponseDto result = appVersionService.getAppVersion(platform, currentVersion);

		// then
		assertNotNull(result);
		assertEquals("FORCE", result.getUpdateType());
		verify(repository).findTopByPlatformOrderByUpdatedAtDesc(platform);
	}

	@DisplayName("앱버전조회_성공_버전비교_다른길이버전")
	@Test
	void getAppVersion_success_differentLengthVersion() {
		// given
		Platform platform = Platform.ANDROID;
		String currentVersion = "1.2";
		String minVersion = "1.2.0";
		String latestVersion = "1.2.1";
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

	@DisplayName("앱버전조회_실패_잘못된버전형식_현재버전")
	@Test
	void getAppVersion_fail_invalidVersionFormat_currentVersion() {
		// given
		Platform platform = Platform.ANDROID;
		String currentVersion = "v1.0.0";
		String minVersion = "1.2.0";
		String latestVersion = "2.0.0";
		AppVersion appVersion = createDummyAppVersion(platform, minVersion, latestVersion);

		when(repository.findTopByPlatformOrderByUpdatedAtDesc(platform))
			.thenReturn(Optional.of(appVersion));

		// when, then
		assertThrows(InvalidArgumentException.class,
			() -> appVersionService.getAppVersion(platform, currentVersion));

		verify(repository).findTopByPlatformOrderByUpdatedAtDesc(platform);
	}

	@DisplayName("앱버전조회_실패_잘못된버전형식_최소버전")
	@Test
	void getAppVersion_fail_invalidVersionFormat_minVersion() {
		// given
		Platform platform = Platform.ANDROID;
		String currentVersion = "1.0.0";
		String minVersion = "1.2.0-beta";
		String latestVersion = "2.0.0";
		AppVersion appVersion = createDummyAppVersion(platform, minVersion, latestVersion);

		when(repository.findTopByPlatformOrderByUpdatedAtDesc(platform))
			.thenReturn(Optional.of(appVersion));

		// when, then
		assertThrows(InvalidArgumentException.class,
			() -> appVersionService.getAppVersion(platform, currentVersion));

		verify(repository).findTopByPlatformOrderByUpdatedAtDesc(platform);
	}

	@DisplayName("앱버전조회_실패_잘못된버전형식_문자포함")
	@Test
	void getAppVersion_fail_invalidVersionFormat_withLetters() {
		// given
		Platform platform = Platform.ANDROID;
		String currentVersion = "1.0.0a";
		String minVersion = "1.2.0";
		String latestVersion = "2.0.0";
		AppVersion appVersion = createDummyAppVersion(platform, minVersion, latestVersion);

		when(repository.findTopByPlatformOrderByUpdatedAtDesc(platform))
			.thenReturn(Optional.of(appVersion));

		// when, then
		assertThrows(InvalidArgumentException.class,
			() -> appVersionService.getAppVersion(platform, currentVersion));

		verify(repository).findTopByPlatformOrderByUpdatedAtDesc(platform);
	}

	@DisplayName("앱버전조회_실패_잘못된버전형식_빈문자열")
	@Test
	void getAppVersion_fail_invalidVersionFormat_emptyString() {
		// given
		Platform platform = Platform.ANDROID;
		String currentVersion = "";
		String minVersion = "1.2.0";
		String latestVersion = "2.0.0";
		AppVersion appVersion = createDummyAppVersion(platform, minVersion, latestVersion);

		when(repository.findTopByPlatformOrderByUpdatedAtDesc(platform))
			.thenReturn(Optional.of(appVersion));

		// when, then
		assertThrows(InvalidArgumentException.class,
			() -> appVersionService.getAppVersion(platform, currentVersion));

		verify(repository).findTopByPlatformOrderByUpdatedAtDesc(platform);
	}

	@DisplayName("앱버전조회_성공_버전비교_경계값_최소버전과같음")
	@Test
	void getAppVersion_success_boundaryValue_equalToMinVersion() {
		// given
		Platform platform = Platform.ANDROID;
		String currentVersion = "1.2.0";
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

	@DisplayName("앱버전조회_성공_버전비교_경계값_최소버전보다조금높음")
	@Test
	void getAppVersion_success_boundaryValue_slightlyHigherThanMinVersion() {
		// given
		Platform platform = Platform.ANDROID;
		String currentVersion = "1.2.1";
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

	private AppVersion createDummyAppVersion(Platform platform, String minVersion,
		String latestVersion) {
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
