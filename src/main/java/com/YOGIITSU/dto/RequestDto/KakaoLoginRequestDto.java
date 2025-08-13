package com.YOGIITSU.dto.RequestDto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class KakaoLoginRequestDto {

	@NotBlank(message = "accessToken은 필수입니다.")
	private final String accessToken;

	@JsonCreator
	public KakaoLoginRequestDto(@JsonProperty("accessToken") String accessToken) {
		this.accessToken = accessToken;
	}
}