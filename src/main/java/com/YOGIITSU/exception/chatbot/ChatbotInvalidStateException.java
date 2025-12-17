package com.YOGIITSU.exception.chatbot;

import com.YOGIITSU.exception.ErrorCode;

public class ChatbotInvalidStateException extends ChatbotException {

	public ChatbotInvalidStateException() {
		super(ErrorCode.CHATBOT_INVALID_STATE);
	}

	public ChatbotInvalidStateException(String detailMessage) {
		super(ErrorCode.CHATBOT_INVALID_STATE, detailMessage);
	}
}
