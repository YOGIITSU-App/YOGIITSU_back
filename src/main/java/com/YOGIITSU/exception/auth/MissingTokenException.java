package com.YOGIITSU.exception.auth;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 토큰이 누락된 예외
 */
public class MissingTokenException extends AuthenticationException {
    
    public MissingTokenException() {
        super(ErrorCode.MISSING_TOKEN);
    }
    
    public MissingTokenException(String detailMessage) {
        super(ErrorCode.MISSING_TOKEN, detailMessage);
    }
}