package com.YOGIITSU.exception.auth;

import com.YOGIITSU.exception.BaseException;
import com.YOGIITSU.exception.ErrorCode;

public class VerificationCodeExpiredException extends BaseException {

	public VerificationCodeExpiredException() {
		super(ErrorCode.VERIFICATION_CODE_EXPIRED);
	}

	public VerificationCodeExpiredException(String detailMessage) {
		super(ErrorCode.VERIFICATION_CODE_EXPIRED, detailMessage);
	}
}