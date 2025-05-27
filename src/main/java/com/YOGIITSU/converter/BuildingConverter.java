package com.YOGIITSU.converter;

import com.YOGIITSU.dto.ResponseDto.*;
import com.YOGIITSU.entity.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BuildingConverter {

	/**
	 * Building 엔티티를 BuildingDetailResponseDto로 변환
	 *
	 * @param building 변환할 Building 엔티티
	 * @return 변환된 BuildingDetailResponseDto
	 */
	public BuildingDetailResponseDto convertToBuildingDetailResponseDto(Building building,
		boolean isFavorite) {
		return BuildingDetailResponseDto.builder()
			.buildingInfo(convertToBuildingInfoResponseDto(building))
			.departments(convertToDepartmentResponseDtos(
				new ArrayList<>(building.getDepartments())))
			.floorPlans(convertToFloorPlanResponseDtos(
				new ArrayList<>(building.getBuildingFloorImages())))
			.isFavorite(isFavorite)
			.build();
	}

	/**
	 * Building 엔티티를 BuildingInfoResponseDto로 변환
	 *
	 * @param building 변환할 Building 엔티티
	 * @return 변환된 BuildingInfoResponseDto
	 */
	private BuildingInfoResponseDto convertToBuildingInfoResponseDto(Building building) {
		return BuildingInfoResponseDto.builder()
			.name(building.getName())
			.tags(building.getBuildingTags().stream()
				.map(BuildingTag::getName)
				.sorted()
				.collect(Collectors.toList()))
			.imageUrl(building.getImageUrl())
			.latitude(building.getLatitude())
			.longitude(building.getLongitude())
			.facilities(building.getBuildingFacilities().stream()
				.sorted(Comparator.comparing(BuildingFacility::getFloor,
						Comparator.nullsLast(String::compareTo))
					.thenComparing(BuildingFacility::getName))
				.map(facility -> FacilityResponseDto.builder()
					.name(facility.getName())
					.floor(facility.getFloor())
					.build())
				.collect(Collectors.toList()))
			.build();
	}

	/**
	 * Building 엔티티의 Department 리스트를 DepartmentResponseDto 리스트로 변환
	 *
	 * @param departments 변환할 Department 리스트
	 * @return 변환된 DepartmentResponseDto 리스트
	 */
	private List<DepartmentResponseDto> convertToDepartmentResponseDtos(
		List<Department> departments) {
		return departments.stream()
			.sorted(Comparator.comparing(Department::getId))
			.map(department -> DepartmentResponseDto.builder()
				.id(department.getId())
				.collegeName(department.getCollegeName())
				.departmentName(department.getDepartmentName())
				.location(department.getLocation())
				.phone(department.getPhone())
				.fax(department.getFax())
				.officeHours(department.getOfficeHours())
				.build())
			.collect(Collectors.toList());
	}

	/**
	 * Building 엔티티의 BuildingFloorImage 리스트를 FloorPlanResponseDto 리스트로 변환
	 *
	 * @param floorImages 변환할 BuildingFloorImage 리스트
	 * @return 변환된 FloorPlanResponseDto 리스트
	 */
	private List<FloorImageResponseDto> convertToFloorPlanResponseDtos(
		List<BuildingFloorImage> floorImages) {
		return floorImages.stream()
			.sorted(Comparator.comparing(BuildingFloorImage::getFloor))
			.map(floorImage -> FloorImageResponseDto.builder()
				.floor(floorImage.getFloor())
				.imageUrl(floorImage.getImageUrl())
				.build())
			.collect(Collectors.toList());
	}
}