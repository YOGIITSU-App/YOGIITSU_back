package com.YOGIITSU.converter;

import com.YOGIITSU.dto.ResponseDto.*;
import com.YOGIITSU.entity.*;
import java.util.Set;
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
				.sorted(Comparator
					.comparing(
						BuildingFacility::getFloor,
						Comparator.nullsLast(
							Comparator.comparingInt(this::convertFloorStringToOrder))
					)
					.thenComparing(BuildingFacility::getName)
				)
				.map(facility -> FacilityResponseDto.builder()
					.name(facility.getName())
					.floor(facility.getFloor())
					.type(facility.getType())
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
			.sorted(Comparator.comparing(
				BuildingFloorImage::getFloor,
				Comparator.nullsLast(Comparator.comparingInt(this::convertFloorStringToOrder))
			))
			.map(floorImage -> FloorImageResponseDto.builder()
				.floor(floorImage.getFloor())
				.imageUrl(floorImage.getImageUrl())
				.build())
			.collect(Collectors.toList());
	}

	/**
	 * "B3F", "1F", "2F" 등의 문자열을 정렬 가능한 숫자로 변환 B3F -> -3, B2F -> -2, B1F -> -1, 1F -> 1, 2F -> 2 등
	 */
	private int convertFloorStringToOrder(String floor) {
		if (floor == null) {
			return Integer.MAX_VALUE; // null은 가장 마지막
		}
		try {
			if (floor.startsWith("B")) {
				return -Integer.parseInt(floor.substring(1, floor.length() - 1));
			} else if (floor.endsWith("F")) {
				return Integer.parseInt(floor.substring(0, floor.length() - 1));
			}
		} catch (NumberFormatException ignored) {
		}
		return Integer.MAX_VALUE; // 예외나 비정상 문자열도 마지막에 정렬
	}

	/**
	 * Repository에서 조회한 건물 목록을 최종 API 응답 DTO 리스트로 변환 (즐겨찾기 여부 설정 및 정렬 포함)
	 *
	 * @param buildings           Repository에서 조회한 원본 건물 리스트
	 * @param favoriteBuildingIds 사용자가 즐겨찾기한 건물 ID Set
	 * @return 즐겨찾기 여부가 설정되고 정렬된 BuildingListResponseDto 리스트
	 */
	public List<BuildingListResponseDto> convertToBuildingListResponseDto(
		List<BuildingListResponseDto> buildings,
		Set<Long> favoriteBuildingIds) {

		return buildings.stream()
			.map(building -> BuildingListResponseDto.builder()
				.buildingId(building.getBuildingId())
				.buildingName(building.getBuildingName())
				.collegeId(building.getCollegeId())
				.collegeName(building.getCollegeName())
				.imageUrl(building.getImageUrl())
				.isFavorite(favoriteBuildingIds.contains(building.getBuildingId()))
				.build())
			.sorted(Comparator.comparing(BuildingListResponseDto::isFavorite)
				.reversed()
				.thenComparing(BuildingListResponseDto::getBuildingId))
			.collect(Collectors.toList());
	}
}