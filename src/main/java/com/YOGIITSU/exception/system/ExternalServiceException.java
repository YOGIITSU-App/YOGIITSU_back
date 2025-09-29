package com.YOGIITSU.exception.system;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 외부 서비스 오류 예외
 */
public class ExternalServiceException extends SystemException {
    
    public ExternalServiceException() {
        super(ErrorCode.EXTERNAL_SERVICE_ERROR);
    }
    
    public ExternalServiceException(String detailMessage) {
        super(ErrorCode.EXTERNAL_SERVICE_ERROR, detailMessage);
    }
}