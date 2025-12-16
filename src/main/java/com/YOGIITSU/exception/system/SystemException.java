package com.YOGIITSU.exception.system;

import com.YOGIITSU.exception.BaseException;
import com.YOGIITSU.exception.ErrorCode;

/**
 * 시스템 관련 예외
 */
public class SystemException extends BaseException {
    
    public SystemException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public SystemException(ErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
    
    public SystemException(ErrorCode errorCode, String detailMessage, Throwable cause) {
        super(errorCode, detailMessage, cause);
    }
}