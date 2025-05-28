package com.YOGIITSU.service;

import com.YOGIITSU.entity.EmailMessage;
import com.YOGIITSU.jwt.EmailVerificationJwtProvider;
import com.YOGIITSU.repository.EmailMessageRepository;
import com.YOGIITSU.repository.MemberRepository;
import java.util.Locale;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import io.jsonwebtoken.Claims;
import java.time.LocalDateTime;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.entity.EmailPurpose;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final EmailMessageRepository emailMessageRepository;

    // EmailService 클래스 안에 추가해야 하는 의존성
    private final MemberRepository memberRepository;
    private final EmailVerificationJwtProvider emailJwtProvider;

    // 인증 코드 생성
    public String generateVerificationCode() {
        Random random = new Random();
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            key.append((char) (random.nextInt(26) + 65)); // A~Z
        }
        return key.toString();
    }

    //이메일 인증 메세지 저장
    @Transactional
    public void saveEmailMessage(EmailMessage emailMessage) {
        emailMessageRepository.save(emailMessage);
    }

    // 이메일 전송 (인증 코드 포함)
    public void sendMail(EmailMessage emailMessage, String type) {
        // 도메인 제한 체크
        if (!isAllowedEmailDomain(emailMessage.getEmail())) {
            throw new IllegalArgumentException(
                "suwon.ac.kr, naver.com, gmail.com 도메인만 인증할 수 있습니다.");
        }

        try {
            MimeMessageHelper mimeMessageHelper = createMimeMessage(emailMessage,
                emailMessage.getCode(), type);
            javaMailSender.send(mimeMessageHelper.getMimeMessage());
            log.info("메일 전송 성공: {}", emailMessage.getEmail());

        } catch (MailException e) {
            log.error("메일 전송 실패: {}", e.getMessage());
            throw new RuntimeException("메일 전송 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // 허용된 이메일 도메인인지 확인
    private boolean isAllowedEmailDomain(String email) {
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        return normalized.endsWith("@suwon.ac.kr")
            || normalized.endsWith("@naver.com")
            || normalized.endsWith("@gmail.com");
    }

    // MimeMessage 생성
    private MimeMessageHelper createMimeMessage(EmailMessage emailMessage, String authNum,
        String type) {
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(
                javaMailSender.createMimeMessage(), false, "UTF-8");

            mimeMessageHelper.setTo(emailMessage.getEmail());
            mimeMessageHelper.setSubject("YOGIITSU 인증 코드: " + authNum);
            mimeMessageHelper.setText(setContext(authNum, type), true);

            return mimeMessageHelper;
        } catch (Exception e) {
            throw new RuntimeException("MimeMessage 생성 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * HTML 메일 본문 생성 (Thymeleaf)
     */
    private String setContext(String code, String type) {
        Context context = new Context();
        context.setVariable("code", code);
        return templateEngine.process(type, context);
    }

    // 목적에 따라 이메일 요청 유효성 검사
    public void validateEmailRequest(String email, EmailPurpose purpose, Authentication auth) {
        switch (purpose) {
            case EMAIL_CHANGE_OLD -> validateEmailForChangeOld(email, auth);

            case EMAIL_CHANGE_NEW -> {
                checkAuthentication(auth);
                if (memberRepository.existsByEmail(email)) {
                    throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
                }
            }

            case PASSWORD_CHANGE, FIND_PASSWORD, FIND_ID -> checkAuthentication(auth);

            case SIGNUP -> {
                if (memberRepository.existsByEmail(email)) {
                    throw new IllegalArgumentException("이미 가입된 이메일입니다.");
                }
            }

            default -> throw new IllegalArgumentException("지원하지 않는 인증 목적입니다.");
        }
    }

    // 인증이 필요한 경우 공통 검사 메서드
    public void checkAuthentication(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalArgumentException("로그인이 필요한 요청입니다.");
        }
    }

    public void validateEmailForChangeOld(String email, Authentication auth) {
        // 1. 로그인된 사용자 확인
        checkAuthentication(auth);

        // 2. DB에 해당 이메일 존재 여부 확인 추가
        if (!memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("해당 이메일로 가입된 계정이 존재하지 않습니다.");
        }

        // 3. 로그인된 사용자 정보 조회
        String memberId = auth.getName();
        Member member = memberRepository.findByMemberId(memberId)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // 4. 입력한 이메일이 로그인된 사용자의 현재 이메일과 일치하는지 확인
        if (!member.getEmail().equals(email)) {
            throw new IllegalArgumentException("입력한 이메일이 로그인된 사용자의 이메일과 일치하지 않습니다.");
        }
    }

    @Transactional
    public void approveEmailCode(String email, String code) {
        EmailMessage message = findAndValidateEmailMessage(email, code);

        message.setIsApproved(true);
        emailMessageRepository.save(message);
    }

    public void checkAuthenticationIfRequired(EmailPurpose purpose, Authentication auth) {
        if (EmailPurpose.requiresLogin(purpose)) {
            checkAuthentication(auth);
        }
    }

    // 이메일 변경 부분
    @Transactional
    public void verifyAndMaybeChangeEmail(String email, String code, String token,
        Authentication auth) {
        // 인증 코드 확인만 별도 메서드로 위임
        EmailMessage message = verifyEmailCode(email, code, token);

        message.setIsApproved(true);
        emailMessageRepository.save(message);

        EmailPurpose purpose = message.getPurpose();
        checkAuthenticationIfRequired(purpose, auth);

        if (purpose == EmailPurpose.EMAIL_CHANGE_NEW) {
            String memberId = auth.getName();
            Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

            if (memberRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }

            member.setEmail(email);
            memberRepository.save(member);
            log.info("이메일 변경 완료 - memberId: {}, newEmail: {}", memberId, email); // 로그 추가
        }
    }

    private EmailMessage verifyEmailCode(String email, String code, String token) {
        Claims claims = emailJwtProvider.parseEmailToken(token)
            .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 토큰입니다."));

        if (!claims.getSubject().equals(email) || !claims.get("code", String.class).equals(code)) {
            throw new IllegalArgumentException("이메일 또는 인증 코드가 일치하지 않습니다.");
        }

        return findAndValidateEmailMessage(email, code);
    }

    private EmailMessage findAndValidateEmailMessage(String email, String code) {
        EmailMessage message = emailMessageRepository.findByEmailAndCode(email, code)
            .orElseThrow(() -> new IllegalArgumentException("DB에 인증 코드가 존재하지 않습니다."));

        if (message.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("인증 코드가 만료되었습니다.");
        }

        return message;
    }
}