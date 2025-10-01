package com.YOGIITSU.exception.user;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 가입 이력이 없는 이메일로 아이디 찾기를 시도할 때 발생하는 예외
 */
public class EmailNotRegisteredException extends UserException {
    
    private static final long serialVersionUID = 1L;
    
    public EmailNotRegisteredException() {
        super(ErrorCode.EMAIL_NOT_REGISTERED);
    }
    
    public EmailNotRegisteredException(String detailMessage) {
        super(ErrorCode.EMAIL_NOT_REGISTERED, detailMessage);
    }
    
    public EmailNotRegisteredException(String detailMessage, Throwable cause) {
        super(ErrorCode.EMAIL_NOT_REGISTERED, detailMessage, cause);
    }
}
