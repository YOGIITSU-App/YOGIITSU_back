package com.YOGIITSU.repository;

import com.YOGIITSU.entity.FcmToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

	Optional<FcmToken> findByToken(String token);

	@Query("SELECT f.token FROM FcmToken f")
	List<String> findAllTokenValues();

	void deleteByToken(String token);
}
