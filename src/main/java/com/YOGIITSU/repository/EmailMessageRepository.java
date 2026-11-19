package com.YOGIITSU.repository;

import com.YOGIITSU.entity.EmailMessage;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface EmailMessageRepository extends JpaRepository<EmailMessage, Long> {

    Optional<EmailMessage> findByEmailAndCode(String email, String code);

    @Modifying
    @Transactional
    @Query("DELETE FROM EmailMessage e WHERE e.expiresAt < :now")
    int deleteAllExpired(@Param("now") LocalDateTime now); //5분 만료된 코드 삭제

    @Modifying
    @Query("DELETE FROM EmailMessage e WHERE e.email = :email")
    int deleteByEmail(@Param("email") String email); // 이메일당 인증 코드 1건 유지

    Optional<EmailMessage> findByEmail(String email);

    Optional<EmailMessage> findByEmailAndIsApprovedTrue(String email);

}
