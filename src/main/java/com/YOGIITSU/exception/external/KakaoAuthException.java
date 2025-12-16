package com.YOGIITSU.exception.external;

import com.YOGIITSU.exception.ErrorCode;

/**
 * Kakao 인증 실패 예외
 */
public class KakaoAuthException extends ExternalServiceException {
    
    public KakaoAuthException() {
        super(ErrorCode.KAKAO_AUTH_FAIL);
    }
    
    public KakaoAuthException(String detailMessage) {
        super(ErrorCode.KAKAO_AUTH_FAIL, detailMessage);
    }
}