package com.YOGIITSU.service;

import com.YOGIITSU.dto.TokenInfo;
import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.YOGIITSU.entity.Member;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

	private final AuthenticationManagerBuilder authenticationManagerBuilder;
	private final JwtTokenProvider jwtTokenProvider;
	private final MemberRepository memberRepository;
	private final Logger logger = LoggerFactory.getLogger(MemberService.class);

	/**
	 * 로그인 처리 메서드
	 *
	 * @param memberId 사용자의 아이디
	 * @param password 사용자의 비밀번호
	 * @return TokenInfo JWT 토큰 정보
	 */
	@Transactional
	public TokenInfo login(String memberId, String password) {
		// 1. 아이디와 비밀번호를 기반으로 Authentication 객체 생성
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
			memberId, password);

		try {
			// 2. 실제 검증 (사용자 비밀번호 체크)이 이루어지는 부분
			Authentication authentication = authenticationManagerBuilder.getObject()
				.authenticate(authenticationToken);

			// 3. 인증 정보를 기반으로 JWT 토큰 생성
			return jwtTokenProvider.generateToken(authentication);

		} catch (BadCredentialsException e) {
			// 4. 인증 실패 시 예외 처리
			throw new RuntimeException("아이디 또는 비밀번호가 잘못되었습니다", e);
		}
	}

	/**
	 * 회원 탈퇴 처리 메서드
	 *
	 * @param memberId 사용자의 아이디
	 */
	@Transactional
	public void deleteMember(String memberId) {
		// 1. 회원 정보 삭제
		memberRepository.deleteByMemberId(memberId);

		// 2. 회원 탈퇴 확인
		if (memberRepository.existsByMemberId(memberId)) {
			throw new RuntimeException("회원 탈퇴 실패: 회원 정보가 삭제되지 않았습니다.");
		}

		// 3. 로그 기록
		logger.info("회원 탈퇴: {}", memberId);

	}

}