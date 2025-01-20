package com.YOGIITSU.service;

import com.YOGIITSU.entity.Member;
import com.YOGIITSU.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	// 사용자 이름(username)을 기반으로 UserDetails 객체를 반환하는 메서드
	@Override
	public UserDetails loadUserByUsername(String memberId) throws UsernameNotFoundException {
		// MemberId를 기준으로 회원을 조회하고, 없으면 예외를 던짐
		return memberRepository.findByMemberId(memberId) // MemberId를 통해 회원 검색
			.map(this::createUserDetails) // 존재한다면 UserDetails 객체로 변환
			.orElseThrow(() -> new UsernameNotFoundException("해당하는 유저를 찾을 수 없습니다."));
	}

	// Member 엔티티를 기반으로 UserDetails 객체를 생성하는 메서드
	private UserDetails createUserDetails(Member member) {
		return User.builder()
			.username(member.getUsername())
			.password(passwordEncoder.encode(member.getPassword()))
			.build();
	}
}