package com.YOGIITSU.exception.auth;

import com.YOGIITSU.exception.BaseException;
import com.YOGIITSU.exception.ErrorCode;

public class VerificationCodeNotFoundException extends BaseException {

	public VerificationCodeNotFoundException() {
		super(ErrorCode.VERIFICATION_CODE_NOT_FOUND);
	}

	public VerificationCodeNotFoundException(String detailMessage) {
		super(ErrorCode.VERIFICATION_CODE_NOT_FOUND, detailMessage);
	}
}