package com.YOGIITSU.exception.auth;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 요청 횟수 제한 초과 예외 (429)
 */
public class TooManyRequestsException extends AuthenticationException {

	public TooManyRequestsException(String detailMessage) {
		super(ErrorCode.TOO_MANY_REQUESTS, detailMessage);
	}
}
