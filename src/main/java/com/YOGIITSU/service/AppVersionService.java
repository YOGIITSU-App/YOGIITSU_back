package com.YOGIITSU.service;


import com.YOGIITSU.exception.resource.ResourceNotFoundException;
import com.YOGIITSU.exception.validation.InvalidArgumentException;
import com.YOGIITSU.dto.ResponseDto.AppVersionResponseDto;
import com.YOGIITSU.entity.AppVersion;
import com.YOGIITSU.enums.Platform;
import com.YOGIITSU.repository.AppVersionRepository;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppVersionService {

	private final AppVersionRepository repository;
	private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+(\\.\\d+)*$");

	@Transactional(readOnly = true)
	public AppVersionResponseDto getAppVersion(Platform platform, String currentVersion) {
		// 1. DB에서 해당 플랫폼의 최신 버전 조회
		AppVersion policy = repository.findTopByPlatformOrderByUpdatedAtDesc(platform)
			.orElseThrow(() -> new ResourceNotFoundException(platform.name()));

		String updateType;

		// 2. 버전 비교 로직
		// 현재 버전이 최소 버전보다 낮으면 -> 강제 업데이트 (FORCE)
		if (compareVersion(currentVersion, policy.getMinVersion()) < 0) {
			updateType = "FORCE";
		}
		// 현재 버전이 최신 버전보다 낮으면 -> 선택 업데이트 (SELECT)
		else if (compareVersion(currentVersion, policy.getLatestVersion()) < 0) {
			updateType = "SELECT";
		}
		// 그 외 (현재 버전이 최신 버전과 같거나 높으면) -> 업데이트 불필요 (NONE)
		else {
			updateType = "NONE";
		}

		// 3. 응답 DTO 빌드
		return AppVersionResponseDto.builder()
			.updateType(updateType)
			.currentVersion(currentVersion)
			.minVersion(policy.getMinVersion())
			.latestVersion(policy.getLatestVersion())
			.build();
	}

	/**
	 * 버전을 비교합니다. (예: "1.2.0", "1.10.5")
	 *
	 * @param v1 비교할 첫 번째 버전
	 * @param v2 비교할 두 번째 버전
	 * @return v1 > v2 이면 1, v1 < v2 이면 -1, 같으면 0
	 */
	private int compareVersion(String v1, String v2) {
		String s1 = v1 == null ? "" : v1.trim();
		String s2 = v2 == null ? "" : v2.trim();

		if (!VERSION_PATTERN.matcher(s1).matches() || !VERSION_PATTERN.matcher(s2).matches()) {
			throw new InvalidArgumentException("버전 문자열 형식이 올바르지 않습니다.");
		}
		String[] parts1 = s1.split("\\.");
		String[] parts2 = s2.split("\\.");

		int length = Math.max(parts1.length, parts2.length);
		for (int i = 0; i < length; i++) {
			int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
			int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
			if (num1 < num2) {
				return -1;
			}
			if (num1 > num2) {
				return 1;
			}
		}
		return 0;
	}
}

