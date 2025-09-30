package com.YOGIITSU.exception.external;

import com.YOGIITSU.exception.BaseException;
import com.YOGIITSU.exception.ErrorCode;

/**
 * 외부 서비스 관련 예외
 */
public class ExternalServiceException extends BaseException {
    
    public ExternalServiceException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public ExternalServiceException(ErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
    
    public ExternalServiceException(ErrorCode errorCode, String detailMessage, Throwable cause) {
        super(errorCode, detailMessage, cause);
    }
}