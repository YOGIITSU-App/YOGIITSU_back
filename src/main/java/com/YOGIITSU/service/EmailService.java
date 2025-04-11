package com.YOGIITSU.service;

import com.YOGIITSU.entity.EmailMessage;
import com.YOGIITSU.repository.EmailMessageRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.springframework.transaction.annotation.Transactional;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

	private final JavaMailSender javaMailSender;
	private final SpringTemplateEngine templateEngine;
	private final EmailMessageRepository emailMessageRepository;

	/**
	 * 이메일 전송 메서드
	 *
	 * @param emailMessage 이메일 메시지 객체
	 * @param type         이메일 타입 (e.g., "password", "verification")
	 * @return 인증 코드
	 */
	@Transactional
	public String sendMail(EmailMessage emailMessage, String type) {
		String authNum = createCode(); // 인증 코드 생성
		String email = emailMessage.getEmail();

		// 이메일 도메인 검사 추가
		if (!isAllowedEmailDomain(email)) {
			throw new IllegalArgumentException("suwon.ac.kr, naver.com, gmail.com 도메인만 인증할 수 있습니다.");
		}

		try {
			// 항상 새로 저장되도록
			EmailMessage newMessage = EmailMessage.builder()
				.email(email)
				.code(authNum)
				.isApproved(false)
				.expiresAt(LocalDateTime.now().plusMinutes(5)) // 5분 유효
				.build();

			emailMessageRepository.save(newMessage);

			// 이메일 전송
			MimeMessageHelper mimeMessageHelper = createMimeMessage(emailMessage, authNum, type);
			javaMailSender.send(mimeMessageHelper.getMimeMessage());

			log.info("메일 전송 성공: {}", email);
			return authNum;

		} catch (MailException e) {
			log.error("메일 전송 실패: {}", e.getMessage());
			throw new RuntimeException("메일 전송 중 오류가 발생했습니다: " + e.getMessage(), e);
		}
	}

	//도메인 검사 메서드 추가
	private boolean isAllowedEmailDomain(String email) {
		return email.endsWith("@suwon.ac.kr")
			|| email.endsWith("@naver.com")
			|| email.endsWith("@gmail.com");
	}

	/**
	 * MimeMessage 생성 메서드 (Spring Mail API)
	 */
	private MimeMessageHelper createMimeMessage(EmailMessage emailMessage, String authNum,
		String type) {
		try {
			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(
				javaMailSender.createMimeMessage(), false, "UTF-8");
			mimeMessageHelper.setTo(emailMessage.getEmail()); // 수신자 이메일
			mimeMessageHelper.setSubject("YOGIITSU 인증 코드: " + authNum); // 이메일 제목
			mimeMessageHelper.setText(setContext(authNum, type), true); // HTML 본문 내용

			return mimeMessageHelper;
		} catch (Exception e) {
			throw new RuntimeException("MimeMessage 생성 중 오류 발생: " + e.getMessage(), e);
		}
	}

	/**
	 * 랜덤 인증번호 생성
	 *
	 * @return 생성된 인증번호 (8자리)
	 */
	private String createCode() {
		Random random = new Random();
		StringBuilder key = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			key.append((char) (random.nextInt(26) + 65)); // A~Z (대문자)
		}
		return key.toString();
	}

	/**
	 * Thymeleaf를 통해 이메일 본문 내용 생성
	 *
	 * @param code 인증 코드
	 * @param type 템플릿 이름
	 * @return HTML 본문 내용
	 */
	private String setContext(String code, String type) {
		Context context = new Context();
		context.setVariable("code", code);
		return templateEngine.process(type, context);
	}
}
