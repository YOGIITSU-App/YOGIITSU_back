package com.YOGIITSU.service;

import com.YOGIITSU.dto.ResponseDto.FacilityMarkerResponseDto;
import com.YOGIITSU.enums.FacilityType;
import com.YOGIITSU.repository.FacilityMarkerRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 시설 마커 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
public class FacilityMarkerService {

	// 시설 마커 정보를 조회하는 JPA Repository
	private final FacilityMarkerRepository facilityMarkerRepository;

	/**
	 * 특정 시설 유형(FacilityType)에 해당하는 마커 리스트를 조회
	 *
	 * @param type 조회할 시설 유형 (예: RESTAURANT, PARKING, CONVENIENCE_CAFE)
	 * @return FacilityMarkerResponseDto
	 */
	public List<FacilityMarkerResponseDto> getMarkersByType(FacilityType type) {
		if (type == null) {
			throw new IllegalArgumentException("시설 유형은 필수값입니다.");
		}
		return facilityMarkerRepository.findByType(type).stream()
			.map(f -> new FacilityMarkerResponseDto(
				f.getPlaceName(),
				f.getLatitude(),
				f.getLongitude(),
				f.getBuilding() != null ? f.getBuilding().getId() : null // 빌딩 ID 추가
			))
			.collect(Collectors.toList());
	}
}
