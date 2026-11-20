package com.YOGIITSU.e2e;

import com.YOGIITSU.entity.Member;
import com.YOGIITSU.repository.MemberRepository;
import com.YOGIITSU.scheduler.CafeteriaScheduler;
import com.YOGIITSU.security.AttemptLimiter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * 앱 사용 흐름 E2E 테스트
 * 실제 메일/운영 DB 없이 앱 전체(보안 필터, 컨트롤러, 서비스, JPA)를 띄워 API를 호출한다.
 * - DB: H2 인메모리 (MySQL 모드)
 * - 메일: 발송 대신 메시지를 저장하는 가짜 메일 발송기, 저장된 메일 본문에서 인증 코드를 읽는다.
 * - 학식 크롤링 스케줄러: 외부 접속 방지를 위해 mock
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
	"spring.datasource.url=jdbc:h2:mem:e2e;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
	"spring.datasource.username=sa",
	"spring.datasource.password=",
	"spring.datasource.driver-class-name=org.h2.Driver",
	"spring.jpa.hibernate.ddl-auto=create-drop",
	"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
	"spring.security.oauth2.client.registration.google.client-id=test-google-client",
	"spring.mail.host=localhost",
	"spring.mail.port=2525",
	"spring.mail.username=test@gmail.com",
	"spring.mail.password=test",
	"MAIL_HOST=localhost",
	"MAIL_PORT=2525",
	"MAIL_USERNAME=test@gmail.com",
	"GMAIL_APP_PASSWORD=test",
	"jwt.secret=ZTJlLXRlc3Qtc2VjcmV0LWtleS1mb3ItaHMyNTYtc2lnbmluZy0zMmJ5dGVzLW1pbmltdW0=",
	"jwt.access-token-expiry=3600000",
	"jwt.refresh-token-expiry=86400000",
	"SWAGGER_SERVER_URL=http://localhost",
	"cors.allowed-origin=http://localhost",
	"apple.client-id=test",
	"apple.team-id=test",
	"apple.key-id=test",
	"apple.private-key-content=test",
	"firebase.service-account-path="
})
class AppFlowE2ETest {

	private static final String PASSWORD = "Test1234!";
	private static final String NEW_PASSWORD = "Newpass12!";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CapturingMailSender mailSender;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AttemptLimiter attemptLimiter;

	@MockitoBean
	private CafeteriaScheduler cafeteriaScheduler;

	@TestConfiguration
	static class FakeMailConfig {
		@Bean
		@Primary
		CapturingMailSender capturingMailSender() {
			return new CapturingMailSender();
		}
	}

	/** 실제로 발송하지 않고 메시지를 보관하는 메일 발송기 */
	static class CapturingMailSender extends JavaMailSenderImpl {
		private final List<MimeMessage> sent = Collections.synchronizedList(new ArrayList<>());

		@Override
		protected void doSend(MimeMessage[] mimeMessages, Object[] originalMessages) {
			sent.addAll(List.of(mimeMessages));
		}

		/** 해당 주소로 마지막에 보낸 메일 본문의 6자리 인증 코드 */
		String lastCodeTo(String email) throws Exception {
			for (int i = sent.size() - 1; i >= 0; i--) {
				MimeMessage message = sent.get(i);
				if (message.getRecipients(Message.RecipientType.TO)[0].toString().equals(email)) {
					Matcher m = Pattern.compile("(?<!\\d)(\\d{6})(?!\\d)").matcher(text(message));
					if (m.find()) {
						return m.group(1);
					}
				}
			}
			throw new AssertionError("메일 없음: " + email);
		}

		int countTo(String email) throws Exception {
			int count = 0;
			for (MimeMessage message : sent) {
				if (message.getRecipients(Message.RecipientType.TO)[0].toString().equals(email)) {
					count++;
				}
			}
			return count;
		}

