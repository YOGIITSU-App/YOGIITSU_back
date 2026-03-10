package com.YOGIITSU.service;

import com.YOGIITSU.dto.RequestDto.FcmTokenRequestDto;
import com.YOGIITSU.entity.FcmToken;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.exception.external.FcmSendException;
import com.YOGIITSU.repository.FcmTokenRepository;
import com.YOGIITSU.repository.MemberRepository;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

	private static final int MAX_BODY_LENGTH = 900;

	private final FcmTokenRepository fcmTokenRepository;
	private final MemberRepository memberRepository;

	@Transactional
	public void saveToken(FcmTokenRequestDto dto, Long memberId) {
		if (fcmTokenRepository.findByToken(dto.getToken()).isPresent()) {
			return;
		}
		Member member = memberRepository.findById(memberId).orElse(null);
		FcmToken fcmToken = FcmToken.builder()
			.member(member)
			.token(dto.getToken())
			.build();
		fcmTokenRepository.save(fcmToken);
	}

	@Transactional
	public void sendNoticeNotification(String title, String body, Long noticeId) {
		List<String> tokens = fcmTokenRepository.findAllTokenValues();
		if (tokens.isEmpty()) {
			log.info("FCM 알림 전송 대상 토큰 없음 - noticeId={}", noticeId);
			return;
		}

		String truncatedBody = body != null && body.length() > MAX_BODY_LENGTH
			? body.substring(0, MAX_BODY_LENGTH)
			: body;

		MulticastMessage message = MulticastMessage.builder()
			.setNotification(Notification.builder()
				.setTitle(title)
				.setBody(truncatedBody)
				.build())
			.putData("noticeId", String.valueOf(noticeId))
			.putData("type", "NOTICE")
			.addAllTokens(tokens)
			.build();

		try {
			BatchResponse batchResponse = FirebaseMessaging.getInstance().sendEachForMulticast(message);
			log.info("FCM 전송 완료 - 성공: {}, 실패: {}, noticeId={}",
				batchResponse.getSuccessCount(), batchResponse.getFailureCount(), noticeId);

			List<SendResponse> responses = batchResponse.getResponses();
			for (int i = 0; i < responses.size(); i++) {
				SendResponse sendResponse = responses.get(i);
				if (!sendResponse.isSuccessful() && sendResponse.getException() != null) {
					String errorCode = sendResponse.getException().getMessagingErrorCode() != null
						? sendResponse.getException().getMessagingErrorCode().name()
						: "UNKNOWN";
					log.warn("FCM 전송 실패 - errorCode={}, message={}, token={}",
						errorCode, sendResponse.getException().getMessage(), tokens.get(i));

					if ("UNREGISTERED".equals(errorCode)) {
						fcmTokenRepository.deleteByToken(tokens.get(i));
						log.info("FCM 만료 토큰 삭제 - token={}", tokens.get(i));
					}
				}
			}
		} catch (Exception e) {
			throw new FcmSendException(e.getMessage());
		}
	}
}
