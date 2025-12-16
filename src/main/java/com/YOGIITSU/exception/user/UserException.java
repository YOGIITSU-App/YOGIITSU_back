package com.YOGIITSU.exception.user;

import com.YOGIITSU.exception.BaseException;
import com.YOGIITSU.exception.ErrorCode;

/**
 * 사용자 관련 예외
 */
public class UserException extends BaseException {
    
    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public UserException(ErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
    
    public UserException(ErrorCode errorCode, String detailMessage, Throwable cause) {
        super(errorCode, detailMessage, cause);
    }
}