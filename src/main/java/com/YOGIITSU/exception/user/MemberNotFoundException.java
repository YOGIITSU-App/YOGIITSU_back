package com.YOGIITSU.exception.user;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 회원을 찾을 수 없는 예외
 */
public class MemberNotFoundException extends UserException {
    
    private static final long serialVersionUID = 1L;
    
    public MemberNotFoundException() {
        super(ErrorCode.MEMBER_NOT_FOUND);
    }
    
    public MemberNotFoundException(String detailMessage) {
        super(ErrorCode.MEMBER_NOT_FOUND, detailMessage);
    }
    
    public MemberNotFoundException(String detailMessage, Throwable cause) {
        super(ErrorCode.MEMBER_NOT_FOUND, detailMessage, cause);
    }
}