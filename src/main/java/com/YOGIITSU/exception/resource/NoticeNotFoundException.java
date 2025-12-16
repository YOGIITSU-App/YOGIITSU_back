package com.YOGIITSU.exception.resource;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 공지사항을 찾을 수 없는 예외
 */
public class NoticeNotFoundException extends ResourceException {
    
    public NoticeNotFoundException(Long noticeId) {
        super(ErrorCode.NOTICE_NOT_FOUND, "noticeId=" + noticeId);
    }
    
    public NoticeNotFoundException() {
        super(ErrorCode.NOTICE_NOT_FOUND);
    }
}