package com.YOGIITSU.exception.external;

import com.YOGIITSU.exception.ErrorCode;

/**
 * Apple 토큰 검증 실패 예외
 */
public class AppleVerificationException extends ExternalServiceException {
    
    public AppleVerificationException() {
        super(ErrorCode.APPLE_VERIFICATION_FAIL);
    }
    
    public AppleVerificationException(String detailMessage) {
        super(ErrorCode.APPLE_VERIFICATION_FAIL, detailMessage);
    }
}