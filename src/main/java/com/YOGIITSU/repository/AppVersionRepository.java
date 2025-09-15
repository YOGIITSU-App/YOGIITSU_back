package com.YOGIITSU.repository;

import com.YOGIITSU.entity.AppVersion;
import com.YOGIITSU.enums.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AppVersionRepository extends JpaRepository<AppVersion, Long> {

	/**
	 * 특정 플랫폼의 가장 최신 버전 정책을 조회합니다.
	 *
	 * @param platform 조회할 플랫폼 (ANDROID, IOS)
	 * @return 가장 최근에 업데이트된 AppVersion Optional 객체
	 */
	Optional<AppVersion> findTopByPlatformOrderByUpdatedAtDesc(Platform platform);
}
