package com.YOGIITSU.exception.resource;

import com.YOGIITSU.exception.ErrorCode;

/**
 * 즐겨찾기를 찾을 수 없는 예외
 */
public class FavoriteNotFoundException extends ResourceException {

	public FavoriteNotFoundException(Long favoriteId) {
		super(ErrorCode.FAVORITE_NOT_FOUND, "favoriteId=" + favoriteId);
	}

	public FavoriteNotFoundException() {
		super(ErrorCode.FAVORITE_NOT_FOUND);
	}
}