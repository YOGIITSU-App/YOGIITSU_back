package com.YOGIITSU.exception.validation;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 필수 필드 누락 예외
 */
public class MissingRequiredFieldException extends ValidationException {
    
    public MissingRequiredFieldException() {
        super(ErrorCode.MISSING_REQUIRED_FIELD);
    }
    
    public MissingRequiredFieldException(String detailMessage) {
        super(ErrorCode.MISSING_REQUIRED_FIELD, detailMessage);
    }
}