		private static String text(Part part) throws Exception {
			Object content = part.getContent();
			if (content instanceof String s) {
				return s;
			}
			if (content instanceof Multipart multipart) {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < multipart.getCount(); i++) {
					BodyPart bodyPart = multipart.getBodyPart(i);
					sb.append(text(bodyPart));
				}
				return sb.toString();
			}
			return "";
		}
	}

	private static final java.util.concurrent.atomic.AtomicInteger USER_SEQ = new java.util.concurrent.atomic.AtomicInteger();
	private int seq = 0;
	private String suffix;

	// 사용자 이름은 중복 불가, 2~8자
	private static String uniqueUserName() {
		return "user" + USER_SEQ.incrementAndGet();
	}

	@BeforeEach
	void setUp() {
		suffix = Long.toString(System.nanoTime() % 1_000_000_000L, 36) + (seq++);
	}

	// ===== helpers =====

	private MvcResult call(MockHttpServletRequestBuilder req, String accessToken, Object body) throws Exception {
		if (accessToken != null) {
			req.header("Authorization", "Bearer " + accessToken);
		}
		if (body != null) {
			req.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body));
		}
		return mockMvc.perform(req).andReturn();
	}

	private JsonNode json(MvcResult result) throws Exception {
		String s = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		if (s.isBlank()) {
			return objectMapper.nullNode();
		}
		try {
			return objectMapper.readTree(s);
		} catch (com.fasterxml.jackson.core.JsonProcessingException e) {
			return objectMapper.getNodeFactory().textNode(s); // JSON이 아닌 문자열 응답
		}
	}

	private String sendCode(String email, String purpose, String accessToken) throws Exception {
		MvcResult r = call(post("/email/send-code").param("purpose", purpose), accessToken, Map.of("email", email));
		assertThat(r.getResponse().getStatus()).as("send-code %s %s", purpose, json(r)).isEqualTo(200);
		return json(r).get("token").asText();
	}

	private MvcResult verify(String verifyToken, String code) throws Exception {
		return call(post("/email/verify").header("X-Email-Verification-Token", verifyToken), null,
			Map.of("code", code));
	}

	private void sendAndVerify(String email, String purpose, String accessToken, StringBuilder tokenOut)
		throws Exception {
		attemptLimiter.reset("send:" + email);
		String token = sendCode(email, purpose, accessToken);
		MvcResult r = verify(token, mailSender.lastCodeTo(email));
		assertThat(r.getResponse().getStatus()).as("verify %s %s", purpose, json(r)).isEqualTo(200);
		if (tokenOut != null) {
			tokenOut.append(token);
		}
	}

	private MvcResult login(String memberId, String password) throws Exception {
		return call(post("/members/login"), null, Map.of("memberId", memberId, "password", password));
	}

	private String accessToken(String memberId, String password) throws Exception {
		MvcResult r = login(memberId, password);
		assertThat(r.getResponse().getStatus()).as("login %s", json(r)).isEqualTo(200);
		return r.getResponse().getHeader("Authorization").substring("Bearer ".length());
	}

	private String signUp(String memberId, String email) throws Exception {
		sendAndVerify(email, "SIGNUP", null, null);
		MvcResult r = call(post("/members/signup"), null,
			Map.of("memberId", memberId, "password", PASSWORD, "email", email, "userName", uniqueUserName()));
		assertThat(r.getResponse().getStatus()).as("signup %s", json(r)).isEqualTo(200);
		return accessToken(memberId, PASSWORD);
	}

	private String createAdmin() throws Exception {
		String adminId = "adm" + suffix;
		memberRepository.save(Member.builder()
			.memberId(adminId)
			.password(passwordEncoder.encode(PASSWORD))
			.email(adminId + "@gmail.com")
			.userName(uniqueUserName())
			.role("ADMIN")
			.joinAt(LocalDateTime.now())
			.build());
		return accessToken(adminId, PASSWORD);
	}

	// ===== scenarios =====

	@Test
	@DisplayName("회원가입_로그인_및_인증토큰에_코드_미포함")
	void signUpAndLogin() throws Exception {
		String email = "su" + suffix + "@gmail.com";
		attemptLimiter.reset("send:" + email);
		String verifyToken = sendCode(email, "SIGNUP", null);

		// 인증 토큰 payload에 인증 코드가 없어야 한다
		String code = mailSender.lastCodeTo(email);
		String payload = new String(Base64.getUrlDecoder().decode(verifyToken.split("[.]")[1]),
			StandardCharsets.UTF_8);
		assertThat(payload).doesNotContain(code).doesNotContain("\"code\"");

		// 틀린 코드는 실패, 맞는 코드는 성공
		assertThat(verify(verifyToken, "000000".equals(code) ? "111111" : "000000").getResponse().getStatus())
			.isEqualTo(400);
		assertThat(verify(verifyToken, code).getResponse().getStatus()).isEqualTo(200);

		String memberId = "su" + suffix;
		MvcResult signup = call(post("/members/signup"), null,
			Map.of("memberId", memberId, "password", PASSWORD, "email", email, "userName", uniqueUserName()));
		assertThat(signup.getResponse().getStatus()).as("%s", json(signup)).isEqualTo(200);

		String token = accessToken(memberId, PASSWORD);
		MvcResult profile = call(get("/mypage/profile"), token, null);
		assertThat(profile.getResponse().getStatus()).as("%s", json(profile)).isEqualTo(200);
	}

	@Test
	@DisplayName("인증코드_발송_1분_1회_제한")
	void sendCodeRateLimit() throws Exception {
		String email = "rl" + suffix + "@gmail.com";
		attemptLimiter.reset("send:" + email);
		sendCode(email, "SIGNUP", null);

		MvcResult second = call(post("/email/send-code").param("purpose", "SIGNUP"), null, Map.of("email", email));
		assertThat(second.getResponse().getStatus()).isEqualTo(429);
		assertThat(json(second).get("code").asText()).isEqualTo("AUTH_012");
		assertThat(mailSender.countTo(email)).as("거부된 요청은 메일을 보내지 않음").isEqualTo(1);
	}

	@Test
	@DisplayName("인증코드_5회_오입력시_코드_무효화")
	void verifyAttemptLimit() throws Exception {
		String email = "va" + suffix + "@gmail.com";
		attemptLimiter.reset("send:" + email);
		String token = sendCode(email, "SIGNUP", null);
		String code = mailSender.lastCodeTo(email);
		String wrong = "000000".equals(code) ? "111111" : "000000";

		for (int i = 1; i <= 4; i++) {
			assertThat(verify(token, wrong).getResponse().getStatus()).as("attempt %d", i).isEqualTo(400);
		}
		MvcResult fifth = verify(token, wrong);
		assertThat(fifth.getResponse().getStatus()).isEqualTo(429);

		// 무효화 후에는 맞는 코드도 실패
		MvcResult afterLimit = verify(token, code);
		assertThat(afterLimit.getResponse().getStatus()).isEqualTo(400);
		assertThat(json(afterLimit).get("code").asText()).isEqualTo("AUTH_011");
	}

	@Test
	@DisplayName("아이디찾기_인증전_마스킹_인증후_전체_1회용")
	void findId() throws Exception {
		String memberId = "fi" + suffix;
		String email = memberId + "@gmail.com";
		signUp(memberId, email);

		// 인증 전: 마스킹
		MvcResult before = call(post("/members/find-id"), null, Map.of("email", email));
		assertThat(before.getResponse().getStatus()).isEqualTo(200);
		String masked = json(before).get("id").asText();
		assertThat(masked).isNotEqualTo(memberId).startsWith(memberId.substring(0, 2)).contains("*");

		// 인증 후: 전체 아이디
		sendAndVerify(email, "FIND_ID", null, null);
		MvcResult after = call(post("/members/find-id"), null, Map.of("email", email));
		assertThat(json(after).get("id").asText()).isEqualTo(memberId);

		// 인증 정보는 1회용
		MvcResult again = call(post("/members/find-id"), null, Map.of("email", email));
		assertThat(json(again).get("id").asText()).isEqualTo(masked);
	}

	@Test
	@DisplayName("비밀번호찾기_인증후_재설정_로그인")
	void findPassword() throws Exception {
		String memberId = "fp" + suffix;
		String email = memberId + "@gmail.com";
		signUp(memberId, email);

		// 인증 없이 재설정 불가
		MvcResult noVerify = call(post("/members/find-password"), null,
			Map.of("email", email, "newPassword", NEW_PASSWORD, "confirmPassword", NEW_PASSWORD));
		assertThat(noVerify.getResponse().getStatus()).isNotEqualTo(200);

		sendAndVerify(email, "FIND_PASSWORD", null, null);
		MvcResult reset = call(post("/members/find-password"), null,
			Map.of("email", email, "newPassword", NEW_PASSWORD, "confirmPassword", NEW_PASSWORD));
		assertThat(reset.getResponse().getStatus()).as("%s", json(reset)).isEqualTo(200);

		assertThat(login(memberId, PASSWORD).getResponse().getStatus()).isEqualTo(401);
		assertThat(login(memberId, NEW_PASSWORD).getResponse().getStatus()).isEqualTo(200);
	}

	@Test
	@DisplayName("이메일_변경")
	void changeEmail() throws Exception {
		String memberId = "ce" + suffix;
		String oldEmail = memberId + "@gmail.com";
		String newEmail = memberId + "new@naver.com";
		String token = signUp(memberId, oldEmail);

		sendAndVerify(oldEmail, "EMAIL_CHANGE_OLD", token, null);
		StringBuilder newVerifyToken = new StringBuilder();
		sendAndVerify(newEmail, "EMAIL_CHANGE_NEW", token, newVerifyToken);

		MvcResult change = call(post("/email/change").header("X-Email-Verification-Token",
			newVerifyToken.toString()), token, null);
		assertThat(change.getResponse().getStatus()).as("%s", json(change)).isEqualTo(200);
		assertThat(memberRepository.findByEmail(newEmail)).isPresent();
		assertThat(memberRepository.findByEmail(oldEmail)).isEmpty();
	}

	@Test
	@DisplayName("로그인_5회_실패시_잠금")
	void loginLock() throws Exception {
		String memberId = "ll" + suffix;
		signUp(memberId, memberId + "@gmail.com");

		for (int i = 1; i <= 5; i++) {
			assertThat(login(memberId, "Wrong123!").getResponse().getStatus()).as("attempt %d", i).isEqualTo(401);
		}
		// 잠금 중에는 올바른 비밀번호도 거부
		MvcResult locked = login(memberId, PASSWORD);
		assertThat(locked.getResponse().getStatus()).isEqualTo(429);
		attemptLimiter.reset("login:" + memberId);
	}

	@Test
	@DisplayName("문의_관리자답변_권한")
	void inquiryAndAdminAnswer() throws Exception {
		String memberId = "iq" + suffix;
		String userToken = signUp(memberId, memberId + "@gmail.com");
		String adminToken = createAdmin();

		MvcResult created = call(post("/inquiries"), userToken,
			Map.of("inquiryTitle", "E2E 문의", "inquiryContent", "내용"));
		assertThat(created.getResponse().getStatus()).as("%s", json(created)).isEqualTo(201);

		long inquiryId = -1;
		for (JsonNode item : json(call(get("/inquiries"), userToken, null))) {
			if (item.path("isMine").asBoolean() && "E2E 문의".equals(item.path("inquiryTitle").asText())) {
				inquiryId = item.get("inquiryId").asLong();
			}
		}
		assertThat(inquiryId).isPositive();
		Map<String, String> answer = Map.of("answerTitle", "답변", "answerContent", "답변 내용");

		// 비로그인 401, 일반 회원 403
		assertThat(call(post("/admin/inquiries/" + inquiryId), null, answer).getResponse().getStatus())
			.isEqualTo(401);
		assertThat(call(post("/admin/inquiries/" + inquiryId), userToken, answer).getResponse().getStatus())
			.isEqualTo(403);
		assertThat(call(delete("/admin/inquiries/" + inquiryId), userToken, null).getResponse().getStatus())
			.isEqualTo(403);

		// 관리자: 답변 등록/수정
		MvcResult answered = call(post("/admin/inquiries/" + inquiryId), adminToken, answer);
		assertThat(answered.getResponse().getStatus()).as("%s", json(answered)).isEqualTo(201);
		MvcResult updated = call(put("/admin/inquiries/" + inquiryId), adminToken,
			Map.of("answerTitle", "답변 수정", "answerContent", "수정된 답변"));
		assertThat(updated.getResponse().getStatus()).as("%s", json(updated)).isEqualTo(200);

		JsonNode detail = json(call(get("/inquiries/" + inquiryId), userToken, null));
		assertThat(detail.get("answerContent").asText()).isEqualTo("수정된 답변");

		// 관리자: 삭제
		assertThat(call(delete("/admin/inquiries/" + inquiryId), adminToken, null).getResponse().getStatus())
			.isEqualTo(204);
		assertThat(call(get("/inquiries/" + inquiryId), userToken, null).getResponse().getStatus())
			.isEqualTo(404);
	}

	@Test
	@DisplayName("공지_등록_권한")
	void noticeAdminOnly() throws Exception {
		String memberId = "nt" + suffix;
		String userToken = signUp(memberId, memberId + "@gmail.com");
		String adminToken = createAdmin();
		Map<String, String> notice = Map.of("title", "E2E 공지", "content", "내용");

		assertThat(call(post("/notices"), null, notice).getResponse().getStatus()).isEqualTo(401);
		assertThat(call(post("/notices"), userToken, notice).getResponse().getStatus()).isEqualTo(403);
		MvcResult created = call(post("/notices"), adminToken, notice);
		assertThat(created.getResponse().getStatus()).as("%s", json(created)).isBetween(200, 201);
	}

	@Test
	@DisplayName("회원탈퇴")
	void deleteAccount() throws Exception {
		String memberId = "da" + suffix;
		String token = signUp(memberId, memberId + "@gmail.com");

		MvcResult deleted = call(delete("/members/delete"), token, null);
		assertThat(deleted.getResponse().getStatus()).as("%s", json(deleted)).isBetween(200, 204);
		assertThat(login(memberId, PASSWORD).getResponse().getStatus()).isEqualTo(401);
	}
}
