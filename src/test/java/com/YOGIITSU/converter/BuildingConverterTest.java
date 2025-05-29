package com.YOGIITSU.converter;

import com.YOGIITSU.dto.ResponseDto.BuildingDetailResponseDto;
import com.YOGIITSU.dto.ResponseDto.DepartmentResponseDto;
import com.YOGIITSU.dto.ResponseDto.FloorImageResponseDto;
import com.YOGIITSU.entity.Building;
import com.YOGIITSU.entity.BuildingFacility;
import com.YOGIITSU.entity.BuildingFloorImage;
import com.YOGIITSU.entity.BuildingTag;
import com.YOGIITSU.entity.Department;
import java.util.HashSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BuildingConverterTest {

	private final BuildingConverter converter = new BuildingConverter();

	@Test
	@DisplayName("departments는 id 기준으로 정렬되어 반환된다")
	void convertToBuildingDetailResponseDto_departmentsSortedById() {
		// given
		Department d1 = Department.builder().id(3L).departmentName("A학과").build();
		Department d2 = Department.builder().id(1L).departmentName("B학과").build();
		Department d3 = Department.builder().id(2L).departmentName("C학과").build();
		Set<Department> unorderedDepartments = new LinkedHashSet<>(List.of(d1, d2, d3));

		Building building = Building.builder()
			.name("지능형SW융합대학")
			.departments(unorderedDepartments)
			.buildingTags(new HashSet<>())
			.buildingFacilities(new HashSet<>())
			.buildingFloorImages(new HashSet<>())
			.build();

		// when
		BuildingDetailResponseDto dto = converter.convertToBuildingDetailResponseDto(building,
			false);
		List<DepartmentResponseDto> result = dto.getDepartments();

		// then
		assertThat(result).hasSize(3);
		assertThat(result.get(0).getId()).isEqualTo(1L);
		assertThat(result.get(1).getId()).isEqualTo(2L);
		assertThat(result.get(2).getId()).isEqualTo(3L);
	}

	@Test
	@DisplayName("tags는 알파벳/한글 오름차순으로 정렬되어 반환된다")
	void convertToBuildingDetailResponseDto_tagsSortedAlphabetically() {
		// given
		Building building = Building.builder()
			.name("지능형SW융합대학")
			.departments(new HashSet<>())
			.buildingTags(Set.of(
				BuildingTag.builder().id(1L).name("ICT융합대학").build(),
				BuildingTag.builder().id(2L).name("벨칸토아트센터").build(),
				BuildingTag.builder().id(3L).name("IT대학").build()
			))
			.buildingFacilities(new HashSet<>())
			.buildingFloorImages(new HashSet<>())
			.build();

		// when
		BuildingDetailResponseDto dto = converter.convertToBuildingDetailResponseDto(building,
			false);
		List<String> result = dto.getBuildingInfo().getTags();

		// then
		assertThat(result).containsExactly("ICT융합대학", "IT대학", "벨칸토아트센터");
	}

	@Test
	@DisplayName("facilities는 floor 우선, 같은 층 내에서는 name 기준으로 정렬된다")
	void convertToBuildingDetailResponseDto_facilitiesSortedByFloorAndName() {
		// given
		Building building = Building.builder()
			.name("지능형SW융합대학")
			.departments(new HashSet<>())
			.buildingTags(new HashSet<>())
			.buildingFacilities(Set.of(
				BuildingFacility.builder().id(1L).floor("2층").name("랩실").build(),
				BuildingFacility.builder().id(2L).floor("1층").name("강의실").build(),
				BuildingFacility.builder().id(3L).floor("1층").name("휴게실").build()
			))

			.buildingFloorImages(new HashSet<>())
			.build();

		// when
		BuildingDetailResponseDto dto = converter.convertToBuildingDetailResponseDto(building,
			false);
		List<String> result = dto.getBuildingInfo().getFacilities().stream()
			.map(f -> f.getFloor() + "-" + f.getName())
			.toList();

		// then
		assertThat(result).containsExactly("1층-강의실", "1층-휴게실", "2층-랩실");
	}

	@Test
	@DisplayName("floorPlans는 floor 기준으로 오름차순 정렬되어 반환된다")
	void convertToBuildingDetailResponseDto_floorPlansSortedByFloor() {
		// given
		Building building = Building.builder()
			.name("지능형SW융합대학")
			.departments(new HashSet<>())
			.buildingTags(new HashSet<>())
			.buildingFacilities(new HashSet<>())
			.buildingFloorImages(Set.of(
				new BuildingFloorImage(1L, null, "3층", "3층.png"),
				new BuildingFloorImage(2L, null, "1층", "1층.png"),
				new BuildingFloorImage(3L, null, "2층", "2층.png")
			))
			.build();

		// when
		BuildingDetailResponseDto dto = converter.convertToBuildingDetailResponseDto(building,
			false);
		List<String> result = dto.getFloorPlans().stream()
			.map(FloorImageResponseDto::getFloor)
			.toList();

		// then
		assertThat(result).containsExactly("1층", "2층", "3층");
	}
}
