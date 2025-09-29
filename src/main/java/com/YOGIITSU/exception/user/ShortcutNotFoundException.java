package com.YOGIITSU.exception.user;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 지름길을 찾을 수 없는 예외
 */
public class ShortcutNotFoundException extends UserException {
    
    public ShortcutNotFoundException(Long shortcutId) {
        super(ErrorCode.RESOURCE_NOT_FOUND, "shortcutId=" + shortcutId);
    }
    
    public ShortcutNotFoundException() {
        super(ErrorCode.RESOURCE_NOT_FOUND, "지름길을 찾을 수 없습니다.");
    }
}