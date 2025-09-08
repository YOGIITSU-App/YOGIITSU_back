package com.YOGIITSU.dto.ResponseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BuildingListResponseDto {

	private Long buildingId;
	private String buildingName;
	private Long collegeId;
	private String collegeName;
	private String imageUrl;

}