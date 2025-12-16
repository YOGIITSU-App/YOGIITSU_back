package com.YOGIITSU.exception.validation;

import com.YOGIITSU.exception.ErrorCode;

public class InvalidEmailDomainException extends ValidationException {

	public InvalidEmailDomainException(String detail) {
		super(ErrorCode.INVALID_EMAIL_DOMAIN, detail);
	}
}