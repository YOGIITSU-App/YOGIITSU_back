package com.YOGIITSU.config.handler;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	// 공통적인 예외 응답을 반환하는 메서드
	private ResponseEntity<Map<String, String>> buildErrorResponse(String message, HttpStatus status) {
		Map<String, String> errorResponse = new HashMap<>();
		errorResponse.put("message", message);
		return ResponseEntity.status(status).body(errorResponse);
	}

	// 토큰이 없을 경우 예외 클래스 정의
	@ExceptionHandler(MissingTokenException.class)
	public ResponseEntity<Map<String, String>> handleMissingTokenException(MissingTokenException e) {
		return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	// 유효하지 않은 토큰일 경우 예외 클래스 정의
	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<Map<String, String>> handleInvalidTokenException(InvalidTokenException e) {
		return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	// 관리자 권한이 없을 경우 예외 클래스 정의
	@ExceptionHandler(AdminAccessDeniedException.class)
	public ResponseEntity<Map<String, String>> handleAdminAccessDeniedException(AdminAccessDeniedException e) {
		return buildErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN);
	}

	// 사용자를 찾을 수 없을 경우 예외 클래스 정의
	@ExceptionHandler(MemberNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleMemberNotFoundException(MemberNotFoundException e) {
		return buildErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN);
	}

	// 비밀번호가 일치하지 않을 경우 예외 처리
	@ExceptionHandler(PasswordMismatchException.class)
	public ResponseEntity<Map<String, String>> handlePasswordMismatchException(PasswordMismatchException e) {
		return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	// 새 비밀번호와 확인 비밀번호가 일치하지 않을 경우 예외 처리
	@ExceptionHandler(PasswordNotEqualsException.class)
	public ResponseEntity<Map<String, String>> handlePasswordNotEqualsException(PasswordNotEqualsException e) {
		return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	// 즐겨찾기 관련 예외 처리 추가
	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleEntityNotFoundException(EntityNotFoundException e) {
		return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
	}

	// 잘못된 인자값이 들어왔을 경우 예외 처리
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
		return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	public static class PasswordMismatchException extends RuntimeException {
		public PasswordMismatchException() {
			super("비밀번호가 일치하지 않습니다.");
		}
	}

	public static class MissingTokenException extends RuntimeException {
		public MissingTokenException() {
			super("토큰을 입력해 주세요.");
		}
	}

	public static class InvalidTokenException extends RuntimeException {
		public InvalidTokenException() {
			super("존재하지 않는 토큰입니다.");
		}
	}

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