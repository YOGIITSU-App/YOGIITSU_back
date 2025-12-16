package com.YOGIITSU.exception.validation;

import com.YOGIITSU.exception.BaseException;
import com.YOGIITSU.exception.ErrorCode;

public class InvalidMemberIdFormatException extends BaseException {

	public InvalidMemberIdFormatException() {
		super(ErrorCode.INVALID_MEMBER_ID_FORMAT);
	}

	public InvalidMemberIdFormatException(String detail) {
		super(ErrorCode.INVALID_MEMBER_ID_FORMAT, detail);
	}
}