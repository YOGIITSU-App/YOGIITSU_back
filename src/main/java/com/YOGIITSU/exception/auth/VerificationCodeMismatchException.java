package com.YOGIITSU.exception.auth;

import com.YOGIITSU.exception.BaseException;
import com.YOGIITSU.exception.ErrorCode;

public class VerificationCodeMismatchException extends BaseException {

	public VerificationCodeMismatchException() {
		super(ErrorCode.VERIFICATION_CODE_MISMATCH);
	}

	public VerificationCodeMismatchException(String detailMessage) {
		super(ErrorCode.VERIFICATION_CODE_MISMATCH, detailMessage);
	}
}