package com.YOGIITSU.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.YOGIITSU.dto.ResponseDto.ShortcutDetailResponseDto;
import com.YOGIITSU.dto.ResponseDto.ShortcutListResponseDto;
import com.YOGIITSU.entity.Shortcut;
import com.YOGIITSU.entity.ShortcutCoordinate;
import com.YOGIITSU.enums.TurnType;
import com.YOGIITSU.exception.resource.ShortcutNotFoundException;
import com.YOGIITSU.repository.ShortcutCoordinateRepository;
import com.YOGIITSU.repository.ShortcutRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class ShortcutServiceTest {

    @Mock
    private ShortcutRepository shortcutRepository;

    @Mock
    private ShortcutCoordinateRepository shortcutCoordinateRepository;

    @InjectMocks
    private ShortcutService shortcutService;

    /* ================= READ: 지름길 리스트 조회 ================= */
    @DisplayName("지름길_전체조회_성공")
    @Test
    void getAllShortcuts_success() {
        Shortcut shortcut1 = createShortcut(1L, "출발점A", "도착점B", 500.5, 300);
        Shortcut shortcut2 = createShortcut(2L, "출발점C", "도착점D", 800.0, 500);

        when(shortcutRepository.findAll()).thenReturn(List.of(shortcut1, shortcut2));
        List<ShortcutListResponseDto> result = shortcutService.getAllShortcuts();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getShortcutId());
        assertEquals(500.5, result.get(0).getDistance());
        verify(shortcutRepository, times(1)).findAll();
    }

    /* ================= READ: 지름길 상세 조회 ================= */
    @DisplayName("지름길상세조회_성공")
    @Test
    void getShortcutDetail_success() {
        Long shortcutId = 1L;
        Shortcut shortcut = createShortcut(shortcutId, "출발지", "도착지", 1000.0, 600);

        List<ShortcutCoordinate> coordinates = List.of(
            createCoordinate(1L, 37.123, 127.123, 1, "직진"),
            createCoordinate(2L, 37.124, 127.124, 2, "우회전")
        );

        when(shortcutRepository.findById(shortcutId)).thenReturn(Optional.of(shortcut));
        when(shortcutCoordinateRepository.findCoordinateByShortcutId(shortcutId)).thenReturn(coordinates);

        ShortcutDetailResponseDto result = shortcutService.getShortcutDetail(shortcutId);
        
        assertNotNull(result);
        assertEquals(shortcutId, result.getShortcutId());
        assertEquals(2, result.getCoordinates().size());
        assertEquals("직진", result.getCoordinates().get(0).getDescription());
        assertEquals(TurnType.STRAIGHT, result.getCoordinates().get(0).getTurnType()); // Enum 타입 검증

        verify(shortcutRepository).findById(shortcutId);
        verify(shortcutCoordinateRepository).findCoordinateByShortcutId(shortcutId);
    }

    @DisplayName("지름길상세조회_실패_존재하지않음")
    @Test
    void getShortcutDetail_fail_notFound() {
        Long shortcutId = 99L;
        when(shortcutRepository.findById(shortcutId)).thenReturn(Optional.empty());

        assertThrows(ShortcutNotFoundException.class, () ->
            shortcutService.getShortcutDetail(shortcutId));

        verify(shortcutRepository).findById(shortcutId);
        verify(shortcutCoordinateRepository, never()).findCoordinateByShortcutId(anyLong());
    }

    /* ================= Dummy methods ================= */
    private Shortcut createShortcut(Long id, String pointA, String pointB, Double distance, Integer duration) {
        Shortcut shortcut = Shortcut.builder()
            .pointA(pointA)
            .pointB(pointB)
            .distance(distance)
            .duration(duration)
            .build();

        ReflectionTestUtils.setField(shortcut, "shortcutId", id);
        return shortcut;
    }

    private ShortcutCoordinate createCoordinate(Long id, Double lat, Double lon, Integer order, String desc) {
        ShortcutCoordinate coord = ShortcutCoordinate.builder()
            .latitude(lat)
            .longitude(lon)
            .pointOrder(order)
            .description(desc)
            .turnType(TurnType.STRAIGHT)
            .segmentDistance(100.0) // 이 필드도 Double일 확률이 높으니 확인 필요
            .imageUrl("http://image.com/1")
            .build();

        ReflectionTestUtils.setField(coord, "coordinateId", id);
        return coord;
    }
}
