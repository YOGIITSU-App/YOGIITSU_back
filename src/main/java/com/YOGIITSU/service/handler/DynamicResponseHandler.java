package com.YOGIITSU.service.handler;

import java.util.Map;

public interface DynamicResponseHandler {

	boolean supports(String responseKey);

	/**
	 * @param nodeId      현재 노드 ID (필요시 로깅/추적용)
	 * @param responseKey 핸들러 구분 키 (예: PRINTER)
	 * @param ctx         추가 컨텍스트 (필요 없으면 빈 맵)
	 * @return raw 문장 (GPT에 다듬기 전)
	 */
	String buildRawAnswer(Long nodeId, String responseKey, Map<String, Object> ctx);
}