package com.YOGIITSU.service;

import com.YOGIITSU.entity.EmailMessage;
import com.YOGIITSU.repository.EmailMessageRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final EmailMessageRepository emailMessageRepository;

    /**
     * 인증 코드 생성 (6자리 대문자)
     */
    public String generateVerificationCode() {
        Random random = new Random();
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            key.append((char) (random.nextInt(26) + 65)); // A~Z
        }
        return key.toString();
    }

    /**
     * 이메일 인증 메시지 저장
     */
    @Transactional
    public void saveEmailMessage(EmailMessage emailMessage) {
        emailMessageRepository.save(emailMessage);
    }

    /**
     * 이메일 전송 (코드 포함)
     */
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

    /**
     * 허용된 이메일 도메인인지 확인
     */
    private boolean isAllowedEmailDomain(String email) {
        return email.endsWith("@suwon.ac.kr")
            || email.endsWith("@naver.com")
            || email.endsWith("@gmail.com");
    }

    /**
     * MimeMessage 생성
     */
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
}
