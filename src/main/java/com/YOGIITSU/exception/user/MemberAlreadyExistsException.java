package com.YOGIITSU.exception.user;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 이미 존재하는 회원 예외
 */
public class MemberAlreadyExistsException extends UserException {
    
    public MemberAlreadyExistsException() {
        super(ErrorCode.MEMBER_ALREADY_EXISTS);
    }
    
    public MemberAlreadyExistsException(String detailMessage) {
        super(ErrorCode.MEMBER_ALREADY_EXISTS, detailMessage);
    }
}