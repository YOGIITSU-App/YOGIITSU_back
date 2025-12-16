package com.YOGIITSU.exception.validation;

import com.YOGIITSU.exception.BaseException;
import com.YOGIITSU.exception.ErrorCode;

/**
 * 유효성 검사 관련 예외
 */
public class ValidationException extends BaseException {
    
    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public ValidationException(ErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
    
    public ValidationException(ErrorCode errorCode, String detailMessage, Throwable cause) {
        super(errorCode, detailMessage, cause);
    }
}