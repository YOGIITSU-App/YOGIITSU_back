package com.YOGIITSU.controller;

import com.YOGIITSU.dto.RequestDto.NoticeRequestDto;
import com.YOGIITSU.dto.ResponseDto.NoticeResponseDto;
import com.YOGIITSU.jwt.CustomUserDetails;
import com.YOGIITSU.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "공지사항 API", description = "공지사항 등록, 조회, 수정, 삭제 기능을 제공합니다.")
@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
class NoticeController {

    private final NoticeService noticeService;

    // 공지 전체 목록 조회 (모든 사용자 가능)
    @Operation(summary = "공지사항 전체 조회", description = "모든 사용자가 공지사항 목록을 최신순으로 조회할 수 있습니다.")
    @GetMapping
    public ResponseEntity<List<NoticeResponseDto>> getNotices() {
        return ResponseEntity.ok(noticeService.getAllNotices());
    }

    // 공지 상세 조회 (모든 사용자 가능)
    @Operation(summary = "공지사항 상세 조회", description = "공지사항의 ID를 통해 상세 내용을 조회할 수 있습니다. 모든 사용자가 접근할 수 있습니다.")
    @GetMapping("/{id}")
    public ResponseEntity<NoticeResponseDto> getNotice(@PathVariable("id") Long id) {
        return ResponseEntity.ok(noticeService.getNoticeById(id));
    }

    // 공지 등록 (관리자만 가능)
    @Operation(summary = "공지사항 등록", description = "제목과 내용을 입력하여 새로운 공지사항을 등록합니다. 관리자만 접근 가능합니다.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> createNotice(@RequestBody @Valid NoticeRequestDto dto, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("관리자 로그인이 필요합니다.");
        }
        noticeService.createNotice(dto, userDetails.getId());
        return ResponseEntity.ok(Map.of("message", "공지사항이 등록되었습니다."));
    }

    // 공지 수정 (관리자만 가능)
    @Operation(summary = "공지사항 수정", description = "기존 공지사항의 제목과 내용을 수정합니다. 관리자만 접근 가능합니다.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> updateNotice(@PathVariable("id") Long id, @RequestBody @Valid NoticeRequestDto dto, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("관리자 로그인이 필요합니다.");
        }
        noticeService.updateNotice(id, dto, userDetails.getId());
        return ResponseEntity.ok(Map.of("message", "공지사항이 수정되었습니다."));
    }

    // 공지 삭제 (관리자만 가능)
    @Operation(summary = "공지사항 삭제", description = "공지사항을 삭제합니다. 관리자만 접근 가능합니다.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteNotice(@PathVariable("id") Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("관리자 로그인이 필요합니다.");
        }
        noticeService.deleteNotice(id, userDetails.getId());
        return ResponseEntity.ok(Map.of("message", "공지사항이 삭제되었습니다."));
    }
}