package com.YOGIITSU.exception.validation;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 잘못된 인수 예외
 */
public class InvalidArgumentException extends ValidationException {
    
    public InvalidArgumentException() {
        super(ErrorCode.INVALID_ARGUMENT);
    }
    
    public InvalidArgumentException(String detailMessage) {
        super(ErrorCode.INVALID_ARGUMENT, detailMessage);
    }
}