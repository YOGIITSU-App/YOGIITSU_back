package com.YOGIITSU.dto.RequestDto;

import com.YOGIITSU.entity.ChatOption;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatOptionItemRequestDto {

	private Long id;
	private String text;
	private Integer order;

	public static ChatOptionItemRequestDto from(ChatOption e) {
		return ChatOptionItemRequestDto.builder()
			.id(e.getId())
			.text(e.getOptionText())
			.order(e.getDisplayOrder())
			.build();
	}

	public static ChatOptionItemRequestDto of(Long id, String text, Integer order) {
		return ChatOptionItemRequestDto.builder()
			.id(id)
			.text(text)
			.order(order)
			.build();
	}
}