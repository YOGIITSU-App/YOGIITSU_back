package com.YOGIITSU.service;

import com.YOGIITSU.entity.Member;
import com.YOGIITSU.repository.MemberRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder; // 비밀번호 암호화를 위해 주입

	@Transactional
	public Member processOAuthUser(String provider, String email, String name) {
		// 1. 이메일로 사용자를 조회합니다.
		Optional<Member> memberOpt = memberRepository.findByEmail(email);

		if (memberOpt.isPresent()) {
			// 2. 이메일이 이미 존재할 경우
			Member member = memberOpt.get();

			// 3. 저장된 provider와 현재 로그인하려는 provider가 다른지 확인
			if (member.getProvider() != null && !member.getProvider().equals(provider)) {
				// provider가 다르면, 계정 탈취 위험이 있으므로 에러를 발생시킵니다.
				throw new RuntimeException(
					"이미 " + member.getProvider() + " 계정으로 가입된 이메일입니다. 해당 방식으로 로그인해주세요.");
			}
			// provider가 같으면, 기존 사용자이므로 그대로 반환합니다.
			return member;
		} else {
			// 4. 신규 사용자이면 새로 생성합니다.
			return createNewMember(provider, email, name);
		}
	}

	// 신규 멤버 생성 로직은 동일
	private Member createNewMember(String provider, String email, String name) {
		String memberId = provider + "_" + UUID.randomUUID().toString().substring(0, 8);
		String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());

		Member newMember = Member.builder()
			.memberId(memberId)
			.email(email)
			.userName(name)
			.password(randomPassword)
			.role("USER")
			.provider(provider)
			.joinAt(LocalDateTime.now())
			.build();

		return memberRepository.save(newMember);
	}
}