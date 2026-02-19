package com.YOGIITSU.service.gpt;

public class TooManyRequestsException extends RuntimeException {

	public final long retryAfterSeconds;

	public TooManyRequestsException(String msg, long retryAfterSeconds) {
		super(msg);
		this.retryAfterSeconds = retryAfterSeconds;
	}
}