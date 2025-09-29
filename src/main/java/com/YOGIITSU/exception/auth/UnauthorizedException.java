package com.YOGIITSU.exception.auth;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 인증되지 않은 사용자 접근 예외
 */
public class UnauthorizedException extends AuthenticationException {
    
    public UnauthorizedException() {
        super(ErrorCode.UNAUTHORIZED);
    }
    
    public UnauthorizedException(String detailMessage) {
        super(ErrorCode.UNAUTHORIZED, detailMessage);
    }
}