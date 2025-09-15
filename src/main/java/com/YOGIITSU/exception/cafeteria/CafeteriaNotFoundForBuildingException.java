package com.YOGIITSU.exception.cafeteria;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CafeteriaNotFoundForBuildingException extends RuntimeException {

	public CafeteriaNotFoundForBuildingException() {
		super("해당 건물에 식당이 없습니다.");
	}
}