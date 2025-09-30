package com.YOGIITSU.exception.user;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 기존 비밀번호와 동일한 비밀번호 변경 시도 예외
 */
public class SamePasswordException extends UserException {
    
    public SamePasswordException() {
        super(ErrorCode.SAME_PASSWORD);
    }
    
    public SamePasswordException(String detailMessage) {
        super(ErrorCode.SAME_PASSWORD, detailMessage);
    }
}