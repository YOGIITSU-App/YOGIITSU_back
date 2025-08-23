package com.YOGIITSU.service.gpt;

public class InsufficientQuotaException extends RuntimeException {

	public InsufficientQuotaException(String msg) {
		super(msg);
	}
}