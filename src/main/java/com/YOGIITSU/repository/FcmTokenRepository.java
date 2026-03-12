package com.YOGIITSU.repository;

import com.YOGIITSU.entity.FcmToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

	Optional<FcmToken> findByToken(String token);

	@Query("SELECT f.token FROM FcmToken f")
	List<String> findAllTokenValues();

	@Modifying
	@Transactional
	@Query("DELETE FROM FcmToken f WHERE f.token = :token")
	void deleteByToken(String token);
}
