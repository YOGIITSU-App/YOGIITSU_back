package com.YOGIITSU.repository;

import com.YOGIITSU.entity.Member;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

	// MemberId를 기준으로 회원을 찾는 메서드
	Optional<Member> findByMemberId(String memberId);

	// memberId를 가진 Member 엔티티에 쓰기 잠금을 걸어 동시성 문제를 방지
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT m FROM Member m WHERE m.memberId = :memberId")
	// memberId로 Member를 조회
	Optional<Member> findByMemberIdWithLock(@Param("memberId") String memberId);

	// 이메일을 기준으로 회원을 찾는 메서드 추가
	Optional<Member> findByEmail(String email);

	// MemberId를 기준으로 회원 존재 여부를 확인하는 메서드 추가
	boolean existsByMemberId(String memberId);

	// userName을 기준으로 이름 중복 여부 확인 메서드
	Optional<Member> findByUserName(String userName);

	boolean existsByEmail(String email);
}