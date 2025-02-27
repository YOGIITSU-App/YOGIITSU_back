package com.YOGIITSU.service;

import com.YOGIITSU.dto.MemberSignUpRequestDto;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SignUpService { // SignUpService는 회원가입 관련 비즈니스 로직을 처리하는 서비스 클래스

    private final MemberRepository memberRepository; // 회원 데이터 저장소
    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화를 위한 인코더

    @Transactional
    public void register(MemberSignUpRequestDto memberSignUpRequestDto) {
        // 1. ID 중복 체크
        if (memberRepository.findByMemberId(memberSignUpRequestDto.getMemberId()).isPresent()) {
            throw new IllegalArgumentException("아이디가 이미 존재합니다."); // ID 중복 시 예외 발생
        }

        // 2. 이메일 중복 체크 및 유효성 검사
        if (memberRepository.findByEmail(memberSignUpRequestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이메일이 이미 존재합니다."); // 이메일 중복 시 예외 발생
        }
        if (!memberSignUpRequestDto.getEmail().endsWith("@suwon.ac.kr")) {
            throw new IllegalArgumentException("이메일은 무조건 @suwon.ac.kr로 끝나야 합니다."); // 이메일 도메인 유효성 검사
        }

        // 3. 비밀번호 유효성 검사
        validatePassword(memberSignUpRequestDto.getPassword());

        // 4. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(memberSignUpRequestDto.getPassword());

        // 5. Member 객체 생성
        Member member = Member.builder()
            .memberId(memberSignUpRequestDto.getMemberId()) // 회원 ID 설정
            .password(encodedPassword) // 암호화된 비밀번호 설정
            .email(memberSignUpRequestDto.getEmail()) // 이메일 설정
            .userName(memberSignUpRequestDto.getUserName()) // 사용자 이름 설정
            .role("USER") // 기본 역할을 USER로 설정
            .joinAt(java.time.LocalDateTime.now()) // 가입 시간 설정
            .build();

        // 6. 데이터 저장
        memberRepository.save(member); // 생성된 Member 객체를 데이터베이스에 저장
    }

    // 비밀번호 유효성 검사 메서드
    private void validatePassword(String password) {
        if (password.length() < 8) { // 비밀번호 길이가 8자 미만이면
            throw new IllegalArgumentException("비밀번호는 최소 8글자여야 합니다."); // 메세지 출력
        }
        if (!Pattern.compile("[A-Z]").matcher(password).find()) { // 대문자가 1개 이상 들어가 있지 않으면
            throw new IllegalArgumentException("비밀번호는 한 개 이상의 대문자를 포함해야 합니다."); // 메세지 출력
        }
        if (!Pattern.compile("[a-z]").matcher(password).find()) { // 소문자가 1개 이상 들어가 있지 않으면
            throw new IllegalArgumentException("비밀번호는 한 개 이상의 소문자를 포함해야 합니다."); // 메세지 출력
        }
        if (!Pattern.compile("[0-9]").matcher(password).find()) { // 숫자가 1개 이상 들어가 있지 않으면
            throw new IllegalArgumentException("비밀번호는 한 개 이상의 숫자를 포함해야 합니다."); // 메세지 출력
        }
        if (!Pattern.compile("[!@#$%^&*(),.?\":{}|<>]").matcher(password).find()) { // 특수문자가 1개 이상 들어가 있지 않으면
            throw new IllegalArgumentException("비밀번호는 한 개 이상의 특수문자를 포함해야 합니다."); // 메세지 출력
        }
    }
}