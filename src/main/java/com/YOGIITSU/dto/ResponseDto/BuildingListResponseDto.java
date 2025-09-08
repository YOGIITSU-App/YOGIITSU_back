package com.YOGIITSU.dto.ResponseDto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
	
	@JsonProperty("isFavorite")
	private boolean favorite;
}