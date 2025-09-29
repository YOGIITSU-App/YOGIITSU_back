package com.YOGIITSU.exception.external;

import com.YOGIITSU.exception.ErrorCode;

/**
 * Apple 토큰이 유효하지 않은 예외
 */
public class AppleTokenInvalidException extends ExternalServiceException {
    
    public AppleTokenInvalidException() {
        super(ErrorCode.APPLE_TOKEN_INVALID);
    }
    
    public AppleTokenInvalidException(String detailMessage) {
        super(ErrorCode.APPLE_TOKEN_INVALID, detailMessage);
    }
}