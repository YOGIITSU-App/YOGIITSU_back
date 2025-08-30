package com.YOGIITSU.dto.ResponseDto;

import com.YOGIITSU.enums.FacilityDetailType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FacilityResponseDto {

	@NotBlank
	private String name;

	private String floor;

	@NotNull
	private FacilityDetailType type;

}
