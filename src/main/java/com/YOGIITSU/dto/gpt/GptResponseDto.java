package com.YOGIITSU.dto.gpt;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GptResponseDto {

	private List<Choice> choices;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Choice {

		private Message message;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Message {

		private String role;
		private String content;
	}
}