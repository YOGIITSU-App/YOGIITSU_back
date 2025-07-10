package com.YOGIITSU.service;

import com.YOGIITSU.config.handler.EmailProperties;
import com.YOGIITSU.entity.EmailMessage;
import com.YOGIITSU.jwt.EmailVerificationJwtProvider;
import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.repository.EmailMessageRepository;
import com.YOGIITSU.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
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
    private final MemberRepository memberRepository;
    private final EmailVerificationJwtProvider emailJwtProvider;
    private final EmailProperties emailProperties;
    private final JwtTokenProvider jwtTokenProvider;

    // 6자리 인증 코드 생성 (A~Z 알파벳)
    public String generateVerificationCode() {
        Random random = new Random();
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            key.append((char) (random.nextInt(26) + 65)); // A~Z
        }
        return key.toString();
    }

    //인증 메일 정보를 DB에 저장
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
        return emailProperties.allowedDomains().stream()
            .anyMatch(domain -> normalized.endsWith("@" + domain));
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

    // 인증 목적에 따른 이메일 유효성 검사
    public void validateEmailRequest(String email, EmailPurpose purpose, Authentication auth) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일 주소는 필수입니다.");
        }

        // 인증이 필요한 목적인지 확인
        if (purpose.isRequiresAuth()) {
            checkAuthentication(auth);
        }

        if (purpose.isMustExist()) {
            checkEmailExists(email);

            if (purpose == EmailPurpose.EMAIL_CHANGE_OLD) {
                validateEmailForChangeOld(email, auth);
            }

        } else {
            if (memberRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }
        }
    }

    // 인증이 필요한 요청에 대해 인증 여부 검사
    public void checkAuthentication(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalArgumentException("로그인이 필요한 요청입니다.");
        }
    }

    // 이메일 변경 전 기존 이메일 검증 (로그인한 사용자와 입력 이메일 비교)
    public void validateEmailForChangeOld(String email, Authentication auth) {
        // 1. DB에 해당 이메일 존재 여부 확인 추가
        checkEmailExists(email);

        // 2. 로그인된 사용자 정보 조회
        String memberId = auth.getName();
        Member member = memberRepository.findByMemberId(memberId)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // 3. 입력한 이메일이 로그인된 사용자의 현재 이메일과 일치하는지 확인
        if (!member.getEmail().equals(email)) {
            throw new IllegalArgumentException("이메일 정보가 일치하지 않습니다.");
        }
    }

    @Transactional
    public void approveEmailCode(String email, String code) {
        EmailMessage message = findEmailMessage(email, code);
        approveAndSave(message);
    }

    // 이메일 변경 수행 (인증 코드 검증 -> 승인 -> 사용자 이메일 변경)
    @Transactional
    public void changeEmail(String token, HttpServletRequest request) {
        // 1. AccessToken 추출 및 유효성 검사
        String accessToken = jwtTokenProvider.resolveToken(request);
        if (accessToken == null || !jwtTokenProvider.validateToken(accessToken)) {
            throw new IllegalArgumentException("유효하지 않은 로그인 토큰입니다.");
        }

        // 2. 인증 객체 획득
        Authentication auth = jwtTokenProvider.getAuthentication(accessToken);

        // 3. JWT 토큰 파싱하여 새 이메일 및 인증 코드 추출
        Claims claims = emailJwtProvider.parseEmailToken(token)
            .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 토큰입니다."));

        String newEmail = claims.getSubject();
        String code = claims.get("code", String.class);

        // 4. 인증 코드 검증 및 승인 처리
        EmailMessage message = verifyEmailCode(newEmail, code);
        approveAndSave(message);

        // 5. 새 이메일 중복 여부 검사
        if (memberRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 6. 사용자 이메일 변경
        String memberId = auth.getName();
        Member member = memberRepository.findByMemberIdWithLock(memberId)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        member.setEmail(newEmail);
        memberRepository.save(member);

        log.info("이메일 변경 완료 - memberId: {}, newEmail: {}", memberId, newEmail);
    }

    // 인증 코드 검증 (존재 + 만료 여부)
    public EmailMessage verifyEmailCode(String email, String code) {
        EmailMessage message = findEmailMessage(email, code);
        if (message.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("인증 코드가 만료되었습니다.");
        }
        return message;
    }

    // 이메일과 코드로 EmailMessage 조회
    private EmailMessage findEmailMessage(String email, String code) {
        return emailMessageRepository.findByEmailAndCode(email, code)
            .orElseThrow(() -> new IllegalArgumentException("인증 코드가 만료되었습니다. 인증 코드를 재전송해 주세요."));
    }

    // 인증 승인 처리 및 저장
    private void approveAndSave(EmailMessage message) {
        message.approve();
        emailMessageRepository.save(message);
    }

    // 이메일 존재 여부 확인
    private void checkEmailExists(String email) {
        if (!memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("해당 이메일로 가입된 계정이 존재하지 않습니다.");
        }
    }
}