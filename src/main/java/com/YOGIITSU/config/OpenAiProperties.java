package com.YOGIITSU.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class OpenAiProperties {

	@Value("${openai.api-key:${OPENAI_API_KEY:}}")
	private String apiKey;

	@Value("${openai.model:gpt-4o-mini-2024-07-18}")
	private String model;

	@Value("${openai.timeout-seconds:10}")
	private int timeoutSeconds;
}