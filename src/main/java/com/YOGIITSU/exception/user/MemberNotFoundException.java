package com.YOGIITSU.exception.user;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 회원을 찾을 수 없는 예외
 */
public class MemberNotFoundException extends UserException {
    
    public MemberNotFoundException() {
        super(ErrorCode.MEMBER_NOT_FOUND);
    }
    
    public MemberNotFoundException(String detailMessage) {
        super(ErrorCode.MEMBER_NOT_FOUND, detailMessage);
    }
}