package com.YOGIITSU.service;

import com.YOGIITSU.dto.RequestDto.MemberSignUpRequestDto;
import com.YOGIITSU.entity.EmailMessage;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.repository.EmailMessageRepository;
import com.YOGIITSU.repository.MemberRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SignUpService {

    private final MemberRepository memberRepository;
    private final EmailMessageRepository emailMessageRepository;
    private final PasswordEncoder passwordEncoder;
    private static final List<String> ALLOWED_DOMAINS = List.of("@suwon.ac.kr", "@gmail.com",
        "@naver.com");

    @Transactional
    public void register(MemberSignUpRequestDto dto) {

        // 1. 이메일 중복 체크
        if (memberRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("해당 이메일로 가입한 내역이 있습니다.");
        }

        // 2. 이메일 인증 확인
        Optional<EmailMessage> verifiedEmail = emailMessageRepository.findByEmailAndIsApprovedTrue(
            dto.getEmail());
        if (verifiedEmail.isEmpty()) {
            throw new IllegalArgumentException("이메일 인증이 완료되지 않았습니다.");
        }

        // 3. 아이디 중복 체크
        if (memberRepository.findByMemberId(dto.getMemberId()).isPresent()) {
            throw new IllegalArgumentException("아이디가 이미 존재합니다.");
        }

        // 3-1. 아이디 유효성 검사
        String memberIdPattern = "^(?=.*[a-zA-Z])[a-zA-Z0-9]{4,20}$";
        if (!Pattern.matches(memberIdPattern, dto.getMemberId())) {
            throw new IllegalArgumentException("아이디는 4~20자의 영문 또는 영문+숫자로만 구성되어야 합니다.");
        }

        // 4. 이메일 도메인 검사
        boolean isValidDomain = ALLOWED_DOMAINS.stream()
            .anyMatch(dto.getEmail()::endsWith);

        if (!isValidDomain) {
            throw new IllegalArgumentException(
                "이메일은 suwon.ac.kr, gmail.com, naver.com 도메인만 허용됩니다.");
        }

        // 5. 이름 중복 체크
        if (memberRepository.findByUserName(dto.getUserName()).isPresent()) {
            throw new IllegalArgumentException("해당 이름으로 가입한 내역이 있습니다.");
        }

        // 6. 이름 유효성 검사 (영문/한글/숫자, 특수문자 불가, 2~8자리)
        String userNamePattern = "^[a-zA-Z0-9가-힣]{2,8}$";
        if (!Pattern.matches(userNamePattern, dto.getUserName())) {
            throw new IllegalArgumentException("이름은 2~8자의 영문, 한글, 숫자로만 입력해주세요. 특수문자는 사용할 수 없습니다.");
        }

        // 7. 비밀번호 유효성 검사
        validatePassword(dto.getPassword());

        // 8. 비밀번호 암호화 및 회원 생성
        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        Member member = Member.builder()
            .memberId(dto.getMemberId())
            .password(encodedPassword)
            .email(dto.getEmail())
            .userName(dto.getUserName())
            .role("USER")  // 반드시 ROLE_ 접두사
            .joinAt(java.time.LocalDateTime.now())
            .build();

        // 9. DB 저장
        memberRepository.save(member);
    }

    private void validatePassword(String password) {
        if (password.length() < 8 || password.length() > 16) {
            throw new IllegalArgumentException("비밀번호는 8자 이상 16자 이하이어야 합니다.");
        }
        if (!Pattern.compile("[a-zA-Z]").matcher(password).find()) {
            throw new IllegalArgumentException("비밀번호에는 영문자가 최소 1자 이상 포함되어야 합니다.");
        }
        if (!Pattern.compile("[0-9]").matcher(password).find()) {
            throw new IllegalArgumentException("비밀번호에는 숫자가 최소 1자 이상 포함되어야 합니다.");
        }
        if (!Pattern.compile("[!@#$%^&*(),.?\":{}|<>]").matcher(password).find()) {
            throw new IllegalArgumentException("비밀번호에는 특수문자가 최소 1자 이상 포함되어야 합니다.");
        }
    }
}
