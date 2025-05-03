package com.YOGIITSU.service;

import com.YOGIITSU.config.handler.GlobalExceptionHandler.EmailVerificationNotApprovedException;
import com.YOGIITSU.config.handler.GlobalExceptionHandler.InvalidLoginException;
import com.YOGIITSU.config.handler.GlobalExceptionHandler.MemberNotFoundException;
import com.YOGIITSU.config.handler.GlobalExceptionHandler.PasswordMismatchException;
import com.YOGIITSU.config.handler.GlobalExceptionHandler.PasswordNotEqualsException;
import com.YOGIITSU.dto.RequestDto.PasswordResetRequestDto;
import com.YOGIITSU.dto.ResponseDto.TokenResponseDto;
import com.YOGIITSU.dto.ResponseDto.UserResponseDto;
import com.YOGIITSU.entity.EmailMessage;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.repository.EmailMessageRepository;
import com.YOGIITSU.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

	private final AuthenticationManagerBuilder authenticationManagerBuilder;
	private final JwtTokenProvider jwtTokenProvider;
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailMessageRepository emailMessageRepository;
	private final Logger logger = LoggerFactory.getLogger(MemberService.class);

	/**
	 * 로그인 처리 메서드
	 *
	 * @param memberId 사용자의 아이디
	 * @param password 사용자의 비밀번호
	 * @return TokenResponse JWT 토큰 정보
	 */
	@Transactional
	public TokenResponseDto login(String memberId, String password) {
		// 1. 아이디와 비밀번호를 기반으로 Authentication 객체 생성
		UsernamePasswordAuthenticationToken authenticationToken =
			new UsernamePasswordAuthenticationToken(memberId, password);

		try {
			// 2. 실제 검증 (사용자 비밀번호 체크)이 이루어지는 부분
			Authentication authentication = authenticationManagerBuilder.getObject()
				.authenticate(authenticationToken);

			// 3. 토큰 생성
			TokenResponseDto tokenInfo = jwtTokenProvider.generateToken(authentication);

			// 4. 사용자 정보 조회
			Member member = memberRepository.findByMemberId(memberId)
				.orElseThrow(MemberNotFoundException::new);

			// 5. 사용자 정보 DTO 생성
			UserResponseDto userDto = UserResponseDto.builder()
				.username(member.getUsername())
				.id(member.getId())
				.email(member.getEmail())
				.role(member.getRole()) // role 추가
				.build();

			// 6. 사용자 정보 포함해서 반환
			return TokenResponseDto.builder()
				.grantType("Bearer")
				.accessToken(tokenInfo.getAccessToken())
				.refreshToken(tokenInfo.getRefreshToken())
				.user(userDto)
				.build();

		} catch (BadCredentialsException e) {
			// 4. 인증 실패 시 예외 처리
			throw new InvalidLoginException();
		}
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

	/**
	 * 비밀번호 변경 메서드
	 *
	 * @param memberId        사용자 아이디
	 * @param newPassword     새로운 비밀번호
	 * @param confirmPassword 새로운 비밀번호 확인
	 */
	@Transactional
	public void changePassword(String memberId, String newPassword, String confirmPassword) {

		// 1. 비밀번호 일치 확인
		if (!newPassword.equals(confirmPassword)) {
			throw new PasswordNotEqualsException();
		}

		// 2. 회원 조회
		Member member = memberRepository.findByMemberId(memberId)
			.orElseThrow(MemberNotFoundException::new);

		// 3. 비밀번호 암호화 후 변경
		String encodedPassword = passwordEncoder.encode(newPassword);
		member.changePassword(encodedPassword);

		// 4. 저장
		memberRepository.save(member);
	}

	/**
	 * 비밀번호 재설정 메서드 (이메일 인증 후)
	 *
	 * @param requestDto 비밀번호 재설정 요청 DTO
	 */
	@Transactional
	public void resetPasswordAfterEmailVerification(PasswordResetRequestDto requestDto) {

		// 1. 이메일로 인증 요청 내역 조회 (없으면 예외 발생)
		EmailMessage emailMessage = emailMessageRepository.findByEmail(requestDto.getEmail())
			.orElseThrow(EmailVerificationNotApprovedException::new);

		// 2. 이메일 인증이 완료되지 않은 경우 예외 발생
		if (emailMessage.getIsApproved() == null || !emailMessage.getIsApproved()) {
			throw new EmailVerificationNotApprovedException();
		}

		// 3. 새 비밀번호와 비밀번호 확인이 일치하지 않으면 예외 발생
		if (!requestDto.getNewPassword().equals(requestDto.getConfirmPassword())) {
			throw new PasswordMismatchException();
		}

		// 4. 이메일로 사용자 조회 (없으면 예외 발생)
		Member member = memberRepository.findByEmail(requestDto.getEmail())
			.orElseThrow(MemberNotFoundException::new);

		// 5. 비밀번호 암호화 후 변경
		member.changePassword(passwordEncoder.encode(requestDto.getNewPassword()));
		memberRepository.save(member);

		// 6. 인증 상태 초기화 (다시 사용하지 못하게)
		emailMessage.setIsApproved(false);
		emailMessageRepository.save(emailMessage);
	}
}