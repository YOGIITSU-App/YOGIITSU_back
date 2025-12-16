package com.YOGIITSU.exception.cafeteria;

import com.YOGIITSU.exception.resource.ResourceException;
import com.YOGIITSU.exception.ErrorCode;

/**
 * 건물에 식당이 없는 예외
 */
public class CafeteriaNotFoundForBuildingException extends ResourceException {

	public CafeteriaNotFoundForBuildingException() {
		super(ErrorCode.CAFETERIA_NOT_FOUND);
	}
	
	public CafeteriaNotFoundForBuildingException(String detailMessage) {
		super(ErrorCode.CAFETERIA_NOT_FOUND, detailMessage);
	}
}