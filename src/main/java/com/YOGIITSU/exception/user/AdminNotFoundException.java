package com.YOGIITSU.exception.user;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 관리자를 찾을 수 없는 예외
 */
public class AdminNotFoundException extends UserException {
    
    public AdminNotFoundException(Long adminId) {
        super(ErrorCode.MEMBER_NOT_FOUND, "adminId=" + adminId);
    }
    
    public AdminNotFoundException() {
        super(ErrorCode.MEMBER_NOT_FOUND, "관리자 계정을 찾을 수 없습니다.");
    }
}