package com.YOGIITSU.exception.resource;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 문의를 찾을 수 없는 예외
 */
public class InquiryNotFoundException extends ResourceException {
    
    public InquiryNotFoundException(Long inquiryId) {
        super(ErrorCode.INQUIRY_NOT_FOUND, "inquiryId=" + inquiryId);
    }
    
    public InquiryNotFoundException() {
        super(ErrorCode.INQUIRY_NOT_FOUND);
    }
}