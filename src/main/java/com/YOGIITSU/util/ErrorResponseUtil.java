package com.YOGIITSU.util;

import com.YOGIITSU.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 에러 응답 생성 유틸리티 클래스
 * JWT 필터와 SecurityConfig에서 공통으로 사용하는 에러 응답 생성 로직을 제공
 */
public class ErrorResponseUtil {

	/**
	 * ErrorCode를 기반으로 ErrorResponse 형식의 Map을 생성
	 *
	 * @param errorCode 에러 코드
	 * @return ErrorResponse 형식의 Map 객체
	 */
	public static Map<String, Object> createErrorResponse(ErrorCode errorCode) {
		Map<String, Object> errorResponse = new HashMap<>();
		errorResponse.put("code", errorCode.getCode());
		errorResponse.put("message", errorCode.getMessage());
		errorResponse.put("detail", null);
		errorResponse.put("timestamp", LocalDateTime.now().toString());
		errorResponse.put("status", errorCode.getHttpStatus().value());
		return errorResponse;
	}
}
