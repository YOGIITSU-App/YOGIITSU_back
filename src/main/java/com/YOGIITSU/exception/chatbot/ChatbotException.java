package com.YOGIITSU.exception.chatbot;

import com.YOGIITSU.exception.ErrorCode;
import com.YOGIITSU.exception.system.SystemException;

public class ChatbotException extends SystemException {

	public ChatbotException(ErrorCode errorCode) {
		super(errorCode);
	}

	public ChatbotException(ErrorCode errorCode, String detailMessage) {
		super(errorCode, detailMessage);
	}
}
