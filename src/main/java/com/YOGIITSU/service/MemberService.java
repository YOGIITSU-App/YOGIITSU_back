package com.YOGIITSU.service;

import com.YOGIITSU.exception.auth.EmailVerificationNotApprovedException;
import com.YOGIITSU.exception.auth.InvalidLoginException;
import com.YOGIITSU.exception.user.MemberNotFoundException;
import com.YOGIITSU.exception.user.PasswordMismatchException;
import com.YOGIITSU.exception.user.PasswordNotEqualsException;
import com.YOGIITSU.exception.user.SamePasswordException;
import com.YOGIITSU.exception.validation.InvalidArgumentException;
import com.YOGIITSU.exception.system.SystemException;
import com.YOGIITSU.exception.ErrorCode;
import com.YOGIITSU.dto.RequestDto.PasswordResetRequestDto;
import com.YOGIITSU.dto.ResponseDto.TokenResponseDto;
import com.YOGIITSU.entity.EmailMessage;
import com.YOGIITSU.entity.EmailPurpose;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.jwt.CustomUserDetails;
import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.repository.EmailMessageRepository;
import com.YOGIITSU.repository.MemberRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.YOGIITSU.exception.auth.TooManyRequestsException;
import com.YOGIITSU.security.AttemptLimiter;
import java.time.Duration;
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
	private final AttemptLimiter attemptLimiter;

	// 로그인 실패 제한: 같은 아이디로 15분 내 5회 실패 시 잠금
	private static final ZoneId KST = ZoneId.of("Asia/Seoul");
	private static final int LOGIN_MAX_FAILURES = 5;
	private static final Duration LOGIN_FAILURE_WINDOW = Duration.ofMinutes(15);
	private final Logger logger = LoggerFactory.getLogger(MemberService.class);

	/**
	 * 로그인 처리 메서드
	 *
	 * @param memberId 사용자의 아이디
	 * @param password 사용자의 비밀번호
	 * @return accessToken은 Authorization 헤더로, refreshToken은 X-Refresh-Token 헤더로 내려보냄
	 */
	@Transactional
	public ResponseEntity<Map<String, Object>> login(String memberId, String password) {
		String limitKey = "login:" + (memberId == null ? "" : memberId.trim().toLowerCase());
		if (attemptLimiter.isLimited(limitKey, LOGIN_MAX_FAILURES, LOGIN_FAILURE_WINDOW)) {
			throw new TooManyRequestsException("로그인 시도가 너무 많습니다. 15분 후 다시 시도해 주세요.");
		}
		try {
			// 1. 아이디와 비밀번호로 Authentication 객체 생성
			Authentication authentication = authenticationManagerBuilder.getObject()
				.authenticate(new UsernamePasswordAuthenticationToken(memberId, password));

			// 2. 인증된 정보를 바탕으로 JWT 토큰 생성
			TokenResponseDto tokenInfo = jwtTokenProvider.generateToken(authentication);

			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			Long userId = userDetails.getId();
			String role = userDetails.getRole();

			// 3. 생성된 토큰을 헤더에 담아 반환
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization",
				"Bearer " + tokenInfo.getAccessToken());   // AccessToken → Authorization 헤더
			headers.set("X-Refresh-Token",
				tokenInfo.getRefreshToken());           // RefreshToken → X-Refresh-Token 헤더

			Map<String, Object> responseBody = new HashMap<>();
			responseBody.put("message", "로그인 성공");
			responseBody.put("userId", userId);
			responseBody.put("role", role);

			attemptLimiter.reset(limitKey);
			return ResponseEntity.ok()
				.headers(headers)
				.body(responseBody);

		} catch (BadCredentialsException e) {
			attemptLimiter.record(limitKey);
			throw new InvalidLoginException();
		}
	}

	/**
	 * 이메일로 아이디 찾기
	 * 아이디 찾기(FIND_ID) 이메일 인증이 완료된 경우에만 전체 아이디를 반환하고, 사용한 인증 정보는 삭제한다.
	 * 인증 전에는 마스킹된 아이디를 반환한다. (앱이 인증 전 가입 여부 확인용으로 호출하므로 호환 유지)
	 *
	 * @param email 사용자의 이메일
	 * @return 사용자 아이디 (미인증 시 마스킹), 가입된 이메일이 없으면 null
	 */
	@Transactional
	public String findIdByEmail(String email) {
		String memberId = memberRepository.findByEmail(email)
			.map(Member::getMemberId)
			.orElse(null);
		if (memberId == null) {
			return null; // 가입된 이메일이 없으면 null 반환
		}

		Optional<EmailMessage> verified = emailMessageRepository.findByEmailAndIsApprovedTrue(email)
			.filter(message -> message.getPurpose() == EmailPurpose.FIND_ID)
			.filter(message -> message.getExpiresAt() == null
				|| !message.getExpiresAt().isBefore(LocalDateTime.now(KST)));
		if (verified.isEmpty()) {
			return maskMemberId(memberId);
		}

		// 인증 정보는 1회만 사용
		emailMessageRepository.delete(verified.get());
		return memberId;
	}

	// 앞 2자리만 노출하고 나머지는 마스킹
	static String maskMemberId(String memberId) {
		int visible = Math.min(2, memberId.length());
		return memberId.substring(0, visible) + "*".repeat(Math.max(memberId.length() - visible, 4));
	}

	/**
	 * 회원 탈퇴 처리 메서드
	 *
	 * @param memberId 사용자의 아이디
	 */
	@Transactional
	public void deleteMember(String memberId) {
		// 1. 회원 조회
		Member member = memberRepository.findByMemberId(memberId)
			.orElseThrow(MemberNotFoundException::new);

		// 2. 회원 정보 삭제
		memberRepository.delete(member);

		// 3. 회원 삭제 후 존재 여부 확인
		if (memberRepository.existsByMemberId(memberId)) {
			throw new SystemException(ErrorCode.DATABASE_ERROR, "회원 탈퇴 실패: 회원 정보가 삭제되지 않았습니다.");
		}

		// 4. 로그 기록
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

		// 3. 기존 비밀번호와 같은지 확인
		if (passwordEncoder.matches(newPassword, member.getPassword())) {
			throw new SamePasswordException();
		}

		// 4. 비밀번호 암호화 후 변경
		String encodedPassword = passwordEncoder.encode(newPassword);
		member.changePassword(encodedPassword);

		// 5. 저장
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
		if (!emailMessage.isApproved()) {
			throw new EmailVerificationNotApprovedException();
		}

		// 3. 새 비밀번호와 비밀번호 확인이 일치하지 않으면 예외 발생
		if (!requestDto.getNewPassword().equals(requestDto.getConfirmPassword())) {
			throw new PasswordMismatchException();
		}

		// 4. 이메일로 사용자 조회 (없으면 예외 발생)
		Member member = memberRepository.findByEmail(requestDto.getEmail())
			.orElseThrow(MemberNotFoundException::new);

		// 5. 새 비밀번호가 기존 비밀번호와 동일한지 확인
		if (passwordEncoder.matches(requestDto.getNewPassword(), member.getPassword())) {
			throw new SamePasswordException();
		}

		// 6. 비밀번호 암호화 후 변경
		member.changePassword(passwordEncoder.encode(requestDto.getNewPassword()));
		memberRepository.save(member);

		// 7. 사용된 인증 메시지 삭제 (재사용 방지)
		emailMessageRepository.delete(emailMessage);
	}

	/**
	 * 전체 회원 수 조회 메서드
	 *
	 * @return 전체 회원 수
	 */
	public Long getMemberCount() {
		return memberRepository.count();
	}
}
