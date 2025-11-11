package com.YOGIITSU.exception.resource;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 리뷰를 찾을 수 없는 예외
 */
public class ReviewNotFoundException extends ResourceException {
    
    public ReviewNotFoundException(Long reviewId) {
        super(ErrorCode.REVIEW_NOT_FOUND, "reviewId=" + reviewId);
    }
    
    public ReviewNotFoundException() {
        super(ErrorCode.REVIEW_NOT_FOUND);
    }
}

