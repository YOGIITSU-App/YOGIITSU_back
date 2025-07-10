package com.YOGIITSU.service;

import com.YOGIITSU.dto.ResponseDto.CoordinateDto;
import com.YOGIITSU.dto.ResponseDto.ShortcutDetailResponseDto;
import com.YOGIITSU.dto.ResponseDto.ShortcutListResponseDto;
import com.YOGIITSU.entity.Shortcut;
import com.YOGIITSU.entity.ShortcutCoordinate;
import com.YOGIITSU.repository.ShortcutCoordinateRepository;
import com.YOGIITSU.repository.ShortcutRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShortcutService {

    private final ShortcutRepository shortcutRepository;
    private final ShortcutCoordinateRepository shortcutCoordinateRepository;

    /**
     * 지름길 전체 리스트 조회
     */
    public List<ShortcutListResponseDto> getAllShortcuts() {
        return shortcutRepository.findAll().stream()
            .map(shortcut -> ShortcutListResponseDto.builder()
                .shortcutId(shortcut.getShortcutId())
                .pointA(shortcut.getPointA())
                .pointB(shortcut.getPointB())
                .distance(shortcut.getDistance())
                .duration(shortcut.getDuration())
                .build())
            .toList();
    }

    /**
     * 지름길 상세 정보 조회
     */
    public ShortcutDetailResponseDto getShortcutDetail(Long shortcutId) {

        // 1. Shortcut 엔티티 조회
        Shortcut shortcut = shortcutRepository.findById(shortcutId)
            .orElseThrow(() -> new IllegalArgumentException("해당 지름길이 존재하지 않습니다."));

        // 2. ShortcutCoordinate 리스트 조회 (pointOrder순 정렬)
        List<ShortcutCoordinate> coordinates = shortcutCoordinateRepository
            .findCoordinateByShortcutId(shortcutId);

        // 3. DTO 변환
        List<CoordinateDto> coordinateDtos = coordinates.stream()
            .map(coord -> new CoordinateDto(
                coord.getLatitude(),
                coord.getLongitude(),
                coord.getPointOrder(),
                coord.getDescription(),
                coord.getTurnType(),
                coord.getSegmentDistance(),
                coord.getImageUrl()
            ))
            .toList();

        return ShortcutDetailResponseDto.builder()
            .shortcutId(shortcut.getShortcutId())
            .pointA(shortcut.getPointA())
            .pointB(shortcut.getPointB())
            .distance(shortcut.getDistance())
            .duration(shortcut.getDuration())
            .coordinates(coordinateDtos)
            .build();
    }
}