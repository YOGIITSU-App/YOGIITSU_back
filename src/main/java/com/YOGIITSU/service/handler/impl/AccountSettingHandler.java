package com.YOGIITSU.service.handler.impl;

import com.YOGIITSU.entity.ChatOption;
import com.YOGIITSU.enums.ResponseType;
import com.YOGIITSU.repository.ChatOptionRepository;
import com.YOGIITSU.service.handler.DynamicResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AccountSettingHandler implements DynamicResponseHandler {

	private final ChatOptionRepository chatOptionRepository;

	@Override
	public boolean supports(String key) {
		// response_key가 있을 때만 매칭 (현재 DB는 null이라 실제로는 호출되지 않을 가능성이 큼)
		return key != null && key.startsWith("ACCOUNT_SETTING");
	}

	@Override
	public String buildRawAnswer(Long nodeId, String key, Map<String, Object> ctx) {
		if (nodeId == null) {
			throw new IllegalArgumentException("nodeId가 필요합니다.");
		}
		ChatOption node = chatOptionRepository.findByIdAndIsActiveTrue(nodeId)
			.orElseThrow(() -> new IllegalArgumentException("계정·설정 노드를 찾을 수 없습니다."));

		var type = node.getResponseType() == null ? ResponseType.STATIC : node.getResponseType();
		if (type != ResponseType.STATIC) {
			throw new IllegalArgumentException("계정·설정 노드의 응답 타입이 STATIC이 아닙니다.");
		}

		// 그대로 반환 (GPT 호출 X)
		return node.getResponseText();
	}
}