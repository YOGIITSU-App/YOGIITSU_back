package com.YOGIITSU.exception.validation;

import com.YOGIITSU.exception.BaseException;
import com.YOGIITSU.exception.ErrorCode;

public class InvalidInquiryStateException extends BaseException {

    public InvalidInquiryStateException() {
        super(ErrorCode.INVALID_INQUIRY_STATE);
    }

    public InvalidInquiryStateException(String detailMessage) {

        super(ErrorCode.INVALID_INQUIRY_STATE, detailMessage);
    }
}
