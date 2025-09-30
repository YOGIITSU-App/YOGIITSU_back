package com.YOGIITSU.controller;

import com.YOGIITSU.exception.auth.InvalidTokenException;
import com.YOGIITSU.exception.user.MemberNotFoundException;
import com.YOGIITSU.exception.auth.MissingTokenException;
import com.YOGIITSU.dto.RequestDto.InquiryRequestDto;
import com.YOGIITSU.dto.ResponseDto.InquiryListResponseDto;
import com.YOGIITSU.dto.ResponseDto.InquiryResponseDto;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.jwt.CustomUserDetails;
import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.repository.MemberRepository;
import com.YOGIITSU.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * InquiryController
 * - 문의 관련 API 제공
 * - 등록, 전체 조회, 단건 조회, 수정, 삭제 기능 포함
 */
@Slf4j
@Tag(name = "문의 API", description = "문의 등록, 조회, 수정, 삭제 기능 제공")
@RestController
@RequestMapping("/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    /**
     * 문의 등록 API
     * - 인증된 사용자가 문의 등록
     * - 응답 body 없이 204 반환
     */
    @Operation(summary = "문의 등록")
    @PostMapping
    public ResponseEntity<String> createInquiry(
        @RequestBody InquiryRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails) {

        inquiryService.createInquiry(requestDto, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body("등록이 완료되었습니다.");
    }

    /**
     * 전체 문의 리스트 조회 API
     * - 전체 공개 (모든 로그인 사용자 접근 가능)
     */
    @Operation(summary = "전체 문의 목록 조회")
    @GetMapping
    public ResponseEntity<List<InquiryListResponseDto>> getAllInquiries(
        HttpServletRequest request) {

        validateToken(request);  // 인증 여부만 확인
        return ResponseEntity.ok(inquiryService.getAllInquiries());
    }

    /**
     * 문의 상세 조회 (전체 공개)
     * - 본인 여부와 관계 없이 열람 가능
     */
    @Operation(summary = "문의 상세 조회")
    @GetMapping("/{inquiryId}")
    public ResponseEntity<InquiryResponseDto> getInquiry(
        @PathVariable Long inquiryId) {

        InquiryResponseDto response = inquiryService.getInquiry(inquiryId);
        return ResponseEntity.ok(response);
    }

    /**
     * 문의 수정 API
     * - 본인의 문의이며, 답변 대기 상태인 경우에만 수정 가능
     */
    @Operation(summary = "문의 수정")
    @PutMapping("/{inquiryId}")
    public ResponseEntity<InquiryResponseDto> updateInquiry(
        @PathVariable Long inquiryId,
        @RequestBody @Valid InquiryRequestDto requestDto,
        HttpServletRequest request) {

        Long memberId = extractMemberIdFromToken(request);

        // 문의 수정
        InquiryResponseDto response = inquiryService.updateInquiry(inquiryId, memberId, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 문의 삭제 API
     * - 본인의 문의이며, 답변 대기 상태인 경우에만 삭제 가능
     */
    @Operation(summary = "문의 삭제")
    @DeleteMapping("/{inquiryId}")
    public ResponseEntity<Void> deleteInquiry(
        @PathVariable Long inquiryId,
        HttpServletRequest request) {

        // JWT 토큰에서 memberId 추출
        Long memberId = extractMemberIdFromToken(request);

        // 문의 삭제
        inquiryService.deleteInquiry(inquiryId, memberId);
        return ResponseEntity.noContent().build();
    }

    /**
     * JWT 토큰에서 memberId 추출
     */
    private Long extractMemberIdFromToken(HttpServletRequest request) {

        validateToken(request);
        String token = jwtTokenProvider.resolveToken(request);

        String memberId = jwtTokenProvider.getAuthentication(token).getName();
        Member member = memberRepository.findByMemberId(memberId)
            .orElseThrow(MemberNotFoundException::new);

        return member.getId();
    }

    /**
     * JWT 토큰 유효성 검사
     */
    private void validateToken(HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request);

        if (token == null) {
            throw new MissingTokenException();
        }
        if (!jwtTokenProvider.validateToken(token)) {
            throw new InvalidTokenException();
        }
    }

}
