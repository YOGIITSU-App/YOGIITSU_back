package com.YOGIITSU.dto.ResponseDto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FindMemberIdResponseDto {

	private String status;
	private String id;
	private String message;

}
