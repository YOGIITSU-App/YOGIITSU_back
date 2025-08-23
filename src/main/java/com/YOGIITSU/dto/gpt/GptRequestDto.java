package com.YOGIITSU.dto.gpt;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GptRequestDto {

	private String model;
	private List<Message> messages;
	private Double temperature;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Message {

		private String role;
		private String content;
	}
}