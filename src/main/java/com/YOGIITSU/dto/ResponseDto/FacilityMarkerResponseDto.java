package com.YOGIITSU.dto.ResponseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FacilityMarkerResponseDto {

	private String name;
	private Double latitude;
	private Double longitude;
	private Long buildingId;
	private String type;
}