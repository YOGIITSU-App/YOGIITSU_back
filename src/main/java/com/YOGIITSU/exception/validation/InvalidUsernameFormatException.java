package com.YOGIITSU.exception.validation;

import com.YOGIITSU.exception.BaseException;
import com.YOGIITSU.exception.ErrorCode;

public class InvalidUsernameFormatException extends BaseException {

	public InvalidUsernameFormatException() {
		super(ErrorCode.INVALID_USERNAME_FORMAT);
	}

	public InvalidUsernameFormatException(String detail) {
		super(ErrorCode.INVALID_USERNAME_FORMAT, detail);
	}
}