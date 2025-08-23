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

		return handlers.stream()
			.filter(h -> h.supports(key))
			.findFirst()
			.map(handler -> {
				log.debug("[Dispatcher] 매칭된 handler={}", handler.getClass().getSimpleName());
				return handler.buildRawAnswer(nodeId, key, ctx);
			})
			.orElseThrow(() -> {
				log.warn("[Dispatcher] 지원하지 않는 response_key={}", key);
				return new IllegalArgumentException("지원하지 않는 response_key: " + key);
			});
	}
}