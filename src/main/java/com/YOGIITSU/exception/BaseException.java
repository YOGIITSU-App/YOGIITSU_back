package com.YOGIITSU.exception;

import lombok.Getter;

/**
 * 애플리케이션의 모든 커스텀 예외의 기본 클래스
 */
@Getter
public abstract class BaseException extends RuntimeException {

	private final ErrorCode errorCode;
	private final String detailMessage;

	protected BaseException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
		this.detailMessage = errorCode.getMessage();
	}

	protected BaseException(ErrorCode errorCode, String detailMessage) {
		super(detailMessage);
		this.errorCode = errorCode;
		this.detailMessage = detailMessage;
	}

	protected BaseException(ErrorCode errorCode, String detailMessage, Throwable cause) {
		super(detailMessage, cause);
		this.errorCode = errorCode;
		this.detailMessage = detailMessage;
	}
}