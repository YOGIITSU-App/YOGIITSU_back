package com.YOGIITSU.exception.user;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 기존 이메일과 동일한 이메일 변경 시도 예외
 */
public class SameEmailException extends UserException {
    
    private static final long serialVersionUID = 1L;
    
    public SameEmailException() {
        super(ErrorCode.SAME_EMAIL);
    }
    
    public SameEmailException(String detailMessage) {
        super(ErrorCode.SAME_EMAIL, detailMessage);
    }
    
    public SameEmailException(String detailMessage, Throwable cause) {
        super(ErrorCode.SAME_EMAIL, detailMessage, cause);
    }
}