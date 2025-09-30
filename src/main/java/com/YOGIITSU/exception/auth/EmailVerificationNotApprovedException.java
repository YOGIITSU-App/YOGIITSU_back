package com.YOGIITSU.exception.auth;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 이메일 인증 미완료 예외
 */
public class EmailVerificationNotApprovedException extends AuthenticationException {
    
    public EmailVerificationNotApprovedException() {
        super(ErrorCode.EMAIL_NOT_VERIFIED);
    }
    
    public EmailVerificationNotApprovedException(String detailMessage) {
        super(ErrorCode.EMAIL_NOT_VERIFIED, detailMessage);
    }
}