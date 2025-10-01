package com.YOGIITSU.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 애플리케이션 전체에서 사용되는 에러 코드 정의
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// 인증/인가 관련 (1000번대)
	UNAUTHORIZED("AUTH_001", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
	INVALID_TOKEN("AUTH_002", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
	TOKEN_EXPIRED("AUTH_003", "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
	MISSING_TOKEN("AUTH_004", "요청에 토큰이 포함되어 있지 않습니다.", HttpStatus.BAD_REQUEST),
	INVALID_LOGIN("AUTH_005", "아이디 또는 비밀번호가 잘못되었습니다.", HttpStatus.UNAUTHORIZED),
	EMAIL_NOT_VERIFIED("AUTH_006", "이메일 인증이 완료되지 않았습니다.", HttpStatus.UNAUTHORIZED),
	ACCESS_DENIED("AUTH_007", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
	ADMIN_ACCESS_DENIED("AUTH_008", "관리자 계정이 아닙니다.", HttpStatus.FORBIDDEN),

	// 사용자 관련 (2000번대)
	MEMBER_NOT_FOUND("USER_001", "존재하지 않는 계정입니다.", HttpStatus.NOT_FOUND),
	MEMBER_ALREADY_EXISTS("USER_002", "이미 존재하는 계정입니다.", HttpStatus.CONFLICT),
	PASSWORD_MISMATCH("USER_003", "비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
	PASSWORD_NOT_EQUALS("USER_004", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
	SAME_PASSWORD("USER_005", "기존 비밀번호와 동일한 비밀번호로는 변경할 수 없습니다.", HttpStatus.BAD_REQUEST),
	SAME_EMAIL("USER_006", "기존 이메일과 동일한 이메일로는 변경할 수 없습니다.", HttpStatus.BAD_REQUEST),
	EMAIL_NOT_REGISTERED("USER_007", "가입 이력이 없는 이메일입니다.", HttpStatus.NOT_FOUND),

	// 리소스 관련 (3000번대)
	BUILDING_NOT_FOUND("RESOURCE_001", "존재하지 않는 건물입니다.", HttpStatus.NOT_FOUND),
	CAFETERIA_NOT_FOUND("RESOURCE_002", "해당 건물에 식당이 없습니다.", HttpStatus.NOT_FOUND),
	SHUTTLE_STOP_NOT_FOUND("RESOURCE_003", "요청하신 정류장을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	NOTICE_NOT_FOUND("RESOURCE_004", "존재하지 않는 공지사항입니다.", HttpStatus.NOT_FOUND),
	INQUIRY_NOT_FOUND("RESOURCE_005", "존재하지 않는 문의입니다.", HttpStatus.NOT_FOUND),
	FAVORITE_ALREADY_EXISTS("RESOURCE_006", "이미 즐겨찾기에 추가된 건물입니다.", HttpStatus.CONFLICT),
	FAVORITE_NOT_FOUND("RESOURCE_007", "존재하지 않는 즐겨찾기입니다.", HttpStatus.NOT_FOUND),
	RESOURCE_NOT_FOUND("RESOURCE_008", "요청하신 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

	// 외부 서비스 관련 (4000번대)
	APPLE_AUTH_FAIL("EXTERNAL_001", "Apple 인증에 실패했습니다.", HttpStatus.UNAUTHORIZED),
	APPLE_TOKEN_INVALID("EXTERNAL_002", "유효하지 않은 Apple identityToken 입니다.", HttpStatus.BAD_REQUEST),
	APPLE_VERIFICATION_FAIL("EXTERNAL_003", "Apple 토큰 검증 중 오류가 발생했습니다.", HttpStatus.BAD_REQUEST),
	GOOGLE_AUTH_FAIL("EXTERNAL_004", "Google 인증에 실패했습니다.", HttpStatus.UNAUTHORIZED),
	KAKAO_AUTH_FAIL("EXTERNAL_005", "Kakao 인증에 실패했습니다.", HttpStatus.UNAUTHORIZED),
	EMAIL_SEND_FAIL("EXTERNAL_006", "이메일 전송 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

	// 유효성 검사 관련 (5000번대)
	VALIDATION_ERROR("VALIDATION_001", "입력값이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
	INVALID_ARGUMENT("VALIDATION_002", "잘못된 인수가 전달되었습니다.", HttpStatus.BAD_REQUEST),
	MISSING_REQUIRED_FIELD("VALIDATION_003", "필수 필드가 누락되었습니다.", HttpStatus.BAD_REQUEST),

	// 시스템 관련 (9000번대)
	INTERNAL_SERVER_ERROR("SYSTEM_001", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	DATABASE_ERROR("SYSTEM_002", "데이터베이스 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	EXTERNAL_SERVICE_ERROR("SYSTEM_003", "외부 서비스 오류가 발생했습니다.", HttpStatus.SERVICE_UNAVAILABLE);

	private final String code;
	private final String message;
	private final HttpStatus httpStatus;

	/**
	 * 에러 코드로 ErrorCode를 찾는 메서드
	 */
	public static ErrorCode fromCode(String code) {
		for (ErrorCode errorCode : values()) {
			if (errorCode.getCode().equals(code)) {
				return errorCode;
			}
		}
		throw new IllegalArgumentException("Unknown error code: " + code);
	}
}