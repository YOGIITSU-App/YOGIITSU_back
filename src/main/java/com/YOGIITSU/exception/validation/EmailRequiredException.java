package com.YOGIITSU.exception.validation;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 이메일 필수 예외
 */
public class EmailRequiredException extends ValidationException {
    
    public EmailRequiredException() {
        super(ErrorCode.MISSING_REQUIRED_FIELD, "이메일 주소는 필수입니다.");
    }
    
    public EmailRequiredException(String detailMessage) {
        super(ErrorCode.MISSING_REQUIRED_FIELD, detailMessage);
    }
}