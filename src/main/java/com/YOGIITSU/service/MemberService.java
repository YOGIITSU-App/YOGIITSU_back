package com.YOGIITSU.service;

import com.YOGIITSU.config.handler.GlobalExceptionHandler.AdminAccessDeniedException;
import com.YOGIITSU.config.handler.GlobalExceptionHandler.PasswordMismatchException;
import com.YOGIITSU.dto.ResponseDto.TokenResponseDto;
import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.YOGIITSU.entity.Member;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

	private final AuthenticationManagerBuilder authenticationManagerBuilder;
	private final JwtTokenProvider jwtTokenProvider;
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final Logger logger = LoggerFactory.getLogger(MemberService.class);

	/**
	 * 로그인 처리 메서드
	 *
	 * @param memberId 사용자의 아이디
	 * @param password 사용자의 비밀번호
	 * @return TokenInfo JWT 토큰 정보
	 */
	@Transactional
	public TokenResponseDto login(String memberId, String password) {
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
	 * 관리자 로그인 처리 메서드
	 *
	 * @param memberId 사용자의 아이디
	 * @param password 사용자의 비밀번호
	 * @return TokenInfo JWT 토큰 정보
	 */
	@Transactional
	public TokenResponseDto adminLogin(String memberId, String password) {
		// 1. 사용자가 존재하는지 확인
		Member member = memberRepository.findByMemberId(memberId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "존재하지 않는 계정입니다."));

		// 2. 관리자 계정인지 확인
		if (!"ADMIN".equals(member.getRole())) {
			throw new AdminAccessDeniedException();
		}

		// 3. 비밀번호 검증 후 토큰 발급
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
			memberId, password);

		Authentication authentication = authenticationManagerBuilder.getObject()
			.authenticate(authenticationToken);

		return jwtTokenProvider.generateToken(authentication);
	}


	/**
	 * 이메일로 아이디 찾기
	 *
	 * @param email 사용자의 이메일
	 * @return 사용자 아이디
	 */
	public String findIdByEmail(String email) {
		// 이메일로 사용자 조회
		return memberRepository.findByEmail(email)
			.map(Member::getMemberId)
			.orElse(null); // 가입된 이메일이 없으면 null 반환
	}

	/**
	 * 회원 탈퇴 처리 메서드
	 *
	 * @param memberId 사용자의 아이디
	 */
	@Transactional
	public void deleteMember(String memberId, String rawPassword) {
		// 1. 회원 조회
		Member member = memberRepository.findByMemberId(memberId)
			.orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

		// 2. 비밀번호 검증 (암호화된 비밀번호와 비교)
		if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
			throw new PasswordMismatchException();
		}

		// 3. 회원 정보 삭제
		memberRepository.delete(member);

		// 4. 회원 삭제 후 존재 여부 확인
		if (memberRepository.existsByMemberId(memberId)) {
			throw new RuntimeException("회원 탈퇴 실패: 회원 정보가 삭제되지 않았습니다.");
		}

		// 5. 로그 기록
		logger.info("회원 탈퇴: {}", memberId);
	}

}
