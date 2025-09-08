package com.YOGIITSU.dto.ResponseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BuildingListResponseDto {

	private Long buildingId;
	private String buildingName;
	private Long collegeId;
	private String collegeName;
	private String imageUrl;
	private boolean isFavorite;
}