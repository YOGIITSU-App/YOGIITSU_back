package com.YOGIITSU.exception.auth;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 접근 권한 없음 예외
 */
public class AccessDeniedException extends AuthenticationException {
    
    public AccessDeniedException() {
        super(ErrorCode.ACCESS_DENIED);
    }
    
    public AccessDeniedException(String detailMessage) {
        super(ErrorCode.ACCESS_DENIED, detailMessage);
    }
}