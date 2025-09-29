package com.YOGIITSU.exception.validation;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 이미 존재하는 사용자명 예외
 */
public class UsernameAlreadyExistsException extends ValidationException {
    
    public UsernameAlreadyExistsException() {
        super(ErrorCode.MEMBER_ALREADY_EXISTS, "이미 사용 중인 이름입니다.");
    }
    
    public UsernameAlreadyExistsException(String detailMessage) {
        super(ErrorCode.MEMBER_ALREADY_EXISTS, detailMessage);
    }
}