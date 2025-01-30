package com.YOGIITSU.repository;

import com.YOGIITSU.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByMemberId(String memberId);
	// 이메일을 기준으로 회원을 찾는 메서드 추가
	Optional<Member> findByEmail(String email);
}