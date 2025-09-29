package com.YOGIITSU.exception.external;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 이메일 전송 실패 예외
 */
public class EmailSendException extends ExternalServiceException {
    
    public EmailSendException() {
        super(ErrorCode.EMAIL_SEND_FAIL);
    }
    
    public EmailSendException(String detailMessage) {
        super(ErrorCode.EMAIL_SEND_FAIL, detailMessage);
    }
}