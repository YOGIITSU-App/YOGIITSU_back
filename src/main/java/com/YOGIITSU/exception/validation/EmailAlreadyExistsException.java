package com.YOGIITSU.exception.validation;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 이미 존재하는 이메일 예외
 */
public class EmailAlreadyExistsException extends ValidationException {
    
    public EmailAlreadyExistsException() {
        super(ErrorCode.MEMBER_ALREADY_EXISTS, "이미 사용 중인 이메일입니다.");
    }
    
    public EmailAlreadyExistsException(String detailMessage) {
        super(ErrorCode.MEMBER_ALREADY_EXISTS, detailMessage);
    }
}