package com.YOGIITSU.config.handler;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import com.YOGIITSU.exception.building.BuildingNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.mail.MailException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	// 공통적인 예외 응답을 반환하는 메서드
	private ResponseEntity<Map<String, String>> buildErrorResponse(String message,
		HttpStatus status) {
		Map<String, String> errorResponse = new HashMap<>();
		errorResponse.put("message", message);
		return ResponseEntity.status(status).body(errorResponse);
	}

	// 토큰이 없을 경우 예외 클래스 정의
	@ExceptionHandler(MissingTokenException.class)
	public ResponseEntity<Map<String, String>> handleMissingTokenException(
		MissingTokenException e) {
		return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	// 유효하지 않은 토큰일 경우 예외 클래스 정의
	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<Map<String, String>> handleInvalidTokenException(
		InvalidTokenException e) {
		return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	// 관리자 권한이 없을 경우 예외 클래스 정의
	@ExceptionHandler(AdminAccessDeniedException.class)
	public ResponseEntity<Map<String, String>> handleAdminAccessDeniedException(
		AdminAccessDeniedException e) {
		return buildErrorResponse(e.getMessage(), FORBIDDEN);
	}

	// 로그인 실패 (잘못된 아이디/비밀번호)
	@ExceptionHandler(InvalidLoginException.class)
	public ResponseEntity<Map<String, String>> handleInvalidLoginException(
		InvalidLoginException e) {
		return buildErrorResponse(e.getMessage(), UNAUTHORIZED);
	}

	// 사용자를 찾을 수 없을 경우 예외 클래스 정의
	@ExceptionHandler(MemberNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleMemberNotFoundException(
		MemberNotFoundException e) {
		return buildErrorResponse(e.getMessage(), FORBIDDEN);
	}

	// 비밀번호가 일치하지 않을 경우 예외 처리
	@ExceptionHandler(PasswordMismatchException.class)
	public ResponseEntity<Map<String, String>> handlePasswordMismatchException(
		PasswordMismatchException e) {
		return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	// 새 비밀번호와 확인 비밀번호가 일치하지 않을 경우 예외 처리
	@ExceptionHandler(PasswordNotEqualsException.class)
	public ResponseEntity<Map<String, String>> handlePasswordNotEqualsException(
		PasswordNotEqualsException e) {
		return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	// 이메일 인증이 승인되지 않은 경우 예외 처리
	@ExceptionHandler(EmailVerificationNotApprovedException.class)
	public ResponseEntity<Map<String, String>> handleNotApproved(
		EmailVerificationNotApprovedException e) {
		return ResponseEntity.status(UNAUTHORIZED)
			.body(Map.of("message", e.getMessage()));
	}

	// 즐겨찾기 관련 예외 처리 추가
	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleEntityNotFoundException(
		EntityNotFoundException e) {
		return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
	}

	// 회원가입 유효성 검사 예외 처리 추가
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleValidationException(
		MethodArgumentNotValidException e) {
		Map<String, String> errorResponse = new HashMap<>();

		// 첫 번째 오류 메시지만 추출하여 반환
		String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
		errorResponse.put("message", errorMessage);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	//IllegalArgumentException 발생 시 처리
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> handleIllegalArgumentException(
		IllegalArgumentException e) {
		return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	// 이메일 전송 실패 예외 처리
	@ExceptionHandler(MailException.class)
	public ResponseEntity<Map<String, String>> handleMailException(MailException e) {
		return buildErrorResponse("이메일 전송 중 오류가 발생했습니다: " + e.getMessage(),
			HttpStatus.INTERNAL_SERVER_ERROR);
	}

	// 기타 예외 (MimeMessage 생성 오류 포함) 처리
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
		return buildErrorResponse("서버 내부 오류 발생: " + e.getMessage(),
			HttpStatus.INTERNAL_SERVER_ERROR);
	}

	// 건물 ID로 건물을 찾을 수 없는 경우 예외 처리
	@ExceptionHandler(BuildingNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleBuildingNotFoundException(
		BuildingNotFoundException e) {
		return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
	}

	// 비밀번호 변경 시 기존 비밀번호와 동일한 비밀번호로 변경하려는 경우 예외 처리
	@ExceptionHandler(SamePasswordException.class)
	public ResponseEntity<Map<String, String>> handleSamePasswordException(
		SamePasswordException e) {
		return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	// 이메일 변경 시 기존 이메일과 동일한 이메일로 변경하려는 경우 예외 처리
	@ExceptionHandler(SameEmailException.class)
	public ResponseEntity<Map<String, String>> handleSameEmailException(
		SameEmailException e) {
		return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	public static class PasswordMismatchException extends RuntimeException {

		public PasswordMismatchException() {
			super("비밀번호가 일치하지 않습니다.");
		}
	}

	public static class MissingTokenException extends RuntimeException {

		public MissingTokenException() {
			super("요청에 토큰이 포함되어 있지 않습니다.");
		}
	}

	public static class InvalidTokenException extends RuntimeException {

		public InvalidTokenException() {
			super("유효하지 않은 토큰입니다.");
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

	public static class EmailVerificationNotApprovedException extends RuntimeException {

		public EmailVerificationNotApprovedException() {
			super("이메일 인증이 완료되지 않았습니다.");
		}
	}

	public static class InvalidLoginException extends RuntimeException {

		public InvalidLoginException() {
			super("아이디 또는 비밀번호가 잘못되었습니다.");
		}
	}

	//로그인 여부 확인
	@ExceptionHandler(UnauthenticatedAccessException.class)
	public ResponseEntity<Map<String, String>> handleUnauthenticatedAccess(
		UnauthenticatedAccessException e) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
			.body(Map.of("message", e.getMessage()));
	}

	public static class UnauthenticatedAccessException extends RuntimeException {

		public UnauthenticatedAccessException() {
			super("로그인된 사용자만 접근 가능합니다.");
		}
	}

	public static class SamePasswordException extends RuntimeException {

		public SamePasswordException() {
			super("기존 비밀번호와 동일한 비밀번호로는 변경할 수 없습니다.");
		}
	}

	public static class SameEmailException extends RuntimeException {

		public SameEmailException() {
			super("기존 이메일과 동일한 이메일로는 변경할 수 없습니다.");
		}
	}
}