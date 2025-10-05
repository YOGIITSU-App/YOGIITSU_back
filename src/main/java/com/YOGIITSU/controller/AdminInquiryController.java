package com.YOGIITSU.controller;

import com.YOGIITSU.dto.RequestDto.AdminAnswerCreateRequestDto;
import com.YOGIITSU.dto.RequestDto.AdminAnswerUpdateRequestDto;
import com.YOGIITSU.dto.ResponseDto.InquiryResponseDto;
import com.YOGIITSU.exception.auth.UnauthorizedException;
import com.YOGIITSU.jwt.CustomUserDetails;
import com.YOGIITSU.service.AdminInquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "관리자 문의 답변 API", description = "관리자 답변 등록 및 수정 기능 제공")
@RestController
@RequestMapping("/admin/inquiries")
@RequiredArgsConstructor
public class AdminInquiryController {

    private final AdminInquiryService adminInquiryService;

    /**
     * 관리자 답변 등록 API
     * - 문의 상태가 'PROCESSING'인 경우에만 등록 가능
     * - 제목 및 내용이 비어있으면 예외 발생
     */
    @Operation(summary = "관리자 답변 등록")
    @PostMapping("/{inquiryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InquiryResponseDto> createAnswer(
        @PathVariable Long inquiryId,
        @RequestBody @Valid AdminAnswerCreateRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        requireLogin(userDetails);

        InquiryResponseDto responseDto = adminInquiryService.createAnswer(inquiryId, requestDto);
        log.info("[ADMIN] 문의 답변 등록 완료 - inquiryId={}", inquiryId);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * 문의 답변 수정 API
     * - 상태가 'COMPLETE'인 문의에만 수정 가능
     * - 기존 제목/내용을 새 값으로 갱신
     */
    @Operation(summary = "관리자 답변 수정")
    @PutMapping("/{inquiryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InquiryResponseDto> updateAnswer(
        @PathVariable Long inquiryId,
        @RequestBody AdminAnswerUpdateRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        requireLogin(userDetails);

        InquiryResponseDto responseDto = adminInquiryService.updateAnswer(inquiryId, requestDto);
        log.info("[ADMIN] 문의 답변 수정 완료 - inquiryId={}", inquiryId);

        return ResponseEntity.ok(responseDto);
    }

    /**
     * 문의글 삭제 API
     * - 관리자 권한으로 사용자 문의글과 답변 모두 삭제
     * - 상태와 관계없이 강제 삭제 가능
     */
    @Operation(summary = "문의 삭제")
    @DeleteMapping("/{inquiryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteInquiryByAdmin(
        @PathVariable Long inquiryId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        requireLogin(userDetails);

        adminInquiryService.deleteInquiryByAdmin(inquiryId);
        log.info("[ADMIN] 문의글 삭제 완료 - inquiryId={}", inquiryId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 로그인 여부 확인
     * - 인증 정보가 없으면 UnauthorizedException 발생
     */
    private void requireLogin(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorizedException();
        }
    }
}
