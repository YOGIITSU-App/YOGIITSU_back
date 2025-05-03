package com.YOGIITSU.dto.ResponseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserResponseDto {
	private Long id;
	private String username;
	private String email;
	private String role;
}
