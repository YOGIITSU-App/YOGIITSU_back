package com.YOGIITSU.exception.external;

import com.YOGIITSU.exception.ErrorCode;

/**
 * FCM 푸시 알림 전송 실패 예외
 */
public class FcmSendException extends ExternalServiceException {

	public FcmSendException(String detailMessage) {
		super(ErrorCode.FCM_SEND_FAIL, detailMessage);
	}
}
