package com.YOGIITSU.dto.ResponseDto;

import com.YOGIITSU.dto.RequestDto.ChatOptionItemRequestDto;
import com.YOGIITSU.entity.ChatOption;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatNodeResponseDto {

	private String type; // OPTIONS | FINAL
	private List<ChatOptionItemRequestDto> options;
	private String responseText;
	private Map<String, Object> meta;

	public static ChatNodeResponseDto options(List<ChatOption> children) {
		var items = Optional.ofNullable(children).orElseGet(List::of)
			.stream()
			.map(ChatOptionItemRequestDto::from)
			.filter(Objects::nonNull)
			.toList();
		return ChatNodeResponseDto.builder()
			.type("OPTIONS")
			.options(items)
			.build();
	}

	public static ChatNodeResponseDto optionsCustom(List<ChatOptionItemRequestDto> items) {
		return ChatNodeResponseDto.builder()
			.type("OPTIONS")
			.options(items != null ? items : List.of())
			.build();
	}

	public static ChatNodeResponseDto finalText(String text, boolean gptUsed, String key) {
		Map<String, Object> m = new HashMap<>();
		m.put("gptUsed", gptUsed);
		if (key != null) {
			m.put("key", key);
		}
		return ChatNodeResponseDto.builder()
			.type("FINAL")
			.responseText(text)
			.meta(m)
			.build();
	}
}