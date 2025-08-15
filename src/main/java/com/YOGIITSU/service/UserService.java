package com.YOGIITSU.service;

import com.YOGIITSU.entity.Member;
import com.YOGIITSU.repository.MemberRepository;
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
	public Member processOAuthUser(String email, String name) {
		// 이메일로 기존 사용자가 있는지 조회합니다.
		return memberRepository.findByEmail(email)
			.orElseGet(() -> {
				//이메일 도메인으로 소셜 타입을 구분
				String provider = (email != null && email.endsWith("@gmail.com")) ? "google_" : "kakao_";
				String memberId = provider + UUID.randomUUID().toString().substring(0, 8);

				// 소셜 로그인 사용자는 비밀번호를 사용하지 않으므로, 임의의 값을 암호화하여 저장합니다.
				String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());

				Member newMember = Member.builder()
					.memberId(memberId)
					.email(email)
					.userName(name)
					.password(randomPassword)
					.role("USER")
					.joinAt(LocalDateTime.now())
					.build();

				return memberRepository.save(newMember);
			});
	}
}