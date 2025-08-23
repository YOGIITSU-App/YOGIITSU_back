package com.YOGIITSU.service;

import com.YOGIITSU.service.handler.DynamicResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicResponseDispatcher {

	private final List<DynamicResponseHandler> handlers;

	public String buildRaw(String key, Long nodeId, Map<String, Object> ctx) {
		log.debug("[Dispatcher] 요청 key={}, nodeId={}, ctx={}", key, nodeId, ctx);

		// 1) 입력 검증: key는 반드시 null이 될 수 없습니다.
		if (key == null) {
			throw new IllegalArgumentException("response_key는 null일 수 없습니다.");
		}

		// 2) supports() 결과 한정: 매칭 결과 개수를 명확히 검사
		var matches = handlers.stream()
			.filter(h -> h.supports(key))
			.toList();

		if (matches.isEmpty()) {
			log.warn("[Dispatcher] 지원하지 않는 response_key={}", key);
			throw new IllegalArgumentException("지원하지 않는 response_key: " + key);
		}

		if (matches.size() > 1) {
			log.error("[Dispatcher] response_key={}에 대해 {}개의 handler가 매칭되었습니다: {}",
				key, matches.size(),
				matches.stream().map(h -> h.getClass().getSimpleName()).toList());
			throw new IllegalStateException("response_key에 다중 핸들러 매칭 발생: " + key);
		}

		// 단일 매칭 핸들러 실행
		var handler = matches.getFirst();
		log.debug("[Dispatcher] 매칭된 handler={}", handler.getClass().getSimpleName());
		return handler.buildRawAnswer(nodeId, key, ctx);
	}
}