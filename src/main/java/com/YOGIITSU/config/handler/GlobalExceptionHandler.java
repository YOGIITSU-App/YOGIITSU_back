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

	// Token 관련 예외들 처리
	@ExceptionHandler(MissingTokenException.class)
	public ResponseEntity<Map<String, String>> handleMissingTokenException(
		MissingTokenException e) {
		Map<String, String> errorResponse = new HashMap<>();
		errorResponse.put("message", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	// InvalidTokenException 발생 시 호출
	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<Map<String, String>> handleInvalidTokenException(
		InvalidTokenException e) {
		Map<String, String> errorResponse = new HashMap<>();
		errorResponse.put("message", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	// AdminAccessDeniedException 발생 시 호출
	@ExceptionHandler(AdminAccessDeniedException.class)
	public ResponseEntity<Map<String, String>> handleAdminAccessDeniedException(
		AdminAccessDeniedException e) {
		Map<String, String> errorResponse = new HashMap<>();
		errorResponse.put("message", e.getMessage());
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse); // 403 반환
	}

	// MemberNotFoundException 발생 시 호출
	@ExceptionHandler(MemberNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleMemberNotFoundException(
		MemberNotFoundException e) {
		Map<String, String> errorResponse = new HashMap<>();
		errorResponse.put("message", e.getMessage());
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
	}

	// PasswordNotEqualsException 발생 시 호출
	@ExceptionHandler(PasswordNotEqualsException.class)
	public ResponseEntity<Map<String, String>> handlePasswordNotEqualsException(
		PasswordNotEqualsException e) {
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

	// 토큰이 없을 경우 예외 클래스 정의
	public static class MissingTokenException extends RuntimeException {

		public MissingTokenException() {
			super("토큰을 입력해 주세요.");
		}
	}

	// 유효하지 않은 토큰일 경우 예외 클래스 정의
	public static class InvalidTokenException extends RuntimeException {

		public InvalidTokenException() {
			super("존재하지 않는 토큰입니다.");
		}
	}

	// 관리자 권한이 없을 경우 예외 클래스 정의
	public static class AdminAccessDeniedException extends RuntimeException {

		public AdminAccessDeniedException() {
			super("관리자 계정이 아닙니다.");
		}
	}
	public static class MemberNotFoundException extends RuntimeException {

		public MemberNotFoundException() {
			super("존재하지 않는 계정입니다.");
		}
	}

	public static class PasswordNotEqualsException extends RuntimeException {

		public PasswordNotEqualsException() {
			super("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
		}
	}

}
