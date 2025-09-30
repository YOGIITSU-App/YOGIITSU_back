package com.YOGIITSU.exception.resource;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 지름길을 찾을 수 없는 예외
 */
public class ShortcutNotFoundException extends ResourceException {
    
    private static final long serialVersionUID = 1L;
    
    public ShortcutNotFoundException(Long shortcutId) {
        super(ErrorCode.RESOURCE_NOT_FOUND, "shortcutId=" + shortcutId);
    }
    
    public ShortcutNotFoundException() {
        super(ErrorCode.RESOURCE_NOT_FOUND, "지름길을 찾을 수 없습니다.");
    }
    
    public ShortcutNotFoundException(String detailMessage) {
        super(ErrorCode.RESOURCE_NOT_FOUND, detailMessage);
    }
    
    public ShortcutNotFoundException(String detailMessage, Throwable cause) {
        super(ErrorCode.RESOURCE_NOT_FOUND, detailMessage, cause);
    }
}