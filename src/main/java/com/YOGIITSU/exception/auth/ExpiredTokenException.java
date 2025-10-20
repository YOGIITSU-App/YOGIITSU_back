package com.YOGIITSU.exception.auth;

import com.YOGIITSU.exception.BaseException;
import com.YOGIITSU.exception.ErrorCode;

public class ExpiredTokenException extends BaseException {

	private static final long serialVersionUID = 1L;

	public ExpiredTokenException() {
		super(ErrorCode.TOKEN_EXPIRED);
	}

	public ExpiredTokenException(String detailMessage) {
		super(ErrorCode.TOKEN_EXPIRED, detailMessage);
	}

	public ExpiredTokenException(String detailMessage, Throwable cause) {
		super(ErrorCode.TOKEN_EXPIRED, detailMessage, cause);
	}
}