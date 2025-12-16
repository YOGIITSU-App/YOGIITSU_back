package com.YOGIITSU.exception.resource;

import com.YOGIITSU.exception.BaseException;
import com.YOGIITSU.exception.ErrorCode;

/**
 * 리소스 관련 예외
 */
public class ResourceException extends BaseException {
    
    public ResourceException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public ResourceException(ErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
    
    public ResourceException(ErrorCode errorCode, String detailMessage, Throwable cause) {
        super(errorCode, detailMessage, cause);
    }
}