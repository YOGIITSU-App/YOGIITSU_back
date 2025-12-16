package com.YOGIITSU.exception.resource;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 리소스를 찾을 수 없는 예외
 */
public class ResourceNotFoundException extends ResourceException {
    
    public ResourceNotFoundException(String platform) {
        super(ErrorCode.RESOURCE_NOT_FOUND, "platform=" + platform);
    }
    
    public ResourceNotFoundException() {
        super(ErrorCode.RESOURCE_NOT_FOUND);
    }
}