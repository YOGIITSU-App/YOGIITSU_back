package com.YOGIITSU.dto.RequestDto;

import com.YOGIITSU.entity.EmailPurpose;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmailPostRequestDto {

	@NotBlank(message = "이메일은 필수 입력 값입니다.")
	@Email(message = "올바른 이메일 형식이어야 합니다.")
	private String email;
	private EmailPurpose purpose;
}
