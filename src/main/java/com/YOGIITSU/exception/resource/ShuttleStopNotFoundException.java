package com.YOGIITSU.exception.resource;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 셔틀 정류장을 찾을 수 없는 예외
 */
public class ShuttleStopNotFoundException extends ResourceException {
    
    public ShuttleStopNotFoundException(String stopId) {
        super(ErrorCode.SHUTTLE_STOP_NOT_FOUND, "stopId=" + stopId);
    }
    
    public ShuttleStopNotFoundException() {
        super(ErrorCode.SHUTTLE_STOP_NOT_FOUND);
    }
}