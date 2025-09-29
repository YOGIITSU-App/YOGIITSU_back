package com.YOGIITSU.exception.external;

import com.YOGIITSU.exception.ErrorCode;

/**
 * Apple 공개키를 찾을 수 없는 예외
 */
public class ApplePublicKeyNotFoundException extends ExternalServiceException {
    
    public ApplePublicKeyNotFoundException() {
        super(ErrorCode.APPLE_AUTH_FAIL, "Apple 공개키를 찾을 수 없습니다.");
    }
    
    public ApplePublicKeyNotFoundException(String detailMessage) {
        super(ErrorCode.APPLE_AUTH_FAIL, detailMessage);
    }
}