package com.YOGIITSU.exception.external;

import com.YOGIITSU.exception.ErrorCode;

/**
 * Google 인증 실패 예외
 */
public class GoogleAuthException extends ExternalServiceException {
    
    public GoogleAuthException() {
        super(ErrorCode.GOOGLE_AUTH_FAIL);
    }
    
    public GoogleAuthException(String detailMessage) {
        super(ErrorCode.GOOGLE_AUTH_FAIL, detailMessage);
    }
}