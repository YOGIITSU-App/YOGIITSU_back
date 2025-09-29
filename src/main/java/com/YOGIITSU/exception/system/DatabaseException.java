package com.YOGIITSU.exception.system;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 데이터베이스 오류 예외
 */
public class DatabaseException extends SystemException {
    
    public DatabaseException() {
        super(ErrorCode.DATABASE_ERROR);
    }
    
    public DatabaseException(String detailMessage) {
        super(ErrorCode.DATABASE_ERROR, detailMessage);
    }
}