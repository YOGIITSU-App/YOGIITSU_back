package com.YOGIITSU.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.YOGIITSU.config.handler.GlobalExceptionHandler;
import com.YOGIITSU.dto.ResponseDto.CoordinateDto;
import com.YOGIITSU.dto.ResponseDto.ShortcutDetailResponseDto;
import com.YOGIITSU.dto.ResponseDto.ShortcutListResponseDto;
import com.YOGIITSU.enums.TurnType;
import com.YOGIITSU.exception.resource.ShortcutNotFoundException;
import com.YOGIITSU.service.ShortcutService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = ShortcutController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
    }
)
@Import(GlobalExceptionHandler.class)
public class ShortcutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShortcutService shortcutService;

    /* ================= GET: 지름길 전체 목록 조회 ================= */
    @DisplayName("지름길_전체목록조회_성공")
    @Test
    void getAllShortcuts_success() throws Exception {
        List<ShortcutListResponseDto> responseList = List.of(
            createListResponseDto(1L, "Point A", "Point B", 500.0),
            createListResponseDto(2L, "Point C", "Point D", 1200.5)
        );

        given(shortcutService.getAllShortcuts()).willReturn(responseList);

        mockMvc.perform(get("/shortcuts")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].shortcutId").value(1L))
            .andExpect(jsonPath("$[1].distance").value(1200.5));
    }

    /* ================= GET: 지름길 상세 정보 조회 ================= */
    @DisplayName("지름길_상세조회_성공")
    @Test
    void getShortcutDetail_success() throws Exception {
        Long shortcutId = 1L;
        ShortcutDetailResponseDto responseDetail = createDetailResponseDto(shortcutId);

        given(shortcutService.getShortcutDetail(shortcutId)).willReturn(responseDetail);

        mockMvc.perform(get("/shortcuts/{shortcutId}", shortcutId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.shortcutId").value(shortcutId))
            .andExpect(jsonPath("$.pointA").value("출발지"))
            .andExpect(jsonPath("$.coordinates[0].turnType").value(11));
    }

    @DisplayName("지름길_상세조회_실패_존재하지않음")
    @Test
    void getShortcutDetail_fail_notFound() throws Exception {
        Long invalidId = 999L;
        given(shortcutService.getShortcutDetail(invalidId))
            .willThrow(new ShortcutNotFoundException(invalidId));

        mockMvc.perform(get("/shortcuts/{shortcutId}", invalidId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound()); // GlobalExceptionHandler가 404를 반환한다고 가정
    }

    /* ================= Helper Methods ================= */
    private ShortcutListResponseDto createListResponseDto(Long id, String a, String b,
        Double dist) {
        return ShortcutListResponseDto.builder()
            .shortcutId(id).pointA(a).pointB(b).distance(dist).duration(300)
            .build();
    }

    private ShortcutDetailResponseDto createDetailResponseDto(Long id) {
        CoordinateDto coord = new CoordinateDto(
            37.1234, 127.1234, 1, "첫 번째 좌표 설명",
            TurnType.STRAIGHT, 150.0, "http://image.com/1.jpg"
        );

        return ShortcutDetailResponseDto.builder()
            .shortcutId(id)
            .pointA("출발지")
            .pointB("도착지")
            .distance(1000.0)
            .duration(600)
            .coordinates(List.of(coord))
            .build();
    }

}
