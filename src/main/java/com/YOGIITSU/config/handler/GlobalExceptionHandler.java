package com.YOGIITSU.config.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	// PasswordMismatchException 발생 시 호출
	@ExceptionHandler(PasswordMismatchException.class)
	public ResponseEntity<Map<String, String>> handlePasswordMismatchException(
		PasswordMismatchException e) {
		Map<String, String> errorResponse = new HashMap<>();
		errorResponse.put("message", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	// 비밀번호 불일치 예외 클래스 정의
	public static class PasswordMismatchException extends RuntimeException {

		public PasswordMismatchException() {
			super("비밀번호가 일치하지 않습니다.");
		}
	}
}