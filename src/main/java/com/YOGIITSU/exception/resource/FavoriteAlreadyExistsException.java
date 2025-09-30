package com.YOGIITSU.exception.resource;

import com.YOGIITSU.exception.BaseException;
import com.YOGIITSU.exception.ErrorCode;

/**
 * 즐겨찾기가 이미 존재하는 경우 발생하는 예외
 */
public class FavoriteAlreadyExistsException extends BaseException {

	public FavoriteAlreadyExistsException() {
		super(ErrorCode.FAVORITE_ALREADY_EXISTS);
	}
}
