package com.YOGIITSU.exception.user;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 새 비밀번호와 확인 비밀번호 불일치 예외
 */
public class PasswordNotEqualsException extends UserException {
    
    public PasswordNotEqualsException() {
        super(ErrorCode.PASSWORD_NOT_EQUALS);
    }
    
    public PasswordNotEqualsException(String detailMessage) {
        super(ErrorCode.PASSWORD_NOT_EQUALS, detailMessage);
    }
}