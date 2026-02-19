package com.YOGIITSU.exception.chatbot;

import com.YOGIITSU.exception.ErrorCode;

public class ChatbotTemporaryException extends ChatbotException {

	public ChatbotTemporaryException() {
		super(ErrorCode.CHATBOT_TEMPORARY_ERROR);
	}

	public ChatbotTemporaryException(String detailMessage) {
		super(ErrorCode.CHATBOT_TEMPORARY_ERROR, detailMessage);
	}
}
