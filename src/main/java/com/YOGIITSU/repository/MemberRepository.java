package com.YOGIITSU.repository;

import com.YOGIITSU.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

	// MemberId를 기준으로 회원을 찾는 메서드
	Optional<Member> findByMemberId(String memberId);

	// 이메일을 기준으로 회원을 찾는 메서드 추가
	Optional<Member> findByEmail(String email);

	// MemberId를 기준으로 회원을 삭제하는 메서드 추가
	void deleteByMemberId(String memberId);

	// MemberId를 기준으로 회원 존재 여부를 확인하는 메서드 추가
	boolean existsByMemberId(String memberId);

    // userName을 기준으로 이름 중복 여부 확인 메서드
	Optional<Member> findByUserName(String userName);

}