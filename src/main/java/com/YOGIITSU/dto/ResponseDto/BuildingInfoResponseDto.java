package com.YOGIITSU.dto.ResponseDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class BuildingInfoResponseDto {

	@NotBlank
	private String name;

	private List<String> tags;

	private String imageUrl;

	private List<FacilityResponseDto> facilities;

	private Double latitude;

	private Double longitude;
}
