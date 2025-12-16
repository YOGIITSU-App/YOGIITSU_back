package com.YOGIITSU.exception.auth;

import com.YOGIITSU.exception.BaseException;
import com.YOGIITSU.exception.ErrorCode;

/**
 * 인증 관련 예외
 */
public class AuthenticationException extends BaseException {
    
    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public AuthenticationException(ErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
    
    public AuthenticationException(ErrorCode errorCode, String detailMessage, Throwable cause) {
        super(errorCode, detailMessage, cause);
    }
}