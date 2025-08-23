package com.YOGIITSU.service.gpt;

import java.time.Duration;

public interface GptService {

	// GPT 호출 결과(텍스트 + 성공여부)
	record Result(String text, boolean success) {

	}

	Result polish(String rawInfo, Duration timeout);
}