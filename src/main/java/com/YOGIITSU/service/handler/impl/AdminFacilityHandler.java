package com.YOGIITSU.service.handler.impl;

import com.YOGIITSU.entity.ChatOption;
import com.YOGIITSU.enums.ResponseType;
import com.YOGIITSU.repository.ChatOptionRepository;
import com.YOGIITSU.service.handler.DynamicResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminFacilityHandler implements DynamicResponseHandler {

	private final ChatOptionRepository chatOptionRepository;

	@Override
	public boolean supports(String key) {
		return "ADMIN_FACILITY".equalsIgnoreCase(key);
	}

	@Override
	public String buildRawAnswer(Long nodeId, String key, Map<String, Object> ctx) {
		log.debug("[AdminFacilityHandler] nodeId={}, key={}", nodeId, key);

		ChatOption node = chatOptionRepository.findByIdAndIsActiveTrue(nodeId)
			.orElseThrow(() -> new IllegalArgumentException("행정·편의시설 노드를 찾을 수 없습니다."));

		var type = node.getResponseType() == null ? ResponseType.STATIC : node.getResponseType();
		if (type != ResponseType.STATIC) {
			throw new IllegalArgumentException("행정·편의시설 노드의 응답 타입이 STATIC이 아닙니다.");
		}

		// 그대로 반환 (GPT 호출 X)
		return node.getResponseText();
	}
}