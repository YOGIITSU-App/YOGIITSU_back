package com.YOGIITSU.exception.user;

import com.YOGIITSU.exception.BaseException;
import com.YOGIITSU.exception.ErrorCode;

public class IdAlreadyExistsException extends BaseException {

	public IdAlreadyExistsException() {
		super(ErrorCode.MEMBER_ALREADY_EXISTS, "이미 사용 중인 아이디입니다.");
	}

	public IdAlreadyExistsException(String detailMessage) {

		super(ErrorCode.MEMBER_ALREADY_EXISTS, detailMessage);
	}
}