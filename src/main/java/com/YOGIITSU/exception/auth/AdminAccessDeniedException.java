package com.YOGIITSU.exception.auth;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 관리자 권한 없음 예외
 */
public class AdminAccessDeniedException extends AuthenticationException {
    
    public AdminAccessDeniedException() {
        super(ErrorCode.ADMIN_ACCESS_DENIED);
    }
    
    public AdminAccessDeniedException(String detailMessage) {
        super(ErrorCode.ADMIN_ACCESS_DENIED, detailMessage);
    }
}