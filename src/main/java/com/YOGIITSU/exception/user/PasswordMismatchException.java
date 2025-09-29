package com.YOGIITSU.exception.user;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 비밀번호 불일치 예외
 */
public class PasswordMismatchException extends UserException {
    
    public PasswordMismatchException() {
        super(ErrorCode.PASSWORD_MISMATCH);
    }
    
    public PasswordMismatchException(String detailMessage) {
        super(ErrorCode.PASSWORD_MISMATCH, detailMessage);
    }
}