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

	// 사용자 이름(username)을 기반으로 UserDetails 객체를 반환하는 메서드
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return memberRepository.findByMemberId(username)
			.map(member -> User.builder()
				.username(member.getMemberId())
				.password(member.getPassword()) // 암호화된 패스워드 사용
				.roles(member.getRole())
				.build())
			.orElseThrow(() -> new UsernameNotFoundException("해당하는 유저를 찾을 수 없습니다: " + username));

	}
}