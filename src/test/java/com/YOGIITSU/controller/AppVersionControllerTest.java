package com.YOGIITSU.controller;

import com.YOGIITSU.dto.ResponseDto.AppVersionResponseDto;
import com.YOGIITSU.enums.Platform;
import com.YOGIITSU.exception.resource.ResourceNotFoundException;
import com.YOGIITSU.exception.validation.InvalidArgumentException;
import com.YOGIITSU.service.AppVersionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
	controllers = AppVersionController.class,
	excludeAutoConfiguration = {
		SecurityAutoConfiguration.class,
		UserDetailsServiceAutoConfiguration.class,
		OAuth2ClientAutoConfiguration.class,
		OAuth2ResourceServerAutoConfiguration.class
	}
)
class AppVersionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AppVersionService appVersionService;

	@DisplayName("앱버전조회_성공_업데이트타입별케이스")
	@ParameterizedTest(name = "updateType={0}")
	@CsvSource({
		"FORCE, 1.0.0, 1.2.0, 2.0.0",
		"SELECT, 1.5.0, 1.2.0, 2.0.0",
		"NONE, 2.0.0, 1.2.0, 2.0.0"
	})
	void getAppVersion_success_updateType(
		String updateType, String currentVersion,
		String minVersion, String latestVersion
	) throws Exception {
		// given
		Platform platform = Platform.ANDROID;
		AppVersionResponseDto responseDto = AppVersionResponseDto.builder()
			.updateType(updateType)
			.currentVersion(currentVersion)
			.minVersion(minVersion)
			.latestVersion(latestVersion)
			.build();

		when(appVersionService.getAppVersion(platform, currentVersion)).thenReturn(responseDto);

		// when & then
		mockMvc.perform(get("/app/version")
				.param("platform", platform.name())
				.param("currentVersion", currentVersion)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.updateType").value(updateType))
			.andExpect(jsonPath("$.currentVersion").value(currentVersion))
			.andExpect(jsonPath("$.minVersion").value(minVersion))
			.andExpect(jsonPath("$.latestVersion").value(latestVersion));

		verify(appVersionService).getAppVersion(platform, currentVersion);
	}

	@DisplayName("앱버전조회_성공_플랫폼별케이스")
	@ParameterizedTest(name = "platform={0}")
	@ValueSource(strings = {"ANDROID", "IOS"})
	void getAppVersion_success_platform(String platformName) throws Exception {
		// given
		Platform platform = Platform.valueOf(platformName);
		String currentVersion = "1.5.0";
		AppVersionResponseDto responseDto = AppVersionResponseDto.builder()
			.updateType("SELECT")
			.currentVersion(currentVersion)
			.minVersion("1.2.0")
			.latestVersion("2.0.0")
			.build();

		when(appVersionService.getAppVersion(platform, currentVersion)).thenReturn(responseDto);

		// when & then
		mockMvc.perform(get("/app/version")
				.param("platform", platformName)
				.param("currentVersion", currentVersion)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.updateType").value("SELECT"));

		verify(appVersionService).getAppVersion(platform, currentVersion);
	}

	@DisplayName("앱버전조회_실패_플랫폼없음")
	@Test
	void getAppVersion_fail_platformNotFound() throws Exception {
		// given
		Platform platform = Platform.ANDROID;
		String currentVersion = "1.0.0";

		when(appVersionService.getAppVersion(platform, currentVersion))
			.thenThrow(new ResourceNotFoundException(platform.name()));

		// when & then
		mockMvc.perform(get("/app/version")
				.param("platform", platform.name())
				.param("currentVersion", currentVersion)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound());

		verify(appVersionService).getAppVersion(platform, currentVersion);
	}

	@DisplayName("앱버전조회_실패_잘못된버전형식")
	@Test
	void getAppVersion_fail_invalidVersionFormat() throws Exception {
		// given
		Platform platform = Platform.ANDROID;
		String currentVersion = "v1.0.0"; // 잘못된 형식 (문자 포함)

		when(appVersionService.getAppVersion(platform, currentVersion))
			.thenThrow(new InvalidArgumentException("버전 문자열 형식이 올바르지 않습니다."));

		// when & then
		mockMvc.perform(get("/app/version")
				.param("platform", platform.name())
				.param("currentVersion", currentVersion)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());

		verify(appVersionService).getAppVersion(platform, currentVersion);
	}

	@DisplayName("앱버전조회_실패_플랫폼파라미터누락")
	@Test
	void getAppVersion_fail_missingPlatformParameter() throws Exception {
		// given
		String currentVersion = "1.0.0";

		// when & then
		mockMvc.perform(get("/app/version")
				.param("currentVersion", currentVersion)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());

		verify(appVersionService, never()).getAppVersion(any(), any());
	}

	@DisplayName("앱버전조회_실패_현재버전파라미터누락")
	@Test
	void getAppVersion_fail_missingCurrentVersionParameter() throws Exception {
		// given
		Platform platform = Platform.ANDROID;

		// when & then
		mockMvc.perform(get("/app/version")
				.param("platform", platform.name())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());

		verify(appVersionService, never()).getAppVersion(any(), any());
	}
}



