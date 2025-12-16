package com.YOGIITSU.exception.auth;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 잘못된 로그인 정보 예외
 */
public class InvalidLoginException extends AuthenticationException {
    
    public InvalidLoginException() {
        super(ErrorCode.INVALID_LOGIN);
    }
    
    public InvalidLoginException(String detailMessage) {
        super(ErrorCode.INVALID_LOGIN, detailMessage);
    }
}