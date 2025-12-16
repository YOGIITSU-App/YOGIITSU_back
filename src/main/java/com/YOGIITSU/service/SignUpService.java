package com.YOGIITSU.service;

import com.YOGIITSU.dto.RequestDto.MemberSignUpRequestDto;
import com.YOGIITSU.entity.EmailMessage;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.exception.user.IdAlreadyExistsException;
import com.YOGIITSU.exception.validation.InvalidEmailDomainException;
import com.YOGIITSU.exception.validation.InvalidMemberIdFormatException;
import com.YOGIITSU.exception.validation.InvalidPasswordFormatException;
import com.YOGIITSU.exception.validation.InvalidUsernameFormatException;
import com.YOGIITSU.repository.EmailMessageRepository;
import com.YOGIITSU.repository.MemberRepository;
import com.YOGIITSU.exception.validation.EmailAlreadyExistsException;
import com.YOGIITSU.exception.auth.EmailVerificationNotApprovedException;
import com.YOGIITSU.exception.validation.UsernameAlreadyExistsException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SignUpService {

	private final MemberRepository memberRepository;
	private final EmailMessageRepository emailMessageRepository;
	private final PasswordEncoder passwordEncoder;
	private static final List<String> ALLOWED_DOMAINS = List.of("@suwon.ac.kr", "@gmail.com",
		"@naver.com");
	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

	@Transactional
	public void register(MemberSignUpRequestDto dto) {

		String email = dto.getEmail() == null ? "" : dto.getEmail().trim().toLowerCase();
		String memberId = dto.getMemberId() == null ? "" : dto.getMemberId().trim();
		String userName = dto.getUserName() == null ? "" : dto.getUserName().trim();

		// 1. 이메일 중복 체크
		if (memberRepository.findByEmail(email).isPresent()) {
			throw new EmailAlreadyExistsException("해당 이메일로 가입한 내역이 있습니다.");
		}

		// 2. 이메일 인증 확인(승인 + 만료 + 용도)
		EmailMessage verified = emailMessageRepository.findByEmailAndIsApprovedTrue(email)
			.orElseThrow(() -> new EmailVerificationNotApprovedException("이메일 인증이 완료되지 않았습니다."));
		if (verified.getExpiresAt() != null && verified.getExpiresAt()
			.isBefore(LocalDateTime.now(KST))) {
			throw new com.YOGIITSU.exception.auth.VerificationCodeExpiredException();
		}
		if (verified.getPurpose() != null
			&& verified.getPurpose() != com.YOGIITSU.entity.EmailPurpose.SIGNUP) {
			throw new EmailVerificationNotApprovedException("회원가입용 인증 정보가 아닙니다.");
		}

		// 3. 아이디 중복 체크
		if (memberRepository.findByMemberId(memberId).isPresent()) {
			throw new IdAlreadyExistsException("아이디가 이미 존재합니다.");
		}

		// 3-1. 아이디 유효성 검사
		validateMemberId(memberId);

		// 4. 이메일 도메인 검사
		boolean isValidDomain = ALLOWED_DOMAINS.stream().anyMatch(email::endsWith);

		if (!isValidDomain) {
			throw new InvalidEmailDomainException(
				"이메일은 suwon.ac.kr, gmail.com, naver.com 도메인만 허용됩니다.");
		}

		// 5. 이름 중복 체크
		if (memberRepository.findByUserName(userName).isPresent()) {
			throw new UsernameAlreadyExistsException("이미 사용 중인 이름입니다. 다른 이름을 입력해주세요.");
		}

		// 6. 이름 유효성 검사 (영문/한글/숫자, 특수문자 불가, 2~8자리)
		String userNamePattern = "^[a-zA-Z0-9가-힣]{2,8}$";
		if (!Pattern.matches(userNamePattern, userName)) {
			throw new InvalidUsernameFormatException(
				"이름은 2~8자의 영문, 한글, 숫자로만 입력해주세요. 특수문자는 사용할 수 없습니다.");
		}

		// 7. 비밀번호 유효성 검사
		validatePassword(dto.getPassword());

		// 8. 비밀번호 암호화 및 회원 생성
		String encodedPassword = passwordEncoder.encode(dto.getPassword());

		Member member = Member.builder()
			.memberId(memberId)
			.password(encodedPassword)
			.email(email)
			.userName(userName)
			.role("USER")  // 반드시 ROLE_ 접두사
			.joinAt(LocalDateTime.now(KST))
			.provider("local") // 로컬 회원가입을 나타내는 provider
			.build();

		// 9. DB 저장
		memberRepository.save(member);
	}

	private void validatePassword(String password) {
		if (password == null || password.isBlank()) {
			throw new InvalidPasswordFormatException("비밀번호가 비어 있거나 null입니다.");
		}
		if (password.length() < 8 || password.length() > 16) {
			throw new InvalidPasswordFormatException("비밀번호는 8자 이상 16자 이하이어야 합니다.");
		}
		if (!Pattern.compile("[a-zA-Z]").matcher(password).find()) {
			throw new InvalidPasswordFormatException("비밀번호에는 영문자가 최소 1자 이상 포함되어야 합니다.");
		}
		if (!Pattern.compile("[0-9]").matcher(password).find()) {
			throw new InvalidPasswordFormatException("비밀번호에는 숫자가 최소 1자 이상 포함되어야 합니다.");
		}
		if (password.matches(".*[^a-zA-Z0-9*!_@#$].*")) {
			throw new InvalidPasswordFormatException("비밀번호에는 *!_@#$ 외의 특수문자는 사용할 수 없습니다.");
		}
		if (!password.matches(".*[*!_@#$].*")) {
			throw new InvalidPasswordFormatException("비밀번호에는 *!_@#$ 중 최소 1자 이상의 특수문자가 포함되어야 합니다.");
		}
	}

	private void validateMemberId(String memberId) {
		if (memberId.length() < 4 || memberId.length() > 20) {
			throw new InvalidMemberIdFormatException("아이디는 4자 이상 20자 이하이어야 합니다.");
		}
		if (!Pattern.compile("^[a-zA-Z]").matcher(memberId).find()) {
			throw new InvalidMemberIdFormatException("아이디는 반드시 영문자로 시작해야 합니다.");
		}
		if (!Pattern.compile("^[a-zA-Z0-9]+$").matcher(memberId).matches()) {
			throw new InvalidMemberIdFormatException("아이디는 영문자와 숫자로만 구성되어야 합니다.");
		}
	}
}