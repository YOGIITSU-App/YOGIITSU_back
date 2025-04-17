package com.YOGIITSU.dto.RequestDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequestDto {

	private String email;
	private String newPassword;
	private String confirmPassword;

}
