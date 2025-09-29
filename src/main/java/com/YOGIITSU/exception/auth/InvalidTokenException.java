package com.YOGIITSU.exception.auth;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 유효하지 않은 토큰 예외
 */
public class InvalidTokenException extends AuthenticationException {

	public InvalidTokenException() {
		super(ErrorCode.INVALID_TOKEN);
	}

	public InvalidTokenException(String detailMessage) {
		super(ErrorCode.INVALID_TOKEN, detailMessage);
	}
}