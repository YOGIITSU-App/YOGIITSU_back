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
	private static final int FCM_BATCH_SIZE = 500;

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

		Notification notification = Notification.builder()
			.setTitle(title)
			.setBody(truncatedBody)
			.build();

		for (int i = 0; i < tokens.size(); i += FCM_BATCH_SIZE) {
			List<String> batch = tokens.subList(i, Math.min(i + FCM_BATCH_SIZE, tokens.size()));
			MulticastMessage message = MulticastMessage.builder()
				.setNotification(notification)
				.putData("noticeId", String.valueOf(noticeId))
				.putData("type", "NOTICE")
				.addAllTokens(batch)
				.build();

			try {
				BatchResponse batchResponse = FirebaseMessaging.getInstance().sendEachForMulticast(message);
				log.info("FCM 배치 전송 완료 - 배치 시작 인덱스: {}, 성공: {}, 실패: {}, noticeId={}",
					i, batchResponse.getSuccessCount(), batchResponse.getFailureCount(), noticeId);

				List<SendResponse> responses = batchResponse.getResponses();
				for (int j = 0; j < responses.size(); j++) {
					SendResponse sendResponse = responses.get(j);
					if (!sendResponse.isSuccessful() && sendResponse.getException() != null) {
						String errorCode = sendResponse.getException().getMessagingErrorCode() != null
							? sendResponse.getException().getMessagingErrorCode().name()
							: "UNKNOWN";
						log.warn("FCM 전송 실패 - errorCode={}, message={}, token={}",
							errorCode, sendResponse.getException().getMessage(), batch.get(j));

						if ("UNREGISTERED".equals(errorCode)) {
							fcmTokenRepository.deleteByToken(batch.get(j));
							log.info("FCM 만료 토큰 삭제 - token={}", batch.get(j));
						}
					}
				}
			} catch (Exception e) {
				throw new FcmSendException(e.getMessage());
			}
		}
	}
}
