package com.YOGIITSU.dto.ResponseDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentResponseDto {

	private Long id;

	@NotBlank
	private String collegeName;

	@NotBlank
	private String departmentName;

	@NotBlank
	private String location;

	private String phone;

	private String fax;

	private String officeHours;
}
