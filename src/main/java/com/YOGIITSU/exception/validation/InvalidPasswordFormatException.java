package com.YOGIITSU.exception.validation;

import com.YOGIITSU.exception.BaseException;
import com.YOGIITSU.exception.ErrorCode;

public class InvalidPasswordFormatException extends BaseException {

	public InvalidPasswordFormatException() {
		super(ErrorCode.INVALID_PASSWORD_FORMAT);
	}

	public InvalidPasswordFormatException(String detail) {
		super(ErrorCode.INVALID_PASSWORD_FORMAT, detail);
	}
}