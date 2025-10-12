package com.YOGIITSU.exception.user;

import com.YOGIITSU.exception.BaseException;
import com.YOGIITSU.exception.ErrorCode;
import javax.swing.plaf.PanelUI;

public class EmailMismatchException extends BaseException {

	public EmailMismatchException() {
		super(ErrorCode.EMAIL_MISMATCH);
	}

	public EmailMismatchException(String detailMessage) {
		super(ErrorCode.EMAIL_MISMATCH, detailMessage);
	}
}