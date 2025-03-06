package com.YOGIITSU.service;

import com.YOGIITSU.dto.RequestDto.MemberSignUpRequestDto;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SignUpService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Map<String, String> register(MemberSignUpRequestDto memberSignUpRequestDto) {
        Map<String, String> response = new HashMap<>();

        // 1. 아이디 중복 체크
        if (memberRepository.findByMemberId(memberSignUpRequestDto.getMemberId()).isPresent()) {
            response.put("message", "아이디가 이미 존재합니다.");
            return response;
        }

        // 2. 이메일 중복 체크
        if (memberRepository.findByEmail(memberSignUpRequestDto.getEmail()).isPresent()) {
            response.put("message", "해당 이메일로 가입한 내역이 있습니다.");
            return response;
        }

        // 이메일 도메인 검사
        if (!memberSignUpRequestDto.getEmail().endsWith("@suwon.ac.kr")) {
            response.put("message", "이메일은 무조건 @suwon.ac.kr로 끝나야 합니다.");
            return response;
        }

        // 3. 이름 중복 체크
        if (memberRepository.findByUserName(memberSignUpRequestDto.getUserName()).isPresent()) {
            response.put("message", "해당 이름으로 가입한 내역이 있습니다.");
            return response;
        }

        // 4. 이름 길이 검사 (최소 2글자 이상)
        if (memberSignUpRequestDto.getUserName().length() < 2) {
            response.put("message", "이름은 최소 2글자 이상이어야 합니다.");
            return response;
        }

        // 5. 비밀번호 유효성 검사
        try {
            validatePassword(memberSignUpRequestDto.getPassword());
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return response;
        }

        // 6. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(memberSignUpRequestDto.getPassword());

        // 7. Member 객체 생성
        Member member = Member.builder()
            .memberId(memberSignUpRequestDto.getMemberId())
            .password(encodedPassword)
            .email(memberSignUpRequestDto.getEmail())
            .userName(memberSignUpRequestDto.getUserName())
            .role("USER")
            .joinAt(java.time.LocalDateTime.now())
            .build();

        // 8. 데이터 저장
        memberRepository.save(member);

        // 9. 성공 응답 반환
        response.put("message", "회원가입이 성공적으로 완료되었습니다.");
        return response;
    }

    // 비밀번호 유효성 검사 메서드
    private void validatePassword(String password) {
        if (password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 최소 8글자여야 합니다.");
        }
        if (!Pattern.compile("^[a-zA-Z0-9!@#$%^&*(),.?\":{}|<>]+$").matcher(password).find()) {
            throw new IllegalArgumentException("비밀번호는 대소문자로 이루어져야 합니다.");
        }
        if (!Pattern.compile("[0-9]").matcher(password).find()) {
            throw new IllegalArgumentException("비밀번호는 숫자를 포함해야 합니다.");
        }
        if (!Pattern.compile("[!@#$%^&*(),.?\":{}|<>]").matcher(password).find()) {
            throw new IllegalArgumentException("비밀번호는 특수문자를 포함해야 합니다.");
        }
    }
}
