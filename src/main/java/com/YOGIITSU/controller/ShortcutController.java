package com.YOGIITSU.controller;

import com.YOGIITSU.dto.ResponseDto.ShortcutDetailResponseDto;
import com.YOGIITSU.dto.ResponseDto.ShortcutListResponseDto;
import com.YOGIITSU.service.ShortcutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "지름길 API", description = "지름길 리스트 및 상세 정보 조회 API 제공")
@RestController
@RequiredArgsConstructor
@RequestMapping("/shortcuts")
public class ShortcutController {

    private final ShortcutService shortcutService;

    /**
     * 지름길 전체 목록 조회 API
     */
    @Operation(summary = "지름길 전체 목록 조회")
    @GetMapping
    public ResponseEntity<List<ShortcutListResponseDto>> getAllShortcuts() {
        List<ShortcutListResponseDto> shortcuts = shortcutService.getAllShortcuts();
        return ResponseEntity.ok(shortcuts);
    }

    /**
     * 특정 지름길 상세 정보 조회 API
     */
    @Operation(summary = "지름길 상세 정보 조회")
    @GetMapping("/{shortcutId}")
    public ResponseEntity<ShortcutDetailResponseDto> getShortcutDetail(
        @PathVariable Long shortcutId) {
        ShortcutDetailResponseDto detail = shortcutService.getShortcutDetail(shortcutId);
        return ResponseEntity.ok(detail);
    }
}
