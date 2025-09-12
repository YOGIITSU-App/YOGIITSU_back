package com.YOGIITSU.dto.ResponseDto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class BuildingDetailResponseDto {

	private BuildingInfoResponseDto buildingInfo;

	private List<DepartmentResponseDto> departments;

	private List<FacilityResponseDto> facilities;

	private List<FloorImageResponseDto> floorPlans;

	private boolean isFavorite;
}
